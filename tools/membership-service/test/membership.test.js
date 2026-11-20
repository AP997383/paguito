import test from 'node:test';
import assert from 'node:assert/strict';
import { MembershipLedger } from '../src/ledger.js';
import { StoreVerifier } from '../src/stores.js';
import { accountUUID, fingerprint, assertVerifiedPurchase, subscriptionUpdate } from '../src/policy.js';

const email = 'buyer@example.com';
const active = () => ({platform: 'apple', productID: 'premium_monthly_basic', eventID: 'transaction-1', originalID: 'chain-1', accountID: accountUUID(email), status: 'active', expiresAt: Date.now() + 86400000, signedAt: Date.now(), amount: 79, currency: 'MXN'});
/** Transacciones serializadas: reproduce contención y solo aplica escrituras tras terminar sin error. */
class MemoryDB {
  rows = new Map(); queue = Promise.resolve();
  query(name,field,value) { return {docs:[...this.rows].filter(([key,data])=>key.startsWith(name+'/') && data[field]===value).map(([key,data])=>({id:key.split('/').at(-1),exists:true,data:()=>data}))}; }
  collection(name) { return {doc: id => ({path: `${name}/${id}`,id}), where: (field,_operator,value) => ({query:{name,field,value},limit:()=>({get:async()=>this.query(name,field,value)})})}; }
  async runTransaction(body) {
    const previous = this.queue; let unlock; this.queue = new Promise(resolve => {unlock = resolve}); await previous;
    const writes = [];
    try {
      const result = await body({get: async ref => ref.query ? this.query(ref.query.name,ref.query.field,ref.query.value) : ({id:ref.id,exists: this.rows.has(ref.path), data: () => this.rows.get(ref.path)}),
        set: (ref, data, options) => writes.push([ref, data, options?.merge]), update: (ref, data) => writes.push([ref, data, true])});
      for (const [ref,data,merge] of writes) {
        const next = merge ? {...this.rows.get(ref.path)} : {};
        for(const [key,value] of Object.entries(data)) {
          const parts=key.split('.');
          if(parts.length===2)next[parts[0]]={...next[parts[0]],[parts[1]]:value};else next[key]=value;
        }
        this.rows.set(ref.path,next);
      }
      return result;
    } finally {unlock()}
  }
}
function database(subscription = {isActive: false}) { const db = new MemoryDB(); db.rows.set(`users/${email}`, {userSuscription: subscription}); return db; }

test('normalización produce la misma identidad y UUID de cuenta', () => {
 assert.equal(accountUUID(' Buyer@Example.com '), accountUUID(email));
 assert.notEqual(accountUUID(email), accountUUID('other@example.com'));
 assert.equal(fingerprint(email).length, 64);
});
test('recibo vencido o revocado nunca concede beneficios', () => {
 assert.throws(() => assertVerifiedPurchase({...active(), expiresAt: 1}, email));
 assert.throws(() => assertVerifiedPurchase({...active(), status: 'revoked'}, email));
});
test('la identidad firmada por la tienda debe coincidir con la cuenta', () => {
 assert.throws(() => assertVerifiedPurchase(active(), 'other@example.com'), /ANOTHER_ACCOUNT/);
});
test('conserva un Premium histórico sin modificar fecha, precio ni identificadores', async () => {
 const existing = {isActive: true, nextExpiration: '2026-06-15', price: 79, idSuscription: '', nameSuscription: 'Escensial'};
 const db = database(existing); await new MembershipLedger(db).confirm(email, active(), 'signed-receipt', {name: 'Premium', price: 99});
 assert.deepEqual(db.rows.get(`users/${email}`).userSuscription, existing);
});
test('conserva un Premium administrativo sin fecha y con precio string', async () => {
 const existing = {isActive: true, active: true, nextExpiration: '', price: '79'};
 const db = database(existing); await new MembershipLedger(db).confirm(email, active(), 'signed-receipt', {});
 assert.deepEqual(db.rows.get(`users/${email}`).userSuscription, existing);
});
test('dos callbacks concurrentes producen un solo registro de compra', async () => {
 const db = database(); const ledger = new MembershipLedger(db); const purchase = active();
 const results = await Promise.all([ledger.confirm(email, purchase, 'same-receipt', {}), ledger.confirm(email, purchase, 'same-receipt', {})]);
 assert.equal([...db.rows.keys()].filter(key => key.startsWith('membershipPurchases/')).length, 1);
 assert.equal(results.filter(r => r.duplicate).length, 1);
 assert.equal(db.rows.get(`users/${email}`).userSuscription.nextExpiration, new Date(purchase.expiresAt).toISOString().slice(0,10));
});
test('la restauración de otro usuario no transfiere la compra ni escribe registros', async () => {
 const db = database(); db.rows.set('users/other@example.com', {userSuscription:{isActive:false}});
 const ledger = new MembershipLedger(db); const purchase = {...active(), accountID:null};
 await ledger.confirm(email, purchase, 'receipt', {}); const count=db.rows.size;
 await assert.rejects(ledger.confirm('other@example.com', purchase, 'receipt', {}), /ANOTHER_ACCOUNT/);
 assert.equal(db.rows.size, count); assert.equal(db.rows.get('users/other@example.com').userSuscription.isActive, false);
});
test('no crea cuentas ni registros ante un usuario inexistente', async () => {
 const db=new MemoryDB(); await assert.rejects(new MembershipLedger(db).confirm(email,active(),'receipt',{}), /ACCOUNT_NOT_FOUND/);assert.equal(db.rows.size,0);
});
test('activación usa la fecha real del recibo, no hoy más treinta días', () => {
 const receipt=active(); const result=subscriptionUpdate({isActive:false},receipt,{price:79});
 assert.equal(result.nextExpiration,new Date(receipt.expiresAt).toISOString().slice(0,10));
});
test('Google devuelve la orden real, el SKU y la identidad de la API, sin inventar importes', async () => {
 const googleData={subscriptionState:'SUBSCRIPTION_STATE_ACTIVE',lineItems:[{productId:'premium_monthly_basic',expiryTime:new Date(Date.now()+86400000).toISOString(),latestSuccessfulOrderId:'GPA.real-order'}],externalAccountIdentifiers:{obfuscatedExternalAccountId:fingerprint(email)}};
 const verifier=new StoreVerifier({publisher:{purchases:{subscriptionsv2:{get:async()=>({data:googleData})}}},apple:{}});
 const result=await verifier.verifyGoogle('real-token','premium_monthly_basic');
 assert.equal(result.eventID,'GPA.real-order');assert.equal(result.amount,null);assert.equal(result.status,'active');assertVerifiedPurchase(result,email);
 await assert.rejects(verifier.verifyGoogle('real-token','different-sku'), /INVALID_PRODUCT/);
});
test('cancelar renovación conserva acceso hasta la fecha realmente pagada', async () => {
 const verifier=new StoreVerifier({publisher:{purchases:{subscriptionsv2:{get:async()=>({data:{subscriptionState:'SUBSCRIPTION_STATE_CANCELED',latestOrderId:'order',lineItems:[{productId:'premium_monthly_basic',expiryTime:new Date(Date.now()+86400000).toISOString()}]}})}}},apple:{}});
 assert.equal((await verifier.verifyGoogle('token','premium_monthly_basic')).status,'active');
});
test('Apple conserva los metadatos firmados y marca el reembolso como revocado', async () => {
 const verifier=new StoreVerifier({publisher:{},apple:{verifyAndDecodeTransaction:async()=>({productId:'com.paguito.premium.monthly',transactionId:'apple-tx',originalTransactionId:'apple-original',expiresDate:Date.now()+86400000,signedDate:123,revocationDate:1,price:79000,currency:'MXN'})}});
 const result=await verifier.verifyApple('signed-jws');assert.equal(result.status,'revoked');assert.equal(result.amount,79);assert.throws(()=>assertVerifiedPurchase(result,email));
});

test('las visitas se deduplican por apertura y cuentan una misma persona entre plataformas', async()=>{
 const {MembershipVisitLedger}=await import('../src/ledger.js');
 const db=database();const before=structuredClone(db.rows.get(`users/${email}`));const visits=new MembershipVisitLedger(db);
 const one='e5990d22-ed12-4d43-a6fb-6439c44dad3d',two='30ee279b-cbbd-4139-a8dc-efcd26b379e5';
 const a=await visits.record(email,one,'apple');const duplicate=await visits.record(email,one,'apple');const b=await visits.record(email,two,'google');
 assert.equal(a.analyticsID,b.analyticsID);assert.equal(duplicate.duplicate,true);
 assert.equal([...db.rows.keys()].filter(k=>k.startsWith('membershipVisits/')).length,2);
 assert.equal([...db.rows.keys()].filter(k=>k.startsWith('membershipVisitAccounts/')).length,1);
 assert.equal(db.rows.get(`membershipVisitAccounts/${fingerprint(email)}`).visits,2);
 assert.deepEqual(db.rows.get(`users/${email}`),before);
 assert.equal([...db.rows.keys()].filter(k=>k.startsWith('membershipPurchases/')).length,0);
});
test('la visita de otra persona tiene otro UUID aleatorio, nunca el correo o su hash',async()=>{
 const {MembershipVisitLedger}=await import('../src/ledger.js');const db=database();db.rows.set('users/other@example.com',{});
 const visits=new MembershipVisitLedger(db);const one=await visits.record(email,'e5990d22-ed12-4d43-a6fb-6439c44dad3d','apple');const two=await visits.record('other@example.com','e5990d22-ed12-4d43-a6fb-6439c44dad3d','google');
 assert.notEqual(one.analyticsID,two.analyticsID);assert.notEqual(one.analyticsID,fingerprint(email));assert.match(one.analyticsID,/^[a-f0-9-]{36}$/);
});
test('Google RTDN moderno no necesita un subscriptionId enviado por el mensaje',async()=>{
 const verifier=new StoreVerifier({publisher:{purchases:{subscriptionsv2:{get:async()=>({data:{subscriptionState:'SUBSCRIPTION_STATE_ACTIVE',latestOrderId:'order',lineItems:[{productId:'premium_monthly_basic',expiryTime:new Date(Date.now()+86400000).toISOString()}]}})}}},apple:{}});
 assert.equal((await verifier.verifyGoogle('token')).productID,'premium_monthly_basic');
});


test('un reembolso verificado revoca solo las compras gestionadas por la tienda',async()=>{
 const db=database();const ledger=new MembershipLedger(db);const purchase=active();
 await ledger.confirm(email,purchase,'receipt',{});
 assert.equal(db.rows.get(`users/${email}`).membershipManagedBy,'verified_store');
 await ledger.applyStoreUpdate({...purchase,status:'revoked',signedAt:purchase.signedAt+1000});
 assert.equal(db.rows.get(`users/${email}`).userSuscription.isActive,false);
 await assert.rejects(ledger.confirm(email,purchase,'receipt',{}),/NOT_ACTIVE/);
 assert.equal([...db.rows.keys()].filter(k=>k.startsWith('membershipPurchases/')).length,1);
});
test('un reembolso de Apple conserva otra membresía vigente de Google',async()=>{
 const db=database();const ledger=new MembershipLedger(db);const apple=active();
 await ledger.confirm(email,apple,'apple-receipt',{});
 const google={...active(),platform:'google',accountID:fingerprint(email),eventID:'GPA.second',originalID:'google-token',expiresAt:Date.now()+172800000};
 await ledger.confirm(email,google,'google-receipt',{});
 await ledger.applyStoreUpdate({...apple,status:'revoked',signedAt:apple.signedAt+1000});
 assert.equal(db.rows.get(`users/${email}`).userSuscription.isActive,true);
});
test('ni una notificación de reembolso cambia el Premium heredado protegido',async()=>{
 const existing={isActive:true,nextExpiration:'2026-06-15',price:'79'};
 const db=database(existing);const ledger=new MembershipLedger(db);const purchase=active();
 await ledger.confirm(email,purchase,'receipt',{});await ledger.applyStoreUpdate({...purchase,status:'revoked',signedAt:purchase.signedAt+1000});
 assert.deepEqual(db.rows.get(`users/${email}`).userSuscription,existing);
});
test('una notificación anterior a la compra no concede acceso sin propietario',async()=>{
 const db=database();const ledger=new MembershipLedger(db);const purchase=active();
 await ledger.applyStoreUpdate(purchase);
 assert.equal(db.rows.get(`users/${email}`).userSuscription.isActive,false);
 assert.equal([...db.rows.keys()].filter(k=>k.startsWith('membershipPurchases/')).length,0);
 await ledger.confirm(email,purchase,'receipt',{});
 assert.equal(db.rows.get(`users/${email}`).userSuscription.isActive,true);
});

test('consultar catálogo no permite cobrar si el acceso a compras no se ha comprobado',async()=>{
 const verifier=new StoreVerifier({publisher:{monetization:{subscriptions:{get:async()=>({})}}},apple:{}});
 await assert.rejects(verifier.ready('google','premium_monthly_basic'),/ACCESS_NOT_VERIFIED/);
});
test('permiso de catálogo con compras prohibidas mantiene bloqueadas las compras nuevas',async()=>{
 const verifier=new StoreVerifier({publisher:{monetization:{subscriptions:{get:async()=>({})}},purchases:{subscriptionsv2:{get:async()=>{throw Object.assign(new Error('denied'),{code:403})}}}},apple:{}});
 await assert.rejects(verifier.ready('google','premium_monthly_basic',['existing-token']),/denied/);
});
test('comprobar un recibo histórico solo usa lecturas y confirma permiso de compras',async()=>{
 const calls=[];
 const verifier=new StoreVerifier({publisher:{monetization:{subscriptions:{get:async()=>{calls.push('catalog-read');return {}}}},purchases:{subscriptionsv2:{get:async()=>{calls.push('purchase-read');return {data:{}}}}}},apple:{}});
 assert.equal(await verifier.ready('google','premium_monthly_basic',['existing-token']),true);
 assert.deepEqual(calls,['purchase-read']);
});

test('mantiene consistente el alias active al activar una cuenta que antes era gratuita',()=>{
 const result=subscriptionUpdate({isActive:false,active:false},active(),{});assert.equal(result.isActive,true);assert.equal(result.active,true);
});
