package com.svaggy.ui.fragments.profile.model

import com.google.gson.annotations.SerializedName

data class Settings(
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean? = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data?    = Data()
)
{
    data class Data (
        @SerializedName("ORDER_DELIVERY" ) var ORDERDELIVERY : String? = null,
        @SerializedName("PROMOTIONAL"    ) var PROMOTIONAL   : String? = null,
        @SerializedName("LANGUAGE"       ) var LANGUAGE      : String? = null
    )
}