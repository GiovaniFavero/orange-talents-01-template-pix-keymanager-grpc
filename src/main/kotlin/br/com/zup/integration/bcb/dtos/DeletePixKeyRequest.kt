package br.com.zup.integration.bcb.dtos

import br.com.zup.pixkey.registration.AssociatedAccount
import io.micronaut.core.annotation.Introspected

@Introspected
data class DeletePixKeyRequest(
    val key: String,
    val participant: String = AssociatedAccount.ITAU_UNIBANCO_ISPB
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeletePixKeyRequest

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}