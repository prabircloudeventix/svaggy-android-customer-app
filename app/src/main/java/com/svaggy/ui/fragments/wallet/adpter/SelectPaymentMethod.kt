package com.svaggy.ui.fragments.wallet.adpter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.ItemSelectPaymentBinding
import com.svaggy.utils.BindingHolder
class SelectPaymentMethod(
    var activity: MainActivity,
    private var arrayList: ArrayList<String>?
) : RecyclerView.Adapter<BindingHolder<ItemSelectPaymentBinding>>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingHolder<ItemSelectPaymentBinding> {
        val binding = DataBindingUtil.inflate<ItemSelectPaymentBinding>(
            LayoutInflater.from(parent.context), R.layout.item_select_payment, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList?.size ?: 0
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemSelectPaymentBinding>, position: Int) {
        val item = arrayList?.get(position)
        holder.binding.cardNameTv.text=item

    }
}