package com.svaggy.ui.fragments.wallet.models

import com.google.gson.annotations.SerializedName

class WalletModel(
    @SerializedName("status_code") var statusCode: Int? = null,
    @SerializedName("is_success") var isSuccess: Boolean? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: Data? = Data()
) {
    data class Data(

        @SerializedName("added_amount") var addedAmount: Double? = null,
        @SerializedName("updated_balance") var updatedBalance: Double? = null

    )
}