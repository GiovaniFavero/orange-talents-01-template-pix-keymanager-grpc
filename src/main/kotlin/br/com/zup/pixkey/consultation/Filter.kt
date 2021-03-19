package br.com.zup.pixkey.consultation

import br.com.zup.integration.bcb.BcbPixKeyClient
import br.com.zup.integration.bcb.PixKeyDetails
import br.com.zup.pixkey.PixKeyRepository
import br.com.zup.shared.exceptions.NotFoundException
import br.com.zup.shared.validators.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filter {

    abstract fun filterKey(repository: PixKeyRepository, bcbPixKeyClient: BcbPixKeyClient): PixKeyDetails

    @Introspected
    data class ByPixId(
        @field:NotBlank @field:ValidUUID val pixId: String,
        @field:NotBlank @field:ValidUUID val customerId: String
    ) : Filter() {

        fun pixIdAsUuid() = UUID.fromString(pixId)
        fun customerIdAsUuid() = UUID.fromString(customerId)

        override fun filterKey(repository: PixKeyRepository, bcbPixKeyClient: BcbPixKeyClient): PixKeyDetails {
            return repository.findById(pixIdAsUuid())
                .filter { it.belongsToCustomer(customerIdAsUuid()) }
                .map(PixKeyDetails::of)
                .orElseThrow { NotFoundException("Pix Key not found!") }
        }

    }

    @Introspected
    data class ByKey(@field:NotBlank @Size(max = 77) val key: String) : Filter() {

        val logger = LoggerFactory.getLogger(this::class.java)

        override fun filterKey(repository: PixKeyRepository, bcbPixKeyClient: BcbPixKeyClient): PixKeyDetails {
            return repository.findByKey(key)
                .map(PixKeyDetails::of)
                .orElseGet {
                    logger.info("Searching Pix Key $key on BCB")

                    val response = bcbPixKeyClient.findBcbPixKey(key)
                    when (response.status) {
                        HttpStatus.OK -> response.body().toModel()
                        else -> throw NotFoundException("Pix Key not found!")
                    }
                }
        }

    }

    @Introspected
    class Invalid : Filter() {
        override fun filterKey(repository: PixKeyRepository, bcbPixKeyClient: BcbPixKeyClient): PixKeyDetails {
            throw IllegalArgumentException("Pix key is invalid or was not sent")
        }
    }
 }
