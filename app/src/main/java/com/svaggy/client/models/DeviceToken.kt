package com.svaggy.client.models

import com.google.gson.annotations.SerializedName

data class DeviceToken(
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean? = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data?    = Data()
)
{
    data class Data (
        @SerializedName("device_token" ) var deviceToken : String? = null
    )
}