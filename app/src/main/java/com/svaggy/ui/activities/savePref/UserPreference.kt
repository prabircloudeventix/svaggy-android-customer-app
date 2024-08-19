package com.svaggy.ui.activities.savePref

import com.google.gson.annotations.SerializedName

data class UserPreference(
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean?        = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : ArrayList<Data> = arrayListOf()
)
{
    data class Data (
        var isSelected : Boolean = false,
        @SerializedName("id"                    ) var id                  : Int?    = null,
        @SerializedName("preference_name"       ) var preferenceName      : String? = null,
        @SerializedName("createdAt"             ) var createdAt           : String? = null,
        @SerializedName("updatedAt"             ) var updatedAt           : String? = null,
        @SerializedName("food_preference_image" ) var foodPreferenceImage : String? = null
    )
}