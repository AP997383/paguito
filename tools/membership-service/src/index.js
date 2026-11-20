import { onMessagePublished } from 'firebase-functions/v2/pubsub';
import { onRequest } from 'firebase-functions/v2/https';
import { initializeApp, getApps } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import { StoreVerifier } from './stores.js';
import { MembershipLedger, MembershipVisitLedger } from './ledger.js';
import { MembershipError, normalizeEmail, fingerprint } from './policy.js';

if (!getApps().length) initializeApp();
const db = getFirestore();
const verifier = new StoreVerifier();
const ledger = new MembershipLedger(db);
const readiness = new Map();
/** Comprueba lectura de compras desde la identidad real del servicio, sin conceder beneficios. */
async function storeReady(platform,productID) {
  const key = `${platform}:${productID}`;
  if (readiness.get(key)?.until > Date.now()) return readiness.get(key).ready;
  let probes = [];
  if (platform === 'google') {
    const rows = await db.collection('suscripciones').where('productId','==',productID).limit(500).get();
    probes = [...new Set(rows.docs.map(doc=>doc.data().purchaseToken).filter(token=>typeof token === 'string' && token.length > 20 && token.split('.').length !== 3))].slice(0,3);
  }
  const ready = await verifier.ready(platform,productID,probes);
  readiness.set(key,{ready,until:Date.now()+60000});
  return ready;
}


/** Error acotado, sin recibos, tokens, correos ni respuestas completas de las tiendas en logs. */
function failure(res, error) {
  if (error instanceof MembershipError) return res.status(error.status).json({success: false, code: error.code, message: 'No se pudo confirmar esta compra. Conserva tu recibo y restaura con la cuenta que la adquirió.'});
  console.error('membership_verification_unavailable', {code: error.code ?? 'UNAVAILABLE'});
  return res.status(503).json({success: false, code: 'VALIDATION_PENDING', message: 'La compra está pendiente de validar. No necesitas volver a pagar.'});
}
/** Sustituye al verificador histórico; solo activa con pruebas oficiales y nunca cobra ni añade días artificiales. */
export const verifyAndSyncPurchase = onRequest({region: 'us-central1', timeoutSeconds: 60}, async (req, res) => {
  if (req.method !== 'POST') return res.status(405).json({success: false});
  try {
    const email = normalizeEmail(req.body?.email);
    const token = String(req.body?.purchaseToken || '');
    const productID = String(req.body?.productId || '');
    if (!token || token.length > 50000 || !productID) throw new MembershipError('INVALID_REQUEST', 400);
    const plan = await db.collection('suscriptions').doc(productID).get();
    if (!plan.exists) throw new MembershipError('INVALID_PRODUCT', 400);
    const purchase = await verifier.verify(token, productID);
    if (purchase.productID !== productID) throw new MembershipError('INVALID_PRODUCT', 400);
    const result = await ledger.confirm(email, purchase, token, plan.data());
    return res.json({success: true, productId: productID, alreadyProcessed: result.duplicate, message: 'Compra validada correctamente.'});
  } catch (error) { return failure(res, error); }
});
/** Consulta previa al cobro: si no se puede verificar la tienda, no se permite iniciar una compra nueva. */
export const membershipPurchaseStatus = onRequest({region: 'us-central1', timeoutSeconds: 30}, async (req, res) => {
  if (req.method !== 'POST') return res.status(405).json({success: false});
  try {
    const email = normalizeEmail(req.body?.email);
    const user = await db.collection('users').doc(email).get();
    if (!user.exists) throw new MembershipError('ACCOUNT_NOT_FOUND', 404);
    const active = user.data().userSuscription?.isActive === true;
    const productID = String(req.body?.productId || '');
    if (!/^[a-zA-Z0-9._-]{1,150}$/.test(productID)) throw new MembershipError('INVALID_PRODUCT',400);
    const plan = await db.collection('suscriptions').doc(productID).get();
    let ready = false;
    let validationCode = 'READY';
    try { ready = plan.exists && await storeReady(req.body?.platform, productID); }
    catch(error) {
      // Diagnóstico acotado: jamás serializar el error del SDK, que incluye el token en la URL.
      const status = Number(error.response?.status ?? error.code);
      const allowedReasons = new Set(['ACCESS_TOKEN_SCOPE_INSUFFICIENT','SERVICE_DISABLED','PERMISSION_DENIED','permissionDenied','insufficientPermissions','authError','unauthorized']);
      const reasons = [error.response?.data?.error?.status, ...(error.response?.data?.error?.errors ?? []).map(item=>item.reason), ...(error.response?.data?.error?.details ?? []).map(item=>item.reason)].filter(reason=>allowedReasons.has(reason));
      console.warn('membership_store_readiness', {status:Number.isFinite(status)?status:null,errorType:error.name,reasons});
      validationCode = error instanceof MembershipError ? error.code : ([401,403].includes(status) ? 'GOOGLE_PLAY_ACCESS_DENIED' : 'STORE_UNAVAILABLE');
      if (!active) throw error;
    }
    // Una configuración de tienda incompleta nunca revoca Premium de una cuenta existente.
    if (active) return res.json({success:true,isPremium:true,canPurchase:false,validationReady:ready,validationCode});
    return res.json({success: true, isPremium: false, canPurchase: ready});
  } catch (error) { return failure(res, error); }
});

/** Visitas únicas por apertura y personas por cuenta, separadas completamente del historial de cobros. */
export const membershipPackagesViewed = onRequest({region: 'us-central1', timeoutSeconds: 30}, async (req, res) => {
  if (req.method !== 'POST') return res.status(405).json({success: false});
  try {
    const result = await new MembershipVisitLedger(db).record(req.body?.email,String(req.body?.visitId || ''),req.body?.platform);
    return res.json({success:true,...result});
  } catch(error) { return failure(res,error); }
});

/** Apple entrega renovaciones y reembolsos firmados; no depende de que el teléfono esté abierto. */
export const membershipAppleNotifications = onRequest({region:'us-central1',timeoutSeconds:60},async(req,res)=>{
  if(req.method !== 'POST')return res.status(405).end();
  try {
    const notification = await verifier.apple.verifyAndDecodeNotification(req.body?.signedPayload);
    if(!notification.data?.signedTransactionInfo)return res.json({received:true});
    const purchase = await verifier.verifyApple(notification.data.signedTransactionInfo);
    await ledger.applyStoreUpdate(purchase);
    return res.json({received:true});
  }catch(error){return failure(res,error)}
});
/** El tópico privado recibe RTDN de Google; el estado se vuelve a consultar en la API oficial. */
export const membershipGoogleNotifications = onMessagePublished({topic:'paguito-play-billing-events',region:'us-central1',retry:true},async(event)=>{
  const message = event.data.message.json;
  if(message?.packageName !== 'com.nexusystem.paguito')return;
  const notification = message.subscriptionNotification ?? message.voidedPurchaseNotification;
  if(!notification?.purchaseToken)return;
  try {
    const purchase = await verifier.verifyGoogle(notification.purchaseToken,notification.subscriptionId);
    await ledger.applyStoreUpdate(purchase);
  } catch(error) {
    if(error instanceof MembershipError && error.status === 400)return;
    throw error;
  }
});
