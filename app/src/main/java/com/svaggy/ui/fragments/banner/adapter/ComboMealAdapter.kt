package com.svaggy.ui.fragments.banner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.ui.fragments.banner.model.ComboMeal

class ComboMealAdapter(
    private val context: Context,
    private var arrayList: ArrayList<ComboMeal.Data>?,
    private val getMealRestaurant: (Int) -> Unit
) : RecyclerView.Adapter<ComboMealAdapter.CompanyViewHolder>()
{
    class CompanyViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        var txtRestaurantName = view.findViewById<TextView>(R.id.txtRestaurantName)
        var txtDistance = view.findViewById<TextView>(R.id.txtDistance)
        var txtRestaurantRating = view.findViewById<TextView>(R.id.txtRestaurantRating)
        var txtComboAvailable = view.findViewById<TextView>(R.id.txtComboAvailable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        return CompanyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_combo, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return arrayList?.size?:0
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, @SuppressLint("RecyclerView") position: Int)
    {
        val item = arrayList?.get(position)

        holder.txtRestaurantName.text = item?.restaurantName
        holder.txtDistance.text = item?.distance
        holder.txtRestaurantRating.text = item?.ratings.toString()
        holder.txtComboAvailable.text = item?.combos.toString() + " Combos Available"
        holder.txtComboAvailable.setOnClickListener {
            getMealRestaurant(item?.id?:0)
        }
    }
}
