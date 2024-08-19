package com.svaggy.utils

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.ui.fragments.home.model.AllRestaurant
import com.svaggy.ui.fragments.home.adapter.CuisinesContryWiseAdapter
import java.util.ArrayList

/**
 * Generic [RecyclerView.ViewHolder] to be used in [RecyclerView.Adapter] and Data Binding.
 */
class BindingHolder<out T : ViewDataBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        recyclerCuisinesCountry: RecyclerView,
        restaurantCuisines: ArrayList<AllRestaurant.Data.RestaurantCuisines>?
    ) {

        val childMembersAdapter = restaurantCuisines?.let { CuisinesContryWiseAdapter(it) }
        recyclerCuisinesCountry.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL,false)
        recyclerCuisinesCountry.adapter = childMembersAdapter
    }

    /*fun funCategoryName(
        context: Context,
        recyclerCategoryItem: RecyclerView,
        arrayList: ArrayList<RestaurantsMenuItem.Data.Categories.MenuItems>?
    ) {
        val childMembersAdapter = arrayList?.let { MenuItemAdapter(context,it) }
        //recyclerCategoryItem.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL,false)
        recyclerCategoryItem.adapter = childMembersAdapter
    }*/

}