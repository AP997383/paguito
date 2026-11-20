import { createHash } from 'node:crypto';

/** Normaliza identidad sin conceder beneficios por un correo o por una lista del cliente. */
export function normalizeEmail(value) {
  const email = String(value || '').trim().toLowerCase();
  if (!/^[^\s/@]+@[^\s/@]+\.[^\s/@]+$/.test(email)) throw new MembershipError('INVALID_ACCOUNT', 400);
  return email;
}
/** Identificador de registros y Analytics; los tokens originales nunca se utilizan como IDs ni en logs. */
export function fingerprint(value) { return createHash('sha256').update(String(value)).digest('hex'); }
/** Coincide exactamente con el UUID que incluye iOS en la transacción de Apple. */
export function accountUUID(email) {
  const bytes = Buffer.from(fingerprint(normalizeEmail(email)).slice(0,32), 'hex');
  bytes[6] = (bytes[6] & 15) | 80; bytes[8] = (bytes[8] & 63) | 128;
  const hex = bytes.toString('hex');
  return `${hex.slice(0,8)}-${hex.slice(8,12)}-${hex.slice(12,16)}-${hex.slice(16,20)}-${hex.slice(20)}`;
}
/** Errores públicos acotados: nunca devuelve errores del SDK que puedan contener recibos o credenciales. */
export class MembershipError extends Error {
  constructor(code, status = 409) { super(code); this.code = code; this.status = status; }
}
/** Solo una confirmación vigente y del producto real puede activar nuevos beneficios. */
export function assertVerifiedPurchase(purchase, email, now = Date.now()) {
  if (!purchase || !['apple', 'google'].includes(purchase.platform)) throw new MembershipError('INVALID_PLATFORM', 400);
  if (!purchase.eventID || !purchase.originalID || !purchase.productID) throw new MembershipError('INVALID_RECEIPT', 400);
  if (purchase.status !== 'active' || !Number.isFinite(purchase.expiresAt) || purchase.expiresAt <= now) throw new MembershipError('PURCHASE_NOT_ACTIVE');
  const expected = purchase.platform === 'apple' ? accountUUID(email) : fingerprint(normalizeEmail(email));
  if (purchase.accountID && purchase.accountID.toLowerCase() !== expected) throw new MembershipError('PURCHASE_OWNED_BY_ANOTHER_ACCOUNT');
}
/** El acceso administrativo o histórico existente se conserva; una validación fallida nunca lo desactiva. */
export function subscriptionUpdate(current, purchase, plan, managed = false) {
  const original = current || {};
  const expiration = new Date(purchase.expiresAt).toISOString().slice(0,10);
  if (original.isActive === true && !managed) return null;
  return {...original, ...(Object.hasOwn(original,'active') ? {active:true} : {}), isActive: true, idSuscription: purchase.productID, nameSuscription: plan.name || 'Paguito Premium', price: purchase.displayPrice ?? plan.price ?? '', nextExpiration: expiration};
}
