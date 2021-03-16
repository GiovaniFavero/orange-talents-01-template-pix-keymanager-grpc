package br.com.zup.keys

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:9091/api/v1/clientes")
interface CustomerRequestClient {

    @Get("/{customerId}")
    fun searchCustomer(customerId: String) : HttpResponse<CustomerResponse>
}

data class CustomerResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: InstitutionResponse
)

data class InstitutionResponse(
    val nome: String,
    val ispb: String
)
