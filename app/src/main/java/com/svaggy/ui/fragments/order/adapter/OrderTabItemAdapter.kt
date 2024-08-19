package com.svaggy.ui.fragments.order.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ItemOrdersTabBinding

class OrderTabItemAdapter(
    selectIndex:Int,
    private val context: Context,
    private val tabTextItem: List<String>,
    private val tabItemClick:(tabPos:Int) -> Unit) :RecyclerView.Adapter<OrderTabItemAdapter.TabHolder>()
{
    private  var itemIndex :Int = selectIndex

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabHolder {
        val binding =  ItemOrdersTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TabHolder(binding)
    }

    override fun getItemCount(): Int {
        return  tabTextItem.size

    }

    fun getSwitchPos(itemIndex:Int){
        this.itemIndex = itemIndex
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: TabHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.tabItemText.text = tabTextItem[position]
        holder.itemView.setOnClickListener {
            tabItemClick(position)
            itemIndex = position
            notifyDataSetChanged()
        }
        if (itemIndex == position){
            holder.tabItemText.setBackgroundResource(R.drawable.white_curve_4side)
        }
        else
        {
            holder.tabItemText.setBackgroundResource(0)
        }

    }

    inner class TabHolder(binding: ItemOrdersTabBinding) : RecyclerView.ViewHolder(binding.root)
    {
        val tabItemText = binding.itemName
    }
}