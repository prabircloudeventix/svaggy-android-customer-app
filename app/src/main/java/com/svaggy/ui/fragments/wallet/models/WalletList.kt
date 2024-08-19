package com.svaggy.ui.fragments.wallet.models

import com.google.gson.annotations.SerializedName

data class WalletList(
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean? = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data?    = Data()
)
{
    data class Data (
        @SerializedName("wallet_amount" ) var walletAmount : Double?                    = null,
        @SerializedName("transactions"  ) var transactions : ArrayList<Transactions> = arrayListOf()
    )
    {
        data class Transactions (
            @SerializedName("id"               ) var id              : Int?    = null,
            @SerializedName("transaction_date" ) var transactionDate : String? = null,
            @SerializedName("amount"           ) var amount          : Double? = null,
            @SerializedName("type"             ) var type            : String? = null,
            @SerializedName("order_id"         ) var orderId         : String? = null
        )
    }
}