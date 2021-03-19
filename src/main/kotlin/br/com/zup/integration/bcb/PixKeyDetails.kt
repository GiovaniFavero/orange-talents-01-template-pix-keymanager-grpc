package br.com.zup.integration.bcb

import br.com.zup.AccountType
import br.com.zup.KeyType
import br.com.zup.pixkey.registration.AssociatedAccount

class PixKeyDetails(
    val type: KeyType,
    val key: String,
    val accountType: AccountType,
    val associatedAccount: AssociatedAccount
)
