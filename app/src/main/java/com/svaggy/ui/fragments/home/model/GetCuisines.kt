package com.svaggy.ui.fragments.home.model

import com.google.gson.annotations.SerializedName

data class GetCuisines(
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean?        = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : ArrayList<Data> = arrayListOf()
)
{
    data class Data (
        @SerializedName("id"            ) var id           : Int?    = null,
        @SerializedName("cuisine"       ) var cuisine      : String? = null,
        @SerializedName("createdAt"     ) var createdAt    : String? = null,
        @SerializedName("updatedAt"     ) var updatedAt    : String? = null,
        @SerializedName("cuisine_image" ) var cuisineImage : String? = null
    )
}