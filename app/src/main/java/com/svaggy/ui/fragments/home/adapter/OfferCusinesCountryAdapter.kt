package com.svaggy.ui.fragments.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ItemCuisinesCountryBinding
import com.svaggy.ui.fragments.home.model.AllRestaurant
import com.svaggy.utils.BindingHolder

class OfferCusinesCountryAdapter(
    private var cuisinesContryWise: ArrayList<AllRestaurant.Data.RestaurantCuisines>
): RecyclerView.Adapter<BindingHolder<ItemCuisinesCountryBinding>>()
{


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemCuisinesCountryBinding>
    {
        val binding = DataBindingUtil.inflate<ItemCuisinesCountryBinding>(
            LayoutInflater.from(parent.context), R.layout.item_cuisines_country, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return cuisinesContryWise.size
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemCuisinesCountryBinding>, position: Int)
    {
        val item = cuisinesContryWise[position]

        var getLast=cuisinesContryWise.last()
        if (cuisinesContryWise[position]==getLast){
            holder.binding.tvCuisinesCountryName.text = item.cuisine
        }
        else{
            holder.binding.tvCuisinesCountryName.text = item.cuisine+", "
        }

    }
}