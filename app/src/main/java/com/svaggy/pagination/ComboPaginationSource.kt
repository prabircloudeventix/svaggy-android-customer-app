package com.svaggy.pagination

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.JsonObject
import com.svaggy.client.service.ApiService
import com.svaggy.ui.fragments.banner.model.ComboMeal

class ComboPaginationSource (private val apiService: ApiService,
                                   private val token: String,
                                   private val json: JsonObject
) : PagingSource<Int, ComboMeal.Data>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ComboMeal.Data> {



        return try {
            val page = params.key ?: 1
            json.addProperty("page",page)
            val response = apiService.getComboMealPagination(token, json)
            if (response.isSuccessful) {
                val data = response.body()
                val totalPages = data?.totalPages ?: 0
                val restaurants = response.body()?.data ?: emptyList()
                //   restaurantDao.insertAll(restaurants)
                PagingSource.LoadResult.Page(
                    data = restaurants,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (page >= totalPages) null else page + 1
                )
            } else {
                PagingSource.LoadResult.Error(Exception("API call failed"))
            }
        } catch (e: Exception) {
            PagingSource.LoadResult.Error(e)
        }
    }






    override fun getRefreshKey(state: PagingState<Int, ComboMeal.Data>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

}
