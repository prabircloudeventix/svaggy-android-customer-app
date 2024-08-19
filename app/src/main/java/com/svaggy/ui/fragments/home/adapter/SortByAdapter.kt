package com.svaggy.ui.fragments.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ItemShortbyFilterBinding
import com.svaggy.ui.fragments.home.model.SortByFilter
import com.svaggy.utils.BindingHolder

class SortByAdapter(
    var context: Context,
    private var sortByList: ArrayList<SortByFilter>,
    private val getRestaurantFilter: (value:String) -> Unit)
    : RecyclerView.Adapter<BindingHolder<ItemShortbyFilterBinding>>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingHolder<ItemShortbyFilterBinding> {
        val binding = DataBindingUtil.inflate<ItemShortbyFilterBinding>(
            LayoutInflater.from(parent.context), R.layout.item_shortby_filter, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return sortByList.size
    }

    override fun onBindViewHolder(
        holder: BindingHolder<ItemShortbyFilterBinding>,
        position: Int) {
        val item = sortByList[position]
        holder.binding.sortNameTv.text=item.sortByName
        holder.binding.sortNameTv.setOnClickListener {
            getRestaurantFilter(item.sortByKey)
        }
    }

}