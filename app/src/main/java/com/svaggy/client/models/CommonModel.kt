package com.svaggy.client.models

import com.google.gson.annotations.SerializedName

data class CommonModel(
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean? = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : TemData?    = null
)

data class TemData(
    @SerializedName("order_id") var orderId       : Int?    = null

)


