package br.com.zup.pixkey

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixKeyRepository : JpaRepository<PixKey, UUID> {

    fun countByKey(key: String): Int

    fun findByIdAndCustomerId(id: UUID, customerId: UUID): Optional<PixKey>

    fun findByKey(key: String) : Optional<PixKey>

    fun findAllByCustomerId(customerId: UUID): List<PixKey>

}