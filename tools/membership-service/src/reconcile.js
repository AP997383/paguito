import { FieldPath } from 'firebase-admin/firestore';
import { MembershipLedger } from './ledger.js';
import { fingerprint, isAdministrative } from './policy.js';

/** Consulta de respaldo: valida en la tienda antes de cambiar derechos, sin registrar compras nuevas. */
export async function reconcileSubscriptions(db, verifier, appleClient, {dryRun = false} = {}) {
  const ledger = new MembershipLedger(db), result = {checked:0,updated:0,administrative:0,unavailable:0};
  let last;
  do {
    let query = db.collection('membershipOwners').orderBy(FieldPath.documentId()).limit(100);
    if (last) query = query.startAfter(last);
    const page = await query.get();
    for (const document of page.docs) {
      const owner = document.data();
      if (!owner.email || !['apple','google'].includes(owner.platform)) continue;
      const [user,grant] = await Promise.all([db.collection('users').doc(owner.email).get(), db.collection('membershipEvents').doc('admin_'+fingerprint(owner.email)).get()]);
      if (!user.exists) continue;
      if (isAdministrative(owner.email,grant.data())) { result.administrative++; continue; }
      result.checked++;
      try {
        let purchase;
        if (owner.platform === 'apple') {
          const response = await appleClient.getAllSubscriptionStatuses(owner.originalID);
          const row = response.data?.flatMap(group=>group.lastTransactions||[]).find(row=>row.originalTransactionId===owner.originalID);
          if (!row?.signedTransactionInfo) throw new Error('CURRENT_STATUS_MISSING');
          purchase = await verifier.verifyApple(row.signedTransactionInfo,row.signedRenewalInfo);
          if (purchase.originalID !== owner.originalID) throw new Error('CURRENT_STATUS_MISMATCH');
          if ([2,3,5].includes(row.status)) purchase.status = row.status===5?'revoked':'expired';
        } else {
          const saved = await db.collection('membershipIntents').doc('store_'+document.id).get();
          let token = saved.data()?.purchaseToken;
          if (!token) {
            const old = await db.collection('suscripciones').where('email','==',owner.email).limit(100).get();
            token = old.docs.map(d=>d.data().purchaseToken).find(t=>typeof t==='string'&&fingerprint(t)===owner.originalID);
          }
          if (!token || fingerprint(token)!==owner.originalID) throw new Error('PRIVATE_RECEIPT_UNAVAILABLE');
          try { purchase = await verifier.verifyGoogle(token,owner.productID); }
          catch (error) {
            if (Number(error.response?.status??error.code)!==410 || owner.expiresAt>Date.now()) throw error;
            purchase = {...owner,eventID:owner.originalID,status:'expired',signedAt:Date.now(),amount:null,currency:null};
          }
        }
        if (purchase.isTest && process.env.GCLOUD_PROJECT==='paguito-92b2d') throw new Error('TEST_PURCHASE_NOT_ALLOWED');
        if (!dryRun) await ledger.applyStoreUpdate(purchase,{recordPurchase:false});
        result.updated++;
      } catch { result.unavailable++; }
    }
    last = page.size===100?page.docs.at(-1):null;
  } while(last);
  return result;
}
