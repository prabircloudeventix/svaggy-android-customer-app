package com.svaggy.ui.fragments.banner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.svaggy.R
import com.svaggy.databinding.ItemCompareFoodMenuBinding
import com.svaggy.ui.fragments.banner.model.CompareFood
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.hide
import com.svaggy.utils.show

class CompareFoodMenuAdapter(
    private val context: Context,val item: CompareFood.Data,
    private var arrayList: ArrayList<CompareFood.Data.MenuItems>?,
    private val getCompareFoodRestaurant: (Int,TextView,Int) -> Unit
) : RecyclerView.Adapter<BindingHolder<ItemCompareFoodMenuBinding>>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int
    ): BindingHolder<ItemCompareFoodMenuBinding>
    {
        val binding = DataBindingUtil.inflate<ItemCompareFoodMenuBinding>(
            LayoutInflater.from(parent.context), R.layout.item_compare_food_menu, parent, false)
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList?.size?:0
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemCompareFoodMenuBinding>,  position: Int) {
        val itemMenu = arrayList?.get(position)

        holder.binding.txtDishName.text = itemMenu?.dishName
        holder.binding.txtDishPrice.text = "CZK "+itemMenu?.price.toString()


        holder.binding.consDishItem.setOnClickListener {
            getCompareFoodRestaurant(itemMenu?.id?:0,holder.binding.txtDishName,item.id?:0)

        }
        if (!itemMenu!!.isActive!!){
            Glide.with(context).load(itemMenu.menuItemImage)
                .placeholder(R.drawable.placeholder_cuisine).into(holder.binding.imgDish)
            holder.binding.txtNotAvailable.show()
            holder.binding.btOrderNow.hide()
            holder.binding.consDishItem.isClickable = false
            val matrix = ColorMatrix()
            matrix.setSaturation(0F)
            val filter = ColorMatrixColorFilter(matrix)
            holder.binding.imgDish.colorFilter = filter

        }else{
            holder.binding.txtNotAvailable.hide()
            holder.binding.btOrderNow.show()
            holder.binding.consDishItem.isClickable = true
            Glide.with(context).load(itemMenu.menuItemImage)
                .placeholder(R.drawable.placeholder_cuisine).into(holder.binding.imgDish)

        }
    }
}
