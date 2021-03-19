package br.com.zup.shared.grpc.handlers

import br.com.zup.shared.grpc.ExceptionHandler
import com.google.protobuf.Any
import com.google.rpc.BadRequest
import com.google.rpc.Code
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class ConstraintViolationExceptionHandler: ExceptionHandler<ConstraintViolationException> {

    override fun handle(e: ConstraintViolationException): ExceptionHandler.StatusWithDetails {

        var details = BadRequest.newBuilder()
            .addAllFieldViolations(e.constraintViolations.map {
                                        BadRequest.FieldViolation.newBuilder()
                                            .setField(it.propertyPath.last().name ?: "?? key ??")
                                            .setDescription(it.message)
                                            .build()
                                    })
            .build()

        val statusProto = com.google.rpc.Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT_VALUE)
            .setMessage("Request with invalid data")
            .addDetails(Any.pack(details))
            .build()

        return ExceptionHandler.StatusWithDetails(statusProto)
    }

    override fun supports(e: Exception): Boolean {
        return e is ConstraintViolationException
    }
}