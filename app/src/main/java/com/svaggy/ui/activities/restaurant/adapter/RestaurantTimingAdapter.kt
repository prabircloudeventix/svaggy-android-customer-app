package com.svaggy.ui.activities.restaurant.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.databinding.RestaurantTimeLayoutBinding
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem

class RestaurantTimingAdapter( private val popularDishes: ArrayList<RestaurantsMenuItem.Data.Categories.MenuItems.MenuAddOns.TodaySchedule>) : RecyclerView.Adapter<RestaurantTimingAdapter.RestaurantHolder>()  {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantHolder {
        val view = RestaurantTimeLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
       return RestaurantHolder(view)
    }

    override fun getItemCount(): Int {
       return popularDishes.size
    }

    override fun onBindViewHolder(holder: RestaurantHolder, position: Int) {
        val item = popularDishes[position]
        holder.getTime.text = item.timings
        holder.getDay.text = item.day


    }


    inner class RestaurantHolder(binding:RestaurantTimeLayoutBinding) : RecyclerView.ViewHolder(binding.root){
        val getDay = binding.getDay
        val getTime = binding.getTime

    }




}