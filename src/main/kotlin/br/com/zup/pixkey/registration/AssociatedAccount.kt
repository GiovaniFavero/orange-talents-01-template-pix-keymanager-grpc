package br.com.zup.pixkey.registration

import javax.persistence.Embeddable

@Embeddable
class AssociatedAccount(
    val institution: String,
    val ownerName: String,
    val ownerCpf: String,
    val branch: String,
    val number: String
) {
    companion object {
        const val ITAU_UNIBANCO_ISPB = "60701190"
    }
}