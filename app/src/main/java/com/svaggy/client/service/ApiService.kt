package com.svaggy.client.service

import com.google.gson.JsonObject
import com.svaggy.client.models.AddOnsModel
import com.svaggy.client.models.CommonModel
import com.svaggy.client.models.DeviceToken
import com.svaggy.client.models.GuestPrefModel
import com.svaggy.ui.fragments.editAuth.model.UserDetail
import com.svaggy.ui.fragments.editAuth.model.SendOtp
import com.svaggy.ui.fragments.home.model.GetCuisines
import com.svaggy.ui.activities.savePref.UserPreference
import com.svaggy.ui.fragments.location.address.model.AddAddress
import com.svaggy.ui.fragments.banner.model.ComboMeal
import com.svaggy.ui.fragments.banner.model.CompareFood
import com.svaggy.ui.fragments.cart.model.AddCart
import com.svaggy.ui.fragments.cart.model.CartCheckout
import com.svaggy.ui.fragments.cart.model.GetCoupons
import com.svaggy.ui.fragments.cart.model.TimerModel
import com.svaggy.ui.fragments.cart.model.ViewCart
import com.svaggy.ui.fragments.home.model.AllRestaurant
import com.svaggy.ui.fragments.home.model.BroadCastModel
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem
import com.svaggy.ui.fragments.home.model.Search
import com.svaggy.ui.fragments.location.model.GetAddress
import com.svaggy.ui.fragments.order.model.orderbyid.OrderById
import com.svaggy.ui.fragments.order.model.Orders
import com.svaggy.ui.fragments.order.model.driverlocation.DriverLocationModel
import com.svaggy.ui.fragments.order.model.order_placed.OrderPlacedModel
import com.svaggy.ui.fragments.order.model.orderwithtracking.OrderWithTracking
import com.svaggy.ui.fragments.payment.model.GetCard
import com.svaggy.ui.fragments.payment.model.StripeCustomer
import com.svaggy.ui.fragments.profile.model.DeleteAccountModel
import com.svaggy.ui.fragments.profile.model.Settings
import com.svaggy.ui.fragments.profile.viewmodel.EditProfile
import com.svaggy.ui.fragments.wallet.models.AddPayCardModel
import com.svaggy.ui.fragments.wallet.models.WalletList
import com.svaggy.ui.fragments.wallet.models.WalletModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ApiService {
    @POST("auth-user/mobile/get-OTP")
    suspend fun sendMobileOtp(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<SendOtp>

    @POST("auth-user/mobile/resend-OTP")
    suspend fun reSendMobileOtp(
        @Body params: Map<String, String>
    ): Response<SendOtp>

    @POST("auth-user/email/get-OTP")
    suspend fun sendEmailOtp(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<SendOtp>

    @POST("auth-user/email/resend-OTP")
    suspend fun reSendEmailOtp(
        @Body params: Map<String, String>
    ): Response<SendOtp>

    @POST("auth-user/verify-otp")
    suspend fun verifyOtp(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<UserDetail>

    @GET("home/cuisines")
    suspend fun getCuisines(
        @Header("Authorization") token: String
    ): Response<GetCuisines>

    @GET("user-preferences")
    suspend fun getPreferences(
        @Header("Authorization") token: String,
    ): Response<UserPreference>

    @FormUrlEncoded
    @POST("user-preferences")
    suspend fun setPreferences(
        @Header("Authorization") token: String,
        @Field("preferences[]") preferences: ArrayList<Int>
    ): Response<CommonModel>

    @FormUrlEncoded
    @POST("user-preferences")
    suspend fun setGuestPreferences(
        @Field("device_id") deviceId: String,
        @Field("preferences") preferences: ArrayList<Int>
    ): Response<GuestPrefModel>

    @POST("home/all-restaurants")
    suspend fun getRestaurants(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<AllRestaurant>

    @POST("menu-items/by-restaurant")
    suspend fun getMenuItem(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<RestaurantsMenuItem>

    @POST("auth-user/social-login")
    suspend fun socialLogin(
        @Body params: Map<String, String>
    ): Response<UserDetail>

    @POST("users-profile")
    suspend fun editProfile(
        @Header("Authorization") token: String, @Body params: Map<String, String>
    ): Response<EditProfile>

    @POST("home/special-offers")
    suspend fun getRestaurantsOffersItem(
        @Header("Authorization") token: String, @Body params: Map<String, String>
    ): Response<AllRestaurant>

    @POST("home/restaurant-by-cuisines")
    suspend fun getCuisinesRestaurant(
        @Header("Authorization") token: String, @Body params: Map<String, String>
    ): Response<AllRestaurant>

    @GET("cart")
    suspend fun getCartDataApi(
        @Header("Authorization") token: String
    ): Response<ViewCart>

    @POST("cart/add-item")
    suspend fun addCartItem(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<AddCart>

    @FormUrlEncoded
    @POST("cart/add-item")
    suspend fun placeOrder(
        @Header("Authorization") token: String,
        @Field("restaurant_id") restaurantId: Int,
        @Field("menu_item[menu_item_id]") menuItemId: Int,
        @Field("menu_item[quantity]") quantity: Int,
        @Field("menu_item[add_ons][]") addOns: List<Int>
    ): Response<AddCart>

    @FormUrlEncoded
    @PATCH("cart/edit-item")
    suspend fun editCartItem(
        @Header("Authorization") token: String,
        @Field("menu_item[cart_id]") cartId: Int,
        @Field("menu_item[quantity]") quantity: Int,
        ): Response<CommonModel>

    @FormUrlEncoded
    @PATCH("cart/edit-item")
    suspend fun editCartItem2(
        @Header("Authorization") token: String,
        @Field("menu_item[cart_id]") cartId: Int,
        @Field("menu_item[quantity]") quantity: Int,
    ): Response<CommonModel>

    @FormUrlEncoded
    @PATCH("cart/edit-item")
    suspend fun updateAddOn(
        @Header("Authorization") token: String,
        @Field("menu_item[cart_id]") cartId: Int,
        @Field("menu_item[quantity]") quantity: Int,
        @Field("menu_item[add_ons][]") addOns: List<Int>
    ): Response<CommonModel>


    @PATCH("cart/edit-item")
    suspend fun updateAddOn2(
        @Header("Authorization") token: String,
        @Body  addOnModel: AddOnsModel
    ): Response<CommonModel>

    @DELETE("cart/delete-item")
    suspend fun deleteCartItem(
        @Header("Authorization") token: String, @QueryMap params: Map<String, String>
    ): Response<CommonModel>

    @DELETE("cart/delete-all")
    suspend fun deleteAllCartItem(
        @Header("Authorization") token: String,
    ): Response<CommonModel>


    @GET("home/popular-cuisines")
    suspend fun getPopularCuisines(
        @Header("Authorization") token: String
    ): Response<GetCuisines>

    @GET("home/search")
    suspend fun searchData(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String>
    ): Response<Search>

    @GET("address/get")
    suspend fun getAddress(
        @Header("Authorization") token: String
    ): Response<GetAddress>

    @DELETE("address/delete")
    suspend fun deleteAddress(
        @Header("Authorization") token: String, @QueryMap params: Map<String, String>
    ): Response<CommonModel>

    @POST("address/create")
    suspend fun addAddress(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<AddAddress>

    @PATCH("address/edit")
    suspend fun editAddress(
        @Header("Authorization") token: String, @Body params: Map<String, String>
    ): Response<AddAddress>

    @POST("cart/checkout")
    suspend fun getCheckOut(
        @Header("Authorization") token: String
    ): Response<CartCheckout>

    @POST("cart/checkout")
    suspend fun getCheckOutDetails(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<CartCheckout>

    @POST("address/set-current-address")
    suspend fun setCurrentAddress(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<CommonModel>

    @GET("orders/coupons")
    suspend fun getCoupons(
        @Header("Authorization") token: String,
        @Query ("restaurant_id") restaurantId: String
    ): Response<GetCoupons>

    @POST("home/restaurant-by-combo")
    suspend fun getComboMeal(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<ComboMeal>

    @POST("home/compare-food-prices")
    suspend fun getCompareFood(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<CompareFood>

    /*@POST("orders/offers")
    suspend fun setCoupon(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<CommonModel>*/

    /*@POST("orders/offers")
    suspend fun couponApply(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<CommonModel>*/

    @FormUrlEncoded
    @POST("orders/offers")
    suspend fun couponApply(
        @Header("Authorization") token: String,
        @Field("coupon_id") couponId: Int,
        @Field("is_coupon_add") couponBoolean: Boolean,
        @Field("checkout_id") checkoutId: String
    ): Response<CommonModel>


    @FormUrlEncoded
    @POST("orders/offers")
    suspend fun setCoupon(
        @Header("Authorization") token: String,
        @Field("coupon_id") couponId: String,
        @Field("is_coupon_add") couponBoolean: Boolean,
        @Field("checkout_id") checkoutId: String
    ): Response<CommonModel>

    @POST("orders/offers")
    suspend fun applyPromo(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<CommonModel>

    @FormUrlEncoded
    @POST("orders/offers")
    suspend fun setWallet(
        @Header("Authorization") token: String,
        @Field("is_wallet") checkWallet: Boolean,
        @Field("checkout_id") checkoutId: String
    ): Response<CommonModel>

    @GET("orders")
    suspend fun ordersList(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String>
    ): Response<Orders>

    @FormUrlEncoded
    @POST("payment/payment-process")
    suspend fun orderPlaced(
        @Header("Authorization") token: String,
        @Field("restaurant_id") cartRestaurantId: Int,
        @Field("checkout_id") checkoutId: Int,
        @Field("delivery_instruction") deliveryInstruction: String,
        @Field("cooking_instruction") cookingInstruction: String,
        @Field("delivery_date") deliveryDate: String,
        @Field("delivery_time") deliveryTime:String,
        @Field("paymentIntent") paymentIntent:String,
        @Field("paymentStatus") paymentStatus:String
    ): Response<OrderPlacedModel>

    @GET("orders/order-by-id")
    suspend fun orderDetails(
        @Header("Authorization") token: String,
        @Query("order_id") orderId: String
    ): Response<OrderById>

    @GET("orders/order-by-id")
    suspend fun orderDetailWithTracking(
        @Header("Authorization") token: String,
        @Query("order_id") orderId: String,
        @Query("type") type: String,
    ): Response<OrderWithTracking>

    @DELETE("orders/cancel")
    suspend fun cancelOrder(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String>
    ): Response<CommonModel>

    @POST("orders/re-order")
    suspend fun reOrder(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<CommonModel>

    @POST("home/set-device-token")
    suspend fun setDeviceToken(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<DeviceToken>
    @POST("orders/restaurant-availabilty")
    suspend fun verifyDate(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<TimerModel>

    /*@POST("wallet/add-funds")
    suspend fun walletAmountAdd(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<WalletModel>*/

    @GET("wallet/all-transactions")
    suspend fun getWalletList(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String>
    ): Response<WalletList>

    @FormUrlEncoded
    @POST("wallet/add-funds")
    suspend fun walletAmountAdd(
        @Header("Authorization") token: String,
        @Field("amount") amount: Int,
        @Field("paymentIntent") paymentIntent:String,
        @Field("paymentStatus") paymentStatus:String
    ): Response<WalletModel>

    //Payment Apis
    @FormUrlEncoded
    @POST("cards/add")
    suspend fun addCard(
        @Header("Authorization") token: String,
        @Field("token") cardToken: String,
        @Field("card_nickname") cardNickname:String
    ): Response<AddPayCardModel>


    @FormUrlEncoded
    @POST("cards/add")
    suspend fun addUserCard(
        @Header("Authorization") token: String,
        @Field("token") cardToken: String,
        @Field("card_nickname") cardNickname:String
    ): Response<AddPayCardModel>


    @FormUrlEncoded
    @POST("/cards/manage")
    suspend fun editCard(
        @Header("Authorization") token: String,
        @Field("stripe_card_id") cardToken: String,
        @Field("card_nickname") cardNickname:String
    ): Response<AddPayCardModel>

    @GET("cards")
    suspend fun getCardsList(
        @Header("Authorization") token: String
    ): Response<GetCard>

    @DELETE("cards/remove")
    suspend fun deleteCard(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String>
    ): Response<CommonModel>

    @GET("payment/intent/order")
    suspend fun stripeCustomer(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String>
    ): Response<StripeCustomer>

    @GET("payment/intent/wallet")
    suspend fun stripeCustomerWallet(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String>
    ): Response<StripeCustomer>

    @GET("settings")
    suspend fun getSettings(
        @Header("Authorization") token: String
    ): Response<Settings>

    @POST("settings/manage")
    suspend fun setSettings(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<CommonModel>


    @POST("/actions/broadcast")
    suspend fun setBroadCast(
        @Header("Authorization") token: String,
        @Body params:JsonObject
    ): Response<BroadCastModel>


    @POST("/actions/booster")
    suspend fun setAction(
        @Header("Authorization") token: String,
        @Body params:JsonObject
    ): Response<BroadCastModel>


    @POST("/actions/custom-order-tracking-notification")
    suspend fun outOrderDeliver(
        @Header("Authorization") token: String,
        @Body params: JsonObject
    ): Response<BroadCastModel>

    @POST("/actions/add-rating")
    suspend fun sentReview(
        @Header("Authorization") token: String,
        @Body params: JsonObject
    ): Response<BroadCastModel>



    @GET("/settings/default-reasons")
    suspend fun deleteAccountReason(
        @Header("Authorization") token: String,
        @Query("reason_for") reasonFor: String,
        @Query("reason_type") reasonType: String,
    ): Response<DeleteAccountModel>


    @POST("/settings/deactivate-account")
    suspend fun deleteAccount(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
        ): Response<CommonModel>

    @POST("/cart/set-delivery-type")
    suspend fun setDeliveryType(
        @Header("Authorization") token: String,
        @Body params: Map<String, String>
    ): Response<CommonModel>


    @GET("/actions/driver-current-location")
    suspend fun getDriverLocation(
        @Header("Authorization") token: String,
        @Query("order_id") orderId:Int
    ): Response<DriverLocationModel>


    @GET("actions/order-driver-tracking")
    suspend fun getDeliveryTime(
        @Query("order_id") orderId:Int,
        @Query("type") type:String
    ): Response<JsonObject>


    @POST("home/all-restaurants")
    suspend fun getRestaurantsNew(
        @Header("Authorization") token: String,
        @Body params:JsonObject
    ): Response<AllRestaurant>


    @POST("home/special-offers")
    suspend fun getRestaurantsOffersItemPag(
        @Header("Authorization") token: String,
        @Body  params:JsonObject
    ): Response<AllRestaurant>


    @POST("home/restaurant-by-cuisines")
    suspend fun getCuisinesRestaurantPagination(
        @Header("Authorization") token: String,
        @Body  params:JsonObject
    ): Response<AllRestaurant>


    @POST("home/restaurant-by-combo")
    suspend fun getComboMealPagination(
        @Header("Authorization") token: String,
        @Body params:JsonObject
    ): Response<ComboMeal>



    @GET("/actions/app-version-update?")
    suspend fun appUpdate(
        @Query("device_os") deviceOs:String,
        @Query("app_type") appType:String,
        @Query("version") version:String,
    ): Response<JsonObject>


    @POST("/home/logout")
    suspend fun userLogOut(
        @Header("Authorization") token: String,
        @Body params:JsonObject): Response<JsonObject>








}