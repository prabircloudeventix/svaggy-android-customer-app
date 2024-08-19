package com.svaggy.ui.fragments.cart.model

import com.google.gson.annotations.SerializedName

data class GetCoupons(
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean?        = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : ArrayList<Data> = arrayListOf()
)
{
    data class Data (
        @SerializedName("id"                 ) var id               : Int?     = null,
        @SerializedName("coupon_code"        ) var couponCode       : String?  = null,
        @SerializedName("discount_type"      ) var discountType     : String?  = null,
        @SerializedName("discount"           ) var discount         : Int?     = null,
        @SerializedName("min_basket_value"   ) var minBasketValue   : Double?     = null,
        @SerializedName("coupon_validity_to" ) var couponValidityTo : String?  = null,
        @SerializedName("is_active"          ) var isActive         : Boolean? = null,
        @SerializedName("description"        ) var description      : String?  = null
    )
}