package com.svaggy.ui.fragments.payment.model

import com.google.gson.annotations.SerializedName

data class StripeCustomer(
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean? = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data?    = Data()
)
{
    data class Data (
        @SerializedName("paymentIntent"  ) var paymentIntent  : String? = null,
        @SerializedName("intent_id"      ) var intentId       : String? = null,
        @SerializedName("ephemeralKey"   ) var ephemeralKey   : String? = null,
        @SerializedName("customer"       ) var customer       : String? = null,
        @SerializedName("publishableKey" ) var publishableKey : String? = null
    )
}