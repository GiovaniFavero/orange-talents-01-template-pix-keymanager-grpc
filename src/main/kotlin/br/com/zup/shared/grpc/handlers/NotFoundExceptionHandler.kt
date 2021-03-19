package br.com.zup.shared.grpc.handlers

import br.com.zup.shared.exceptions.NotFoundException
import br.com.zup.shared.grpc.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class NotFoundExceptionHandler: ExceptionHandler<NotFoundException> {

    override fun handle(e: NotFoundException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(Status.NOT_FOUND
                                                    .withDescription(e.message)
                                                    .withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is NotFoundException
    }
}