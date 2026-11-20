package com.nexusystem.paguito.domain.usescases.billing

/**
 * La compra solo se reconoce después de validarla y disponer de beneficios en la misma cuenta.
 * Las comprobaciones entre suspensiones evitan aplicar una compra a una sesión reemplazada.
 */
suspend fun confirmMembershipPurchase(
    owner: String, currentOwner: () -> String,
    validate: suspend () -> Boolean, refresh: suspend () -> Boolean, acknowledge: suspend () -> Boolean
) {
    check(owner.isNotBlank() && owner == currentOwner())
    check(validate()); check(owner == currentOwner())
    check(refresh()); check(owner == currentOwner())
    check(acknowledge()); check(owner == currentOwner())
}
