package com.svaggy.ui.fragments.home.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.ItemFilterRestaurantsBinding
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem
import com.svaggy.utils.BindingHolder
class AboutRestaurantAdapter(
    private val context: Context, private val homeActivity: Activity,
    private var aboutRestaurantAdapter: ArrayList<RestaurantsMenuItem.Data.RestaurantDetails.RestaurantCuisines>
): RecyclerView.Adapter<BindingHolder<ItemFilterRestaurantsBinding>>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemFilterRestaurantsBinding>
    {
        val binding = DataBindingUtil.inflate<ItemFilterRestaurantsBinding>(
            LayoutInflater.from(parent.context), R.layout.item_filter_restaurants, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return aboutRestaurantAdapter.size
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemFilterRestaurantsBinding>, position: Int)
    {
        holder.binding.getSortBy.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
      //  holder.binding.filterIcon.visibility=View.GONE
        val item = aboutRestaurantAdapter.get(position)
        holder.binding.getSortBy.text = item.cuisine
    }
}