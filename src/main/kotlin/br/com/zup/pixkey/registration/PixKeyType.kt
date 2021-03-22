package br.com.zup.pixkey.registration

import br.com.zup.KeyType
import io.micronaut.validation.validator.constraints.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class PixKeyType {

    CPF {
        override fun validate(key: String?): Boolean {
            if (key.isNullOrBlank()) {
                return false
            }
            if (!key.matches("^[0-9]{11}\$".toRegex())) {
                return false
            }
            return CPFValidator().run {
                initialize(null)
                isValid(key, null)
            }
        }

    },
    PHONE {
        override fun validate(key: String?): Boolean {
            if(key.isNullOrBlank()) {
                return false
            }
            return key.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }

    },
    EMAIL {
        override fun validate(key: String?): Boolean {
            if(key.isNullOrBlank()) {
                return false
            }
            return EmailValidator().run {
                initialize(null)
                isValid(key, null)
            }
        }

    },
    RANDOM {
        override fun validate(key: String?): Boolean {
            return key.isNullOrBlank()
        }
    };

    abstract fun validate(key: String?): Boolean
}
