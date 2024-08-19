package com.svaggy.ui.fragments.home.model

import com.google.gson.annotations.SerializedName

data class Search(
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean?        = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : ArrayList<Data> = arrayListOf()
)
{
    data class Data (
        @SerializedName("id"            ) var id           : Int?    = null,
        @SerializedName("restaurant_id" ) var restaurantId : Int?    = null,
        @SerializedName("type"          ) var type         : String? = null,
        @SerializedName("name"          ) var name         : String? = null
    )
}