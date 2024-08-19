package com.svaggy.client.service

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.google.gson.JsonObject
import com.svaggy.client.models.AddOnsModel
import com.svaggy.pagination.ComboPaginationSource
import com.svaggy.pagination.OfferRestaurantPagingSource
import com.svaggy.pagination.RestaurantPagingSource
import com.svaggy.pagination.adapter.CuisinesPagingSource
import com.svaggy.ui.fragments.banner.model.ComboMeal
import com.svaggy.ui.fragments.home.model.AllRestaurant
import com.svaggy.utils.result
import com.svaggy.utils.result2
import javax.inject.Inject

class ApiManager @Inject constructor(private val service: ApiService) {
//    suspend fun sendMobileOtp(token: String, map: Map<String, String>) =
//        service.sendMobileOtp(token, map)

    suspend fun reSendMobileNewOtp(map: Map<String, String>) = service.reSendMobileOtp(map)

    suspend fun reSendEmailOtp(map: Map<String, String>) = service.reSendEmailOtp(map)
    suspend fun verifyOtp(token: String, map: Map<String, String>) = service.verifyOtp(token, map)
    suspend fun socialLogin(map: Map<String, String>) = service.socialLogin(map)
    suspend fun editProfile(token: String, map: Map<String, String>) =
        service.editProfile(token, map)

    suspend fun getCuisines(token: String) = service.getCuisines(token)
    suspend fun getPreferences(token: String) = service.getPreferences(token)
    suspend fun setPreferences(token: String, preferences: ArrayList<Int>) = service.setPreferences(token, preferences)
    suspend fun guestSetPreferences(token: String, preferences: ArrayList<Int>) = result { service.setGuestPreferences(token, preferences)  }

    suspend fun getRestaurantsOffersItem(token: String, map: Map<String, String>) = service.getRestaurantsOffersItem(token, map)



    suspend fun getCartDataFn(token: String) = service.getCartDataApi(token)


    suspend fun placeOrder(
        token: String,
        restaurantId: Int,
        menuItemId: Int,
        quantity: Int,
        addOns: List<Int>
    ) = service.placeOrder(token, restaurantId, menuItemId, quantity, addOns)


    suspend fun deleteCartItem(token: String, mMap: Map<String, String>) =
        service.deleteCartItem(token, mMap)

    suspend fun deleteAllCartItem(token: String) = service.deleteAllCartItem(token)
    suspend fun getPopularCuisines(token: String) = service.getPopularCuisines(token)
    suspend fun searchData(token: String, mMap: Map<String, String>) =
        service.searchData(token, mMap)

    suspend fun getAddress(token: String) = service.getAddress(token)
    suspend fun deleteAddress(token: String, mMap: Map<String, String>) =
        service.deleteAddress(token, mMap)

    suspend fun addAddress(token: String, mMap: Map<String, String>) = service.addAddress(token, mMap)

    suspend fun addUserAddress(token: String,mMap: Map<String, String>) = result { service.addAddress(token,mMap) }

    suspend fun editAddress(token: String, mMap: Map<String, String>) = service.editAddress(token, mMap)
    suspend fun editUserAddress(token: String, mMap: Map<String, String>) = result { service.editAddress(token, mMap) }

    suspend fun getCheckOut(token: String) = service.getCheckOut(token)
    suspend fun getCheckOutDetails(token: String, mMap: Map<String, String>) = service.getCheckOutDetails(token, mMap)


    suspend fun setCurrentAddress(token: String, mMap: Map<String, String>) =
        service.setCurrentAddress(token, mMap)


    suspend fun couponApply(token: String, couponId: Int, couponBoolean: Boolean, checkoutId: String) =
        service.couponApply(token = token, couponId = couponId, couponBoolean = couponBoolean, checkoutId = checkoutId)

    suspend fun setWallet(token: String, checkWallet: Boolean, checkoutId: String) =
        service.setWallet(token = token, checkWallet  = checkWallet, checkoutId = checkoutId)

    suspend fun ordersList(token: String, mMap: Map<String, String>) = service.ordersList(token, mMap)

    suspend fun orderPlaced(
        token: String,
        cartRestaurantId: Int,
        checkoutId: Int,
        deliveryInstruction: String,
        cookingInstruction: String,
        deliveryDate: String,
        deliveryTime: String,
        paymentIntent:String,
        paymentStatus:String
    ) = service.orderPlaced(
        token,
        cartRestaurantId,
        checkoutId,
        deliveryInstruction,
        cookingInstruction,
        deliveryDate,
        deliveryTime,
        paymentIntent,
        paymentStatus
    )



    suspend fun cancelOrder(
        token: String, mMap: Map<String, String>
    ) = service.cancelOrder(token, mMap)





    suspend fun verifyDate(
        token: String, mMap: Map<String, String>
    ) = service.verifyDate(token, mMap)

    suspend fun getWalletList(
        token: String, mMap: Map<String, String>
    ) = service.getWalletList(token, mMap)
    suspend fun walletAmountAdd(
        token: String, amount: Int, paymentIntent:String, paymentStatus:String
    ) = service.walletAmountAdd(token, amount, paymentIntent, paymentStatus)

    suspend fun addCard(
        token: String, cardToken: String, cardNickname:String
    ) = service.addCard(token, cardToken, cardNickname)

    suspend fun getCardsList(
        token: String
    ) = service.getCardsList(token)

    suspend fun deleteCard(
        token: String, mMap: Map<String, String>
    ) = service.deleteCard(token, mMap)

    suspend fun stripeCustomer(
        token: String, mMap: Map<String, String>
    ) = service.stripeCustomer(token, mMap)

    suspend fun stripeCustomerWallet(
        token: String, mMap: Map<String, String>
    ) = service.stripeCustomerWallet(token, mMap)



    //Preet
    suspend fun getSettings(token: String) = result { service.getSettings(token) }

    suspend fun setSettings(token: String, mMap: Map<String, String>) = result { service.setSettings(token, mMap) }


    suspend fun getCoupons(token: String,restaurantId: String) = result { service.getCoupons(token = token, restaurantId = restaurantId) }

    suspend fun setCoupon(authToken:String,couponId: String,checkoutId: String,isCouponAdded:Boolean) = result {
        service.setCoupon(
            token = authToken,
            couponId = couponId,
            couponBoolean = isCouponAdded,
            checkoutId = checkoutId)

    }
    suspend fun orderDetails(token: String,orderId:String) = result { service.orderDetails(token, orderId) }
    suspend fun applyPromo(token: String,map: Map<String, String>) = result { service.applyPromo(token, params =map ) }
    suspend fun orderDetailWithTrack(token: String,orderId:String,type:String) = result { service.orderDetailWithTracking(token, orderId,type) }

    suspend fun getAllRestaurant(token: String, mMap: Map<String, String>) = result { service.getRestaurants(token = token, params = mMap) }
    suspend fun getAllRestaurantNew(token: String, mMap: Map<String, String>) = service.getRestaurants(token = token, params = mMap)

    suspend fun getMeMenuItem(token: String, mMap: Map<String, String>) = result { service.getMenuItem(token = token, params = mMap) }

    suspend fun getCompareFood(token: String, mMap: Map<String, String>) = result {  service.getCompareFood(token, mMap) }

    suspend fun setDeviceToken(token: String, mMap: Map<String, String>) = result { service.setDeviceToken(token, mMap) }

    suspend fun getCartItem(token: String) = result { service.getCartDataApi(token = token) }

    suspend fun updateCartItem(token: String, cartId: Int, quantity: Int) =  result {
        service.editCartItem(token, cartId, quantity) }




    suspend fun getCheckOutDetailsNew(token: String, mMap: Map<String, String>) = result { service.getCheckOutDetails(token = token, params = mMap) }


    suspend fun getCombo(token: String, mMap: Map<String, String>) = result { service.getComboMeal(token, mMap) }

    suspend fun isRestaurantOpen( token: String, mMap: Map<String, String>) = result2 {  service.verifyDate(token, mMap) }

    suspend fun  getCuisineRestaurant(token: String, mMap: Map<String, String>) = result {  service.getCuisinesRestaurant(token, mMap) }

    suspend fun getOfferRestaurant(token: String, map: Map<String, String>) = result { service.getRestaurantsOffersItem(token, map)  }

    suspend fun setBroadCast(token: String,json:JsonObject) = result { service.setBroadCast(token = token, params = json) }
    suspend fun setAction(token: String,json:JsonObject) = result { service.setAction(token = token, params = json) }

    suspend fun setStripe(token: String,map: Map<String, String>) = result { service. stripeCustomer(token = token, params = map) }

    suspend fun getUserAddress(token: String) = result {     service.getAddress(token) }


    suspend fun getUserCard( token: String) = result {  service.getCardsList(token) }

    suspend fun addUserCard( token: String, cardToken: String, cardNickname:String) = result { service.addUserCard(token = token, cardToken = cardToken, cardNickname = cardNickname) }

    suspend fun editCard(token: String,stripeToken:String,cardNickname: String) = result { service.editCard(token = token, cardToken = stripeToken, cardNickname = cardNickname) }

    suspend fun deleteUserCard(token: String, mMap: Map<String, String>) = result { service.deleteCard(token, mMap) }
    suspend fun deleteAllCartItemNew(token: String) = result {   service.deleteAllCartItem(token) }

    suspend fun getWallet(token: String,map: Map<String, String>) = result { service.getWalletList(token = token, params = map) }

    suspend fun editCartItem(token: String, cartId: Int, quantity: Int) = result { service.editCartItem(token, cartId, quantity) }

    suspend fun updateAddOn(token: String, cartId: Int, quantity: Int, addOns: List<Int>) = result { service.updateAddOn(token = token, cartId = cartId, quantity = quantity, addOns = addOns) }
    suspend fun updateAddOn2(token: String, addOnModel: AddOnsModel) = result { service.updateAddOn2(token = token, addOnModel) }

   suspend fun orderOutDeliver(token: String,jsonObject: JsonObject) = result { service.outOrderDeliver(token = token, params = jsonObject) }
   suspend fun sentReview(token: String,jsonObject: JsonObject) = result { service.sentReview(token = token, params = jsonObject) }

    suspend fun reOrder(token: String, mMap: Map<String, String>) = result {service.reOrder(token, mMap)  }

    suspend fun forceReOrder(token: String, mMap: Map<String, String>) = result { service.reOrder(token, mMap) }

    suspend fun  sendEmailOtp(token: String, map: Map<String, String>) = result { service.sendEmailOtp(token, map)  }
    suspend fun  updateNumber(token: String, map: Map<String, String>) = result { service.sendMobileOtp(token, map)  }

    suspend fun deleteMe(token: String,map: Map<String, String>) = result { service.deleteAccount(token = token,map) }

    suspend fun canceledOrder(token: String, mMap: Map<String, String>) = result {   service.cancelOrder(token, mMap) }

    suspend fun deleteAccountReason(token: String,reasonFor:String,reasonType:String) = result { service.deleteAccountReason(token =token , reasonFor =reasonFor , reasonType =reasonType ) }

    suspend fun setDeliveryType(token: String, mMap: Map<String, String>) = result { service.
    setDeliveryType(token = token,mMap) }

    suspend fun sendMobileOtp(token: String, map: Map<String, String>) = result {  service.sendMobileOtp(token, map) }

    suspend fun reSendMobileOtp(map: Map<String, String>) = result {  service.reSendMobileOtp(map) }

    suspend fun getDriverLocation(token: String,orderId: Int) = result { service.getDriverLocation(token = token, orderId =orderId) }
    suspend fun getDeliveryTime(orderId: Int) = result { service.
    getDeliveryTime( orderId =orderId,"PUSHER_EVENT") }


    suspend fun getTodayOrder(authToken: String,map: Map<String, String>) = result {   service.ordersList(token = authToken,map) }



    fun getRestaurants(authToken: String,queryParams: JsonObject): Pager<Int, AllRestaurant.Data> {
        return Pager(
            config = PagingConfig(pageSize = 20, maxSize = 100),
            pagingSourceFactory = { RestaurantPagingSource(service, authToken, queryParams) }
        )
    }
    fun getRestaurantsLiveData(authToken: String,queryParams: JsonObject) =Pager(
        config = PagingConfig(pageSize = 20, maxSize = 100),
        pagingSourceFactory = { RestaurantPagingSource(service, authToken, queryParams) } ).liveData

    fun getOfferRestaurantsPag(authToken: String,queryParams: JsonObject): Pager<Int, AllRestaurant.Data> {
        return Pager(
            config = PagingConfig(pageSize = 20, maxSize = 100),
            pagingSourceFactory = { OfferRestaurantPagingSource(service, authToken, queryParams) }
        )
    }

    fun getCuisinesPage(authToken: String,queryParams: JsonObject): Pager<Int, AllRestaurant.Data> {
        return Pager(
            config = PagingConfig(pageSize = 20, maxSize = 100),
            pagingSourceFactory = { CuisinesPagingSource(service, authToken, queryParams) }
        )
    }


    fun getComboPagination(authToken: String,queryParams: JsonObject): Pager<Int, ComboMeal.Data> {
        return Pager(
            config = PagingConfig(pageSize = 20, maxSize = 100),
            pagingSourceFactory = { ComboPaginationSource(service, authToken, queryParams) }
        )
    }

    fun getAppUpdate(deviceOs:String,appType:String,buildVersion:String) = result { service.appUpdate(deviceOs =deviceOs, appType = appType, version =buildVersion  ) }
    fun userLogout(authToken: String,json: JsonObject) = result { service.userLogOut(token = authToken, params = json ) }



}

