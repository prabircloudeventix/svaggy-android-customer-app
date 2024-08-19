package com.svaggy.ui.fragments.order.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ItemOrderDetailsProductBinding
import com.svaggy.ui.fragments.order.model.orderbyid.OrderById
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.hide

class OrderItemDetailsAdapter(
    private val context: Context,
    private var arrayList: ArrayList<OrderById.Data.CartData.CartItem>?,
    private var currencyKey: String,

) : RecyclerView.Adapter<BindingHolder<ItemOrderDetailsProductBinding>>()
{
    var quantity:Int?=0
    val selectedItems = mutableListOf<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemOrderDetailsProductBinding> {
        val binding = DataBindingUtil.inflate<ItemOrderDetailsProductBinding>(
            LayoutInflater.from(parent.context), R.layout.item_order_details_product, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList?.size ?: 0
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BindingHolder<ItemOrderDetailsProductBinding>, position: Int) {
        val item = arrayList?.get(position)
        val quantity = item!!.quantity
        val multiPrices = quantity * item.actual_price
        holder.binding.txtDishName.text=item.dish_name
        holder.binding.txtQuantityPrice.text="${item.quantity} * $currencyKey ${item.actual_price}"
        if (item.menu_add_ons.isNullOrEmpty()){
            holder.binding.txtDishDesc.hide()

        }else{
            item.menu_add_ons.forEach { menuAddOn ->
                menuAddOn.add_ons_relations.forEach { addOnRelation ->
                    selectedItems.add(addOnRelation.item_name)
                }
            }

            val selectedItemsString = selectedItems.joinToString(separator = ", ")
            holder.binding.txtDishDesc.text = selectedItemsString

        }

        holder.binding.txtDishPrice.text = "$currencyKey $multiPrices"


    }

}