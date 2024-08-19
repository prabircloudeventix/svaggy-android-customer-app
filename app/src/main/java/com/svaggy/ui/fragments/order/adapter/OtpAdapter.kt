package com.svaggy.ui.fragments.order.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.databinding.OtpItemBinding

class OtpAdapter : RecyclerView.Adapter<OtpAdapter.OtpHolder>() {

    private val optNumList:ArrayList<Int> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtpHolder {
        val view = OtpItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OtpHolder(view)
    }

    override fun getItemCount(): Int {
        return  optNumList.size
    }

    override fun onBindViewHolder(holder: OtpHolder, position: Int) {
        holder.text.text = optNumList[position].toString()

    }

    fun updateAdapter(otpList: List<Int>){
        optNumList.clear()
        optNumList.addAll(otpList)
        notifyDataSetChanged()

    }

    inner class OtpHolder(binding:OtpItemBinding) : RecyclerView.ViewHolder(binding.root){
        val text = binding.getOpt
    }
}