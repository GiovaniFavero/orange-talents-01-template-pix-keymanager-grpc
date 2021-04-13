package br.com.zup.pixkey.removal

import br.com.zup.*
import br.com.zup.integration.bcb.BcbPixKeyClient
import br.com.zup.integration.bcb.dtos.DeletePixKeyRequest
import br.com.zup.integration.bcb.dtos.DeletePixKeyResponse
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class PixKeyRemovalEndpointTest (
    private val repository: PixKeyRepository,
    private val grpcClient: PixKeyRemovalGrpcServiceGrpc.PixKeyRemovalGrpcServiceBlockingStub) {

    @Inject
    lateinit var bcbClient: BcbPixKeyClient

    @MockBean(BcbPixKeyClient::class)
    fun bcbClient(): BcbPixKeyClient = Mockito.mock(BcbPixKeyClient::class.java)

    companion object {
        const val VALID_CPF = "09030442930"
        @JvmStatic
        fun invalidDataArgument (): Stream<Arguments> {
            return Stream.of(
                Arguments.of(mapOf(Pair("PIX_ID", "1234"), Pair("CUSTOMER_ID", UUID.randomUUID().toString()))),
                Arguments.of(mapOf(Pair("PIX_ID", UUID.randomUUID().toString()), Pair("CUSTOMER_ID", "1234"))),
                Arguments.of(mapOf(Pair("PIX_ID", ""), Pair("CUSTOMER_ID", UUID.randomUUID().toString()))),
                Arguments.of(mapOf(Pair("PIX_ID", UUID.randomUUID().toString()), Pair("CUSTOMER_ID", "")))
            )
        }
    }

    @Test
    fun `Must remove a pix Key` () {
        Mockito
            .`when`(bcbClient.deleteBcbPixKey(VALID_CPF, DeletePixKeyRequest(key = VALID_CPF)))
            .thenReturn(HttpResponse.ok(
                DeletePixKeyResponse(
                    key = VALID_CPF,
                    participant = "",
                    deletedAt = LocalDateTime.now()
            )))

        val pixKey = insertPixKey()

        val response = this.removePixKeyGrpc(pixId = pixKey.id.toString(), customerId = pixKey.customerId.toString())

        val optionalPixKey = repository.findById(pixKey.id!!)

        assertFalse(optionalPixKey.isPresent)
        assertEquals("Pix key removed successfully!", response.message)
    }

    @ParameterizedTest
    @MethodSource("invalidDataArgument")
    fun `Must validate customerId e pixKeyId format` (arguments: Map<String, String>) {
        val error = assertThrows(StatusRuntimeException::class.java) {
            this.removePixKeyGrpc(pixId = arguments["PIX_ID"]!!, customerId = arguments["CUSTOMER_ID"]!!)
        }
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Request with invalid data", error.status.description)
    }

    @Test
    fun `Must validate if the pixId exists in database` () {
        repository.deleteAll()

        val error = assertThrows(StatusRuntimeException::class.java) {
            this.removePixKeyGrpc(pixId = UUID.randomUUID().toString(), customerId = UUID.randomUUID().toString())
        }
        assertEquals(Status.NOT_FOUND.code, error.status.code)
        assertEquals("Pix Key not found or it does not belong to this customer!", error.status.description)
    }

    @Test
    fun `Must throw not found exception if pix key not removed of BCB` () {
        Mockito
            .`when`(bcbClient.deleteBcbPixKey(VALID_CPF, DeletePixKeyRequest(key = VALID_CPF)))
            .thenReturn(HttpResponse.notFound())

        val pixKey = insertPixKey()

        val error = assertThrows(StatusRuntimeException::class.java) {
            this.removePixKeyGrpc(pixId = pixKey.id.toString(), customerId = pixKey.customerId.toString())
        }

        assertEquals(Status.FAILED_PRECONDITION.code, error.status.code)
        assertEquals("Something went wrong to delete pix key in BCB", error.status.description)
    }

    private fun removePixKeyGrpc(pixId: String, customerId: String) : PixKeyRemovalResponse {
        return grpcClient.removeKey(
                    PixKeyRemovalRequest
                        .newBuilder()
                        .setPixId(pixId)
                        .setCustomerId(customerId)
                        .build())
    }

    private fun insertPixKey() : PixKey {
        val customerId = UUID.randomUUID()
        return repository.save(PixKey(
            customerId = customerId,
            keyType = KeyType.CPF,
            key = VALID_CPF,
            accountType = AccountType.CURRENT_ACCOUNT,
            account = AssociatedAccount(
                institution = "",
                ownerName = "",
                ownerCpf = "",
                branch = "",
                number = ""
            )
        ))
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyRemovalGrpcServiceGrpc.PixKeyRemovalGrpcServiceBlockingStub {
            return PixKeyRemovalGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}