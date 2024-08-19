package com.svaggy.ui.fragments.cart.model

data class CartCheckout(
    val `data`: Data,
    val is_success: Boolean,
    val message: String,
    val status_code: Int
){
    data class Data(
        val bill_details: ArrayList<BillDetail>,
        val cart_data: CartData?=null,
        val checkout_id: Int,
        val coupon: Coupon? = null,
        val currency_key: String,
        val delivery_address: DeliveryAddress,
        val wallet_amount: Double,
        val promo_details: PromoDetails?,
        val promo_count: Int?,
        val current_delivery_type: String?,
    ){
        data class BillDetail(
            val amount: Double,
            val amount_text: String?,
            val coupon_code: String,
            val distance: String,
            val id: Int,
            val tax_percent: Int,
            val text: String,
            val type: String
        )

        data class PromoDetails(
            val promo_code:String?,
            val promo_balance:Double?,
            val text:String?,
            val promo_amount:Double?)

        data class CartData(
            val cart_items: ArrayList<ViewCart.Data.CartItems>?= arrayListOf(),
            val restaurant_details: RestaurantDetails,
            val total_amount: Double
        ){

            data class RestaurantDetails(
                val delivery_time: String,
                val distance: String,
                val address: String,
                val id: Int,
                val is_active: Boolean,
                val ratings: Double,
                val delivery_cart_minimum_amount: Double,
                val booster_ids: ArrayList<Int>? = null,
                val restaurant_cuisines: List<RestaurantCuisine>,
                val restaurant_image: String,
                val restaurant_name: String,
                val delivery_type:String,
                val is_rush_mode:Boolean,
            ){
                data class RestaurantCuisine(
                val cuisine: String,
                val id: Int
            )}

        }

        data class Coupon(
            val amount: Double?,
            val coupon_code: String?,
            val id: Int?,
            val text: String?,
            val type: String?,
            val is_promo: Boolean?,
            val promo_balance: String?,
        )

        data class DeliveryAddress(
            val address_type: String,
            val city: String,
            val complete_address: String,
            val id: Int,
            val is_default: Boolean,
            val is_for_self: Boolean,
            val latitude: String,
            val longitude: String,
            val phone_number: String,
            val postal_code: String,
            val recipient_name: String,
            val region_info: String,
            val residence_info: String,
            val user_id: Int
        )
    }
}