package com.svaggy.ui.fragments.profile.viewmodel

import com.google.gson.annotations.SerializedName

data class EditProfile(
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean? = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data?    = Data()
)
{
    data class Data (
        @SerializedName("id"           ) var id          : Int?    = null,
        @SerializedName("first_name"   ) var firstName   : String? = null,
        @SerializedName("last_name"    ) var lastName    : String? = null,
        @SerializedName("email"        ) var email       : String? = null,
        @SerializedName("phone_number" ) var phoneNumber : String? = null
    )
}