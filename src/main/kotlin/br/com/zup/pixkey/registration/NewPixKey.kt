package br.com.zup.pixkey.registration

import br.com.zup.AccountType
import br.com.zup.KeyType
import br.com.zup.pixkey.PixKey
import br.com.zup.shared.validators.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.Get
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NewPixKey(
    @ValidUUID
    @field:NotBlank
    val customerId: String?,
    @field:NotNull
    val keyType: PixKeyType?,
    @field:Size(max = 77)
    val key: String?,
    @field:NotNull
    val accountType: AccountType?
) {

    fun toModel(account: AssociatedAccount): PixKey {
        return PixKey(
            customerId = UUID.fromString(this.customerId),
            keyType = KeyType.valueOf(this.keyType!!.name),
            key = if (this.keyType == PixKeyType.RANDOM) UUID.randomUUID().toString() else this.key!!,
            accountType = AccountType.valueOf(this.accountType!!.name),
            account = account
        )
    }

    fun getConvertedAccountType(): String {
        return when (accountType) {
            AccountType.CURRENT_ACCOUNT -> "CONTA_CORRENTE"
            AccountType.SAVINGS_ACCOUNT -> "CONTA_POUPANCA"
            else -> ""
        }
    }
}