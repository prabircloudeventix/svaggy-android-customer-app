package com.svaggy.ui.fragments.cart.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
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
import com.svaggy.R
import com.svaggy.databinding.ItemAddMoreBinding
import com.svaggy.ui.fragments.cart.model.ViewCart
import com.svaggy.ui.fragments.home.adapter.MenuAddOneCustomizeAdapter
import com.svaggy.ui.fragments.home.adapter.MenuAddOnsAdapter
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.hide
import com.svaggy.utils.setSafeOnClickListener
import java.text.DecimalFormat

class  MoreItemAdapter(
    val context: Context,
    private var arrayList: ArrayList<ViewCart.Data.CartItems>?,
    private val updateCartItem: (cartId: Int, quantity: Int,addOns: ArrayList<Int>) -> Unit,
    private val updateAddOn: (cartId: Int, quantity: Int, addOns: ArrayList<Int>) -> Unit,
    private val dataReset:() -> Unit)
    : RecyclerView.Adapter<BindingHolder<ItemAddMoreBinding>>() {
    var quantity: Int? = 0
    private var itemIdGet: Int? = 0
    private lateinit var set : ArrayList<Int>

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BindingHolder<ItemAddMoreBinding> {
        val binding = DataBindingUtil.inflate<ItemAddMoreBinding>(
            LayoutInflater.from(parent.context), R.layout.item_add_more, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList?.size ?: 0
    }

    @SuppressLint("StringFormatInvalid")
    override fun onBindViewHolder(holder: BindingHolder<ItemAddMoreBinding>, position: Int) {
        val item = arrayList?.get(position)
        var addOnPrices = 0.0
        var addOnActualPrices = 0.0
        val selectedItems = mutableListOf<String>()

        if (item != null && item.menuAddOns.isEmpty())
            holder.binding.txtCustomizeBtn.hide()


        item?.menuAddOns?.forEach { menuAddOn ->
            menuAddOn.addOnsRelations
            if (menuAddOn.addOnsRelations.isEmpty()){
                holder.binding.txtCustomizeBtn.hide()
            }
            menuAddOn.addOnsRelations.forEach { addOnRelation ->
                if (addOnRelation.isSelected == true) {
                   addOnPrices += addOnRelation.price ?: 0.0
                    addOnActualPrices += addOnRelation.actualPrice ?: 0.0
                    addOnRelation.itemName?.let {
                        selectedItems.add(it)
                    }
                }
            }
        }

//        val itemActualPrices = item!!.actualPrice!!.plus(addOnActualPrices)
//        val disCountPrices = item.price!!.plus(addOnPrices)

        val itemActualPrices = item?.actualPrice ?: 0.0
        val disCountPrices = item?.price ?: 0.0






        holder.binding.txtDishPrice.text = context.getString(R.string.curencyWith, DecimalFormat("#.##").format(disCountPrices))
        holder.binding.txtActualDishPrice.text = context.getString(R.string.curencyName, DecimalFormat("#.##").format(itemActualPrices))
        val selectedItemsString = selectedItems.joinToString(separator = ", ")
        holder.binding.txtDishDesc.text = selectedItemsString
        if (disCountPrices < itemActualPrices){
            holder.binding.txtActualDishPrice.text =  context.getString(R.string.curencyWith,DecimalFormat("#.##").format(itemActualPrices))
            holder.binding.txtActualDishPrice.paintFlags = holder.binding.txtActualDishPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }




        when (item?.dishType) {
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
        quantity = item?.quantity
        holder.binding.txtItemCount.text = quantity.toString()
        holder.binding.txtDishName.text = item?.dishName






        holder.binding.imgMinus.setSafeOnClickListener {
            set = ArrayList()
            item?.menuAddOns?.forEach {menuAddOns ->
                menuAddOns.addOnsRelations.forEach {addOnsRelations ->
                    if (addOnsRelations.isSelected == true){
                        set.add(addOnsRelations.id ?: 0)
                    }

                }

            }

            var quantity = holder.binding.txtItemCount.text.toString().toInt()
            quantity--
            updateCartItem(item?.id ?: 0,quantity,set)

        }

        holder.binding.imgAdd.setSafeOnClickListener {
            set = ArrayList()
            item?.menuAddOns?.forEach {menuAddOns ->
                menuAddOns.addOnsRelations.forEach {addOnsRelations ->
                    if (addOnsRelations.isSelected == true){
                        set.add(addOnsRelations.id ?: 0)
                    }

                }

            }
            var quantity = holder.binding.txtItemCount.text.toString().toInt()
            quantity++
            updateCartItem(item?.id ?: 0,quantity,set)

        }

        holder.binding.txtCustomizeBtn.setSafeOnClickListener {
            itemIdGet = item?.id

            if (item?.menuAddOns?.size!! > 0) {
                val inflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val viewDialog: View = inflater.inflate(R.layout.sheet_update_new, null)
                val dialog = BottomSheetDialog(context)
                dialog.setOnShowListener {
                    val bottomSheetDialogFragment =
                        dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    (viewDialog.parent as View).setBackgroundColor(
                        ContextCompat.getColor(
                            context, R.color.transparent
                        )
                    )
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
                val recyclerCustomizeItems =
                    viewDialog.findViewById<RecyclerView>(R.id.recyclerCustomizeItems)
               val  txtTotalAddonsPrice = viewDialog.findViewById<TextView>(R.id.txtTotalAddonsPrice)
                txtCustomizeName.text = item.dishName
                itemPrice.text = item.priceWithQuantity
                txtCustomizeDesc.text = item.description.toString()
                //val totalPrices  = item.price!!.plus(addOnPrices)
                txtTotalAddonsPrice?.text= context.getString(R.string.total_Czk,DecimalFormat("#.##").format(item.price))

                btnCancel.setOnClickListener {
                    dataReset()
                    dialog.dismiss()
                    addToCartPopBt.alpha = 0.5f
                }

                val childMembersAdapter = arrayList?.let {
                    MenuAddOneCustomizeAdapter(
                      context = context,
                      menuAddOns =  item.menuAddOns,
                        updateCart = {addOnsPrices, _, isPriceAdd,_  ->
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

                dialog.setContentView(viewDialog)
                dialog.show()
            }
        }
    }

    }