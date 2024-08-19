package com.svaggy.ui.fragments.banner.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.gson.JsonObject
import com.svaggy.client.service.ApiManager
import com.svaggy.client.service.SingleLiveEvent
import com.svaggy.ui.fragments.banner.model.ComboMeal
import com.svaggy.ui.fragments.banner.model.CompareFood
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BannerViewModel @Inject constructor(private val apiManager: ApiManager) : ViewModel() {
    var prefUtils: PrefUtils? = null
    private val context: Context? = null

    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    val getComboDataSingle = SingleLiveEvent<ComboMeal>()
    val getComboDataMutable = MutableLiveData<ComboMeal>()
    val getCompareDataMutable = MutableLiveData<CompareFood>()

    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }

    fun getComboMeal(token: String, latitude: String, longitude: String, filter_text: String,
                     top_rated: String, is_veg: String, is_non_veg: String, is_vegan: String){
        val mMap = HashMap<String, String>()
        mMap["latitude"] = latitude
        mMap["longitude"] = longitude
        mMap["filter_text"] = filter_text
        mMap["top_rated"] = top_rated
        mMap["is_veg"] = is_veg
        mMap["is_non_veg"] = is_non_veg
        mMap["is_vegan"] = is_vegan
        viewModelScope.launch {
//            try {
//                val response = apiManager.getComboMeal(token,mMap)
//                if (response.isSuccessful) {
//                    if (response.body()!!.isSuccess != null) {
//                        getComboDataMutable.postValue(response.body()!!)
//                    }
//                } else if (!response.isSuccessful) {
//                    onError("Error : ${response.message()} ")
//                }
//            }catch
//                (e:Exception){
//            }
        }
    }



//Preet
    suspend fun getCompareFood(token: String,map: Map<String,String>) = apiManager.getCompareFood(token = token, mMap = map)

    suspend fun getCombo(token: String,map: Map<String, String>) = apiManager.getCombo(token = token, mMap = map)


    fun getComboPagination(authToken:String,queryParams: JsonObject): Flow<PagingData<ComboMeal.Data>> {
        return apiManager.getComboPagination(authToken,queryParams).flow.cachedIn(viewModelScope)
    }




}