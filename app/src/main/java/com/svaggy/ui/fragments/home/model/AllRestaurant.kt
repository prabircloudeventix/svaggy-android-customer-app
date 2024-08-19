package com.svaggy.ui.fragments.home.model

import com.google.gson.annotations.SerializedName

data class AllRestaurant(
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean?        = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("total_pages"     ) var totalPages    : Int?     = null,
    @SerializedName("total_item_count" ) var total_item_count : Int?         = null,
    @SerializedName("current_page"     ) var current_page    : Int?         = null,
    @SerializedName("page_size"     ) var page_size    : Int?         = null,
    @SerializedName("is_show_promo_banner"     ) var is_show_promo_banner    : Boolean,
    @SerializedName("data"        ) var data       : ArrayList<Data> = arrayListOf(),

)
{
    data class Data (
        @SerializedName("id"                  ) var id                 : Int?                          = null,
        @SerializedName("restaurant_name"     ) var restaurantName     : String?                       = null,
        @SerializedName("delivery_type"       ) val deliveryType       : String?                       = null,
        @SerializedName("is_featured"         ) var isFeatured         : Boolean?                       = null,
        @SerializedName("is_rush_mode"        ) var is_rush_mode      : Boolean?                       = null,
        @SerializedName("booster_ids"         ) val boosterId          : ArrayList<Int?>? = arrayListOf(),
        @SerializedName("is_active"           ) var isActive           : Boolean?                      = null,
        @SerializedName("restaurant_cuisines" ) var restaurantCuisines : ArrayList<RestaurantCuisines> = arrayListOf(),
        @SerializedName("restaurant_image"    ) var restaurantImage    : String?                       = null,
        @SerializedName("distance"            ) var distance           : String?                       = null,
        @SerializedName("ratings"             ) var ratings            : Double?                       = null,
        @SerializedName("delivery_time"       ) var deliveryTime       : String?                       = null,
        @SerializedName("discount"           ) var discount       : String?                       = null,
    )
    {
        data class RestaurantCuisines (
            @SerializedName("id"      ) var id      : Int?    = null,
            @SerializedName("cuisine" ) var cuisine : String? = null
        )
    }
}