package br.com.zup.pixkey.consultation

import br.com.zup.PixKeyListConsultationServiceGrpc
import br.com.zup.PixKeyListRequest
import br.com.zup.PixKeyListResponse
import br.com.zup.pixkey.PixKeyRepository
import br.com.zup.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class PixKeyConsultationListEndpoint(@Inject private val repository: PixKeyRepository) :
    PixKeyListConsultationServiceGrpc.PixKeyListConsultationServiceImplBase() {

    override fun getPixKeyList(request: PixKeyListRequest, responseObserver: StreamObserver<PixKeyListResponse>) {

        if (request.customerId.isNullOrBlank()) {
            throw IllegalStateException("Customer Id cannot be blank or null!")
        }

        val customerId = UUID.fromString(request.customerId)
        val keys = repository.findAllByCustomerId(customerId).map {
            PixKeyListResponse.PixKey.newBuilder()
                .setPixId(it.id.toString())
                .setType(it.keyType)
                .setKey(it.key)
                .setAccountType(it.accountType)
                .build()
        }

        responseObserver.onNext(PixKeyListResponse.newBuilder()
            .setCustomerId(customerId.toString())
            .addAllKeys(keys)
            .build()
        )
        responseObserver.onCompleted()
    }
}