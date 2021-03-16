package br.com.zup.keys

import br.com.zup.KeyType
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface PixKeyRepository : JpaRepository<PixKey, Long> {

    fun countByCustomerIdAndKeyType(customerId: String, keyType: KeyType): Int

}