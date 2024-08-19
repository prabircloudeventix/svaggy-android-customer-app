package com.svaggy.ui.fragments.banner.model

import com.google.gson.annotations.SerializedName

data class CompareFood(
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean?        = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : ArrayList<Data> = arrayListOf()
)
{
    data class Data (
        @SerializedName("id"              ) var id             : Int?                 = null,
        @SerializedName("restaurant_name" ) var restaurantName : String?              = null,
        @SerializedName("is_active"       ) var isActive       : Boolean?             = null,
        @SerializedName("distance"        ) var distance       : String?              = null,
        @SerializedName("ratings"         ) var ratings        : Double?              = null,
        @SerializedName("menu_items"      ) var menuItems      : ArrayList<MenuItems> = arrayListOf()
    )
    {
        data class MenuItems (
            @SerializedName("id"              ) var id            : Int?     = null,
            @SerializedName("dish_name"       ) var dishName      : String?  = null,
            @SerializedName("dish_type"       ) var dishType      : String?  = null,
            @SerializedName("description"     ) var description   : String?  = null,
            @SerializedName("is_active"       ) var isActive      : Boolean? = null,
            @SerializedName("price"           ) var price         : Double?  = null,
            @SerializedName("actual_prices"   ) var actualPrices  : Double?     = null,
            @SerializedName("menu_item_image" ) var menuItemImage : String?  = null
        )
    }
}