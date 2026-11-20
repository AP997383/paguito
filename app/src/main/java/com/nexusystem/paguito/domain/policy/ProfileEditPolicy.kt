package com.nexusystem.paguito.domain.policy

import com.nexusystem.paguito.data.local.entity.UserProfileEntity

/** Combina solo datos editables y conserva los derechos Premium de la sesión vigente. */
fun mergeEditableProfile(current: UserProfileEntity, edited: UserProfileEntity): UserProfileEntity {
    require(current.email.trim().equals(edited.email.trim(), ignoreCase = true))
    return current.copy(fullName = edited.fullName, phone = edited.phone, bussinesName = edited.bussinesName, fotoUrl = edited.fotoUrl)
}
