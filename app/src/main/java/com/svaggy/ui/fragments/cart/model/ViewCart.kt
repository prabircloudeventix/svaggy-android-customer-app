package com.svaggy.ui.fragments.cart.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem

data class ViewCart(
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean? = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data?    = Data()
) {

    data class Data(
        @SerializedName("restaurant_details" ) var restaurantDetails : RestaurantDetails?   = RestaurantDetails(),
        @SerializedName("total_amount"       ) var totalAmount       : Double?              = null,
        @SerializedName("total_cart_quantity") var totalCartQuantity : Int? = null,
        @SerializedName("is_show_promo_banner") var isShowPromoBanner : Boolean? = null,
        @SerializedName("is_combo_available") var    isComboAvailable : Boolean? = null,
        @SerializedName("cart_items"         ) var cartItems         : ArrayList<CartItems> = arrayListOf()
    ) {
        data class RestaurantDetails(
            @SerializedName("id"                  ) var id                 : Int?                          = null,
            @SerializedName("restaurant_name"     ) var restaurantName     : String?                       = null,
            @SerializedName("address"     ) var address     : String?                       = null,
            @SerializedName("is_active"           ) var isActive           : Boolean?                      = null,
            @SerializedName("restaurant_cuisines" ) var restaurantCuisines : ArrayList<RestaurantCuisines> = arrayListOf(),
            @SerializedName("restaurant_image"    ) var restaurantImage    : String?                       = null,
            @SerializedName("distance"            ) var distance           : String?                       = null,
            @SerializedName("ratings"             ) var ratings            : Double?                       = null,
            @SerializedName("delivery_time"       ) var deliveryTime       : String?                       = null,
            @SerializedName("delivery_type"       ) var deliveryType      : String?                       = null
        )
        {
            data class RestaurantCuisines (
                @SerializedName("id"      ) var id      : Int?    = null,
                @SerializedName("cuisine" ) var cuisine : String? = null
            )
        }
        data class CartItems(
            @SerializedName("id"              ) var id: Int?                  = null,
            @SerializedName("dish_name"       ) var dishName: String?               = null,
            @SerializedName("dish_type"       ) var dishType: String?               = null,
            @SerializedName("price"           ) var price: Double?               = null,
            @SerializedName("price_with_quantity") var priceWithQuantity: String?               = null,
            @SerializedName("description"     ) var description: String?               = null,
            @SerializedName("is_active"       ) var isActive: Boolean?              = null,
            @SerializedName("menu_add_ons"    ) var menuAddOns: ArrayList<RestaurantsMenuItem.Data.Categories.MenuItems.MenuAddOns> = arrayListOf(),
            @SerializedName("menu_item_image" ) var menuItemImage: String?             = null,
            @SerializedName("actual_price" ) var actualPrice: Double?               = null,
            @SerializedName("menu_total_quantity" ) var menu_total_quantity: Int?               = null,
            @SerializedName("quantity"        ) var quantity: Int?                  = null,
            @SerializedName("menu_item_id"    ) var menuItemId: Int?                  = null
        ) : Parcelable{
            constructor(parcel: Parcel) : this(
                parcel.readValue(Int::class.java.classLoader) as? Int,
                parcel.readString(),
                parcel.readString(),
                parcel.readValue(Double::class.java.classLoader) as? Double,
                parcel.readString(),
                parcel.readString(),
                parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
                TODO("menuAddOns"),
                parcel.readString(),
                parcel.readValue(Int::class.java.classLoader) as? Double,
                parcel.readValue(Int::class.java.classLoader) as? Int
            )

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeValue(id)
                parcel.writeString(dishName)
                parcel.writeString(dishType)
                parcel.writeValue(price)
                parcel.writeString(description)
                parcel.writeValue(isActive)
                parcel.writeString(menuItemImage)
                parcel.writeValue(quantity)
                parcel.writeValue(menuItemId)
            }

            override fun describeContents(): Int {
                return 0
            }

            companion object CREATOR : Parcelable.Creator<CartItems> {
                override fun createFromParcel(parcel: Parcel): CartItems {
                    return CartItems(parcel)
                }

                override fun newArray(size: Int): Array<CartItems?> {
                    return arrayOfNulls(size)
                }
            }

        }
    }
}