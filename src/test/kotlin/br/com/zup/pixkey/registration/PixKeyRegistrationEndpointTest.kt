package br.com.zup.pixkey.registration

import br.com.zup.*
import br.com.zup.integration.bcb.BcbPixKeyClient
import br.com.zup.integration.bcb.dtos.BankAccount
import br.com.zup.integration.bcb.dtos.CreatePixKeyRequest
import br.com.zup.integration.bcb.dtos.CreatePixKeyResponse
import br.com.zup.integration.bcb.dtos.Owner
import br.com.zup.integration.itau.CustomerRequestClient
import br.com.zup.pixkey.PixKey
import br.com.zup.pixkey.PixKeyRepository
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
import org.mockito.*
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class PixKeyRegistrationEndpointTest(
    @Inject val repository: PixKeyRepository,
    @Inject val grpcClient: PixKeyRegistrationGrpcServiceGrpc.PixKeyRegistrationGrpcServiceBlockingStub
) {

    private val customerId = UUID.randomUUID().toString()
    private val accountType = "CONTA_CORRENTE"
    private val cpfValidKey = "71263991033"

    @Inject
    lateinit var itauClient: CustomerRequestClient

    @Inject
    lateinit var bcbClient: BcbPixKeyClient

    @MockBean(CustomerRequestClient::class)
    fun itauClient(): CustomerRequestClient = Mockito.mock(CustomerRequestClient::class.java)

    @MockBean(BcbPixKeyClient::class)
    fun bcbClient(): BcbPixKeyClient = Mockito.mock(BcbPixKeyClient::class.java)

    @BeforeEach
    fun before() {
        repository.deleteAll();
    }

    @Test
    fun `Must register a Pix Key`() {
        this.mockItauExistingCustomerConsultation()
        this.mockBcbPixKeyRegistrationSuccess()

        val response = this.registerPixKeyGrpc()

        val key = repository.findById(UUID.fromString(response.pixId))

        assertTrue(key.isPresent)
        key.get().let {
            assertAll("Should insert a PixKey in database",
                { assertEquals(it.key, cpfValidKey) },
                { assertEquals(it.keyType, KeyType.CPF) },
                { assertEquals(it.customerId, UUID.fromString(customerId)) }
            )
        }
    }

    @Test
    fun `Must not register a duplicated Pix Key`() {
        this.mockItauExistingCustomerConsultation()
        this.mockBcbPixKeyRegistrationSuccess()

        val pixKey = PixKey(
            customerId = UUID.fromString(customerId),
            keyType = KeyType.CPF,
            key = cpfValidKey,
            accountType = AccountType.CURRENT_ACCOUNT,
            account = AssociatedAccount(
                institution = "",
                ownerName = "",
                ownerCpf = "",
                branch = "",
                number = ""
            )
        )
        repository.save(pixKey)

        val error: StatusRuntimeException = assertThrows(StatusRuntimeException::class.java) {
            this.registerPixKeyGrpc()
        }
        with(error) {
            assertEquals(status.code, Status.ALREADY_EXISTS.code)
            assertEquals("Pix key ${pixKey.key} is already registered", status.description)
        }
    }

    @Test
    fun `Must not register Pix Key to nonexistent customer`() {
        this.mockItauNonExistingCustomerConsultation()
        this.mockBcbPixKeyRegistrationSuccess()

        val error: StatusRuntimeException = assertThrows(StatusRuntimeException::class.java) {
            this.registerPixKeyGrpc()
        }
        with(error){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Customer not found!", status.description)
        }
    }

    @Test
    fun `Must not register Pix Key if not created at BCB`() {
        this.mockItauExistingCustomerConsultation()
        this.mockBcbPixKeyRegistrationFail()

        val error: StatusRuntimeException = assertThrows(StatusRuntimeException::class.java) {
            this.registerPixKeyGrpc()
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Something went wrong to register pix key in BCB", status.description)
        }

    }

    private fun registerPixKeyGrpc() : PixKeyResponse {
        return grpcClient.registerKey(PixKeyRequest.newBuilder()
            .setCustomerId(this.customerId)
            .setKeyType(KeyType.CPF)
            .setKey(cpfValidKey)
            .setAccountType(AccountType.CURRENT_ACCOUNT)
            .build())
    }

    private fun mockItauExistingCustomerConsultation() {
        Mockito
            .`when`(itauClient.getAccountByType(this.customerId, this.accountType))
            .thenReturn(HttpResponse
                .created(AccountDataResponse(
                    instituicao = InstitutionResponse(
                        nome = "nome",
                        ispb = "ispb"
                    ),
                    tipo = "tipo",
                    agencia = "agencia",
                    numero = "numero",
                    titular = OwnerResponse(
                        id = "id",
                        nome = "nome",
                        cpf = "cpf"
                    )
                )))
    }

    private fun mockItauNonExistingCustomerConsultation() {
        Mockito
            .`when`(itauClient.getAccountByType(this.customerId, this.accountType))
            .thenReturn(HttpResponse
                .notFound())
    }

    private fun mockBcbPixKeyRegistrationSuccess() {
        Mockito
            .`when`(bcbClient.registerBcbPixKey(
                CreatePixKeyRequest(
                    keyType = KeyType.CPF,
                    key = cpfValidKey,
                    bankAccount = BankAccount(
                        participant = AssociatedAccount.ITAU_UNIBANCO_ISPB,
                        branch = "branch",
                        accountNumber = "accountNumber",
                        accountType = BankAccount.AccountType.CACC
                    ),
                    owner = Owner(
                        type = Owner.OwnerType.NATURAL_PERSON,
                        name = "name",
                        taxIdNumber = "taxIdNumber"
                    )
                )))
            .thenReturn(
                HttpResponse.created(
                    CreatePixKeyResponse(
                        keyType = KeyType.CPF,
                        key = cpfValidKey,
                        bankAccount = BankAccount(
                            participant = AssociatedAccount.ITAU_UNIBANCO_ISPB,
                            branch = "branch",
                            accountNumber = "accountNumber",
                            accountType = BankAccount.AccountType.CACC
                        ),
                        owner = Owner(
                            type = Owner.OwnerType.NATURAL_PERSON,
                            name = "name",
                            taxIdNumber = "taxIdNumber"
                        ),
                        createdAt = LocalDateTime.now()
                    )
                )
            )
    }

    private fun mockBcbPixKeyRegistrationFail() {
        Mockito
            .`when`(bcbClient.registerBcbPixKey(
                CreatePixKeyRequest(
                    keyType = KeyType.CPF,
                    key = cpfValidKey,
                    bankAccount = BankAccount(
                        participant = AssociatedAccount.ITAU_UNIBANCO_ISPB,
                        branch = "branch",
                        accountNumber = "accountNumber",
                        accountType = BankAccount.AccountType.CACC
                    ),
                    owner = Owner(
                        type = Owner.OwnerType.NATURAL_PERSON,
                        name = "name",
                        taxIdNumber = "taxIdNumber"
                    )
                )))
            .thenReturn(
                HttpResponse.unprocessableEntity()
            )
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyRegistrationGrpcServiceGrpc.PixKeyRegistrationGrpcServiceBlockingStub {
            return PixKeyRegistrationGrpcServiceGrpc.newBlockingStub(channel)
        }
    }



}