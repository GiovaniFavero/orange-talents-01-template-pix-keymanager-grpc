package br.com.zup.pixkey.registration

import br.com.zup.integration.bcb.BcbPixKeyClient
import br.com.zup.integration.bcb.dtos.CreatePixKeyRequest
import br.com.zup.pixkey.PixKey
import br.com.zup.pixkey.PixKeyRepository
import br.com.zup.integration.itau.CustomerRequestClient
import br.com.zup.shared.exceptions.ExistingPixKeyException
import br.com.zup.shared.exceptions.NotFoundException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class PixKeyRegistrationService(
    val repository: PixKeyRepository,
    val itauClient: CustomerRequestClient,
    val bcbPixKeyClient: BcbPixKeyClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun register(@Valid newPixKey: NewPixKey): PixKey {

        logger.info("Checking for existing key...")
        if (repository.countByKey(newPixKey.key!!) > 0) {
            throw ExistingPixKeyException("Pix key ${newPixKey.key} is already registered")
        }

        val response: HttpResponse<AccountDataResponse>? =
            try { itauClient.getAccountByType(newPixKey.customerId!!, newPixKey.getConvertedAccountType()) } catch(e: Exception) { null }
        val account = response?.body()?.toModel() ?: throw NotFoundException("Customer not found!")

        val key = newPixKey.toModel(account)
        repository.save(key)

        val bcbRequest = CreatePixKeyRequest.of(key).also {
            logger.info("Registering pix key in BCB. $it")
        }

        val bcbResponse = bcbPixKeyClient.registerBcbPixKey(bcbRequest)
        if (bcbResponse.status != HttpStatus.CREATED) {
            throw IllegalStateException("Something went wrong to register pix key in BCB")
        }

        key.updateKey(bcbResponse.body().key)

        return key
    }

}