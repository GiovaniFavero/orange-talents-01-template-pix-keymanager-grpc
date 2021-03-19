package br.com.zup.pixkey.consultation

import br.com.zup.PixKeyConsultationResponse
import br.com.zup.integration.bcb.PixKeyDetails

class PixKeyDetailsConverter {

    fun convert(keyDetails: PixKeyDetails) : PixKeyConsultationResponse {
        return PixKeyConsultationResponse.newBuilder()
            .setPixId(keyDetails.pixId?.toString() ?: "")
            .setCustomerId(keyDetails.customerId?.toString() ?: "")
            .setKey(PixKeyConsultationResponse.PixKeyDetails
                .newBuilder()
                .setKeyType(keyDetails.type)
                .setKey(keyDetails.key)
                .setAccount(PixKeyConsultationResponse.PixKeyDetails.Account.newBuilder()
                    .setAccountType(keyDetails.accountType)
                    .setInstitution(keyDetails.associatedAccount.institution)
                    .setOwnerName(keyDetails.associatedAccount.ownerName)
                    .setOwnerCpf(keyDetails.associatedAccount.ownerCpf)
                    .setBranch(keyDetails.associatedAccount.branch)
                    .setAccountNumber(keyDetails.associatedAccount.number)
                    .build()
                )
                .build()
            )
            .build()
    }
}