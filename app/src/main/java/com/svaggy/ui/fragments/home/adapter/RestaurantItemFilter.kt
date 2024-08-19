package com.svaggy.ui.fragments.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ItemFilterRestaurantsBinding
import com.svaggy.ui.fragments.home.model.RestaurantFilter
import com.svaggy.utils.BindingHolder

class RestaurantItemFilter(var context: Context,
                           private var restaurantFilter: ArrayList<RestaurantFilter>,
                           private val  getSelectType:(list:ArrayList<RestaurantFilter>) -> Unit
) : RecyclerView.Adapter<BindingHolder<ItemFilterRestaurantsBinding>>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingHolder<ItemFilterRestaurantsBinding> {
        val binding = DataBindingUtil.inflate<ItemFilterRestaurantsBinding>(
            LayoutInflater.from(parent.context), R.layout.item_filter_restaurants, parent, false)
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return restaurantFilter.size
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemFilterRestaurantsBinding>, position: Int) {
        val item = restaurantFilter[position]
        holder.binding.getSortBy.text = item.filterName
        if (item.filterImage != null)
            holder.binding.getSortBy.setCompoundDrawablesWithIntrinsicBounds(item.filterImage, null, null, null)
        holder.binding.getSortBy.setOnClickListener {
            item.filterBoolean = !item.filterBoolean
            getSelectType(restaurantFilter)
            if (item.filterBoolean ){
                holder.binding.getSortBy.setTextColor(context.getColor(R.color.primaryColor))
                holder.binding.getSortBy.background = ContextCompat.getDrawable(context, R.drawable.red_curve)
            }else{
                holder.binding.getSortBy.setTextColor(context.getColor(R.color.black))
                holder.binding.getSortBy.background = ContextCompat.getDrawable(context, R.drawable.curve_stroke_four_dp)
            }
        }


    }
}