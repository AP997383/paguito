import { FieldValue } from 'firebase-admin/firestore';
import { randomUUID } from 'node:crypto';
import { MembershipError, fingerprint, normalizeEmail, assertVerifiedPurchase, subscriptionUpdate, isAdministrative } from './policy.js';

/** Ledger del servidor: una compra y un propietario, aunque lleguen varios callbacks o restauraciones. */
export class MembershipLedger {
  constructor(db) { this.db = db; }
  /** Recupera el propietario histórico sin copiar ni publicar los tokens existentes. */
  async legacyOwner(token) {
    const rows = await this.db.collection('suscripciones').where('purchaseToken', '==', token).limit(100).get();
    return new Set(rows.docs.map(doc => normalizeEmail(doc.data().email)));
  }
  /** La transacción evita duplicar registros y mantiene intacto un Premium ya concedido. */
  async confirm(email, purchase, token, plan) {
    email = normalizeEmail(email); assertVerifiedPurchase(purchase, email);
    if (purchase.isTest && process.env.GCLOUD_PROJECT === 'paguito-92b2d') throw new MembershipError('TEST_PURCHASE_NOT_ALLOWED', 400);
    const legacy = await this.legacyOwner(token);
    if (legacy.size > 1 || legacy.size && !legacy.has(email)) throw new MembershipError('PURCHASE_OWNED_BY_ANOTHER_ACCOUNT');
    const ownerRef = this.db.collection('membershipOwners').doc(fingerprint(`${purchase.platform}:${purchase.originalID}`));
    const eventRef = this.db.collection('membershipPurchases').doc(fingerprint(`${purchase.platform}:${purchase.eventID}`));
    const userRef = this.db.collection('users').doc(email);
    const grantRef = this.db.collection('membershipEvents').doc('admin_' + fingerprint(email));
    const linkedRef = purchase.linkedOriginalID ? this.db.collection('membershipOwners').doc(fingerprint(`google:${purchase.linkedOriginalID}`)) : null;
    return this.db.runTransaction(async tx => {
      const [owner, event, user, linked, grant] = await Promise.all([tx.get(ownerRef), tx.get(eventRef), tx.get(userRef), linkedRef ? tx.get(linkedRef) : null, tx.get(grantRef)]);
      if (!user.exists) throw new MembershipError('ACCOUNT_NOT_FOUND', 404);
      for (const snapshot of [owner, linked]) if (snapshot?.exists && snapshot.data().email && snapshot.data().email !== email) throw new MembershipError('PURCHASE_OWNED_BY_ANOTHER_ACCOUNT');
      if (event.exists && event.data().ownerKey !== fingerprint(email)) throw new MembershipError('PURCHASE_OWNED_BY_ANOTHER_ACCOUNT');
      // Una notificación más reciente de reembolso no puede ser revertida por un recibo antiguo.
      if (owner.exists && (owner.data().signedAt ?? 0) > purchase.signedAt && owner.data().status !== 'active') throw new MembershipError('PURCHASE_NOT_ACTIVE');
      const duplicate = event.exists;
      tx.set(ownerRef, {email, platform: purchase.platform, originalID: purchase.originalID, productID: purchase.productID,
        status: purchase.status, expiresAt: purchase.expiresAt, signedAt: purchase.signedAt,
        updatedAt: FieldValue.serverTimestamp()}, {merge: true});
      tx.set(eventRef, {ownerKey: fingerprint(email), platform: purchase.platform, transactionID: purchase.eventID,
        originalID: purchase.originalID, productID: purchase.productID, status: purchase.status,
        expiresAt: purchase.expiresAt, amount: purchase.amount, currency: purchase.currency,
        ...(duplicate ? {} : {verifiedAt: FieldValue.serverTimestamp()})}, {merge: true});
      if (purchase.platform === 'google') tx.set(this.db.collection('membershipIntents').doc('store_' + ownerRef.id), {type:'store_receipt', platform:'google', purchaseToken:token, originalID:purchase.originalID, updatedAt:FieldValue.serverTimestamp()}, {merge:true});
      const patch = isAdministrative(email, grant.data()) ? null : subscriptionUpdate(user.data().userSuscription, purchase, plan, true);
      if (patch) tx.update(userRef, {userSuscription: patch, membershipManagedBy: "verified_store"});
      return {duplicate, active: true};
    });
  }
  /** Notificaciones firmadas: un reembolso no puede revocar una concesión histórica o administrativa. */
  async applyStoreUpdate(purchase, {recordPurchase = true, token} = {}) {
    const ownerRef = this.db.collection('membershipOwners').doc(fingerprint(`${purchase.platform}:${purchase.originalID}`));
    const eventRef = this.db.collection('membershipPurchases').doc(fingerprint(`${purchase.platform}:${purchase.eventID}`));
    return this.db.runTransaction(async tx => {
      const [owner,event] = await Promise.all([tx.get(ownerRef),tx.get(eventRef)]);
      if (owner.exists && (owner.data().signedAt ?? 0) > purchase.signedAt) return {ignored:true};
      const email = owner.data()?.email;
      const userRef = email ? this.db.collection('users').doc(email) : null;
      const user = userRef ? await tx.get(userRef) : null;
      const grant = email ? await tx.get(this.db.collection('membershipEvents').doc('admin_' + fingerprint(email))) : null;
      const others = email ? await tx.get(this.db.collection('membershipOwners').where('email','==',email)) : null;
      const activeExpirations = (others?.docs ?? []).filter(doc => doc.id !== ownerRef.id)
        .map(doc => doc.data()).filter(row => row.status === 'active' && row.expiresAt > Date.now()).map(row => row.expiresAt);
      if (purchase.status === 'active' && purchase.expiresAt > Date.now()) activeExpirations.push(purchase.expiresAt);
      tx.set(ownerRef, {...(email ? {email} : {}), platform:purchase.platform, originalID:purchase.originalID,
        productID:purchase.productID,status:purchase.status,expiresAt:purchase.expiresAt,signedAt:purchase.signedAt,
        updatedAt:FieldValue.serverTimestamp()}, {merge:true});
      // Sin propietario todavía no se cuenta una conversión ni se concede acceso a ninguna cuenta.
      if (email && recordPurchase) tx.set(eventRef, {ownerKey:fingerprint(email),platform:purchase.platform,transactionID:purchase.eventID,
        originalID:purchase.originalID,productID:purchase.productID,status:purchase.status,expiresAt:purchase.expiresAt,
        amount:purchase.amount,currency:purchase.currency,...(event.exists ? {} : {verifiedAt:FieldValue.serverTimestamp()})}, {merge:true});
      if (purchase.platform === 'google' && token) tx.set(this.db.collection('membershipIntents').doc('store_' + ownerRef.id), {type:'store_receipt',platform:'google',purchaseToken:token,originalID:purchase.originalID,updatedAt:FieldValue.serverTimestamp()}, {merge:true});
      if (user?.exists && !isAdministrative(email, grant?.data())) {
        tx.update(userRef, {membershipManagedBy:'verified_store', 'userSuscription.isActive':activeExpirations.length > 0,
          ...(Object.hasOwn(user.data().userSuscription ?? {},'active') ? {'userSuscription.active':activeExpirations.length > 0} : {}),
          'userSuscription.nextExpiration':new Date(activeExpirations.length ? Math.max(...activeExpirations) : purchase.expiresAt).toISOString().slice(0,10)});
      }
      return {updated:true,claimed:!!email};
    });
  }

}


/** Visitas por cuenta y por apertura; un UUID aleatorio evita enviar correos o sus hashes a Analytics. */
export class MembershipVisitLedger {
  constructor(db) { this.db = db; }
  /** No escribe en users ni en las colecciones de compras. */
  async record(email, visitID, platform, metadata = {}) {
    email = normalizeEmail(email);
    if (!/^[a-f0-9-]{36}$/i.test(visitID) || !['apple','google'].includes(platform)) throw new MembershipError('INVALID_REQUEST',400);
    const key = fingerprint(email);
    const visitRef = this.db.collection('membershipVisits').doc(fingerprint(`${key}:${visitID}`));
    const accountRef = this.db.collection('membershipVisitAccounts').doc(key);
    return this.db.runTransaction(async tx => {
      const [visit,account,user] = await Promise.all([tx.get(visitRef),tx.get(accountRef),tx.get(this.db.collection('users').doc(email))]);
      if (!user.exists) throw new MembershipError('ACCOUNT_NOT_FOUND',404);
      const analyticsID = account.data()?.analyticsID ?? randomUUID();
      if (!visit.exists) {
        const clean = value => typeof value === 'string' ? value.replace(/[\u0000-\u001f\u007f]/g,'').trim() : '';
        const installation = clean(metadata.installationId);
        const details = {
          ...( /^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/i.test(installation) ? {installationKey:fingerprint(`${platform}:${installation.toLowerCase()}`)} : {}),
          ...(clean(metadata.deviceModel) ? {deviceModel:clean(metadata.deviceModel).slice(0,100)} : {}),
          ...(clean(metadata.appVersion) ? {appVersion:clean(metadata.appVersion).slice(0,30)} : {}),
          ...(clean(metadata.appBuild) ? {appBuild:clean(metadata.appBuild).slice(0,30)} : {}),
          ...(clean(metadata.osVersion) ? {osVersion:clean(metadata.osVersion).slice(0,40)} : {}),
          ...(['profile_membership','membership_packages'].includes(metadata.source) ? {source:metadata.source} : {}),
          ...(['prod','dev','qa'].includes(metadata.environment) ? {environment:metadata.environment} : {})
        };
        tx.set(visitRef,{ownerKey:key,platform,timestamp:FieldValue.serverTimestamp(),...details});
        tx.set(accountRef,{analyticsID,visits:(account.data()?.visits ?? 0)+1,lastVisitAt:FieldValue.serverTimestamp()},{merge:true});
      }
      return {analyticsID,duplicate:visit.exists};
    });
  }
}
