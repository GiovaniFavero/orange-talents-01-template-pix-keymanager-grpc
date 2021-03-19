package br.com.zup.integration.bcb.dtos

import io.micronaut.core.annotation.Introspected

@Introspected
data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
) {
    enum class OwnerType {
        NATURAL_PERSON
    }
}