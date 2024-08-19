package com.svaggy.ui.fragments.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.svaggy.R
import com.svaggy.databinding.OfferViewBinding
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem.Data.Categories.MenuItems.MenuAddOns.OfferModel

class SpecificRestaurantAdapter(private val offerList: ArrayList<OfferModel>,
                                private val context: Context) :RecyclerView.Adapter<SpecificRestaurantAdapter.SpecificHolder>() {

    private var lineCount = ArrayList<Int>()





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecificHolder {
       val view = OfferViewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SpecificHolder(view)
    }

    override fun getItemCount(): Int {
      return offerList.size
    }

    override fun onBindViewHolder(holder: SpecificHolder, position: Int) {
        val items = offerList[position]
        holder.offerCode.text = items.coupon_code
        holder.offerDes.text = items.description
        holder.offerDes.post {
            lineCount.add(holder.offerDes.lineCount)

        }
//        holder.icDrop.setOnClickListener {
//            showCouponDetails(items.coupon_code,items.description)
//
//        }

    }

    private fun showCouponDetails(couponCode: String, description: String) {
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
            val offerTitle  =viewDialog.findViewById<TextView>(R.id.couponTitle)
            val offerDis =viewDialog.findViewById<TextView>(R.id.getCoupons)
            val ivCancelDialog =viewDialog.findViewById<ImageView>(R.id.ivCancleDilog)
        offerTitle.text = couponCode.toString()
        offerDis.text = description
        ivCancelDialog.setOnClickListener {
                dialog.dismiss()
            }

            dialog.setContentView(viewDialog)
            dialog.show()


    }

    inner class SpecificHolder(binding:OfferViewBinding):RecyclerView.ViewHolder(binding.root){
        val offerCode = binding.offerCode
        val offerDes = binding.offerDes
        val parentLayout = binding.parentLayout

    }
}