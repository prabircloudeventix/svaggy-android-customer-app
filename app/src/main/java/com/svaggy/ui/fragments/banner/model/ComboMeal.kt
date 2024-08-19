package com.svaggy.ui.fragments.banner.model

import com.google.gson.annotations.SerializedName

data class ComboMeal(
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean?        = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("total_pages"     ) var totalPages    : Int?     = null,
    @SerializedName("total_item_count" ) var total_item_count : Int?         = null,
    @SerializedName("current_page"     ) var current_page    : Int?         = null,
    @SerializedName("page_size"     ) var page_size    : Int?         = null,
    @SerializedName("data"        ) var data       : ArrayList<Data> = arrayListOf()
)
{
    data class Data (
        @SerializedName("id"              ) var id             : Int?     = null,
        @SerializedName("restaurant_name" ) var restaurantName : String?  = null,
        @SerializedName("is_active"       ) var isActive       : Boolean? = null,
        @SerializedName("combos"          ) var combos         : Int?     = null,
        @SerializedName("distance"        ) var distance       : String?  = null,
        @SerializedName("ratings"         ) var ratings        : Double?  = null
    )
}