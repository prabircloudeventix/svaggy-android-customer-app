package com.svaggy.ui.fragments.banner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ItemCompareFoodBinding
import com.svaggy.ui.fragments.banner.model.CompareFood
import com.svaggy.utils.BindingHolder

class CompareFoodAdapter(
    private val context: Context,
    private var arrayList: ArrayList<CompareFood.Data>?,
    private val getCompareRestaurant: (restaurantId:Int, menuId:Int, productName:TextView) -> Unit
) : RecyclerView.Adapter<BindingHolder<ItemCompareFoodBinding>>()
{
    private var restId:Int = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int
    ): BindingHolder<ItemCompareFoodBinding>
    {
        val binding = DataBindingUtil.inflate<ItemCompareFoodBinding>(
            LayoutInflater.from(parent.context), R.layout.item_compare_food, parent, false)
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList?.size?:0
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemCompareFoodBinding>, @SuppressLint("RecyclerView") position: Int)
    {
        val item = arrayList?.get(position)

        holder.binding.txtRestaurantsName.text = item?.restaurantName
        holder.binding.txtDistance.text = item?.distance
        restId = item?.id!!

        holder.binding.clMainItem.setOnClickListener {
        }

        holder.binding.recyclerCompareMenu.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        val compareFoodAdapter = CompareFoodMenuAdapter(context,item, item.menuItems,:: getCompareFoodRestaurant)
        holder.binding.recyclerCompareMenu.adapter = compareFoodAdapter


    }

    private fun getCompareFoodRestaurant(menuId: Int, productName: TextView,restaurantId:Int) {
        getCompareRestaurant(restaurantId, menuId,productName)
    }
}
