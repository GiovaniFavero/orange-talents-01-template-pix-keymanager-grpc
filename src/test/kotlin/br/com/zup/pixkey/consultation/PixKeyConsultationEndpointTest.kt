package br.com.zup.pixkey.consultation

import br.com.zup.*
import br.com.zup.integration.bcb.BcbPixKeyClient
import br.com.zup.pixkey.PixKey
import br.com.zup.pixkey.PixKeyRepository
import br.com.zup.pixkey.registration.AssociatedAccount
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import java.util.*
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class PixKeyConsultationEndpointTest(
    val repository: PixKeyRepository,
    @Inject val grpcClient: PixKeyConsultationServiceGrpc.PixKeyConsultationServiceBlockingStub
) {

    private val customerId: String = "10287c77-c22c-48f9-8bfd-f1892b5746f0"
    private val validCpf = "71263991033"
    private var pixKey: PixKey? = null

    @Inject
    lateinit var bcbClient: BcbPixKeyClient

    @MockBean(BcbPixKeyClient::class)
    fun bcbClient(): BcbPixKeyClient = Mockito.mock(BcbPixKeyClient::class.java)

    @BeforeEach
    fun before(){
        repository.deleteAll()
    }

    @Test
    fun `Must consult a PixKey by Id and customerId`() {
        this.insertPixKey()

        val response = grpcClient.getPixKeyDetail(PixKeyConsultationRequest
            .newBuilder()
            .setPixId(PixKeyConsultationRequest.PixKeyId
                .newBuilder()
                .setPixId(this.pixKey?.id.toString())
                .setCustomerId(this.pixKey?.customerId.toString())
                .build())
            .build()
        )
        assertNotNull(response)
        with(response) {
            assertEquals(KeyType.CPF, response.key.keyType)
            assertEquals(validCpf, response.key.key)
            assertEquals(customerId, response.customerId)
        }
    }

    @Test
    fun `Must consult a Pix Key by Key`(){
        this.insertPixKey()

        val response = grpcClient.getPixKeyDetail(PixKeyConsultationRequest
            .newBuilder()
            .setKey(validCpf)
            .build()
        )
        assertNotNull(response)
        with(response) {
            assertEquals(KeyType.CPF, response.key.keyType)
            assertEquals(validCpf, response.key.key)
            assertEquals(customerId, response.customerId)
        }
    }

    @ParameterizedTest
    @MethodSource("getNotFoundArguments")
    fun `Must throws not found exception to nonexistent pix key`(argument: PixKeyConsultationRequest) {
        Mockito.`when`(bcbClient.findBcbPixKey("1234"))
            .thenReturn(HttpResponse.notFound())

        val error = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.getPixKeyDetail(argument)
        }
        assertEquals(Status.NOT_FOUND.code, error.status.code)
        assertEquals("Pix Key not found!", error.status.description)
    }

    companion object {
        @JvmStatic
        fun getNotFoundArguments() : Stream<Arguments> {
            return Stream.of(Arguments.of(
                PixKeyConsultationRequest.newBuilder()
                    .setKey("1234")
                    .build()),
                Arguments.of(PixKeyConsultationRequest.newBuilder()
                    .setPixId(
                        PixKeyConsultationRequest.PixKeyId.newBuilder()
                            .setPixId("10287c77-c22c-48f9-8bfd-f1892b5746f0")
                            .setCustomerId("10287c77-c22c-48f9-8bfd-f1892b5746f0")
                            .build())
                    .build()
            ))
        }
    }

    private fun insertPixKey() {
        val pixKey = PixKey(
            customerId = UUID.fromString(this.customerId),
            keyType = KeyType.CPF,
            key = validCpf,
            accountType = AccountType.CURRENT_ACCOUNT,
            account = AssociatedAccount(
                institution = "",
                ownerName = "",
                ownerCpf = "",
                branch = "",
                number = ""
            )
        )
        this.pixKey = repository.save(pixKey)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyConsultationServiceGrpc.PixKeyConsultationServiceBlockingStub {
            return PixKeyConsultationServiceGrpc.newBlockingStub(channel)
        }
    }

}