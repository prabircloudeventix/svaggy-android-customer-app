package com.svaggy.ui.fragments.home.model

import com.google.gson.annotations.SerializedName

data class RestaurantSpecialOffers(
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean?        = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : ArrayList<Data> = arrayListOf()
){
    data class Data(
        @SerializedName("id"                  ) var id                 : Int?                          = null,
        @SerializedName("restaurant_name"     ) var restaurantName     : String?                       = null,
        @SerializedName("is_active"           ) var isActive           : Boolean?                      = null,
        @SerializedName("restaurant_cuisines" ) var restaurantCuisines : ArrayList<RestaurantCuisines> = arrayListOf(),
        @SerializedName("restaurant_image"    ) var restaurantImage    : String?                       = null,
        @SerializedName("distance"            ) var distance           : String?                       = null,
        @SerializedName("ratings"             ) var ratings            : Double?                       = null,
        @SerializedName("delivery_time"       ) var deliveryTime       : String?                       = null
    ){
        data class RestaurantCuisines(
            @SerializedName("id"      ) var id      : Int?    = null,
            @SerializedName("cuisine" ) var cuisine : String? = null
        )
    }
}