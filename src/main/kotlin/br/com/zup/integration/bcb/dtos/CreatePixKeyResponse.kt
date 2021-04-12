package br.com.zup.integration.bcb.dtos

import br.com.zup.KeyType
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime

@Introspected
data class CreatePixKeyResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)