package br.com.zup.integration.itau

import br.com.zup.pixkey.registration.AccountDataResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.contas.url}")
interface CustomerRequestClient {

    @Get("/api/v1/clientes/{customerId}/contas{?tipo}")
    fun getAccountByType(@PathVariable customerId: String, @QueryValue("tipo") type: String): HttpResponse<AccountDataResponse>
}