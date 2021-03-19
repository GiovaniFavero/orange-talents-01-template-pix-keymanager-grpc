package br.com.zup.integration.bcb.dtos

import io.micronaut.core.annotation.Introspected

@Introspected
data class BankAccount (
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType?
) {
    enum class AccountType {
        CACC,
        SVGS
    }
}