package com.svaggy.ui.fragments.payment.model

import com.google.gson.annotations.SerializedName

data class GetCard(
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean?        = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : ArrayList<Data> = arrayListOf()
)
{
    data class Data (
        @SerializedName("id"                ) var id              : Int?     = null,
        @SerializedName("stripe_account_id" ) var stripeAccountId : String?  = null,
        @SerializedName("stripe_card_id"    ) var stripeCardId    : String?  = null,
        @SerializedName("last_four_digit"   ) var lastFourDigit   : String?  = null,
        @SerializedName("card_type"         ) var cardType        : String?  = null,
        @SerializedName("card_brand"        ) var cardBrand       : String?  = null,
        @SerializedName("country"           ) var country         : String?  = null,
        @SerializedName("is_deleted"        ) var isDeleted       : Boolean? = null,
        @SerializedName("is_saved"          ) var isSaved         : Boolean? = null,
        @SerializedName("exp_month"         ) var expMonth        : Int?     = null,
        @SerializedName("exp_year"          ) var expYear         : Int?     = null,
        @SerializedName("is_default"        ) var isDefault       : Boolean? = null,
        @SerializedName("card_nickname"     ) var cardNickname    : String?  = null
    )
}