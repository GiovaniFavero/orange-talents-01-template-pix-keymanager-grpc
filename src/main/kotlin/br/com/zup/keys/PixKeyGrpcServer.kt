package br.com.zup.keys

import br.com.zup.PixKeyManagerGrpcServiceGrpc
import br.com.zup.PixKeyRequest
import br.com.zup.PixKeyResponse
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class PixKeyGrpcServer(
    private val customerRequestClient: CustomerRequestClient,
    private val pixKeyRepository: PixKeyRepository
) : PixKeyManagerGrpcServiceGrpc.PixKeyManagerGrpcServiceImplBase() {

    val logger = LoggerFactory.getLogger(PixKeyGrpcServer::class.java)

    override fun registerKey(request: PixKeyRequest?, responseObserver: StreamObserver<PixKeyResponse>?) {

        val errors = processRequestValidations(request, customerRequestClient, pixKeyRepository);
        errors.forEach {
            responseObserver?.onError(it)
        }
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
        responseObserver?.onCompleted()

    }
}