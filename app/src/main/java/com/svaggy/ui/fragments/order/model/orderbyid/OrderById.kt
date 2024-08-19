package com.svaggy.ui.fragments.order.model.orderbyid

import com.fasterxml.jackson.annotation.JsonProperty
import com.svaggy.ui.fragments.cart.model.CartCheckout

data class OrderById(
    val `data`: Data,
    val is_success: Boolean,
    val message: String,
    val status_code: Int
){
    data class Data(
        val bill_details: ArrayList<CartCheckout.Data.BillDetail>,
        val cart_data: CartData,
        val currency_key: String?="",
        val order_details: ArrayList<OrderDetail>,
        val order_tracking:ArrayList<OrderTracking?>,
        val order_status:String,
        val order_confirmation_otp:String,
        val driver_map_details: DriverMapDetails?

        ){
        data class BillDetail(
            val amount: Double,
            val coupon_code: String,
            val distance: String,
            val id: Int,
            val tax_percent: Int,
            val text: String,
            val type: String
        )
        data class CartData(
            val cart_items: ArrayList<CartItem>? = arrayListOf(),
            val restaurant_details: RestaurantDetails,
            val total_amount: Double
        )
        {
            data class CartItem(
                val actual_price: Double,
                val description: String,
                val dish_name: String,
                val dish_type: String,
                val id: Int,
                val is_active: Boolean,
                val menu_add_ons: List<MenuAddOn>,
                val menu_item_id: Int,
                val menu_item_image: String,
                val price: Double,
                val quantity: Int
            ){
                data class MenuAddOn(
                    val add_ons_id: Int,
                    val add_ons_name: String,
                    val add_ons_relations: List<AddOnsRelation>,
                    val choose_upto: Int,
                    val id: Int,
                    val is_required: Boolean
                ){
                    data class AddOnsRelation(
                        val actual_price: Double,
                        val dish_type: String,
                        val id: Int,
                        val is_selected: Boolean,
                        val item_name: String,
                        val price: Double
                    )
                }
            }

            data class RestaurantDetails(
                val delivery_time: String,
                val latitude: String,
                val longitude: String,
                val distance: String,
                val address: String?="",
                val id: Int,
                val is_active: Boolean,
                val ratings: Double,
                val restaurant_cuisines: List<Any>,
                val restaurant_image: String,
                val restaurant_name: String
            )
        }
        data class OrderDetail(
            val text: String,
            val type: String,
            val value: String,
            val latitude: String?,
            val longitude: String?,
            val distance: String?,
        )
        data class OrderTracking(
            val id:String,
            val order_id:String,
            val timestamp:String,
            val order_status:String


        )


        data class DriverMapDetails (

            val driver_id: Long,
            val driver_name: String,
            val driver_phone_number: String,
            val delivery_distance: String,
            val delivery_duration: String,
            val restaurant_to_user_polyline: String,
            val google_map_metadata: GoogleMapMetadata
        )

        data class GoogleMapMetadata (
            @JsonProperty("geocoded_waypoints")
            val geocodedWaypoints: List<GeocodedWaypoint>,
            val routes: List<Route>,
            val status: String
        )

        data class GeocodedWaypoint (
            @JsonProperty("geocoder_status")
            val geocoderStatus: String,

            @JsonProperty("place_id")
            val placeID: String,

            val types: List<String>
        )

        data class Route (
            val bounds: Bounds,
            val copyrights: String,
            val legs: List<Leg>,

            @JsonProperty("overview_polyline")
            val overviewPolyline: Polyline,

            val summary: String,
            val warnings: List<Any?>,

            @JsonProperty("waypoint_order")
            val waypointOrder: List<Any?>
        )

        data class Bounds (
            val northeast: Northeast,
            val southwest: Northeast
        )
        data class Northeast (
            val lat: Double,
            val lng: Double
        )
        data class Leg (
            val distance: Distance,
            val duration: Distance,

            @JsonProperty("end_address")
            val endAddress: String,

            @JsonProperty("end_location")
            val endLocation: Northeast,

            @JsonProperty("start_address")
            val startAddress: String,

            @JsonProperty("start_location")
            val startLocation: Northeast,

            val steps: List<Step>,

            @JsonProperty("traffic_speed_entry")
            val trafficSpeedEntry: List<Any?>,

            @JsonProperty("via_waypoint")
            val viaWaypoint: List<Any?>
        )

        data class Distance (
            val text: String,
            val value: Long
        )

        data class Step (
            val distance: Distance,
            val duration: Distance,

            @JsonProperty("end_location")
            val endLocation: Northeast,

            @JsonProperty("html_instructions")
            val htmlInstructions: String,

            val polyline: Polyline,

            @JsonProperty("start_location")
            val startLocation: Northeast,

            @JsonProperty("travel_mode")
            val travelMode: String,

            val maneuver: String? = null
        )

        data class Polyline (
            val points: String
        )
    }
}