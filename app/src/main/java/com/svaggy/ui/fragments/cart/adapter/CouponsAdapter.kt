package com.svaggy.ui.fragments.cart.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ItemCouponsBinding
import com.svaggy.ui.fragments.cart.model.GetCoupons

class CouponsAdapter(
    private var arrayList: ArrayList<GetCoupons.Data>?,
    private val getCouponId: (couponId:String) -> Unit
) : RecyclerView.Adapter<CouponsAdapter.CompanyViewHolder>()
{


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val view = ItemCouponsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CompanyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayList?.size?:0
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = arrayList?.get(position)
        val couponId = item?.id
        holder.couponText.text = item?.couponCode ?: ""
        holder.couponDes.text = item?.description ?: ""

        if (position == itemCount-1){
            holder.borderText.visibility = View.INVISIBLE
        }
        holder.btnApply.setOnClickListener {
            getCouponId(couponId.toString())
        }
    }

    inner class CompanyViewHolder(binding: ItemCouponsBinding) : RecyclerView.ViewHolder(binding.root) {


        val couponText = binding.txtCouponCode
        val couponDes = binding.txtCouponDesc
        val btnApply = binding.btnApplyCoupon
        val borderText = binding.txtLine
    }

}
