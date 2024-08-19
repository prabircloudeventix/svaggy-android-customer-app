package com.svaggy.ui.fragments.location.model

import com.google.gson.annotations.SerializedName

data class GetAddress(
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean?        = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : ArrayList<Data> = arrayListOf()
)
{
    data class Data (
        @SerializedName("id"               ) var id              : Int?     = null,
        @SerializedName("user_id"          ) var userId          : Int?     = null,
        @SerializedName("is_for_self"      ) var isForSelf       : Boolean? = null,
        @SerializedName("is_default"       ) var isDefault       : Boolean? = null,
        @SerializedName("recipient_name"   ) var recipientName   : String?  = null,
        @SerializedName("residence_info"   ) var residenceInfo   : String?  = null,
        @SerializedName("region_info"      ) var regionInfo      : String?  = null,
        @SerializedName("postal_code"      ) var postalCode      : String?  = null,
        @SerializedName("city"             ) var city            : String?  = null,
        @SerializedName("latitude"         ) var latitude        : String?  = null,
        @SerializedName("longitude"        ) var longitude       : String?  = null,
        @SerializedName("address_type"     ) var addressType     : String?  = null,
        @SerializedName("phone_number"     ) var phoneNumber     : String?  = null,
        @SerializedName("complete_address" ) var completeAddress : String?  = null,
        @SerializedName("google_pinned_address" ) var googlePin : String?  = null,
        @SerializedName("floor" ) var floor : String?  = null,
        @SerializedName("company_name" ) var companyName : String?  = null,
        @SerializedName("street_number" ) var streetNumber : String?  = null,
    )
}