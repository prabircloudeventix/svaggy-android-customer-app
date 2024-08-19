package com.svaggy.ui.fragments.order.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.ItemBilldetailsBinding
import com.svaggy.ui.fragments.order.model.orderbyid.OrderById
import com.svaggy.utils.BindingHolder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class OrderDetailsAdapter(
    var mainActivity: Activity,
    private var billDetails: java.util.ArrayList<OrderById.Data.OrderDetail>
) : RecyclerView.Adapter<BindingHolder<ItemBilldetailsBinding>>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemBilldetailsBinding> {
        val binding = DataBindingUtil.inflate<ItemBilldetailsBinding>(
            LayoutInflater.from(parent.context), R.layout.item_billdetails, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return billDetails.size
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: BindingHolder<ItemBilldetailsBinding>, position: Int) {
        val item = billDetails[position]

        if (item.text == "Date"){
            val apiTime = item.value
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val currentDate = apiTime.let { sdf.parse(it) }
            currentDate?.let {
                val calendar = Calendar.getInstance()
                calendar.time = currentDate
                val formattedDate = SimpleDateFormat("dd MMM yyyy").format(calendar.time)
                val formattedTime = SimpleDateFormat("h:mm a").format(calendar.time)
                holder.binding.txtBillAmount.text  = "$formattedDate, $formattedTime"
            }

        }else{
            holder.binding.billDetailsName.text = item.text
            holder.binding.txtBillAmount.text = item.value

        }
    }
}
