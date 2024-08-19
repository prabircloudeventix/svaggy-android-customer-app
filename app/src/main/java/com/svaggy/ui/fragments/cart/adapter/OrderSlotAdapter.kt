package com.svaggy.ui.fragments.cart.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.databinding.TimeViewItemBinding

class OrderSlotAdapter : RecyclerView.Adapter<OrderSlotAdapter.OrderSlotHolder>() {

    private val timeSlotList :ArrayList<String> = ArrayList()






    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderSlotHolder {
        val view = TimeViewItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OrderSlotHolder(view)

    }

    override fun getItemCount(): Int {
       return timeSlotList.size
    }

    override fun onBindViewHolder(holder: OrderSlotHolder, position: Int) {
        val pos = timeSlotList[position]
        holder.timeText.text = pos
    }

    @SuppressLint("NotifyDataSetChanged")
     fun listUpdate(list:ArrayList<String>){
         timeSlotList.clear()
        timeSlotList.addAll(list)
        notifyDataSetChanged()
    }

    inner class OrderSlotHolder(binding:TimeViewItemBinding) : RecyclerView.ViewHolder(binding.root){
        val timeText = binding.timeText

    }
}