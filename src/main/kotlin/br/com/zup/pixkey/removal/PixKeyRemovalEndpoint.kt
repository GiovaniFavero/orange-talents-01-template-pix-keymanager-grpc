package br.com.zup.pixkey.removal

import br.com.zup.PixKeyRemovalGrpcServiceGrpc
import br.com.zup.PixKeyRemovalRequest
import br.com.zup.PixKeyRemovalResponse
import br.com.zup.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class PixKeyRemovalEndpoint(
    private val service: PixKeyRemovalService
) : PixKeyRemovalGrpcServiceGrpc.PixKeyRemovalGrpcServiceImplBase() {

    override fun removeKey(
        request: PixKeyRemovalRequest,
        responseObserver: StreamObserver<PixKeyRemovalResponse>?
    ) {

        service.remove(pixId = request.pixId, customerId = request.customerId)

        PixKeyRemovalResponse.newBuilder()
            .setMessage("Pix key removed successfully!")
            .build()
            .let {
                responseObserver?.onNext(it)
            }

        responseObserver?.onCompleted()
    }
}