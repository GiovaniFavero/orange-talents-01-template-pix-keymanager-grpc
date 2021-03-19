package br.com.zup.integration.bcb.dtos

import br.com.zup.AccountType
import br.com.zup.KeyType
import br.com.zup.pixkey.PixKey
import br.com.zup.pixkey.registration.AssociatedAccount
import io.micronaut.core.annotation.Introspected

@Introspected
data class CreatePixKeyRequest(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
) {
    companion object {
        fun of(key: PixKey) : CreatePixKeyRequest {
            return CreatePixKeyRequest(
                keyType = key.keyType,
                key = key.key,
                bankAccount = BankAccount(
                    participant = AssociatedAccount.ITAU_UNIBANCO_ISPB,
                    branch = key.account.branch,
                    accountNumber = key.account.number,
                    accountType = when (key.accountType) {
                        AccountType.CURRENT_ACCOUNT -> BankAccount.AccountType.CACC
                        AccountType.SAVINGS_ACCOUNT -> BankAccount.AccountType.SVGS
                        else -> null
                    }
                ),
                owner = Owner(
                    type = Owner.OwnerType.NATURAL_PERSON,
                    name = key.account.ownerName,
                    taxIdNumber = key.account.ownerCpf
                )
            )
        }
    }
}
