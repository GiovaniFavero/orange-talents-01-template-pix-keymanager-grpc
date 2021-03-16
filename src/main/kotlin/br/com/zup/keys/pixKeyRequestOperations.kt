package br.com.zup.keys

import br.com.zup.KeyType
import br.com.zup.PixKeyRequest
import io.grpc.Status
import io.grpc.StatusRuntimeException
import java.util.*
import java.util.regex.Pattern

fun PixKeyRequest.toModel(): PixKey {
    return PixKey(this.customerId,
        this.keyType,
        if (this.keyType == KeyType.RANDOM_KEY)
            UUID.randomUUID().toString()
        else
            this.key,
        this.accountType)
}

fun processRequestValidations(
    request: PixKeyRequest?,
    customerRequestClient: CustomerRequestClient,
    pixKeyRepository: PixKeyRepository
): MutableList<StatusRuntimeException> {
    val errors = mutableListOf<StatusRuntimeException>()
    if (request!!.customerId.isBlank()) {
        errors.add(Status.NOT_FOUND
            .withDescription("CustomerId must not be blank!")
            .asRuntimeException())
    }

    when (request?.keyType) {
        KeyType.CPF -> {
            if (!request?.key.matches("^[0-9]{11}\$".toRegex())) {
                errors.add(Status.INVALID_ARGUMENT
                    .withDescription("CPF is invalid!")
                    .asRuntimeException())
            }
        }
        KeyType.PHONE_NUMBER -> {
            if (!request?.key.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())) {
                errors.add(Status.INVALID_ARGUMENT
                    .withDescription("Phone number is invalid!")
                    .asRuntimeException())
            }
        }
        KeyType.EMAIL -> {
            if (!isEmailValid(request?.key)) {
                errors.add(Status.INVALID_ARGUMENT
                    .withDescription("Email is invalid!")
                    .asRuntimeException())
            }
        }
        KeyType.RANDOM_KEY -> {
            if (request?.key.isNotBlank()) {
                errors.add(Status.INVALID_ARGUMENT
                    .withDescription("For random Key, it must be blank!")
                    .asRuntimeException())
            }
        }
        else -> {
            errors.add(Status.INVALID_ARGUMENT
                .withDescription("KeyType is unknown!")
                .asRuntimeException())
        }

    }
    if (errors.isEmpty()) {
        errors.addAll(checkCustomerId(request?.customerId, customerRequestClient))
    }
    if (errors.isEmpty()) {
        errors.addAll(checkDuplicatedKey(request, pixKeyRepository))
    }

    return errors
}

fun checkCustomerId(
    customerId: String,
    customerRequestClient: CustomerRequestClient
) : MutableList<StatusRuntimeException> {
    val errors = mutableListOf<StatusRuntimeException>()
    val response = customerRequestClient.searchCustomer(customerId).body()
    if(response == null) {
        errors.add(Status.NOT_FOUND
            .withDescription("CustomerId not found!")
            .asRuntimeException())
    }
    return errors
}

fun checkDuplicatedKey(
    request: PixKeyRequest,
    pixKeyRepository: PixKeyRepository
): MutableList<StatusRuntimeException>{
    val errors = mutableListOf<StatusRuntimeException>()
    val exists = pixKeyRepository.countByCustomerIdAndKeyType(request?.customerId, request?.keyType)
    if(exists > 0) {
        errors.add(Status.ALREADY_EXISTS
            .withDescription("KeyType already exists to customer")
            .asRuntimeException())
    }
    return errors
}

private fun isEmailValid(email: String): Boolean {
    return Pattern.compile(
        "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
    ).matcher(email).matches()
}