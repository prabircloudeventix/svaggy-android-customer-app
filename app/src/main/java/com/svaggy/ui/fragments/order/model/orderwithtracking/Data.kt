package com.svaggy.ui.fragments.order.model.orderwithtracking

data class Data(
    val cart_data: CartData?,
    val currency_key: String?,
    val current_order_text: String?,
    val delivery_type: String?,
    val driver_map_details: DriverMapDetails?,
    val order_confirmation_otp: String?,
    val order_details: List<Any?>?,
    val order_status: String?,
    val restaurant_name: String?,
    val restaurant_phone_number: String?,
    val restaurant_latitide: String?,
    val restaurant_longitude: String?,
    val users_latitide: String?,
    val users_longitude: String?,
    val order_tracking: List<OrderTracking>?,
    val cancelled_order_popup_text: String?,
)