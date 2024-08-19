package com.svaggy.ui.fragments.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.google.gson.JsonObject
import com.svaggy.client.service.ApiManager
import com.svaggy.ui.fragments.home.model.AllRestaurant
import com.svaggy.ui.fragments.home.model.GetCuisines
import com.svaggy.ui.fragments.home.model.Search
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val apiManager: ApiManager) : ViewModel() {


    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    val getCuisinesMutable = MutableLiveData<GetCuisines>()
    val getCuisinesLiveData: LiveData<GetCuisines> = getCuisinesMutable
    val getSearchDataMutable = MutableLiveData<Search>()




    private val _allResData = MutableLiveData<AllRestaurant>()
    val allResData: LiveData<AllRestaurant> get() = _allResData












    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }

    fun getCuisines(token: String) {
        viewModelScope.launch {
            try {
                val response = apiManager.getCuisines(token)
                if (response.isSuccessful) {
                    if (response.body()?.isSuccess!!) {
                        getCuisinesMutable.postValue(response.body())
                    } else {
                        onError("Error : ${response.message()} ")
                    }
                } else {
                    onError("Error : ${response.message()} ")
                }
            } catch (e: Exception) {
            }

        }
    }



    fun getPopularCuisines(token: String) {
        viewModelScope.launch {
            try {
                val response = apiManager.getPopularCuisines(token)
                if (response.isSuccessful) {
                    if (response.body()?.isSuccess!!) {
                        getCuisinesMutable.postValue(response.body())
                    } else {
                        onError("Error : ${response.message()} ")
                    }
                } else {
                    onError("Error : ${response.message()} ")
                }
            } catch (e: Exception) {
            }

        }
    }

    fun searchData(token: String, searchText: String, latitude: String, longitude: String) {
        val mMap = HashMap<String, String>()
        mMap["search_text"] = searchText
        mMap["latitude"] = latitude
        mMap["longitude"] = longitude
        viewModelScope.launch {
            try {
                val response = apiManager.searchData(token, mMap)
                if (response.isSuccessful) {
                    getSearchDataMutable.postValue(response.body()!!)
                } else if (!response.isSuccessful) {
                    onError("Error : ${response.message()} ")
                }
            } catch (e: Exception) {
            }
        }
    }

    //Preet



suspend fun getMeMenuItem(token: String,map: Map<String, String>) = apiManager.getMeMenuItem(token = token, mMap = map)


    suspend fun  getCuisineRestaurant(token: String, mMap: Map<String, String>) = apiManager.getCuisineRestaurant(token = token, mMap = mMap)

    suspend fun getOfferRestaurant(token: String, mMap: Map<String, String>) = apiManager.getOfferRestaurant(token = token, map = mMap)

    suspend fun setBroadCast(token: String,jsonObject: JsonObject) = apiManager.setBroadCast(token = token, json =  jsonObject)

    suspend fun setActionBooster(token: String,jsonObject: JsonObject) = apiManager.setAction(token = token, json = jsonObject)


    fun getRestaurantsPagination(authToken:String,queryParams: JsonObject): Flow<PagingData<AllRestaurant.Data>> {
        return apiManager.getRestaurants(authToken,queryParams).flow.cachedIn(viewModelScope)
    }

    fun getOfferRestaurantsPagination(authToken:String,queryParams: JsonObject): Flow<PagingData<AllRestaurant.Data>> {
        return apiManager.getOfferRestaurantsPag(authToken,queryParams).flow.cachedIn(viewModelScope)
    }

    fun getCuisinesPagination(authToken:String,queryParams: JsonObject): Flow<PagingData<AllRestaurant.Data>> {
        return apiManager.getCuisinesPage(authToken,queryParams).flow.cachedIn(viewModelScope)
    }

    suspend fun getCartItem(token: String) = apiManager.getCartItem(token = token)



}