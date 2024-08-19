package com.svaggy.ui.fragments.cart.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.svaggy.R
import com.svaggy.app.SvaggyApplication
import com.svaggy.databinding.ItemCartBinding
import com.svaggy.ui.fragments.cart.model.ViewCart
import com.svaggy.ui.fragments.home.adapter.MenuAddOneCustomizeAdapter
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.hide
import com.svaggy.utils.setSafeOnClickListener
import java.text.DecimalFormat

class CartItemAdapter(
    private val context: Context,
    val activity: Activity,
    private var arrayList: ArrayList<ViewCart.Data.CartItems>?,
    private val updateQuantity: (menuItemId:Int?,cartId: Int, quantity: Int,addOns: ArrayList<Int> ) -> Unit,
    private val updateAddOn: (cartId: Int, quantity: Int, addOns: ArrayList<Int>) -> Unit,
    private val dataReset:() -> Unit,
) : RecyclerView.Adapter<BindingHolder<ItemCartBinding>>() {
    private var itemId: Int? = 0
    private lateinit var set:ArrayList<Int>










    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BindingHolder<ItemCartBinding> {
        val binding = DataBindingUtil.inflate<ItemCartBinding>(
            LayoutInflater.from(parent.context), R.layout.item_cart, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList?.size ?: 0
    }


    @SuppressLint("SuspiciousIndentation", "NotifyDataSetChanged", "StringFormatInvalid")
    override fun onBindViewHolder(holder: BindingHolder<ItemCartBinding>, position: Int) {
        val item = arrayList?.get(position)
        val selectedAddOns = mutableListOf<String>()







        val menuItem = item!!.menuAddOns
            for (i in 0 until menuItem.size) {
                for (j in 0 until menuItem[i].addOnsRelations.size) {
                    if (menuItem[i].addOnsRelations[j].isSelected == true) {
                        selectedAddOns.add(menuItem[i].addOnsRelations[j].itemName ?: "")
                    }

                }

                if (menuItem[i].addOnsRelations.isEmpty())
                    holder.binding.txtCustomizeBtn.hide()
            }




//        val itemActualPrices = item.actualPrice!!.plus(addOnActualPrices)
//        val disCountPrices = item.price!!.plus(addOnPrices)


        val itemActualPrices = item.actualPrice ?: 0.0
        val disCountPrices = item.price ?: 0.0


        holder.binding.txtDishPrice.text = context.getString(R.string.curencyWith,  DecimalFormat("#.##").format(disCountPrices))
        val selectedItemsString = selectedAddOns.joinToString(separator = ", ")
        holder.binding.itemAddOnsName.text = selectedItemsString
        if (disCountPrices < itemActualPrices){
            holder.binding.txtActualDishPrice.text =  context.getString(R.string.curencyWith,DecimalFormat("#.##").format(itemActualPrices))
            holder.binding.txtActualDishPrice.paintFlags = holder.binding.txtActualDishPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }








        if (item.menuItemImage == null || item.menuItemImage == "") {
            val set = ConstraintSet()
            set.clone(holder.binding.addCartBt)
            holder.binding.imgDish.visibility = View.GONE
            val param = holder.binding.constraintImage.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(0, 60, 0, 0)
            holder.binding.constraintImage.layoutParams = param
//            set.connect(R.id.textView105, ConstraintSet.TOP,R.id.textView105,ConstraintSet.RIGHT,0)
        } else {
            if (item.isActive == true) {
                holder.binding.imgDish.visibility = View.VISIBLE
                Glide.with(activity).load(item.menuItemImage)
                    .placeholder(R.drawable.placeholder_cuisine).into(holder.binding.imgDish)
            } else {
                holder.binding.imgDish.visibility = View.VISIBLE
                Glide.with(activity).load(item.menuItemImage)
                    .placeholder(R.drawable.placeholder_cuisine).into(holder.binding.imgDish)

                val matrix = ColorMatrix()
                matrix.setSaturation(0F)
                val filter = ColorMatrixColorFilter(matrix)
                holder.binding.imgDish.colorFilter = filter
            }
        }

        when (item.dishType) {
            "Vegan" -> {
                holder.binding.imgDishType.setImageResource(R.drawable.vegan)
            }

            "Veg" -> {
                holder.binding.imgDishType.setImageResource(R.drawable.veg_icon)
            }

            "Non-Veg" -> {
                holder.binding.imgDishType.setImageResource(R.drawable.nonveg_icon)
            }
        }

        if (item.menuAddOns.isEmpty()) {
            holder.binding.txtCustomizeBtn.hide()
        }
        holder.binding.txtDishName.text = item.dishName
        holder.binding.txtItemCount.text = item.quantity.toString()

        holder.binding.imgMinus.setSafeOnClickListener {
            SvaggyApplication.progressBarLoader.start(context)
            var quantity = holder.binding.txtItemCount.text.toString().toInt()
            quantity--
            set = ArrayList()
            item.menuAddOns.forEach {menuAddOns ->
                menuAddOns.addOnsRelations.forEach {addOnsRelations ->
                    if (addOnsRelations.isSelected == true){
                        set.add(addOnsRelations.id ?: 0)
                    }

                }

            }
           updateQuantity(item.menuItemId,item.id ?: 0, quantity, set)

        }

        holder.binding.imgAdd.setSafeOnClickListener {
            SvaggyApplication.progressBarLoader.start(context)
            var quantity = holder.binding.txtItemCount.text.toString().toInt()
            quantity++
            set = ArrayList()
            item.menuAddOns.forEach {menuAddOns ->
                menuAddOns.addOnsRelations.forEach {addOnsRelations ->
                    if (addOnsRelations.isSelected == true){
                        set.add(addOnsRelations.id ?: 0)
                    }

                }

            }
          updateQuantity(item.menuItemId,item.id ?: 0, quantity, set)
        }

        holder.binding.txtCustomizeBtn.setSafeOnClickListener {
            itemId = item.id
            //item=(activity).cartItems?.get(position)
            val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewDialog: View = inflater.inflate(R.layout.sheet_update_item, null)
                val dialog = BottomSheetDialog(activity)
                dialog.setOnShowListener {
                    val bottomSheetDialogFragment =
                        dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    (viewDialog.parent as View).setBackgroundColor(ContextCompat.getColor(activity, R.color.transparent))
                    val behavior = bottomSheetDialogFragment?.let {
                        BottomSheetBehavior.from(it)
                    }
                    behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
                }
                val btnCancel = viewDialog.findViewById<ImageView>(R.id.imgCancel)
                val addToCartPopBt = viewDialog.findViewById<TextView>(R.id.addToCartPopBt)
                val txtCustomizeName = viewDialog.findViewById<TextView>(R.id.txtcustomizeName)
                val itemPrice = viewDialog.findViewById<TextView>(R.id.itemPrice)
                val txtCustomizeDesc = viewDialog.findViewById<TextView>(R.id.txtCustomizeDesc)
                val   txtTotalAddonsPrice = viewDialog.findViewById<TextView>(R.id.txtTotalAddonsPrice)
                val recyclerCustomizeItems = viewDialog.findViewById<RecyclerView>(R.id.recyclerCustomizeItems)

               // txtTotalAddonsPrice?.text= context.getString(R.string.total_Czk,item.price!!.plus(addOnPrices).toString())
             //   val totalPrices  = item.price!!.plus(addOnPrices)
                txtTotalAddonsPrice?.text= context.getString(R.string.total_Czk,DecimalFormat("#.##").format(item.price))
               dialog.setCancelable(false)
               txtCustomizeName.text = item.dishName

                itemPrice.text = item.priceWithQuantity.toString()
                txtCustomizeDesc.text = item.description.toString()


                btnCancel.setOnClickListener {
                    dialog.dismiss()
                  dataReset()
                    addToCartPopBt.alpha = 0.5f
                }

                val childMembersAdapter = arrayList?.let {
                    MenuAddOneCustomizeAdapter(
                        activity,
                        item.menuAddOns,
                        updateCart = { addOnsPrices, _, isPriceAdd, _, ->
                            if (addOnsPrices != null && itemPrice != null && isPriceAdd != null){
                                val price =  txtTotalAddonsPrice.text.toString()
                                val splitPrices:String
                                val parts = price.split(" ")
                                if (parts.size >= 4 && parts[0] == "Total" && parts[1] == "|" && parts[2] == "CZK") {
                                    // Reconstruct the string without the prefix
                                    splitPrices = parts.subList(3, parts.size).joinToString(" ")
                                    if (isPriceAdd){
                                        val itemQuantityPrices = addOnsPrices * item.quantity!!
                                        val totalPrice = splitPrices.toDouble().plus(itemQuantityPrices)
                                        val decimalFormat = DecimalFormat("#.##").format(totalPrice)
                                        txtTotalAddonsPrice?.text = context.getString(R.string.total_Czk,decimalFormat.toString())
                                    }
                                    else{
                                        val itemQuantityPrices = addOnsPrices * item.quantity!!
                                        val totalPrice = splitPrices.toDouble().minus(itemQuantityPrices)
                                        val decimalFormat = DecimalFormat("#.##").format(totalPrice)
                                        txtTotalAddonsPrice?.text =  context.getString(R.string.total_Czk,decimalFormat.toString())
                                    }

                                }

                            }
                            addToCartPopBt.alpha = 1f

                        }
                    )
                }
                recyclerCustomizeItems.adapter = childMembersAdapter

                addToCartPopBt.setOnClickListener {
                    if (addToCartPopBt.alpha == 1f) {
                        SvaggyApplication.progressBarLoader.start(context)

                        outer@ for ((index, menuAddOns) in item.menuAddOns.withIndex()) {
                            var tootleSelectedValue = 0
                            var isItemValid = true
                            var chooseUpto = 0

                            if (menuAddOns.isRequired == true) {

                                /**
                                 *  Handle the case when item is  required
                                 */
                                chooseUpto = menuAddOns.chooseUpto ?: 0

                                for (innerItems in menuAddOns.addOnsRelations) {
                                    if (innerItems.isSelected == true) {
                                        tootleSelectedValue++
                                    }
                                }

                                if (tootleSelectedValue >= chooseUpto) {
                                    if (index == item.menuAddOns.size - 1) {
                                        set = ArrayList()
                                        for (menuAddOnsInner in item.menuAddOns) {
                                            for (addOnsRelations in menuAddOnsInner.addOnsRelations) {
                                                if (addOnsRelations.isSelected == true) {
                                                    set.add(addOnsRelations.id ?: 0)
                                                }
                                            }
                                        }
                                        val quantity =
                                            holder.binding.txtItemCount.text.toString().toInt()
                                        updateAddOn(item.id ?: 0, quantity, set)
                                        dialog.dismiss()
                                    }

                                } else {
                                    isItemValid = false
                                }
                            } else {
                                /**
                                 *  Handle the case when item is not required
                                 */

                                chooseUpto = menuAddOns.chooseUpto ?: 0
                                for (innerItems in menuAddOns.addOnsRelations) {
                                    if (innerItems.isSelected == true) {
                                        tootleSelectedValue++
                                    }
                                }


                                if (tootleSelectedValue <= chooseUpto) {
                                    if (index == item.menuAddOns.size - 1) {
                                        set = ArrayList()
                                        for (menuAddOnsInner in item.menuAddOns) {
                                            for (addOnsRelations in menuAddOnsInner.addOnsRelations) {
                                                if (addOnsRelations.isSelected == true) {
                                                    set.add(addOnsRelations.id ?: 0)
                                                }
                                            }
                                        }
                                        val quantity =
                                            holder.binding.txtItemCount.text.toString().toInt()
                                        updateAddOn(item.id ?: 0, quantity, set)
                                        dialog.dismiss()
                                    }

                                } else {
                                    isItemValid = false
                                }

                            }
                            if (!isItemValid) {
                                SvaggyApplication.progressBarLoader.stop()
                                Toast.makeText(
                                    context,
                                    "Selected item does not meet requirements",
                                    Toast.LENGTH_SHORT
                                ).show()
                                break@outer // Break out of the outer loop
                            }
                        }


                    }







                }


//            addToCartPopBt.setOnClickListener {
//                set = ArrayList()
//                item.menuAddOns.forEach {menuAddOns ->
//                    menuAddOns.addOnsRelations.forEach {addOnsRelations ->
//                        if (addOnsRelations.isSelected!!){
//                            set.add(addOnsRelations.id!!)
//                        }
//
//                    }
//
//                }
//
//                if (Constants.requiredCounterValue > 0 || Constants.requiredWithChoseUp > 0){
//                    if (Constants.requiredWithChoseUp > 0)
//                        Toast.makeText(context,"Please select at least ${Constants.requiredWithChoseUp} add-on",
//                            Toast.LENGTH_SHORT).show()
//                    else
//                        Toast.makeText(context,"Please select at least ${Constants.requiredCounterValue} add-on",
//                            Toast.LENGTH_SHORT).show()
//                }else{
//
//                    val quantity = holder.binding.txtItemCount.text.toString().toInt()
//                    updateAddOn(item.id!!,quantity,set)
//                    dialog.dismiss()
////                    if (isCustomization && sortItemList != null){
////                        if (isDuplicate(newSet = set, repeatList = sortItemList)){
////                            val duplicateInfo = findDuplicateInfo(newSet = set, repeatList = sortItemList)
////                            val findIdDuplicateCartId = duplicateInfo?.itemId
////                            var quantity =  duplicateInfo?.quantity ?: 0
////                            val menuItemId =   duplicateInfo?.menuItemId ?: 0
////
////                            quantity++
////                            editRepeatCart(findIdDuplicateCartId ?: 0,quantity,menuItemId,adapterPosition,addCartBt,clAddSub,itemCount,false)
////                            dialog.dismiss()
////                        }
////                        else{
////                            placeOrder(itemId,set,adapterPosition,addCartBt,clAddSub,itemCount)
////                            dialog.dismiss()
////                            item.menuAddOns.forEach { menuAddOn ->
////                                menuAddOn.addOnsRelations
////                                menuAddOn.addOnsRelations.forEach { addOnRelation ->
////                                    addOnRelation.isSelected = false
////                                } }
////
////                        }
////
////
////                    }
////                    else{
////                        placeOrder(itemId,set,adapterPosition,addCartBt,clAddSub,itemCount)
////                        dialog.dismiss()
////                        item.menuAddOns.forEach { menuAddOn ->
////                            menuAddOn.addOnsRelations
////                            menuAddOn.addOnsRelations.forEach { addOnRelation ->
////                                addOnRelation.isSelected = false
////                            } }
////
////                    }
//
//
//
//
//
//
//
//
//
//                }
//
//            }
                dialog.setContentView(viewDialog)
                dialog.show()
            }
        }
    }

