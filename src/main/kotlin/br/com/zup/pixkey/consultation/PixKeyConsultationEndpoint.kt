package br.com.zup.pixkey.consultation

import br.com.zup.PixKeyConsultationRequest
import br.com.zup.PixKeyConsultationResponse
import br.com.zup.PixKeyConsultationServiceGrpc
import br.com.zup.integration.bcb.BcbPixKeyClient
import br.com.zup.pixkey.PixKeyRepository
import br.com.zup.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class PixKeyConsultationEndpoint(
    @Inject private val repository: PixKeyRepository,
    private val bcbPixKeyClient: BcbPixKeyClient,
    private val validator: Validator
) : PixKeyConsultationServiceGrpc.PixKeyConsultationServiceImplBase() {

    override fun getPixKeyDetail(
        request: PixKeyConsultationRequest,
        responseObserver: StreamObserver<PixKeyConsultationResponse>
    ) {

        val filter = request.toModel(validator)
        val keyInfo = filter.filterKey(repository = repository, bcbPixKeyClient = bcbPixKeyClient)

        responseObserver.onNext(PixKeyDetailsConverter().convert(keyInfo))
        responseObserver.onCompleted()
    }

}