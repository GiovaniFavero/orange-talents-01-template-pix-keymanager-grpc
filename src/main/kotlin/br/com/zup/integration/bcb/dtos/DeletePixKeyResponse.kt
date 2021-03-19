package br.com.zup.integration.bcb.dtos

import java.time.LocalDateTime

class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)
