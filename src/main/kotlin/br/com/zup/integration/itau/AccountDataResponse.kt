package br.com.zup.pixkey.registration

data class AccountDataResponse(
    val tipo: String,
    val instituicao: InstitutionResponse,
    val agencia: String,
    val numero: String,
    val titular: OwnerResponse
) {

    fun toModel(): AssociatedAccount {
        return AssociatedAccount(
            institution = this.instituicao.nome,
            ownerName = this.titular.nome,
            ownerCpf = this.titular.cpf,
            branch = this.agencia,
            number = numero
        )
    }
}

data class InstitutionResponse(
    val nome: String,
    val ispb: String
)

data class OwnerResponse(
    val id: String,
    val nome: String,
    val cpf: String
)