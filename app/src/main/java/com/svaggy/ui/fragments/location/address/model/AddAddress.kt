package com.svaggy.ui.fragments.location.address.model

import com.google.gson.annotations.SerializedName

data class AddAddress(
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean? = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data?    = Data()
)
{
    data class Data (
        @SerializedName("id"             ) var id            : Int?     = null,
        @SerializedName("is_for_self"    ) var isForSelf     : Boolean? = null,
        @SerializedName("is_default"     ) var isDefault     : Boolean? = null,
        @SerializedName("recipient_name" ) var recipientName : String?  = null,
        @SerializedName("residence_info" ) var residenceInfo : String?  = null,
        @SerializedName("region_info"    ) var regionInfo    : String?  = null,
        @SerializedName("postal_code"    ) var postalCode    : String?  = null,
        @SerializedName("city"           ) var city          : String?  = null,
        @SerializedName("latitude"       ) var latitude      : String?  = null,
        @SerializedName("longitude"      ) var longitude     : String?  = null,
        @SerializedName("address_type"   ) var addressType   : String?  = null,
        @SerializedName("user_id"        ) var userId        : Int?     = null,
        @SerializedName("updatedAt"      ) var updatedAt     : String?  = null,
        @SerializedName("createdAt"      ) var createdAt     : String?  = null
    )
}