package br.com.zup.shared.grpc.handlers

import br.com.zup.shared.exceptions.ExistingPixKeyException
import br.com.zup.shared.grpc.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ExistingPixKeyExceptionHandler: ExceptionHandler<ExistingPixKeyException> {

    override fun handle(e: ExistingPixKeyException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(Status.ALREADY_EXISTS
                                                    .withDescription(e.message)
                                                    .withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ExistingPixKeyException
    }


}