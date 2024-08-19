package com.svaggy.ui.fragments.order.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.CancelOrderItemBinding
import com.svaggy.ui.fragments.profile.model.Data

class CancelOrderAdapter(private val context: Context,
                         private val data: List<Data?>,
                         private val userSelection:(reason:String) -> Unit) : RecyclerView.Adapter<CancelOrderAdapter.CancelHolder>() {
    private  var itemIndex :Int? = null





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CancelHolder {
        val view = CancelOrderItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CancelHolder(view)

    }

    override fun getItemCount(): Int {
        return data.size

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: CancelHolder, @SuppressLint("RecyclerView") position: Int) {
        val item =data[position]
        holder.tvReason.text = item?.reasons ?: ""


        holder.checkBoxLayout.setOnClickListener {
            itemIndex = position
            notifyDataSetChanged()
            userSelection(item?.reasons?: "")
        }

        if (itemIndex == position) {
            holder.checkBox.isChecked = true
            holder.checkBox.buttonTintList = ColorStateList.valueOf(context.getColor(R.color.primaryColor))
        } else{
            holder.checkBox.isChecked = false
            holder.checkBox.buttonTintList = ColorStateList.valueOf(context.getColor(R.color.sub_txt_gray))
        }

    }

    inner class CancelHolder(binding:CancelOrderItemBinding) : RecyclerView.ViewHolder(binding.root){
        val tvReason = binding.getReason
        val checkBoxLayout = binding.checkLayout
        val checkBox = binding.checkBox
    }
}