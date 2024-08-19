package com.svaggy.ui.fragments.cart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.svaggy.client.models.AddOnsModel
import com.svaggy.client.models.CommonModel
import com.svaggy.client.service.ApiManager
import com.svaggy.client.service.SingleLiveEvent
import com.svaggy.ui.fragments.cart.model.AddCart
import com.svaggy.ui.fragments.cart.model.CartCheckout
import com.svaggy.ui.fragments.cart.model.GetCoupons
import com.svaggy.ui.fragments.cart.model.ViewCart
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(private val apiManager: ApiManager) : ViewModel() {

    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()


    private val getCartDataMutable = MutableLiveData<ViewCart>()
    val getCartDataLive: LiveData<ViewCart> = getCartDataMutable

    private val editCartDataMutable = MutableLiveData<CommonModel>()
    val editCartDataLive: LiveData<CommonModel> = editCartDataMutable

    private val deleteCartItemDataMutable = MutableLiveData<CommonModel>()
    val deleteCartItemDataLive: LiveData<CommonModel> = deleteCartItemDataMutable

    private val deleteAllCartDataMutable = MutableLiveData<CommonModel>()
    val deleteAllCartDataLive: LiveData<CommonModel> = deleteAllCartDataMutable

    private val getCheckOutDataMutable = MutableLiveData<CartCheckout>()
    val getCheckOutDataLive: LiveData<CartCheckout> = getCheckOutDataMutable

    private val setCurrentAddress = MutableLiveData<CommonModel>()
    val setCurrentAddressDataLive: LiveData<CommonModel> = setCurrentAddress

    private val setOffersMutLive = MutableLiveData<CommonModel>()
    val setOffersDataLive: LiveData<CommonModel> = setOffersMutLive

    private val couponApplyMutLive = MutableLiveData<CommonModel>()
    val couponApplyDataLive: LiveData<CommonModel> = couponApplyMutLive

    val verifyDateDataLive = SingleLiveEvent<CommonModel>()
    //val verifyDateDataLive: LiveData<CommonModel> = verifyDateMutable

    val getCouponsDataMutable = MutableLiveData<GetCoupons>()

    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }


    private val _response = MutableLiveData<AddCart>()
    val response: LiveData<AddCart> get() = _response

    fun placeOrder(
        token: String,
        restaurantId: Int,
        menuItemId: Int,
        quantity: Int,
        addOns: List<Int>
    ) {
        viewModelScope.launch {
            try {
                val result =
                    apiManager.placeOrder(token, restaurantId, menuItemId, quantity, addOns)
                if (result.isSuccessful) {
                    _response.value = result.body()
                } else {
                    // Handle error
                }
            } catch (e: Exception) {
                // Handle network or other errors
            }
        }
    }

    fun getAppUpdate(deviceOs:String,appType:String,buildVersion:String) =
        apiManager.getAppUpdate(deviceOs =deviceOs, appType = appType, buildVersion = buildVersion  )


    fun getCartData(token: String) {
        viewModelScope.launch {
            try {
                val response = apiManager.getCartDataFn(token)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        getCartDataMutable.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setCurrentAddress(token: String, addressGet: Int) {
        val mMap = HashMap<String, String>()
        mMap["address_id"] = addressGet.toString()
        viewModelScope.launch {
            try {
                val response = apiManager.setCurrentAddress(token, mMap)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        setCurrentAddress.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            } catch (e: Exception) {
            }
        }
    }



    fun setWallet(
        token: String,
        checkWallet: Boolean,
        checkoutId: String
    ) {
        viewModelScope.launch {
            try {
                val response = apiManager.setWallet(
                    token = token,
                   checkWallet  = checkWallet,
                    checkoutId = checkoutId
                )
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        setOffersMutLive.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("setCoupon Error: ${response.message()}")
                }
            } catch (e: Exception) {
            }
        }
    }

    fun couponApply(
        token: String,
        couponId: Int,
        couponBoolean: Boolean,
        hitType: String,
        checkoutId: String
    ) {
        viewModelScope.launch {
            try {
                val response = apiManager.couponApply(token, couponId, couponBoolean, checkoutId)

                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        couponApplyMutLive.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            } catch (e: Exception) {
            }
        }
    }



    // Preet
    suspend fun getCoupon(authToken:String,restaurantId:String) = apiManager.getCoupons(token = authToken, restaurantId = restaurantId)

    suspend fun setCoupon(authToken:String,couponId: String,checkoutId: String,isCouponAdded:Boolean) =
        apiManager.setCoupon(
            authToken = authToken,
            couponId = couponId,
            isCouponAdded = isCouponAdded,
            checkoutId = checkoutId)

    suspend fun applyPromo(token: String,map: Map<String, String>) = apiManager.applyPromo(token,map )


    suspend fun getCartItem(token: String) = apiManager.getCartItem(token = token)

    suspend fun updateCartItem (token: String, cartId: Int, quantity: Int)=
        apiManager.updateCartItem(token = token, cartId = cartId, quantity = quantity)
    suspend fun getCheckOutDetailsNew(token: String, map: Map<String,String>)  = apiManager.getCheckOutDetailsNew(token = token, mMap = map)

    suspend fun isRestaurantOpen(token: String,map: Map<String, String>) = apiManager.isRestaurantOpen(token = token, mMap = map)

    suspend fun getUserAddress(token: String) = apiManager.getUserAddress(token = token)

    suspend fun deleteAllCartItem(token: String) = apiManager.deleteAllCartItemNew(token = token)

    suspend fun editCart(token: String,itemId:Int,quantity: Int) = apiManager.editCartItem(token = token, cartId =itemId, quantity = quantity )
    suspend fun updateAddOn(token: String, cartId: Int, quantity: Int, addOns: List<Int>) = apiManager.updateAddOn(token = token, cartId = cartId, quantity = quantity, addOns = addOns)
    suspend fun updateAddOn2(token: String,addOnModel: AddOnsModel) = apiManager.updateAddOn2(token = token, addOnModel)

    suspend fun setDeliveryType(token: String,map: Map<String, String>) = apiManager.setDeliveryType(token = token, mMap = map)
     fun userLogout(authToken: String,json: JsonObject) =   apiManager.userLogout(authToken = authToken, json = json)




}


