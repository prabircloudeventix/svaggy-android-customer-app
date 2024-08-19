package com.svaggy.utils

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.TextView
import com.svaggy.ui.fragments.home.model.AllRestaurant
import com.svaggy.ui.fragments.home.model.GetCuisines

@SuppressLint("StaticFieldLeak")
object Constants {
    // for login
    const val EmailOrPhone = "email_or_phone"
    const val Token = "Token"
    const val DeviceToken = "DeviceToken"
    const val UserId = "UserId"
    const val UserFirstName = "UserFirstName"
    const val UserLastName = "UserLastName"
    const val UserEmail = "UserEmail"
    const val UserMobile = "UserMobile"
    const val NUM = "Num"
    const val IsLogin = "false"
    const val IsGuestUser = "IsGuestUser"
    const val PK_Test = "pk_test"
    const val GOOGLE_MAP_KEY = "googleMapKey"
    const val PUSHER_KEY = "PUSHER_KEY"
    const val PUSHER_CLUSTER = "PUSHER_CLUSTER"
    const val stripeKey = "stripeKey"


    // for page maintain
    const val Type = "type"
    const val FragmentBackName = "homefragment"
    const val AddressSave = "AddressSave"
    const val BackPressHandel = ""
    const val IsNotFirstOpen = "IsFirstOpen"
    const val AddOnsArray = "AddOnsArray"
    const val ComingFragment = "ComingFragment"
    const val CartRestaurantId = "CartRestaurantId"
    const val MenutRestaurantId = "MenuRestaurantId"
    const val CurrentDestinationId = "CurrentDestinationId"
    const val Longitude = "Longitude"
    const val Latitude = "Latitude"
    const val RESTAURANT_ADDRESS = "Address"
    const val AddressId = "AddressId"
    const val Address = "Address"
    const val RestaurantName = "RestaurantName"
    const val DeliveryDate = "DeliveryDate"
    const val DeliveryTime = "DeliveryTime"
    var currentLocale = "en"
    //Preet
    const val IS_NOTIFICATION = "notification"
    const val BEARER = "Bearer"
    const val ORDER_DELIVERY = "ORDER_DELIVERY"
    const val OFF = "OFF"
    const val ON = "ON"
    const val PROMOTIONAL = "PROMOTIONAL"
    const val en = "en"
    const val LANGUAGE = "LANGUAGE"
    const val ENGLISH = "ENGLISH"
    const val CZECH = "Czech"
    const val PATH = "Path"
    const val Restaurant_is_available = "Restaurant is available !!"
    const val order_placed = "Order Placed Successfully !!"
    const val ITEM_ID = "item_id"
    const val DELIVERY_TYPE = "deliveryType"
    const val BROADCAST_ID = "broadcast_id"
    const val HOME = "Home"
    const val WORK = "Work"
    const val IS_SHOW_TIME_BAR = "IS_SHOW_TIME_BAR"
    const val DELIVERY_BY_SVAGGY = "DELIVERY_BY_SVAGGY"
    const val DELIVERY_BY_RESTAURANT = "DELIVERY_BY_RESTAURANT"
    const val PICKUP_ONLY = "PICKUP_ONLY"
    const val SCHEDULED = "SCHEDULED"
    const val NOW = "NOW"
    const val COMPLETE_BY_WALLET = "COMPLETE_BY_WALLET"
    const val REMOVE_PROMO = "REMOVE_PROMO"
    const val ADD_PROMO = "ADD_PROMO"
    const val CONVERSION = "CONVERSION"
    const val IS_SHOW_BAN = true
    //
    var requiredCounterValue = 0
    var requiredWithChoseUp = 0
    var isInitBottomNav = false
    //
    val allRestaurant = ArrayList<AllRestaurant.Data>()
    val offerRestaurant = ArrayList<AllRestaurant.Data>()
    val getCuisines = ArrayList<GetCuisines.Data>()

    val imageUrl = ArrayList<Int>()


}