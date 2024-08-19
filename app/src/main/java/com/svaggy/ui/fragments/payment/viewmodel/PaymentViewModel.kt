package com.svaggy.ui.fragments.payment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.svaggy.client.models.CommonModel
import com.svaggy.client.service.ApiManager
import com.svaggy.client.service.SingleLiveEvent
import com.svaggy.ui.fragments.order.model.order_placed.OrderPlacedModel
import com.svaggy.ui.fragments.payment.model.GetCard
import com.svaggy.ui.fragments.wallet.models.AddPayCardModel
import com.svaggy.utils.PrefUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject
@HiltViewModel
class PaymentViewModel @Inject constructor(private val apiManager: ApiManager) : ViewModel() {
    var prefUtils: PrefUtils? = null
    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    private val orderPlaceMutable = MutableLiveData<OrderPlacedModel>()
    val addCartDataLive: LiveData<OrderPlacedModel> = orderPlaceMutable
    //
    val addCardData = SingleLiveEvent<AddPayCardModel>()
    val getCardListData = SingleLiveEvent<GetCard>()
    val deleteCardData = SingleLiveEvent<CommonModel>()

    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }

    fun orderPlaced(
        token: String,
        cartRestaurantId: Int,
        checkout_id: Int,
        delivery_instruction: String,
        cooking_instruction: String,
        delivery_date: String,
        deliveryTime: String,
        paymentIntent: String,
        paymentStatus: String,
        type:String){

        viewModelScope.launch {
            try {
                val response = if (type == "Schedule"){
                    apiManager.orderPlaced(token,
                        cartRestaurantId,checkout_id,delivery_instruction,
                        cooking_instruction,delivery_date, deliveryTime,
                        paymentIntent, paymentStatus)

                }else{
                    apiManager.orderPlaced(token,
                        cartRestaurantId,checkout_id,delivery_instruction,
                        cooking_instruction,delivery_date, "",
                        paymentIntent, paymentStatus)
                }

                if (response.isSuccessful) {
                    orderPlaceMutable.postValue(response.body()!!)

                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            }catch (e:Exception){
            }
        }
    }

    fun addCard(token: String, cardToken: String, cardNickname:String){
        viewModelScope.launch {
            try {
                val response = apiManager.addCard(token,cardToken, cardNickname)
                if (response.isSuccessful) {
                    addCardData.postValue(response.body())

                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun getCardsList(
        token: String
    ){
        viewModelScope.launch {
            try {
                val response = apiManager.getCardsList(token)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        getCardListData.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            }catch (e:Exception){
                e.printStackTrace()

            }
        }
    }

    fun deleteCard(
        token: String, cardId:String
    ){
        val mMap = HashMap<String, String>()
        mMap["card_id"] = cardId
        viewModelScope.launch {
            try {
                val response = apiManager.deleteCard(token,mMap)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        deleteCardData.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            }catch (e:Exception){
                e.printStackTrace()

            }
        }
    }


    //preet
    suspend fun setConversion(token: String,jsonObject: JsonObject) = apiManager.setBroadCast(token = token, json = jsonObject)
    suspend fun setBoosterConversion(token: String,jsonObject: JsonObject) = apiManager.setAction(token = token, json = jsonObject)

    suspend fun setStripePayment(token: String,map: Map<String,String>) = apiManager.setStripe(token =token, map = map )

    suspend fun getUserCard (token: String) = apiManager.getUserCard(token)

    suspend fun editCard(token: String,stripeToken:String,cardNickname: String) = apiManager.editCard(token = token, stripeToken= stripeToken, cardNickname = cardNickname)
    suspend fun deleteUserCard(token: String,map: Map<String, String>) = apiManager.deleteUserCard(token = token, mMap = map)

    suspend fun addUserCard(token: String, cardToken: String, cardNickname:String) = apiManager.addUserCard(token = token, cardToken = cardToken, cardNickname = cardNickname    )
    suspend fun sentReview(token: String,jsonObject: JsonObject) = apiManager.sentReview(token = token, jsonObject = jsonObject)


}