package br.com.zup.pixkey

import br.com.zup.AccountType
import br.com.zup.KeyType
import br.com.zup.pixkey.registration.AssociatedAccount
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(uniqueConstraints = [UniqueConstraint(
    name = "uk_pix_key",
    columnNames = ["key"]
)])
data class PixKey(
    @field:NotNull
    @Column(nullable = false)
    val customerId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val keyType: KeyType,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    var key: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val accountType: AccountType,

    @field:Valid
    @Embedded
    val account: AssociatedAccount
) {

    @Id
    @GeneratedValue
    val id: UUID? = null

    fun updateKey (key: String) {
        this.key = key
    }

}
