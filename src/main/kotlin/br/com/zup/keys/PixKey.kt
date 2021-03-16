package br.com.zup.keys

import br.com.zup.AccountType
import br.com.zup.KeyType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class PixKey(
    val customerId: String,
    val keyType: KeyType,
    val key: String,
    val accountType: AccountType
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    override fun toString(): String {
        return "Id: ${this.id}"
    }
}
