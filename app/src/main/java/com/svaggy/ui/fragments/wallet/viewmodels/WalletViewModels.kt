package com.svaggy.ui.fragments.wallet.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svaggy.client.service.ApiManager
import com.svaggy.client.service.SingleLiveEvent
import com.svaggy.ui.fragments.payment.model.StripeCustomer
import com.svaggy.ui.fragments.wallet.models.WalletList
import com.svaggy.ui.fragments.wallet.models.WalletModel
import com.svaggy.utils.PrefUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModels @Inject constructor(private val apiManager: ApiManager) : ViewModel() {
    var prefUtils: PrefUtils? = null

    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    val getWalletListMutable = SingleLiveEvent<WalletList>()
    val walletAmountAddMutable = SingleLiveEvent<WalletModel>()
    val stripeCustomerWalletData = SingleLiveEvent<StripeCustomer>()
    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }

    fun getWalletList(token: String, type: String) {
        val mMap = HashMap<String, String>()
        mMap["type"] = type
        viewModelScope.launch {
            try {
                val response = apiManager.getWalletList(token,mMap)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        getWalletListMutable.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            }catch (e:Exception){
            }
        }
    }
    fun walletAmountAdd(token: String, amount: String, paymentIntent:String, paymentStatus:String) {
        /*val mMap = HashMap<String, String>()
        mMap["amount"] = amount*/
        viewModelScope.launch {
            try {
                val response = apiManager.walletAmountAdd(token,amount.toInt(), paymentIntent, paymentStatus)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        walletAmountAddMutable.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            }catch (e:Exception){
            }
        }
    }

    fun stripeCustomerWallet(
        token: String, amount:String
    ){
        val mMap = HashMap<String, String>()
        mMap["amount"] = amount
        viewModelScope.launch {
            try {
                val response = apiManager.stripeCustomerWallet(token,mMap)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        stripeCustomerWalletData.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            }catch (e:Exception){
            }
        }
    }

    suspend fun getWallet(token: String,map: Map<String,String>) = apiManager.getWallet(token = token, map = map)

}