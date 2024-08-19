package com.svaggy.ui.fragments.editAuth.viewmodel

import android.content.Context
import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svaggy.client.models.CommonModel
import com.svaggy.client.models.DeviceToken
import com.svaggy.ui.fragments.editAuth.model.SendOtp
import com.svaggy.ui.fragments.editAuth.model.UserDetail
import com.svaggy.ui.activities.savePref.UserPreference
import com.svaggy.client.service.ApiManager
import com.svaggy.client.service.SingleLiveEvent
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val apiManager: ApiManager) : ViewModel() {
    var prefUtils: PrefUtils? = null

    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    private val getCommonMutable = MutableLiveData<CommonModel>()
    val getCommonLiveData: LiveData<CommonModel> = getCommonMutable

    val otpResponseLiveData = SingleLiveEvent<UserDetail>()
    //val otpResponseLiveData: LiveData<UserDetail> = otpResponseMutable

    val sendMobileOtpLiveData = SingleLiveEvent<SendOtp>()
    //val sendMobileOtpLiveData: LiveData<SendOtp> = sendMobileOtpMutable

    val sendEmailOtpLiveData = SingleLiveEvent<SendOtp>()
    //val sendEmailOtpLiveData: LiveData<SendOtp> = sendEmailOtpMutable

    private val getPreferenceMutable = MutableLiveData<UserPreference>()
    val getPreferenceLiveData: LiveData<UserPreference> = getPreferenceMutable

    val getDeviceTokenMutable = MutableLiveData<DeviceToken>()

    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }

//    fun sendMobileOtp(token: String, mobileNumber: String) {
//        val mMap = HashMap<String, String>()
//        mMap["mobileNumber"] = mobileNumber
//        viewModelScope.launch {
//            try {
//                val response = apiManager.sendMobileOtp(token,mMap)
//                if (response.isSuccessful) {
//                    if (response.body()!!.isSuccess!!) {
//                        sendMobileOtpLiveData.postValue(response.body()!!)
//                    }
//                } else if (!response.isSuccessful) {
//                    prefUtils?.alertDialog(context,
//                        "",
//                        response.message().toString(),
//                        okAlert = { it.dismiss() },
//                        cancelAlert = { it.dismiss() },
//                        false
//                    )
////                    onError("Error : ${response.message()} ")
//                }
//            } catch (e: Exception) {
//                prefUtils?.alertDialog(
//                    context,
//                    "",
//                    e.toString(),
//                    okAlert = { it.dismiss() },
//                    cancelAlert = { it.dismiss() },
//                    false
//                )
//            }
//        }
//    }
    fun reSendMobileNewOtp(mobileNumber: String) {
        val mMap = HashMap<String, String>()
        mMap["mobileNumber"] = mobileNumber
        viewModelScope.launch {
            val response = apiManager.reSendMobileNewOtp(mMap)
            if (response.isSuccessful) {
                if (response.body()!!.isSuccess!!) {
                    sendMobileOtpLiveData.postValue(response.body()!!)
                }
            } else if (!response.isSuccessful) {
                onError("Error : ${response.message()} ")
            }
        }
    }



    fun reSendEmailOtp(email: String) {
        val mMap = HashMap<String, String>()
        mMap["email"] = email
        viewModelScope.launch {
            val response = apiManager.reSendEmailOtp(mMap)
            if (response.isSuccessful) {
                if (response.body()!!.isSuccess!!) {
                    sendEmailOtpLiveData.postValue(response.body()!!)
                }
            } else if (!response.isSuccessful) {
                onError("Error : ${response.message()} ")
            }
        }
    }

    fun verifyOtp(token: String, otpString: String, type: String, emailId: String) {
        val mMap = HashMap<String, String>()
        mMap["otp"] = otpString
        mMap["type"] = type
        mMap["email_or_phone"] = emailId
        viewModelScope.launch{
            val response = apiManager.verifyOtp(token,mMap)

            if (response.code()==200){
                otpResponseLiveData.postValue(response.body()!!)
            }
            else if (response.code()==400){
                onError("Error : Incorrect otp !! ")
            }
            else if (response.code()==500){
                onError("Error : Internal Server Error !! ")
            }
        }
    }

    fun socialLogin(
        email: String,
        firstName: String,
        lastName: String,
        deviceToken: String,
        type: String
    ) {
        val mMap = HashMap<String, String>()
        mMap["email"] = email
        mMap["first_name"] = firstName
        mMap["last_name"] = lastName
        mMap["device_token"] = deviceToken
        mMap["type"] = type
        viewModelScope.launch {
            val response = apiManager.socialLogin(mMap)

            if (response.isSuccessful) {
                if (response.body()!!.isSuccess!!) {
                    otpResponseLiveData.postValue(response.body()!!)
                }
            } else if (!response.isSuccessful) {
                onError("Error : ${response.message()} ")
            }
        }
    }
    fun getPreferences(token: String) {
        viewModelScope.launch {
            try {
                val response = apiManager.getPreferences(token)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess!!) {
                        getPreferenceMutable.postValue(response.body()!!)
                    }
                } else {
                    onError("Error : ${response.message()} ")
                }

            }catch (e:Exception){

            }

        }
    }

  //  suspend fun guestPrefList() = apiManager.guestGetPreferences()

    fun setPreferences(token: String, preferences: ArrayList<Int>) {
        viewModelScope.launch {
            val response = apiManager.setPreferences(token, preferences)

            if (response.isSuccessful) {
                if (response.body()!!.isSuccess!!) {
                    getCommonMutable.postValue(response.body()!!)
                } else {
                    onError("Error : ${response.message()} ")
                }
            } else if (!response.isSuccessful) {
                onError("Error : ${response.message()} ")
            }
        }
    }

    suspend fun setGuestPreferences(deviceId: String, preferences: ArrayList<Int>) = apiManager.guestSetPreferences(token = deviceId, preferences = preferences)

    fun loginValidation(email: String): Pair<Boolean, String> {
        var result = Pair(true, "")
        if (TextUtils.isEmpty(email)) {
            result = Pair(false, "Please Provide Email address")
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            result = Pair(false, "Please Provide valid email")
        }
        return result
    }

    //

    suspend fun setToken(token: String,map: Map<String,String>) = apiManager.setDeviceToken(token = token, mMap = map)
    suspend fun updateEmail(token: String,map: Map<String, String>) = apiManager.sendEmailOtp(token = token, map = map)
    suspend fun updatePhone(token: String,map: Map<String, String>) = apiManager.updateNumber(token = token, map = map)

   suspend fun sendMobileOtp(token: String, map: Map<String, String>) = apiManager.sendMobileOtp(token = token, map =map )
    suspend fun reSendMobileOtp(map: Map<String, String>) =  apiManager.reSendMobileOtp(map)

}

