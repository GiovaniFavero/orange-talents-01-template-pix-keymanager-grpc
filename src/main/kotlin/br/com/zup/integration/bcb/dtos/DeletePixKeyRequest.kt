package br.com.zup.integration.bcb.dtos

import br.com.zup.pixkey.registration.AssociatedAccount
import io.micronaut.core.annotation.Introspected

@Introspected
data class DeletePixKeyRequest(
    val key: String,
    val participant: String = AssociatedAccount.ITAU_UNIBANCO_ISPB
)