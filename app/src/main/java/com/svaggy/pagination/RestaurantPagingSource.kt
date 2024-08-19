package com.svaggy.pagination

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.svaggy.client.service.ApiService
import com.svaggy.ui.fragments.home.model.AllRestaurant
import com.svaggy.utils.Constants

class RestaurantPagingSource(
    private val apiService: ApiService,
   // private val restaurantDao: RestaurantDao,
    private val token: String,
    private val json: JsonObject
) : PagingSource<Int, AllRestaurant.Data>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AllRestaurant.Data> {
        val page = params.key ?: 1

        //params.loadSize, page,

       // json.addProperty("page_size",params.loadSize)
        json.addProperty("page",page)



        return try {
            val response = apiService.getRestaurantsNew(token, json)
            if (response.isSuccessful) {
                val data = response.body()
                val totalPages = data?.totalPages ?: 0
                val restaurants = response.body()?.data ?: emptyList()
                Constants.isInitBottomNav = data?.is_show_promo_banner ?: true
                LoadResult.Page(
                    data = restaurants,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (page >= totalPages) null else page + 1
                )
            } else {
                LoadResult.Error(Exception("API call failed"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }catch (e: JsonSyntaxException) {
            LoadResult.Error(e)
        }
    }






    override fun getRefreshKey(state: PagingState<Int, AllRestaurant.Data>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

}

