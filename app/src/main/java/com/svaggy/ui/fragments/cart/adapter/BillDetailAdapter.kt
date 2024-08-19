package com.svaggy.ui.fragments.cart.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.ItemBilldetailsBinding
import com.svaggy.ui.fragments.cart.model.CartCheckout
import com.svaggy.ui.fragments.cart.model.ViewCart
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.PrefUtils
import java.util.ArrayList
class BillDetailAdapter(
    private val context:Context,
    private var billDetails: ArrayList<CartCheckout.Data.BillDetail>,
    private val walletAvailable: (walletAvailable: Boolean) -> Unit
)
    : RecyclerView.Adapter<BindingHolder<ItemBilldetailsBinding>>()
{


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingHolder<ItemBilldetailsBinding> {
        val binding = DataBindingUtil.inflate<ItemBilldetailsBinding>(
            LayoutInflater.from(parent.context), R.layout.item_billdetails, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return billDetails.size
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BindingHolder<ItemBilldetailsBinding>, position: Int) {
        val item = billDetails[position]
        holder.binding.billDetailsName.text= item.text
        holder.binding.txtBillAmount.text= item.amount_text
     //   holder.binding.txtBillAmount.text=String.format("%.2f", context.getString(R.string.curencyWith,item.amount.toString()))
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_info) // Replace R.drawable.ic_example with your drawable resource

        if (item.text == "Minimum Order Fee"){
            holder.binding.billDetailsName.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        }else{
            holder.binding.billDetailsName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

        }
        holder.binding.billDetailsName.setOnClickListener {
            if (item.text == "Minimum Order Fee"){
                showMiniPrizeDetails()

            }

        }

        if (billDetails[position].type=="wallet"){
            walletAvailable(true)
        }
    }

    private fun showMiniPrizeDetails() {
        val viewDialog = View.inflate(context, R.layout.coupon_details_item, null)
        val dialog = BottomSheetDialog(context)
        dialog.setOnShowListener {
            val bottomSheetDialogFragment = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            (viewDialog.parent as View).setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.transparent
                )
            )
            val behavior = bottomSheetDialogFragment?.let {
                BottomSheetBehavior.from(it)
            }
            behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        }
        val ivCancelDialog =viewDialog.findViewById<ImageView>(R.id.ivCancleDilog)
        ivCancelDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(viewDialog)
        dialog.show()


    }

}
