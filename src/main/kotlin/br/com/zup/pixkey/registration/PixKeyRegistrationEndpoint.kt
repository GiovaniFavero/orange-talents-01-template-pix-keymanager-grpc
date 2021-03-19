package br.com.zup.pixkey.registration

import br.com.zup.PixKeyRegistrationGrpcServiceGrpc
import br.com.zup.PixKeyRequest
import br.com.zup.PixKeyResponse
import br.com.zup.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@ErrorHandler
@Singleton
class PixKeyRegistrationEndpoint(
    private val service: PixKeyRegistrationService
) : PixKeyRegistrationGrpcServiceGrpc.PixKeyRegistrationGrpcServiceImplBase() {

    val logger = LoggerFactory.getLogger(PixKeyRegistrationEndpoint::class.java)

    override fun registerKey(
        request: PixKeyRequest,
        responseObserver: StreamObserver<PixKeyResponse>
    ) {

        val newKeyRequest = request.toModel()
        val key = service.register(newKeyRequest)

        responseObserver.onNext(PixKeyResponse.newBuilder()
                                .setPixId(key.id.toString())
                                .build())

        responseObserver.onCompleted()

        /*
        val errors = processRequestValidations(request, customerRequestClient, pixKeyRepository);
        errors.forEach {
            responseObserver?.onError(it)
        }

      //  val bcbPixKeyRequest: CreatePixKeyRequest =

        /* Salvar no banco */
        val pixKey: PixKey? = request?.toModel()

        pixKeyRepository.save(pixKey!!)
        logger.info("Pix Key: ${pixKey.toString()}")

        PixKeyResponse.newBuilder()
            .setPixId(pixKey.id.toString()!!)
            .build()
            .let {
                responseObserver!!.onNext(it)
            }
        responseObserver?.onCompleted()*/

    }
}