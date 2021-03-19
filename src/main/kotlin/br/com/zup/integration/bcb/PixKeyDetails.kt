package br.com.zup.integration.bcb

import br.com.zup.AccountType
import br.com.zup.KeyType
import br.com.zup.pixkey.PixKey
import br.com.zup.pixkey.registration.AssociatedAccount
import java.util.*

class PixKeyDetails(
    val customerId: UUID? = null,
    val pixId: UUID? = null,
    val type: KeyType,
    val key: String,
    val accountType: AccountType,
    val associatedAccount: AssociatedAccount
) {

    companion object {
        fun of(pixKey: PixKey): PixKeyDetails {
            return PixKeyDetails(
                customerId = pixKey.customerId,
                pixId = pixKey.id,
                type = pixKey.keyType,
                key = pixKey.key,
                accountType = pixKey.accountType,
                associatedAccount = pixKey.account
            )
        }
    }
}
