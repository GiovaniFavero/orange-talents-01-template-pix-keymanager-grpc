package br.com.zup.pixkey.consultation

import br.com.zup.AccountType
import br.com.zup.KeyType
import br.com.zup.PixKeyListConsultationServiceGrpc
import br.com.zup.PixKeyListRequest
import br.com.zup.pixkey.PixKey
import br.com.zup.pixkey.PixKeyRepository
import br.com.zup.pixkey.registration.AssociatedAccount
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class PixKeyConsultationListEndpointTest (
    private val repository: PixKeyRepository,
    private val grpcClient: PixKeyListConsultationServiceGrpc.PixKeyListConsultationServiceBlockingStub
    ) {

    companion object {
        const val CUSTOMER_ID = "e4ff63ac-83e4-44a8-8bb6-2ecfd2c1bd80"
    }

    @BeforeEach
    fun before() {
        repository.deleteAll()
    }

    @Test
    fun `Must consult pix keys by customerId`() {
        repository.save(getNewPixKey(KeyType.CPF, "71263991033"))
        repository.save(getNewPixKey(KeyType.EMAIL, "email@email.com"))
        repository.save(getNewPixKey(KeyType.PHONE, "+5547991341697"))
        repository.save(getNewPixKey(KeyType.RANDOM, "89023749u4nh32junbikju"))

        val response = grpcClient.getPixKeyList(PixKeyListRequest.newBuilder()
            .setCustomerId(CUSTOMER_ID)
            .build())
        assertEquals(4, response.keysCount)
    }

    @Test
    fun `Must throws exception to blank customerId`() {
        val error = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.getPixKeyList(PixKeyListRequest.newBuilder()
                .setCustomerId("")
                .build())
        }
        assertEquals(Status.FAILED_PRECONDITION.code, error.status.code)
        assertEquals("Customer Id cannot be blank or null!", error.status.description)
    }

    private fun getNewPixKey(keyType: KeyType, key: String): PixKey {
        return PixKey(
            customerId = UUID.fromString(CUSTOMER_ID),
            keyType = keyType,
            key = key,
            accountType = AccountType.CURRENT_ACCOUNT,
            account = AssociatedAccount(
                institution = "",
                ownerName = "",
                ownerCpf = "",
                branch = "",
                number = ""
            )
        )
    }

    @Factory
    class ClientsGrpc {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyListConsultationServiceGrpc.PixKeyListConsultationServiceBlockingStub {
            return PixKeyListConsultationServiceGrpc.newBlockingStub(channel)
        }
    }
}