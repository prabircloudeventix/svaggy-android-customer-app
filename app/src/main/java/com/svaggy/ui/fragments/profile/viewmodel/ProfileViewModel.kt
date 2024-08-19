package com.svaggy.ui.fragments.profile.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.svaggy.client.models.CommonModel
import com.svaggy.client.service.ApiManager
import com.svaggy.client.service.SingleLiveEvent
import com.svaggy.ui.fragments.profile.model.Settings
import com.svaggy.utils.PrefUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ProfileViewModel @Inject constructor(private val apiManager: ApiManager) : ViewModel() {
    var prefUtils: PrefUtils? = null
    private val context: Context? = null

    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    val editProfileDataSingle = SingleLiveEvent<EditProfile>()
    val getSettingsDataSingle = SingleLiveEvent<Settings>()
    val setSettingsDataSingle = SingleLiveEvent<CommonModel>()

    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }

    fun editProfile(token: String, firstName: String, lastName: String) {
        val mMap = HashMap<String, String>()
        mMap["first_name"] = firstName
        mMap["last_name"] = lastName
        viewModelScope.launch {
            try {
                val response = apiManager.editProfile(token,mMap)
                if (response.isSuccessful) {
                    if (response.body()!!.isSuccess != null) {
                        editProfileDataSingle.postValue(response.body()!!)
                    }
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            }catch (e:Exception){
            }
        }
    }
    //Preet
  suspend fun getSettings(authToken:String) = apiManager.getSettings(token = authToken)

  suspend  fun setSettings(authToken: String, map: Map<String,String>) = apiManager.setSettings(authToken,map)

    suspend fun deleteAccountReason(authToken: String,reasonFor:String,reasonType:String) = apiManager.deleteAccountReason(token = authToken, reasonFor =reasonFor , reasonType =reasonType)
    suspend fun deleteMe(authToken: String,map: Map<String, String>) = apiManager.deleteMe(token = authToken, map = map)
}