package com.svaggy.ui.activities.restaurant.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.svaggy.R
import com.svaggy.databinding.RepeatOrderRcItemBinding
import com.svaggy.ui.fragments.home.model.RepeatOrderModel

class RepeatOrderAdapter(
    private val list: ArrayList<RepeatOrderModel>?,
    private val bottomSheet: BottomSheetDialog,
    private val incrementItem:(quantity:Int, itemId:Int, menuItemId:Int) -> Unit,
    private val decrementItem:(quantity:Int, itemId:Int, menuItemId:Int, isRemoveCart:Boolean) -> Unit) : RecyclerView.Adapter<RepeatOrderAdapter.RepeatHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepeatHolder {
        val  view = RepeatOrderRcItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return RepeatHolder(view)
    }

    override fun getItemCount(): Int {
       return  list?.size ?: 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RepeatHolder, position: Int) {
      if (list != null){
         // if (clickMenuId == list[position].menuItemId){
              holder.itemName.text = list[position].dishName
              holder.itemPrices.text = "CZK ${list[position].actualPrice.toString()}"
              holder.itemQuantity.text = list[position].quantity.toString()
          val selectedItemsString = list[position].addOns.joinToString(separator = ", ") { it.addOnName }
          holder.itemAddOns.text = selectedItemsString
              holder.addBtn.setOnClickListener {
                  var quantity = holder.itemQuantity.text.toString().toInt()
                  quantity++
                  holder.itemQuantity.text = quantity.toString()
                  incrementItem(quantity,list[position].itemId ?: 0,list[position].menuItemId ?: 0)

              }
              holder.minusBtn.setOnClickListener {
                  var quantity = holder.itemQuantity.text.toString().toInt()
                  quantity--
                  holder.itemQuantity.text = quantity.toString()
                  if (quantity == 0 && itemCount == 1){
                      bottomSheet.dismiss()
                      decrementItem(quantity,list[position].itemId ?: 0,list[position].menuItemId ?: 0,true)
                  }else{
                      decrementItem(quantity,list[position].itemId ?: 0,list[position].menuItemId ?: 0,false)

                  }
                  if (quantity == 0){
                      removeItem(position)
                  }
              }
              when (list[position].dishType) {
                  "Vegan" -> {
                      holder.getOderType.setImageResource(R.drawable.vegan)
                  }

                  "Veg" -> {
                      holder.getOderType.setImageResource(R.drawable.veg_icon)
                  }

                  "Non-Veg" -> {
                      holder.getOderType.setImageResource(R.drawable.nonveg_icon)
                  }
              }
//          }else{
//              holder.itemName.hide()
//              holder.itemPrices.hide()
//              holder.itemQuantity.hide()
//              holder.itemAddOns.hide()
//              holder.addBtn.hide()
//              holder.minusBtn.hide()
//              holder.getOderType.hide()
//
//          }

      }
    }
    private fun removeItem(position: Int){
        list?.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class RepeatHolder(binding:RepeatOrderRcItemBinding) : RecyclerView.ViewHolder(binding.root){
        val itemName = binding.getItemName
        val itemPrices = binding.getItemPrices
        val itemAddOns = binding.getAddOn
        val getOderType = binding.getOderType
        val addBtn = binding.imgAdd
        val minusBtn = binding.imgMinus
        val itemQuantity = binding.txtItemCount


    }
}