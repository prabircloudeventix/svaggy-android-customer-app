package com.svaggy.ui.fragments.wallet.adpter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ItemAmountShowBinding
import com.svaggy.ui.fragments.home.model.SortByFilter
import com.svaggy.utils.BindingHolder

class SelectAmountAdapter(
    var context: Context,
    private var arrayList: ArrayList<SortByFilter>?=null,
    private var shortByTextView: TextView,
    private val getPriceClicked: (String,Boolean,TextView,String) -> Unit
) : RecyclerView.Adapter<BindingHolder<ItemAmountShowBinding>>() {
    private var checkedPosition = -1
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingHolder<ItemAmountShowBinding> {
        val binding = DataBindingUtil.inflate<ItemAmountShowBinding>(
            LayoutInflater.from(parent.context), R.layout.item_amount_show, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList?.size ?: 0
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemAmountShowBinding>, position: Int) {
        val item = arrayList?.get(position)
        holder.binding.txtAmount.text=item?.sortByName
        if (checkedPosition == -1) {
            holder.binding.clAmount.background= ContextCompat.getDrawable(context, R.drawable.curve_stroke_four_dp)
            holder.binding.txtAmount.setTextColor(context.getColor(R.color.txt_black))
        } else {
            if (checkedPosition == position) {
                holder.binding.clAmount.background= ContextCompat.getDrawable(context, R.drawable.red_curve)
                holder.binding.txtAmount.setTextColor(context.getColor(R.color.primaryColor))

            } else {
                holder.binding.clAmount.background= ContextCompat.getDrawable(context, R.drawable.curve_stroke_four_dp)
                holder.binding.txtAmount.setTextColor(context.getColor(R.color.txt_black))
            }
        }



        holder.binding.clAmount.setOnClickListener {
            holder.binding.clAmount.background= ContextCompat.getDrawable(context, R.drawable.red_curve)
            holder.binding.txtAmount.setTextColor(context.getColor(R.color.primaryColor))
            if (checkedPosition != position) {
                notifyItemChanged(checkedPosition);
                checkedPosition = position
            }
            getPriceClicked(item?.sortByName?:"",item?.filterBollean?:false,shortByTextView,item?.sortByKey?:"")
        }
    }
}