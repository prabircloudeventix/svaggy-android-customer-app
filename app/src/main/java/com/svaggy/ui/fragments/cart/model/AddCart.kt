package com.svaggy.ui.fragments.cart.model

import com.google.gson.annotations.SerializedName

data class AddCart(
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean? = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data?    = Data()
)
{
    data class Data (
        @SerializedName("id"            ) var id           : Int?    = null,
        @SerializedName("user_id"       ) var userId       : Int?    = null,
        @SerializedName("restaurant_id" ) var restaurantId : Int?    = null,
        @SerializedName("menu_item_id"  ) var menuItemId   : Int?    = null,
        @SerializedName("quantity"      ) var quantity     : Int?    = null,
        @SerializedName("updatedAt"     ) var updatedAt    : String? = null,
        @SerializedName("createdAt"     ) var createdAt    : String? = null
    )
}