package com.svaggy.ui.fragments.wallet.adpter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.WalletTransactionItemBinding
import com.svaggy.ui.fragments.wallet.models.WalletList
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.PrefUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WalletTxnHistoryAdapter(
    private val context: Context,
    private var arrayList: ArrayList<WalletList.Data.Transactions>
) : RecyclerView.Adapter<BindingHolder<WalletTransactionItemBinding>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<WalletTransactionItemBinding> {
        val binding = DataBindingUtil.inflate<WalletTransactionItemBinding>(LayoutInflater.from(parent.context),
            R.layout.wallet_transaction_item, parent, false)
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: BindingHolder<WalletTransactionItemBinding>, position: Int) {
        val item = arrayList[position]

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date: Date? = inputFormat.parse(item.transactionDate)
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val outputDateString: String = outputFormat.format(date)


        if (item.type == "CREDITED" || item.type == "CREDITED_FOR_REGISTRATION") {
            holder.binding.txtTxnType.text = ContextCompat.getString(context,R.string.added_to_wallet)
            holder.binding.txtTxnAmount.setTextColor(ContextCompat.getColor(context, R.color.green_clr))
            holder.binding.txtTxnAmount.text = context.getString(R.string.priceRtn,  "+" + context.getString(R.string.curency_name) + " " + item.amount.toString())
            if (item.type == "CREDITED_FOR_REGISTRATION"){
                holder.binding.txtTxnType.text = ContextCompat.getString(context,R.string.added_to_wallet_bouns)

            }
        }
        else
        {
            holder.binding.txtTxnType.text = context.getString(R.string.priceRtn,ContextCompat.getString(context,R.string.paid_for_order)+item.orderId)
            holder.binding.txtTxnAmount.setTextColor(ContextCompat.getColor(context, R.color.primaryColor))
            holder.binding.txtTxnAmount.text = context.getString(R.string.priceRtn,  "-" + context.getString(R.string.curency_name) + item.amount.toString())
        }
        holder.binding.txtTxnDate.text = outputDateString
    }
}