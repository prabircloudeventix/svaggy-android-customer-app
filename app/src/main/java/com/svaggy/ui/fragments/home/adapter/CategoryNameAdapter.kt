package com.svaggy.ui.fragments.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ItemCategoryNameBinding
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem
import com.svaggy.utils.BindingHolder

class CategoryNameAdapter(
    private var arrayList: ArrayList<RestaurantsMenuItem.Data.Categories>?,
    private var getPosition: (pos:Int,categoryName:String)->Unit
): RecyclerView.Adapter<BindingHolder<ItemCategoryNameBinding>>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemCategoryNameBinding>
    {
        val binding = DataBindingUtil.inflate<ItemCategoryNameBinding>(
            LayoutInflater.from(parent.context), R.layout.item_category_name, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList?.size?:0
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemCategoryNameBinding>, position: Int)
    {
        val item = arrayList?.get(position)
        holder.binding.categoryName.text = item?.categoryName
        holder.binding.categoryItemCount.text= item?.menuItems?.size.toString()

        holder.binding.categoryName.setOnClickListener {
            getPosition(position,item?.categoryName ?:"")
        }
    }
}