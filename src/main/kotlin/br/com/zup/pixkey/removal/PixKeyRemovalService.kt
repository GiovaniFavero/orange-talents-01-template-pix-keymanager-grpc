package br.com.zup.pixkey.removal

import br.com.zup.integration.bcb.BcbPixKeyClient
import br.com.zup.integration.bcb.dtos.DeletePixKeyRequest
import br.com.zup.pixkey.PixKeyRepository
import br.com.zup.shared.exceptions.NotFoundException
import br.com.zup.shared.validators.ValidUUID
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class PixKeyRemovalService(
    @Inject val repository: PixKeyRepository,
    @Inject val bcbPixKeyClient: BcbPixKeyClient
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun remove(@NotBlank @ValidUUID(message = "Pix ID with invalid format") pixId: String?,
               @NotBlank @ValidUUID(message = "Customer ID with invalid format") customerId: String?) {

        val uuIdPix = UUID.fromString(pixId)
        val uuIdCustomer = UUID.fromString(customerId)

        val key = repository.findByIdAndCustomerId(uuIdPix, uuIdCustomer)
            .orElseThrow { NotFoundException("Pix Key not found or it does not belong to this customer!") }

        repository.deleteById(uuIdPix)

        val request = DeletePixKeyRequest(key.key)

        logger.info("Deleting pix key in BCB: ${key.key} - $request")
        val bcbResponse = bcbPixKeyClient.deleteBcbPixKey(key = key.key, request = request)
        if (bcbResponse.status != HttpStatus.OK) {
            throw IllegalStateException("Something went wrong to delete pix key in BCB")
        }
    }

}
