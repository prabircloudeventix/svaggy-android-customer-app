package com.svaggy.ui.fragments.editAuth.model

import com.google.gson.annotations.SerializedName

data class UserDetail(
    @SerializedName("status_code" ) var statusCode : Int?     = null,
    @SerializedName("is_success"  ) var isSuccess  : Boolean? = null,
    @SerializedName("message"     ) var message    : String?  = null,
    @SerializedName("data"        ) var data       : Data?    = Data()
)
{
    data class Data (
        @SerializedName("user_details" ) var userDetails : UserDetails? = UserDetails()
    )
    {
        data class UserDetails (
            @SerializedName("id"           ) var id          : Int?                   = null,
            @SerializedName("first_name"   ) var firstName   : String?                = null,
            @SerializedName("last_name"    ) var lastName    : String?                = null,
            @SerializedName("email"        ) var email       : String?                = null,
            @SerializedName("profile_url"  ) var profileUrl  : String?                = null,
            @SerializedName("device_token" ) var deviceToken : String?                = null,
            @SerializedName("social_token" ) var socialToken : String?                = null,
            @SerializedName("type"         ) var type        : String?                = null,
            @SerializedName("phone_number" ) var phoneNumber : String?                = null,
            @SerializedName("createdAt"    ) var createdAt   : String?                = null,
            @SerializedName("updatedAt"    ) var updatedAt   : String?                = null,
            @SerializedName("preferences"  ) var preferences : ArrayList<Preferences> = arrayListOf(),
            @SerializedName("access_token" ) var accessToken : String?                = null,
            @SerializedName("configurations" ) var configuration: Configurations?     = null,
            @SerializedName("current_language" ) var currentLanguage: String?     = null,

        )
        {
            data class Preferences (
                @SerializedName("id"               ) var id              : Int?    = null,
                @SerializedName("preference_id"    ) var preferenceId    : Int?    = null,
                @SerializedName("preference_name"  ) var preferenceName  : String? = null,
                @SerializedName("preference_image" ) var preferenceImage : String? = null,
                @SerializedName("PUSHER_APP_ID" ) var pusherId : String? = null,

            )

            data class Configurations(
                @SerializedName("publishable_key") val publishableKey  : String?    = null,
                @SerializedName("STRIPE_PUBLISH_KEY") val stripeKey  : String?    = null,
                @SerializedName("GOOGLE_CLIENT_ID") val googleClientId  : String?    = null,
                @SerializedName("GOOGLE_CLIENT_SECRET") val googleClient  : String?    = null,
                @SerializedName("GOOGLE_MAP_API_KEY") val googleMapKey  : String?    = null,
                @SerializedName("PUSHER_KEY" ) var PUSHER_KEY : String? = null,
                @SerializedName("PUSHER_SECRET" ) var PUSHER_SECRET : String? = null,
                @SerializedName("PUSHER_CLUSTER" ) var PUSHER_CLUSTER : String? = null,

            )
        }
    }
}