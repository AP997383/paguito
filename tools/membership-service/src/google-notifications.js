import { MembershipError } from './policy.js';

/** Procesa RTDN sin registrar tokens; los mensajes irrecuperables no generan reintentos permanentes. */
export async function processGoogleNotification(message, verifier, ledger) {
  if (message?.packageName !== 'com.nexusystem.paguito') return {ignored:true};
  const notification = message.subscriptionNotification ?? message.voidedPurchaseNotification;
  if (!notification?.purchaseToken) return {ignored:true};
  try {
    const purchase = await verifier.verifyGoogle(notification.purchaseToken,notification.subscriptionId);
    return await ledger.applyStoreUpdate(purchase,{token:notification.purchaseToken});
  } catch(error) {
    const status = Number(error.response?.status ?? error.status ?? error.code);
    if ((error instanceof MembershipError && error.status === 400) || [400,404,410].includes(status)) return {ignored:true};
    // El error original del SDK puede contener el token dentro de la URL de la petición.
    // Permisos, límites de API y fallos temporales se reintentan con un error público acotado.
    throw new Error('MEMBERSHIP_STORE_UNAVAILABLE');
  }
}
