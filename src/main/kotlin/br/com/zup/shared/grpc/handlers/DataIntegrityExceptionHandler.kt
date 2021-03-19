package br.com.zup.shared.grpc.handlers

import br.com.zup.shared.grpc.ExceptionHandler
import io.grpc.Status
import io.micronaut.context.MessageSource
import org.hibernate.exception.ConstraintViolationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataIntegrityExceptionHandler(@Inject var messageSource: MessageSource): ExceptionHandler<ConstraintViolationException> {

    override fun handle(e: ConstraintViolationException): ExceptionHandler.StatusWithDetails {
        val constraintName = e.constraintName
        if(constraintName.isNullOrBlank()) {
            return internalServerError(e)
        }

        val message = messageSource.getMessage("data.integrity.error.$constraintName", MessageSource.MessageContext.DEFAULT)
        return message
            .map { alreadyExistsError(it, e) }
            .orElse(internalServerError(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ConstraintViolationException
    }

    private fun alreadyExistsError(message: String?, e: ConstraintViolationException) =
        ExceptionHandler.StatusWithDetails(Status.ALREADY_EXISTS
                                            .withDescription(message)
                                            .withCause(e))

    private fun internalServerError(e: ConstraintViolationException) =
        ExceptionHandler.StatusWithDetails(Status.INTERNAL
                                            .withDescription("Unexpected internal server error")
                                            .withCause(e))

}