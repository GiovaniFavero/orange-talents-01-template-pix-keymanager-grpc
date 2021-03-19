package br.com.zup.integration.bcb

import br.com.zup.integration.bcb.dtos.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.pix.url}")
interface BcbPixKeyClient {

    @Post("/api/v1/pix/keys",
          consumes = [MediaType.APPLICATION_XML],
          produces = [MediaType.APPLICATION_XML])
    fun registerBcbPixKey(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/api/v1/pix/keys/{key}",
           consumes = [MediaType.APPLICATION_XML],
           produces = [MediaType.APPLICATION_XML])
    fun deleteBcbPixKey(@PathVariable key: String, @Body request: DeletePixKeyRequest): HttpResponse<DeletePixKeyResponse>

    @Get("/api/v1/pix/keys/{key}",
        consumes = [MediaType.APPLICATION_XML])
    fun findBcbPixKey(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>
}