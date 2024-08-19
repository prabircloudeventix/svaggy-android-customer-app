package com.svaggy.ui.fragments.order.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.ui.fragments.order.model.Orders

class OrderCartDishAdapter(
    private var arrayList: ArrayList<Orders.Data.OrderData.MenuItems>?
) : RecyclerView.Adapter<OrderCartDishAdapter.CompanyViewHolder>()
{
    class CompanyViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        var textRestaurantDish = view.findViewById<TextView>(R.id.textRestaurantDish)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        return CompanyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_cart_dish, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return arrayList?.size?:0
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, @SuppressLint("RecyclerView") position: Int)
    {
        val item = arrayList?.get(position)

        holder.textRestaurantDish.text = "${item?.dishName} x ${item?.quantity}"
    }
}
