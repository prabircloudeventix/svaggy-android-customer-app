package com.svaggy.ui.fragments.order.model

import com.google.gson.annotations.SerializedName

data class Orders(
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean? = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data?    = Data()
)
{
    data class Data (
        @SerializedName("type"       ) var type      : String?              = null,
        @SerializedName("order_data" ) var orderData : ArrayList<OrderData> = arrayListOf()
    )
    {
        data class OrderData (
            @SerializedName("order_id"           ) var orderId           : Int?                 = null,
            @SerializedName("app_order_id"       ) var appOrderId        : String?              = null,
            @SerializedName("order_status"       ) var orderStatus       : String?              = null,
            @SerializedName("restaurant_name"    ) var restaurantName    : String?              = null,
            @SerializedName("restaurant_address" ) var address : String?              = null,
            @SerializedName("total_amount"      ) var totalAmount       : Double?              = null,
            @SerializedName("currency"           ) var currency          : String?              = null,
            @SerializedName("menu_items"         ) var menuItems         : ArrayList<MenuItems> = arrayListOf(),
            @SerializedName("date"               ) var date              : String?              = null,
            @SerializedName("order_review"       ) val orderReview       : OrderReview?              = null,
            @SerializedName("delivery_type"      ) val  deliveryType     : String?              = null
        )
        {
            data class MenuItems (

                @SerializedName("dish_name" ) var dishName : String? = null,
                @SerializedName("quantity"  ) var quantity : Int?    = null

            )

            data class OrderReview (
                @SerializedName("ratings" ) val rating : Int? = null,
                @SerializedName("reviews" ) val reviews : String? = null
            )
        }
    }
}