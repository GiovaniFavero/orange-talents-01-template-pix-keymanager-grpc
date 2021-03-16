package br.com.zup.keys

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/teste")
class TestController(val customerRequestClient: CustomerRequestClient) {

    @Get
    fun teste(): String {
        val consukta = customerRequestClient.searchCustomer("c56dfef4-7901-44fb-84e2-a2cefb157890")
        return consukta.body().toString()
    }

}