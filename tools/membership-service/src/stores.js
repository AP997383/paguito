import { readFileSync } from 'node:fs';
import { androidpublisher, auth } from '@googleapis/androidpublisher';
import { SignedDataVerifier, Environment } from '@apple/app-store-server-library';
import { MembershipError, fingerprint } from './policy.js';

/** Verificadores oficiales de las tiendas; ninguna operación de este módulo inicia cargos. */
export class StoreVerifier {
  constructor({ publisher, apple, now = () => Date.now() } = {}) {
    this.now = now;
    this.packageName = 'com.nexusystem.paguito';
    this.publisher = publisher ?? androidpublisher({version: 'v3', auth: new auth.GoogleAuth({scopes: ['https://www.googleapis.com/auth/androidpublisher']})});
    const certificates = ['AppleRootCA-G2', 'AppleRootCA-G3'].map(name => readFileSync(new URL(`../certificates/${name}.cer`, import.meta.url)));
    this.apple = apple ?? new SignedDataVerifier(certificates, true, Environment.PRODUCTION, 'com.nexus.ecosystem.Paguito', 6767476660);
  }
  /** Comprueba lectura de compras antes de permitir que el cliente abra el diálogo de pago. */
  async ready(platform, productID, probeTokens = []) {
    if (platform === 'apple') return productID === 'premium_monthly_basic';
    if (platform !== 'google') return false;
    // Solo requiere lectura de compras. Editar o consultar configuración del catálogo no demuestra ese permiso.
    // La existencia del plan se comprueba en el servidor y el producto/precio del teléfono vienen de Play Billing.
    for (const token of probeTokens) {
      try { await this.publisher.purchases.subscriptionsv2.get({packageName:this.packageName,token}); return true; }
      catch(error) {
        const status = error.response?.status ?? error.code;
        if (status === 410) return true; // Recibo encontrado, pero demasiado antiguo: el permiso fue comprobado.
        if (status === 400 || status === 404) continue;
        throw error;
      }
    }
    throw new MembershipError('GOOGLE_PLAY_PURCHASE_ACCESS_NOT_VERIFIED',503);
  }
  /** Apple firma identidad, producto, importe y vigencia; el cuerpo enviado por la app no los sustituye. */
  async verifyApple(token, renewalToken) {
    const tx = await this.apple.verifyAndDecodeTransaction(token);
    if (tx.productId !== 'com.paguito.premium.monthly' || !tx.originalTransactionId || !tx.transactionId) throw new MembershipError('INVALID_PRODUCT', 400);
    const renewal = renewalToken ? await this.apple.verifyAndDecodeRenewalInfo(renewalToken) : null;
    if (renewal?.originalTransactionId && renewal.originalTransactionId !== tx.originalTransactionId) throw new MembershipError('INVALID_RECEIPT',400);
    const graceEnd = renewal?.gracePeriodExpiresDate ?? 0;
    const expiresAt = Math.max(tx.expiresDate ?? 0, graceEnd > this.now() ? graceEnd : 0);
    return {platform: 'apple', productID: 'premium_monthly_basic', storeProductID: tx.productId,
      eventID: tx.transactionId, originalID: tx.originalTransactionId, accountID: tx.appAccountToken ?? null,
      expiresAt, signedAt: Math.max(tx.signedDate ?? 0,renewal?.signedDate ?? 0), status: tx.revocationDate ? 'revoked' : (expiresAt > this.now() ? 'active' : 'expired'),
      amount: typeof tx.price === 'number' ? tx.price / 1000 : null, displayPrice: typeof tx.price === 'number' ? String(tx.price/1000) : null, currency: tx.currency ?? null};
  }
  /** Consulta el token en Google: no confía en un productId, precio o estado del teléfono. */
  async verifyGoogle(token, productID) {
    const {data} = await this.publisher.purchases.subscriptionsv2.get({packageName: this.packageName, token});
    const item = data.lineItems?.find(line => productID ? line.productId === productID : line.productId === 'premium_monthly_basic');
    if (!item || !item.latestSuccessfulOrderId && !data.latestOrderId) throw new MembershipError('INVALID_PRODUCT', 400);
    const expiresAt = Date.parse(item.expiryTime);
    const recurring = item.autoRenewingPlan?.recurringPrice;
    const displayPrice = recurring ? String(Number(recurring.units ?? 0) + Number(recurring.nanos ?? 0)/1e9) : null;
    const active = ['SUBSCRIPTION_STATE_ACTIVE', 'SUBSCRIPTION_STATE_IN_GRACE_PERIOD', 'SUBSCRIPTION_STATE_CANCELED'].includes(data.subscriptionState) && expiresAt > this.now();
    return {platform: 'google', productID: item.productId, storeProductID: item.productId,
      eventID: item.latestSuccessfulOrderId ?? data.latestOrderId, originalID: fingerprint(token),
      accountID: data.externalAccountIdentifiers?.obfuscatedExternalAccountId ?? null,
      linkedOriginalID: data.linkedPurchaseToken ? fingerprint(data.linkedPurchaseToken) : null,
      expiresAt, signedAt: this.now(), status: active ? 'active' : 'expired', isTest: !!data.testPurchase,
      // El precio recurrente de catálogo no demuestra el importe efectivamente cobrado.
      amount: null, displayPrice, currency: null};
  }
  /** Detecta únicamente el formato; la firma o la API de Google realizan la validación real. */
  async verify(token, productID) {
    return token.split('.').length === 3 ? this.verifyApple(token) : this.verifyGoogle(token, productID);
  }
}
