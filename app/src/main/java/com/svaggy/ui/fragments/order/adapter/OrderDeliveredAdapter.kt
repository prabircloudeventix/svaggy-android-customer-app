package com.svaggy.ui.fragments.order.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ItemOrdersTodayBinding
import com.svaggy.ui.fragments.order.model.Orders
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.invisible
import com.svaggy.utils.show

class OrderDeliveredAdapter(  private val context: Context,
private var arrayList: ArrayList<Orders.Data.OrderData>?,var type: String,
private val getOrderId: (orderId: Int, cancelClick: Boolean, trackClick:Boolean, reOrderClick:Boolean,
                         restaurantClick:Boolean, giveReviewClick:Boolean,orderStatus:String,
                         deliveryType:String,restaurantName:String) -> Unit,
    private val clickSupport:() ->Unit,
) : RecyclerView.Adapter<BindingHolder<ItemOrdersTodayBinding>>()
{
    var quantity:Int?=0
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingHolder<ItemOrdersTodayBinding> {
        val binding = DataBindingUtil.inflate<ItemOrdersTodayBinding>(
            LayoutInflater.from(parent.context), R.layout.item_orders_today, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList?.size ?: 0
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BindingHolder<ItemOrdersTodayBinding>, position: Int) {
        val item = arrayList?.get(position)
        val orderStatus = item?.orderStatus ?: ""
        val deliveryTpe = item?.deliveryType

            holder.binding.btnGiveReview.visibility= View.VISIBLE
        holder.binding.txtRestaurantName.text = item?.restaurantName
        holder.binding.textRestaurantAddress.text = item?.address?:""
        holder.binding.textRestaurantDishPrice.text = "${item?.currency} ${PrefUtils.instance.formatDouble(item?.totalAmount!!)}"
        if (item.orderReview != null){
            holder.binding.getReview.show()
            holder.binding.btnGiveReview.invisible()
            holder.binding.getReview.text = context.getString(R.string.you_rated,item.orderReview.rating.toString())
        }else{
            holder.binding.getReview.invisible()
            holder.binding.btnGiveReview.show()

        }

        val ordersAdapter = OrderCartDishAdapter(item.menuItems)
        holder.binding.recyclerOrdersDish.adapter = ordersAdapter

        holder.binding.btnReOrder.setOnClickListener {
            getOrderId(item.orderId?:0,
                false,false,true,false,false,orderStatus,deliveryTpe.toString(),item.restaurantName.toString())
        }
        holder.binding.clRestaurantName.setOnClickListener {
            getOrderId(item.orderId?:0,
                false,false,false,true,false,orderStatus,deliveryTpe.toString(),item.restaurantName.toString())
        }
        holder.binding.btnGiveReview.setOnClickListener {
            getOrderId(item.orderId?:0,
                false,false,false,false,true,orderStatus,deliveryTpe.toString(),item.restaurantName.toString())
        }
        holder.binding.tvSupport.setOnClickListener {
            clickSupport()

        }
    }
}
