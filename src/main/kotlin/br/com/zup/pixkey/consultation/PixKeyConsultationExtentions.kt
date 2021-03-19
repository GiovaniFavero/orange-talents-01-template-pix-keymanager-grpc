package br.com.zup.pixkey.consultation

import br.com.zup.PixKeyConsultationRequest
import br.com.zup.PixKeyConsultationRequest.FilterCase.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun PixKeyConsultationRequest.toModel(validator: Validator): Filter {

    val filter = when (filterCase) {
        PIXID -> pixId.let {
            Filter.ByPixId(pixId = it.pixId, customerId = it.customerId)
        }
        KEY -> Filter.ByKey(key)
        FILTER_NOT_SET -> Filter.Invalid()
    }

    val violations = validator.validate(filter)
    if(violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }
    return filter
}