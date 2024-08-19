package com.svaggy.ui.fragments.editAuth.model

import com.google.gson.annotations.SerializedName

data class SendOtp(
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean? = null,
    @SerializedName("message"     ) var message    : String?  = null,
//    @SerializedName("data"        ) var data       : Data?    = Data()
)