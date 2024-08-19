package com.svaggy.ui.fragments.cart.model

import com.google.gson.annotations.SerializedName

data class CartEdit(@SerializedName("restaurant_id" ) var restaurantId : Int?      = null,
                    @SerializedName("menu_item"     ) var menuItem     : MenuItem? = MenuItem()
) {
    data class MenuItem (

        @SerializedName("menu_item_id" ) var menuItemId : Int?              = null,
        @SerializedName("quantity"     ) var quantity   : Int?              = null,
        @SerializedName("add_ons"      ) var addOns     : ArrayList<String> = arrayListOf()

    )
}