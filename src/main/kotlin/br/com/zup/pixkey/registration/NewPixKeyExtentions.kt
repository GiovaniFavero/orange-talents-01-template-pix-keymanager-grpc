package br.com.zup.pixkey.registration

import br.com.zup.AccountType
import br.com.zup.KeyType
import br.com.zup.PixKeyRequest

fun PixKeyRequest.toModel(): NewPixKey {
    return NewPixKey(
        customerId = customerId,
        keyType = when(keyType) {
            KeyType.UNKNOWN_KEY_TYPE -> null
            else -> PixKeyType.valueOf(keyType.name)
        },
        key = key,
        accountType = when (accountType) {
            AccountType.UNKNOWN_ACCOUNT -> null
            else -> AccountType.valueOf(accountType.name)
        }
    )
}