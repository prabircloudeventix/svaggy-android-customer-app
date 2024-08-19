package com.svaggy.pagination.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.ui.fragments.banner.model.ComboMeal

class ComboMelPageAdapter(
    private val context: Context,
    private val getMealRestaurant: (Int) -> Unit
) : PagingDataAdapter<ComboMeal.Data, ComboMelPageAdapter.CompanyViewHolder>(DIFF_CALLBACK) {

    class CompanyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val txtDistance: TextView = view.findViewById(R.id.txtDistance)
        val txtRestaurantRating: TextView = view.findViewById(R.id.txtRestaurantRating)
        val txtComboAvailable: TextView = view.findViewById(R.id.txtComboAvailable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        return CompanyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_combo, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.txtRestaurantName.text = it.restaurantName
            holder.txtDistance.text = it.distance
            holder.txtRestaurantRating.text = it.ratings.toString()
            holder.txtComboAvailable.text = "${it.combos} ${ContextCompat.getString(context, R.string.combos_available)}"
            holder.txtComboAvailable.setOnClickListener {
                getMealRestaurant(item.id ?: 0)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ComboMeal.Data>() {
            override fun areItemsTheSame(oldItem: ComboMeal.Data, newItem: ComboMeal.Data): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ComboMeal.Data, newItem: ComboMeal.Data): Boolean {
                return oldItem == newItem
            }
        }
    }
}