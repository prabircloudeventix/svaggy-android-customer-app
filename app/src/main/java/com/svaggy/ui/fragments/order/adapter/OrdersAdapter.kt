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
import com.svaggy.databinding.OrderDeliverdItemBinding
import com.svaggy.ui.fragments.order.model.Orders
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.setSafeOnClickListener

class OrdersAdapter(
    private val context: Context,
    private var arrayList: ArrayList<Orders.Data.OrderData>?,var type: String,
    private val getOrderId: (orderId: Int, cancelClick: Boolean,
                             trackClick:Boolean, reOrderClick:Boolean,
                             restaurantClick:Boolean, giveReviewClick:Boolean,
                             orderStatus:String,deliveryType:String) -> Unit,
    private val clickSupport:() ->Unit,
) : RecyclerView.Adapter<BindingHolder<OrderDeliverdItemBinding>>()
{
    var quantity:Int?=0
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingHolder<OrderDeliverdItemBinding> {
        val binding = DataBindingUtil.inflate<OrderDeliverdItemBinding>(
            LayoutInflater.from(parent.context), R.layout.order_deliverd_item, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList?.size ?: 0
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BindingHolder<OrderDeliverdItemBinding>, position: Int) {
        val item = arrayList?.get(position)
        val orderStatus = item?.orderStatus ?: ""
        val deliveryTpe = item?.deliveryType

        if (type=="TODAY"){

            holder.binding.btnTrackOrder.visibility=View.VISIBLE
            holder.binding.txtCancelled.visibility=View.GONE
            holder.binding.btnReOrder.visibility=View.GONE
            holder.binding.btnGiveReview.visibility=View.GONE
            holder.binding.divider20.visibility=View.GONE
            holder.binding.ivInfo.visibility=View.GONE
            holder.binding.txtUpcominMessage.visibility=View.GONE
            if (item!!.orderStatus.equals("ORDER_PENDING")){
                holder.binding.btnCancelOrder.visibility=View.VISIBLE
            }
            else{
                holder.binding.btnCancelOrder.visibility=View.INVISIBLE

            }



        }
        else if (type=="CANCELLED"){
            holder.binding.btnCancelOrder.visibility=View.GONE
            holder.binding.btnTrackOrder.visibility=View.GONE
            holder.binding.txtCancelled.visibility=View.VISIBLE
            holder.binding.btnReOrder.visibility=View.GONE
            holder.binding.btnGiveReview.visibility=View.GONE
            holder.binding.divider20.visibility=View.GONE
            holder.binding.ivInfo.visibility=View.GONE
            holder.binding.txtUpcominMessage.visibility=View.GONE
        }
        else if (type=="UPCOMING"){
            holder.binding.divider20.visibility=View.VISIBLE
            holder.binding.btnTrackOrder.isEnabled=false
            holder.binding.btnCancelOrder.isEnabled=false
            holder.binding.ivInfo.visibility=View.VISIBLE
            holder.binding.txtUpcominMessage.visibility=View.VISIBLE
            holder.binding.txtCancelled.visibility = View.GONE
        }
        holder.binding.txtRestaurantName.text = item?.restaurantName
        //holder.textRestaurantAddress.text = item?.restaurantAddress
        holder.binding.textRestaurantAddress.text = item?.address?:""
        holder.binding.textRestaurantDishPrice.text = "${item?.currency} ${PrefUtils.instance.formatDouble(item?.totalAmount!!)}"

        val ordersAdapter = OrderCartDishAdapter(item.menuItems)
        holder.binding.recyclerOrdersDish.adapter = ordersAdapter

        holder.binding.btnCancelOrder.setSafeOnClickListener {
            getOrderId(item.orderId?:0,
                true,false,false,false,false, orderStatus,deliveryTpe ?: "")
        }
        holder.binding.btnTrackOrder.setSafeOnClickListener {
            getOrderId(item.orderId?:0,
                false,true,false,false,false,orderStatus,deliveryTpe ?: "")
        }

        holder.binding.btnReOrder.setSafeOnClickListener {
            getOrderId(item.orderId?:0,
                false,false,true,false,false,orderStatus,deliveryTpe ?: "")
        }
        holder.binding.clRestaurantName.setSafeOnClickListener {
            getOrderId(item.orderId?:0,
                false,false,false,true,false,orderStatus,deliveryTpe ?: "")
        }
        holder.binding.btnGiveReview.setSafeOnClickListener {
            getOrderId(item.orderId?:0,
                false,false,false,false,true,orderStatus,deliveryTpe ?: "")
        }
        holder.binding.tvSupport.setSafeOnClickListener {
            clickSupport()

        }
    }
}
