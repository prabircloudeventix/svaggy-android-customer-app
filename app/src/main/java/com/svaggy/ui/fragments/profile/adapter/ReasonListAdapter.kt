package com.svaggy.ui.fragments.profile.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ReasonListItemBinding
import com.svaggy.ui.fragments.profile.model.Data

class ReasonListAdapter(
    private val context:Context,
    private val reasonList: List<Data?>,
    private val userSelection:(reason:String) -> Unit) :RecyclerView.Adapter<ReasonListAdapter.ReasonHolder>() {
    private  var itemIndex :Int? = null




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReasonHolder {
        val view = ReasonListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ReasonHolder(view)

    }

    override fun getItemCount(): Int {
      return  reasonList.size

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ReasonHolder, @SuppressLint("RecyclerView") position: Int) {

        val item = reasonList[position]
        holder.tvReason.text = item?.reasons ?: ""


        holder.checkBoxLayout.setOnClickListener {
            itemIndex = position
            notifyDataSetChanged()
            userSelection(item?.reasons!!)
        }
        if (itemIndex == position){
            holder.checkBox.isChecked = true
            holder.checkBox.buttonTintList = ColorStateList.valueOf(context.getColor(R.color.primaryColor))
        }else{
            holder.checkBox.isChecked = false
            holder.checkBox.buttonTintList = ColorStateList.valueOf(context.getColor(R.color.sub_txt_gray))


        }

    }

    inner class ReasonHolder(binding:ReasonListItemBinding) : RecyclerView.ViewHolder(binding.root){
        val tvReason = binding.getReason
        val checkBoxLayout = binding.checkLayout
        val checkBox = binding.checkBox

    }
}