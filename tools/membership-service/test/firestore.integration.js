import { initializeTestEnvironment, assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { readFileSync } from 'node:fs';
import { doc, getDoc, setDoc, updateDoc, deleteDoc } from 'firebase/firestore';
import test, { before, after, beforeEach } from 'node:test';
let env;
before(async()=>{env=await initializeTestEnvironment({projectId:'demo-paguito-memberships',firestore:{host:'127.0.0.1',port:8787,rules:readFileSync('firestore.rules','utf8')}})});
after(async()=>{await env?.cleanup()});
beforeEach(async()=>{await env.clearFirestore();await env.withSecurityRulesDisabled(async ctx=>{
 await setDoc(doc(ctx.firestore(),'users/premium@example.com'),{fullName:'Premium',userSuscription:{isActive:true,price:79,nextExpiration:'2026-06-15'}});
 await setDoc(doc(ctx.firestore(),'users/free@example.com'),{fullName:'Free',userSuscription:{isActive:false}});
 await setDoc(doc(ctx.firestore(),'membershipPurchases/private'),{transactionID:'private-order'});
 await setDoc(doc(ctx.firestore(),'suscripciones/legacy'),{purchaseToken:'private-receipt'});
})});
test('un cliente no puede activar Premium ni borrar la cuenta para recrearla',async()=>{
 const db=env.unauthenticatedContext().firestore();
 await assertFails(updateDoc(doc(db,'users/free@example.com'),{'userSuscription.isActive':true}));
 await assertFails(deleteDoc(doc(db,'users/premium@example.com')));
 await assertFails(updateDoc(doc(db,'users/free@example.com'),{membershipManagedBy:'manual'}));
 await assertFails(setDoc(doc(db,'users/new@example.com'),{userSuscription:{isActive:true}}));
});
test('conserva edición de perfil, formato de precio y recordatorios sin tocar el derecho Premium',async()=>{
 const db=env.unauthenticatedContext().firestore();
 await assertSucceeds(updateDoc(doc(db,'users/premium@example.com'),{fullName:'Nuevo nombre',RemindersActive:true,'userSuscription.price':'79'}));
 const result=await getDoc(doc(db,'users/premium@example.com'));
 if(result.data().userSuscription.isActive!==true)throw Error('Premium alterado');
});
test('también bloquea cambiar los alias de membresía',async()=>{
 const db=env.unauthenticatedContext().firestore();
 await assertFails(updateDoc(doc(db,'users/free@example.com'),{'userSubscription.isActive':true}));
 await assertFails(updateDoc(doc(db,'users/free@example.com'),{'userSuscription.active':true}));
});
test('historial de compras y recibos antiguos son privados incluso para clientes autenticados',async()=>{
 for(const ctx of [env.unauthenticatedContext(),env.authenticatedContext('user')]){
  const db=ctx.firestore();
  await assertFails(getDoc(doc(db,'membershipPurchases/private')));
  await assertFails(getDoc(doc(db,'suscripciones/legacy')));
  await assertFails(setDoc(doc(db,'membershipOwners/forged'),{email:'attacker@example.com'}));
 }
});
test('mantiene CRUD heredado de productos, clientes y catálogo',async()=>{
 const db=env.unauthenticatedContext().firestore();
 for(const path of ['productos/user/productos/p1','deudores/user/deudores/c1','suscriptions/basic']){
  await assertSucceeds(setDoc(doc(db,path),{name:'existing-flow'}));
  await assertSucceeds(getDoc(doc(db,path)));await assertSucceeds(deleteDoc(doc(db,path)));
 }
});
