package br.com.zup.integration.bcb.dtos

import br.com.zup.AccountType
import br.com.zup.KeyType
import br.com.zup.integration.bcb.PixKeyDetails
import br.com.zup.pixkey.registration.AssociatedAccount
import java.time.LocalDateTime

class PixKeyDetailsResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {

    fun toModel(): PixKeyDetails {
        return PixKeyDetails(
            type = keyType,
            key = key,
            accountType = when (this.bankAccount.accountType) {
                BankAccount.AccountType.CACC -> AccountType.CURRENT_ACCOUNT
                BankAccount.AccountType.SVGS -> AccountType.SAVINGS_ACCOUNT
                else -> AccountType.UNKNOWN_ACCOUNT
            },
            associatedAccount = AssociatedAccount(
                institution = bankAccount.participant,
                ownerName = owner.name,
                ownerCpf = owner.taxIdNumber,
                branch = bankAccount.branch,
                number = bankAccount.accountNumber
            )
        )

    }

}
