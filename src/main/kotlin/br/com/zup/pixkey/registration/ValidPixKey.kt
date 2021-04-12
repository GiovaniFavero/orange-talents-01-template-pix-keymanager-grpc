package br.com.zup.pixkey.registration

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidPixKeyValidator::class])
annotation class ValidPixKey(
    val message: String = "Invalid pix key (\${validatedValue.type})",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
class ValidPixKeyValidator: ConstraintValidator<ValidPixKey, NewPixKey> {

    override fun isValid(
        value: NewPixKey?,
        annotationMetadata: AnnotationValue<ValidPixKey>?,
        context: io.micronaut.validation.validator.constraints.ConstraintValidatorContext?
    ): Boolean {

        if(value?.keyType == null) {
            return false
        }

        return value.keyType.validate(value.key)
    }
}



