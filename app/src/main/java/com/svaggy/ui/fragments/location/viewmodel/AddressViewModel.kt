package com.svaggy.ui.fragments.location.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svaggy.client.models.CommonModel
import com.svaggy.client.service.ApiManager
import com.svaggy.client.service.SingleLiveEvent
import com.svaggy.ui.fragments.location.address.model.AddAddress
import com.svaggy.ui.fragments.location.model.GetAddress
import com.svaggy.utils.PrefUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class AddressViewModel @Inject constructor(private val apiManager: ApiManager) : ViewModel() {
    var prefUtils: PrefUtils? = null

    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    private val getAddressDataMutable = MutableLiveData<GetAddress>()
    val getAddressDataLive: LiveData<GetAddress> = getAddressDataMutable

    private val deleteAddressDataMutable = MutableLiveData<CommonModel>()
    val deleteAddressDataLive: LiveData<CommonModel> = deleteAddressDataMutable

    val addAddressDataSingle = SingleLiveEvent<AddAddress>()
//    val addAddressDataLive: LiveData<AddAddress> = addAddressDataMutable

    val editAddressDataSingle = SingleLiveEvent<AddAddress>()
//    val editAddressDataLive: LiveData<AddAddress> = editAddressDataMutable

    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }

    fun getAddress(token: String) {
        viewModelScope.launch {
            try {
                val response = apiManager.getAddress(token)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        getAddressDataMutable.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            }catch (e:Exception){
            }
        }
    }

    fun deleteAddress(token: String, address_id: Int) {
        val mMap = HashMap<String, String>()
        mMap["address_id"] = address_id.toString()

        viewModelScope.launch {
            try {
                val response = apiManager.deleteAddress(token,mMap)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        deleteAddressDataMutable.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            }catch (e:Exception){
            }
        }
    }

    fun addAddress(token: String, isForSelf: Boolean, isDefault: Boolean, recipientName: String, phoneNumber: String, residenceInfo: String, regionInfo: String,
                   postalCode: String, city: String, latitude: String, longitude: String, addressType: String) {
        val mMap = HashMap<String, String>()
        mMap["is_for_self"] = isForSelf.toString()
        mMap["is_default"] = isDefault.toString()
        mMap["recipient_name"] = recipientName
        mMap["phone_number"] = phoneNumber
        mMap["residence_info"] = residenceInfo
        mMap["region_info"] = regionInfo
        mMap["postal_code"] = postalCode
        mMap["city"] = city
        mMap["latitude"] = latitude
        mMap["longitude"] = longitude
        mMap["address_type"] = addressType
        viewModelScope.launch {
            try {
                val response = apiManager.addAddress(token,mMap)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        addAddressDataSingle.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            }catch (e:Exception){
            }
        }
    }

    fun editAddress(token: String, addressId: Int, isForSelf: Boolean, isDefault: Boolean, recipientName: String, phoneNumber: String, residenceInfo: String, regionInfo: String,
                    postalCode: String, city: String, latitude: String, longitude: String, addressType: String, isEditFromDefault:Boolean) {
        val mMap = HashMap<String, String>()

        if (isEditFromDefault){
            mMap["id"] = addressId.toString()
            mMap["is_default"] = isDefault.toString()
        }
        else
        {
            mMap["id"] = addressId.toString()
            mMap["is_for_self"] = isForSelf.toString()
            mMap["recipient_name"] = recipientName
            mMap["phone_number"] = phoneNumber
            mMap["residence_info"] = residenceInfo
            mMap["region_info"] = regionInfo
            mMap["postal_code"] = postalCode
            mMap["city"] = city
            mMap["latitude"] = latitude
            mMap["longitude"] = longitude
            mMap["address_type"] = addressType
        }

        viewModelScope.launch {
            try {
                val response = apiManager.editAddress(token,mMap)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        editAddressDataSingle.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            }catch (e:Exception){
            }
        }
    }


    suspend fun addUserAddress(token: String,map: Map<String,String>) = apiManager.addUserAddress(token,map)
    suspend fun editUserAddress(token: String,map: Map<String,String>) = apiManager.editUserAddress(token,map)
}