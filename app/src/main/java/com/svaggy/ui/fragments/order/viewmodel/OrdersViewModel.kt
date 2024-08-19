package com.svaggy.ui.fragments.order.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.svaggy.client.models.CommonModel
import com.svaggy.client.service.ApiManager
import com.svaggy.ui.fragments.order.model.orderbyid.OrderById
import com.svaggy.ui.fragments.order.model.Orders
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class OrdersViewModel @Inject constructor(private val apiManager: ApiManager) : ViewModel() {

    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    val getOrdersListDataMutable = MutableLiveData<Orders>()
    val getOrderDetailsDataMutable = MutableLiveData<OrderById>()
    val cancelOrderDataMutable = MutableLiveData<CommonModel>()
    val reOrderDataMutable = MutableLiveData<CommonModel>()

    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }

    fun ordersList(token: String, type: String
    ){
        val mMap = HashMap<String, String>()
        mMap["type"] = type
        viewModelScope.launch {
            try {
                val response = apiManager.ordersList(token,mMap)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        getOrdersListDataMutable.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            }catch (e:Exception){
            }
        }
    }

    fun cancelOrder(token: String, orderId: String
    ){
        val mMap = HashMap<String, String>()
        mMap["order_id"] = orderId
        viewModelScope.launch {
            try {
                val response = apiManager.cancelOrder(token,mMap)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess!= null) {
                        cancelOrderDataMutable.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            }catch (e:Exception){
            }
        }
    }

//    fun reOrder(token: String, orderId: String){
//        val mMap = HashMap<String, String>()
//        mMap["order_id"] = orderId
//        viewModelScope.launch {
//            try {
//                val response = apiManager.reOrder(token,mMap)
//                if (response.isSuccessful) {
//                    if (response.body()!!.isSuccess!= null) {
//                        reOrderDataMutable.postValue(response.body()!!)
//                    }
//                } else if (!response.isSuccessful) {
//                    onError("Error : ${response.message()} ")
//                }
//            }catch (e:Exception){
//            }
//        }
//    }
    suspend  fun orderDetails(token: String, orderId: String) = apiManager.orderDetails(token = token, orderId = orderId)
    suspend  fun orderDetailsWithTrack(token: String, orderId: String,type:String) = apiManager.orderDetailWithTrack(token = token, orderId = orderId,type)

    suspend fun orderOutDelivery(token: String,jsonObject: JsonObject) = apiManager.orderOutDeliver(token = token, jsonObject = jsonObject)

    suspend fun reOrder(token: String,map: Map<String, String>) = apiManager.reOrder(token = token, mMap = map)

    suspend fun forceReOrder(token: String,map: Map<String,String>) = apiManager.forceReOrder(token = token, mMap = map)

    suspend fun canceledOrder(token: String,map: Map<String, String>) = apiManager.canceledOrder(token = token, mMap = map)

    suspend fun deleteAccountReason(authToken: String,reasonFor:String,reasonType:String) = apiManager.deleteAccountReason(token = authToken, reasonFor =reasonFor , reasonType =reasonType)

    suspend fun getDriverDetails(token: String,orderId: Int) = apiManager.getDriverLocation(token = token, orderId =orderId )
    suspend fun getDeliveryTime(orderId: Int) = apiManager.getDeliveryTime(orderId =orderId )
    suspend fun getTodayOrder(authToken: String,map: Map<String, String>) = apiManager.getTodayOrder(authToken, map)




}