package com.svaggy.ui.fragments.home.adapter

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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.svaggy.R
import com.svaggy.app.SvaggyApplication
import com.svaggy.databinding.ItemMenuBinding
import com.svaggy.ui.activities.restaurant.adapter.RepeatOrderAdapter
import com.svaggy.ui.activities.restaurant.model.DuplicateInfo
import com.svaggy.ui.fragments.home.model.RepeatOrderModel
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.requiredCounterValue
import com.svaggy.utils.Constants.requiredWithChoseUp
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.formatDouble
import com.svaggy.utils.hide
import com.svaggy.utils.invisible
import com.svaggy.utils.setSafeOnClickListener
import com.svaggy.utils.show
import java.text.DecimalFormat

class MenuItemAdapter(
    var context: Context,
    private val homeActivity: Activity,
    private var arrayList: ArrayList<RestaurantsMenuItem.Data.Categories.MenuItems>?,
    private val clearAllCart: () -> Unit,
    private val placeOrder: (itemId: Int, addOnList: ArrayList<Int>, adapterPosition: Int, addCartBt: ConstraintLayout, clAddSub: ConstraintLayout, itemCount: TextView) -> Unit,
    private val editCart: (itemId: Int, quantity: Int, cartId: Int?, adapterPosition: Int, addCartBt: ConstraintLayout, clAddSub: ConstraintLayout, itemCount: TextView) -> Unit,
    private val editRepeatCart: (cartId: Int, quantity: Int, menuItemId: Int, adapterPosition: Int, addCartBt: ConstraintLayout, clAddSub: ConstraintLayout, itemCount: TextView, isRemoveCart: Boolean) -> Unit,
    private val adapterPosition: Int,
) :
    RecyclerView.Adapter<BindingHolder<ItemMenuBinding>>() {
    private var itemCount = 0
    private var dialog: BottomSheetDialog? = null
    private lateinit var set:ArrayList<Int>





    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingHolder<ItemMenuBinding> {
        val binding = DataBindingUtil.inflate<ItemMenuBinding>(LayoutInflater.from(parent.context), R.layout.item_menu, parent, false)
     //   updateUI(binding)
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList?.size ?: 0
    }

    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    override fun onBindViewHolder(holder: BindingHolder<ItemMenuBinding>, position: Int) {
        val item = arrayList?.get(position)
        holder.binding.txtMoreDetail.visibility = View.VISIBLE
        val list = PrefUtils.instance.getMenuItemsList("${item?.id}")
        if (!list.isNullOrEmpty()){
            list.forEach {
               if (it.menuItemId == item!!.id){
                   holder.binding.addCartBt.invisible()
                   holder.binding.clAddSub.show()
                   holder.binding.txtItemCount.text = it.quantity.toString()
               }
            }

        }



        // This code is using product not available then visible txtNotAvailable textview
        if (item?.isActive == false) {
            holder.binding.addCartBt.setBackgroundResource(R.drawable.qty_add_bg)
            holder.binding.imgDish.visibility = View.VISIBLE
            holder.binding.addCartBt.isClickable=false
            holder.binding.txtNotAvailable.visibility = View.VISIBLE
            holder.binding.txtAdd.visibility = View.GONE
            holder.binding.imgAdd1.visibility = View.GONE
        }

        if (item?.menuItemImage.isNullOrEmpty()) {
            val set = ConstraintSet()
            set.clone(holder.binding.addCartBt)
            holder.binding.imgDish.hide()
            val param = holder.binding.constraintImage.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(0, 60, 80, 0)
            holder.binding.constraintImage.layoutParams = param
//            set.connect(R.id.textView105, ConstraintSet.TOP,R.id.textView105,ConstraintSet.RIGHT,0)
        }
        else {
            if (item?.isActive == true) {
                holder.binding.imgDish.visibility = View.VISIBLE
                    Glide.with(context)
                        .load(item.menuItemImage)
                        .placeholder(R.drawable.placeholder_cuisine)
                        .override(300, 200) // Example size, adjust as needed
                        .error(R.drawable.placeholder_cuisine)
                        .into(holder.binding.imgDish)
            }
            else {
                holder.binding.addCartBt.setBackgroundResource(R.drawable.qty_add_bg)
                holder.binding.imgDish.visibility = View.VISIBLE
                holder.binding.txtNotAvailable.visibility = View.VISIBLE
                holder.binding.txtAdd.visibility = View.GONE
                holder.binding.imgAdd1.visibility = View.GONE
                holder.binding.addCartBt.isClickable=false
                Glide.with(context).load(item?.menuItemImage)
                    .placeholder(R.drawable.placeholder_cuisine)
                    .error(R.drawable.placeholder_cuisine)
                    .into(holder.binding.imgDish)

                val matrix = ColorMatrix()
                matrix.setSaturation(0F)
                val filter = ColorMatrixColorFilter(matrix)
                holder.binding.imgDish.colorFilter = filter

            }
        }

        holder.binding.txtDishName.text = item?.dishName
        holder.binding.txtDishPrice.text = context.getString(R.string.curency_name) + formatDouble(item?.price ?: 0.0)
        holder.binding.txtActualDishPrice.text = context.getString(R.string.curency_name) + formatDouble(item?.actualPrice ?: 0.0)
        holder.binding.txtActualDishPrice.paintFlags = holder.binding.txtActualDishPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        holder.binding.constraintItemCart.setBackgroundResource(R.color.white)
        holder.binding.txtMoreDetail.setBackgroundResource(R.drawable.gray_curve_fourdp)
        holder.binding.txtMoreDetail.setTextColor(context.getColor(R.color.txt_gray))



        if (item?.price == item?.actualPrice) {
            holder.binding.txtActualDishPrice.visibility = View.GONE
        }
        else {
            holder.binding.txtActualDishPrice.visibility = View.VISIBLE
        }


        if (item?.menuAddOns?.size!! > 0) {
            holder.binding.txtCustomizable.visibility = View.VISIBLE
        }

        holder.binding.txtMoreDetail.setSafeOnClickListener {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewDialog: View = inflater.inflate(R.layout.sheet_more_details, null)
            dialog = BottomSheetDialog(context)
            dialog?.setOnShowListener {
                val bottomSheetDialogFragment =
                    dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
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
            val btnCancel = viewDialog.findViewById<ImageView>(R.id.imgCancel)
            val imgDishMoreDetail = viewDialog.findViewById<ImageView>(R.id.imgDishMoreDetail)
            val imgDishTypeMoreDetail = viewDialog.findViewById<ImageView>(R.id.imgDishTypeMoreDetail)
            val txtDishNameMoreDetail =
                viewDialog.findViewById<TextView>(R.id.txtDishNameMoreDetail)
            val txtDishPriceMoreDetail =
                viewDialog.findViewById<TextView>(R.id.txtDishPriceMoreDetail)
            val txtDishDescMoreDetail =
                viewDialog.findViewById<TextView>(R.id.txtDishDescMoreDetail)
            val txtAddToCart = viewDialog.findViewById<TextView>(R.id.txtAddCartMoreDetail)
            val txtItemPrice = viewDialog.findViewById<TextView>(R.id.txtItemPrice)
            val clAddQtyAdd = viewDialog.findViewById<ConstraintLayout>(R.id.clAddQtyAdd)
            val btQtyConfirm = viewDialog.findViewById<TextView>(R.id.btQtyConfirm)
            val txtItemCount = viewDialog.findViewById<TextView>(R.id.txt_item_count)
            val imgMinus = viewDialog.findViewById<ImageView>(R.id.imgMinus)
            val imgAdd = viewDialog.findViewById<ImageView>(R.id.imgAdd)


            Glide.with(context).load(item.menuItemImage)
                .placeholder(R.drawable.placeholder_cuisine).into(imgDishMoreDetail)
            when (item.dishType) {
                "Vegan" -> {
                    imgDishTypeMoreDetail.setImageResource(R.drawable.vegan)
                }

                "Veg" -> {
                    imgDishTypeMoreDetail.setImageResource(R.drawable.veg_icon)
                }

                "Non-Veg" -> {
                    imgDishTypeMoreDetail.setImageResource(R.drawable.nonveg_icon)
                }
            }

            txtDishNameMoreDetail.text = item.dishName
            txtDishPriceMoreDetail.text = context.getString(
                R.string.stringAdd, context.getString(R.string.curency_name) + item.price
            )
            txtDishDescMoreDetail.text = item.description

            btnCancel.setOnClickListener {
                dialog?.dismiss()
            }
            itemCount = holder.binding.txtItemCount.text.toString().toInt()
            if (item.isActive == true){
                txtAddToCart.alpha = 1f

            }else{
               // btQtyConfirm.alpha = 1f
                txtAddToCart.alpha = 0.5f
                val matrix = ColorMatrix()
                matrix.setSaturation(0F)
                val filter = ColorMatrixColorFilter(matrix)
                imgDishMoreDetail.colorFilter = filter

            }

            if (itemCount > 0) {
                clAddQtyAdd.visibility = View.VISIBLE
                btQtyConfirm.visibility = View.VISIBLE
                txtItemCount.text = holder.binding.txtItemCount.text
                txtItemCount.setTextColor(context.getColor(R.color.txt_black))

                txtAddToCart.visibility = View.GONE
                txtItemPrice.visibility = View.GONE
            }
            else {
                txtAddToCart.visibility = View.VISIBLE
                txtItemPrice.visibility = View.VISIBLE

                clAddQtyAdd.visibility = View.GONE
                btQtyConfirm.visibility = View.GONE
                txtItemCount.text = ContextCompat.getString(context, R.string._1)
            }
            imgAdd.setSafeOnClickListener {
                var quantity = txtItemCount.text.toString().toInt()
                quantity++
                txtItemCount.text = quantity.toString()
                editCart(item.id!!,quantity,item.menuItemId,adapterPosition,holder.binding.addCartBt,holder.binding.clAddSub,holder.binding.txtItemCount)

            }


            imgMinus.setSafeOnClickListener {
//                itemCount = txtItemCount.text.toString().toInt()
//                if (itemCount == 1) {
//                    clAddQtyAdd.visibility = View.VISIBLE
//                    btQtyConfirm.visibility = View.VISIBLE
//
//                    txtAddToCart.visibility = View.GONE
//                    txtItemPrice.visibility = View.GONE
//                    txtItemCount.text = itemCount.toString()
//                    txtItemCount.setTextColor(context.getColor(R.color.txt_black))
//                } else if (itemCount > 1) {
//                    itemCount = txtItemCount.text.toString().toInt() - 1
//                    txtItemCount.text = itemCount.toString()
//                    txtItemCount.setTextColor(context.getColor(R.color.txt_black))
//                }
                var quantity = txtItemCount.text.toString().toInt()
                quantity--
                txtItemCount.text = quantity.toString()
                editCart(item.id!!,quantity,item.menuItemId,adapterPosition,holder.binding.addCartBt,holder.binding.clAddSub,holder.binding.txtItemCount)
                if (quantity == 0){
                    dialog?.dismiss()
                }

            }

            txtAddToCart.setOnClickListener {
                if (txtAddToCart.alpha == 1f){
                    dialog?.dismiss()
                    if (item != null) {
                        funOpenCustomizeLayout(
                            item?.id ?: 0, item,
                            holder.binding.addCartBt,
                            adapterPosition,
                            holder.binding.clAddSub,
                            holder.binding.txtItemCount,
                            sortItemList = null
                        )
                    }

                }

            }

            btQtyConfirm.setOnClickListener {
                itemCount = txtItemCount.text.toString().toInt()

                holder.binding.txtItemCount.text = itemCount.toString()
                dialog?.dismiss()

            }

            dialog?.setContentView(viewDialog)
            dialog?.show()
        }

        holder.binding.constraintImage.setSafeOnClickListener {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewDialog: View = inflater.inflate(R.layout.sheet_more_details, null)
            dialog = BottomSheetDialog(context)
            dialog?.setOnShowListener {
                val bottomSheetDialogFragment =
                    dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
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
            val btnCancel = viewDialog.findViewById<ImageView>(R.id.imgCancel)
            val imgDishMoreDetail = viewDialog.findViewById<ImageView>(R.id.imgDishMoreDetail)
            val imgDishTypeMoreDetail = viewDialog.findViewById<ImageView>(R.id.imgDishTypeMoreDetail)
            val txtDishNameMoreDetail =
                viewDialog.findViewById<TextView>(R.id.txtDishNameMoreDetail)
            val txtDishPriceMoreDetail =
                viewDialog.findViewById<TextView>(R.id.txtDishPriceMoreDetail)
            val txtDishDescMoreDetail =
                viewDialog.findViewById<TextView>(R.id.txtDishDescMoreDetail)
            val txtAddToCart = viewDialog.findViewById<TextView>(R.id.txtAddCartMoreDetail)
            val txtItemPrice = viewDialog.findViewById<TextView>(R.id.txtItemPrice)
            val clAddQtyAdd = viewDialog.findViewById<ConstraintLayout>(R.id.clAddQtyAdd)
            val btQtyConfirm = viewDialog.findViewById<TextView>(R.id.btQtyConfirm)
            val txtItemCount = viewDialog.findViewById<TextView>(R.id.txt_item_count)
            val imgMinus = viewDialog.findViewById<ImageView>(R.id.imgMinus)
            val imgAdd = viewDialog.findViewById<ImageView>(R.id.imgAdd)


            Glide.with(context).load(item.menuItemImage)
                .placeholder(R.drawable.placeholder_cuisine).into(imgDishMoreDetail)
            when (item.dishType) {
                "Vegan" -> {
                    imgDishTypeMoreDetail.setImageResource(R.drawable.vegan)
                }

                "Veg" -> {
                    imgDishTypeMoreDetail.setImageResource(R.drawable.veg_icon)
                }

                "Non-Veg" -> {
                    imgDishTypeMoreDetail.setImageResource(R.drawable.nonveg_icon)
                }
            }

            txtDishNameMoreDetail.text = item.dishName
            txtDishPriceMoreDetail.text = context.getString(
                R.string.stringAdd, context.getString(R.string.curency_name) + item.price
            )
            txtDishDescMoreDetail.text = item.description

            btnCancel.setOnClickListener {
                dialog?.dismiss()
            }
            itemCount = holder.binding.txtItemCount.text.toString().toInt()
            if (item.isActive == true){
                txtAddToCart.alpha = 1f

            }else{
                // btQtyConfirm.alpha = 1f
                txtAddToCart.alpha = 0.5f
                val matrix = ColorMatrix()
                matrix.setSaturation(0F)
                val filter = ColorMatrixColorFilter(matrix)
                imgDishMoreDetail.colorFilter = filter

            }

            if (itemCount > 0) {
                clAddQtyAdd.visibility = View.VISIBLE
                btQtyConfirm.visibility = View.VISIBLE
                txtItemCount.text = holder.binding.txtItemCount.text
                txtItemCount.setTextColor(context.getColor(R.color.txt_black))

                txtAddToCart.visibility = View.GONE
                txtItemPrice.visibility = View.GONE
            }
            else {
                txtAddToCart.visibility = View.VISIBLE
                txtItemPrice.visibility = View.VISIBLE

                clAddQtyAdd.visibility = View.GONE
                btQtyConfirm.visibility = View.GONE
                txtItemCount.text = ContextCompat.getString(context, R.string._1)
            }
            imgAdd.setOnClickListener {
                var quantity = txtItemCount.text.toString().toInt()
                quantity++
                txtItemCount.text = quantity.toString()
                editCart(item.id!!,quantity,item.menuItemId,adapterPosition,holder.binding.addCartBt,holder.binding.clAddSub,holder.binding.txtItemCount)

            }


            imgMinus.setOnClickListener {
//                itemCount = txtItemCount.text.toString().toInt()
//                if (itemCount == 1) {
//                    clAddQtyAdd.visibility = View.VISIBLE
//                    btQtyConfirm.visibility = View.VISIBLE
//
//                    txtAddToCart.visibility = View.GONE
//                    txtItemPrice.visibility = View.GONE
//                    txtItemCount.text = itemCount.toString()
//                    txtItemCount.setTextColor(context.getColor(R.color.txt_black))
//                } else if (itemCount > 1) {
//                    itemCount = txtItemCount.text.toString().toInt() - 1
//                    txtItemCount.text = itemCount.toString()
//                    txtItemCount.setTextColor(context.getColor(R.color.txt_black))
//                }
                var quantity = txtItemCount.text.toString().toInt()
                quantity--
                txtItemCount.text = quantity.toString()
                editCart(item.id!!,quantity,item.menuItemId,adapterPosition,holder.binding.addCartBt,holder.binding.clAddSub,holder.binding.txtItemCount)
                if (quantity == 0){
                    dialog?.dismiss()
                }

            }

            txtAddToCart.setOnClickListener {
                if (txtAddToCart.alpha == 1f){
                    dialog?.dismiss()
                    if (item != null) {
                        funOpenCustomizeLayout(
                            item?.id ?: 0, item,
                            holder.binding.addCartBt,
                            adapterPosition,
                            holder.binding.clAddSub,
                            holder.binding.txtItemCount,
                            sortItemList = null
                        )
                    }

                }

            }

            btQtyConfirm.setOnClickListener {
                itemCount = txtItemCount.text.toString().toInt()

                holder.binding.txtItemCount.text = itemCount.toString()
                dialog?.dismiss()

            }

            dialog?.setContentView(viewDialog)
            dialog?.show()

        }


        holder.binding.addCartBt.setSafeOnClickListener {
            if (item.isActive == false) {
                holder.binding.addCartBt.isClickable=false
                holder.binding.addCartBt.setBackgroundResource(R.drawable.qty_add_bg)
            }
            else {
                if (PrefUtils.instance.getString(Constants.CartRestaurantId) == PrefUtils.instance.getString(Constants.MenutRestaurantId)
                    || PrefUtils.instance.getString(Constants.CartRestaurantId) == "" || PrefUtils.instance.getString(Constants.CartRestaurantId) == "null") {


                        funOpenCustomizeLayout(
                            itemId = item.id ?: 0,
                            item,
                            holder.binding.addCartBt,
                            adapterPosition,
                            holder.binding.clAddSub,
                            holder.binding.txtItemCount,
                            sortItemList = null)

                }
                else {
                    val inflater =
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val viewDialog: View = inflater.inflate(R.layout.sheet_delete_cart, null)
                    val dialog = BottomSheetDialog(context)
                    dialog.setOnShowListener {
                        val bottomSheetDialogFragment =
                            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
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
                    val clearCartBt = viewDialog.findViewById<TextView>(R.id.txtClearCart)
                    val txtCancel = viewDialog.findViewById<TextView>(R.id.txtCancel)
                    val cancelImage = viewDialog.findViewById<ImageView>(R.id.cancelImage)
                    txtCancel.setOnClickListener {
                        dialog.dismiss()
                    }
                    cancelImage.setOnClickListener {
                        dialog.dismiss()
                    }

                    clearCartBt.setOnClickListener {
                      clearAllCart()
                        dialog.dismiss()
                    }

                    dialog.setContentView(viewDialog)
                    dialog.show()
                }
            }
        }





        holder.binding.imgMinus.setSafeOnClickListener {
            var quantity = holder.binding.txtItemCount.text.toString().toInt()
            quantity--
            editCart(item.id ?:0,quantity,item.menuItemId,adapterPosition,holder.binding.addCartBt,holder.binding.clAddSub,holder.binding.txtItemCount)
        }

        holder.binding.imgAdd.setSafeOnClickListener {
            var quantity = holder.binding.txtItemCount.text.toString().toInt()
            quantity++
            editCart(item.id ?: 0,quantity,item.menuItemId,adapterPosition,holder.binding.addCartBt,holder.binding.clAddSub,holder.binding.txtItemCount)

        }

        // update code

//        holder.binding.imgAdd.setOnClickListener {
//            if ( item.menuAddOns.size > 0){
//                repeatOrder(item.id ?:0,item,holder.binding.addCartBt, adapterPosition,holder.binding.clAddSub,holder.binding.txtItemCount,true)
//
//            }
//            else{
//                var quantity = holder.binding.txtItemCount.text.toString().toInt()
//                quantity++
//                editCart(item.id!!,quantity,item.menuItemId,adapterPosition,holder.binding.addCartBt,holder.binding.clAddSub,holder.binding.txtItemCount)
//
//            }
//
//
//        }
//
//        holder.binding.imgMinus.setOnClickListener {
//            if ( item.menuAddOns.size > 0){
//                repeatOrder(item.id ?:0,item,holder.binding.addCartBt, adapterPosition,holder.binding.clAddSub,holder.binding.txtItemCount,false)
//
//            }else{
//                var quantity = holder.binding.txtItemCount.text.toString().toInt()
//                quantity--
//                editCart(item.id ?: 0,quantity,item.menuItemId,adapterPosition,holder.binding.addCartBt,holder.binding.clAddSub,holder.binding.txtItemCount)
//
//            }
//
//
//        }




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

    //    holder.binding.constraintItemCart.setOnClickListener {
//            goMenuScreen()
   //     }
    }


//    fun getPositionByValue(value: String?): Int {
//        return arrayList.indexOf(value)
//    }

    private fun repeatOrder(
        itemMenuId: Int,
        item: RestaurantsMenuItem.Data.Categories.MenuItems,
        addCartBt: ConstraintLayout,
        adapterPosition: Int,
        clAddSub: ConstraintLayout,
        itemCount:TextView,
        isShowAddNewBtn:Boolean) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewDialog: View = inflater.inflate(R.layout.repeat_order_dialog, null)
        val sortItemList = ArrayList<RepeatOrderModel>()
        val dialog = BottomSheetDialog(context)
        dialog.setOnShowListener {
            val bottomSheetDialogFragment =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
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
        val btnCancel = viewDialog.findViewById<ImageView>(R.id.imgCancel)
        val newOrderBtn = viewDialog.findViewById<TextView>(R.id.addNewBtn)
        val itemRc = viewDialog.findViewById<RecyclerView>(R.id.recyclerCustomizeItems)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        if (isShowAddNewBtn){
            newOrderBtn.show()
        }else{
            newOrderBtn.hide()
        }

        val list = PrefUtils.instance.getRepeatItemsList("repeatList")
        list?.forEach {data ->
            if (data.menuItemId == itemMenuId){
                sortItemList.add(data)
            }

        }

        newOrderBtn.setOnClickListener {
            dialog.dismiss()
            funOpenCustomizeLayout(
                    item.id ?: 0,
                   item,
                   addCartBt,
                    adapterPosition,
                    clAddSub,
                   itemCount,
                isCustomization = true,
                sortItemList)
        }


        val repeatOrderAdapter = RepeatOrderAdapter(
            list = sortItemList,
            bottomSheet = dialog,
            incrementItem = { quantity, cartId, menuItemId ->
                editRepeatCart(cartId,quantity,menuItemId,adapterPosition,addCartBt,clAddSub,itemCount,false)
            }
        ) { quantity, cartId, menuItemId, isRemoveCart ->
            editRepeatCart(
                cartId,
                quantity,
                menuItemId,
                adapterPosition,
                addCartBt,
                clAddSub,
                itemCount,
                isRemoveCart
            )
        }

        itemRc.adapter = repeatOrderAdapter

        dialog.setContentView(viewDialog)
        dialog.show()
    }



    private fun updateUI(binding: ItemMenuBinding) {
        val addCartBt = binding.addCartBt
        val clAddSub = binding.clAddSub
        val txtCount = binding.txtItemCount
    //    holder.binding.addCartBt.invisible()
    //    holder.binding.clAddSub.show()
     //   holder.binding.txtItemCount.text = it.quantity.toString()

    }

    @SuppressLint("MissingInflatedId", "SuspiciousIndentation")
    private fun funOpenCustomizeLayout(
        itemId: Int,
        item: RestaurantsMenuItem.Data.Categories.MenuItems,
        addCartBt: ConstraintLayout,
        adapterPosition: Int,
        clAddSub: ConstraintLayout,
        itemCount: TextView,
        isCustomization: Boolean = false,
        sortItemList: ArrayList<RepeatOrderModel>?) {

        //Preet
        if (item.menuAddOns.size > 0) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewDialog: View = inflater.inflate(R.layout.sheet_more_item, null)
            val dialog = BottomSheetDialog(context)
            dialog.setOnShowListener {
                val bottomSheetDialogFragment =
                    dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                (viewDialog.parent as View).setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.transparent))
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
            val recyclerCustomizeItems =
                viewDialog.findViewById<RecyclerView>(R.id.recyclerCustomizeItems)


            txtTotalAddonsPrice?.text =  context.getString(R.string.total_Czk,item.price.toString())

            txtCustomizeName.text = item.dishName

            itemPrice.text = context.getString(R.string.curency_name) + item.price
            if (item.description!!.isNotEmpty())
                txtCustomizeDesc.text = item.description.toString()
            else
                txtCustomizeDesc.hide()




            btnCancel.setOnClickListener {
                item.menuAddOns.forEach { menuAddOn ->
                    menuAddOn.addOnsRelations
                    menuAddOn.addOnsRelations.forEach { addOnRelation ->
                        addOnRelation.isSelected = false
                    } }

                dialog.dismiss()
                requiredWithChoseUp = 0
            }
            dialog.setOnDismissListener {
                item.menuAddOns.forEach { menuAddOn ->
                    menuAddOn.addOnsRelations
                    menuAddOn.addOnsRelations.forEach { addOnRelation ->
                        addOnRelation.isSelected = false
                    } }

                requiredWithChoseUp = 0

            }

            val childMembersAdapter = arrayList?.let {
                MenuAddOnsAdapter(
                    context,
                    item.menuAddOns,
                    updateCart = {addOnsPrices, _, isPriceAdd,_  ->
                        if (addOnsPrices != null && itemPrice != null && isPriceAdd != null){
                            val price = txtTotalAddonsPrice.text.toString()
                            val splitPrices:String
                            val parts = price.split(" ")
                            if (parts.size >= 4 && parts[0] == "Total" && parts[1] == "|" && parts[2] == "CZK") {
                                // Reconstruct the string without the prefix
                                splitPrices = parts.subList(3, parts.size).joinToString(" ")
                                if (isPriceAdd){
                                    val totalPrice = splitPrices.toDouble().plus(addOnsPrices)
                                    val decimalFormat = DecimalFormat("#.##").format(totalPrice)
                                    txtTotalAddonsPrice?.text =  context.getString(R.string.total_Czk,decimalFormat.toString())
                                }
                                else{
                                    val totalPrice = splitPrices.toDouble().minus(addOnsPrices)
                                    val decimalFormat = DecimalFormat("#.##").format(totalPrice)
                                    txtTotalAddonsPrice?.text =  context.getString(R.string.total_Czk,decimalFormat.toString())
                                }

                        }

                        }
                    }
                )
            }
            recyclerCustomizeItems.adapter = childMembersAdapter

            addToCartPopBt.setOnClickListener {
                set = ArrayList()
                item.menuAddOns.forEach {menuAddOns ->
                    menuAddOns.addOnsRelations.forEach {addOnsRelations ->
                        if (addOnsRelations.isSelected!!){
                            set.add(addOnsRelations.id!!)
                        }

                    }

                }

                if (requiredCounterValue > 0 || requiredWithChoseUp > 0){
                    if (requiredWithChoseUp > 0)
                        Toast.makeText(context,"Please select at least $requiredWithChoseUp add-on",Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(context,"Please select at least $requiredCounterValue add-on",Toast.LENGTH_SHORT).show()
                }else{
                    if (isCustomization && sortItemList != null){
                        if (isDuplicate(newSet = set, repeatList = sortItemList)){
                            val duplicateInfo = findDuplicateInfo(newSet = set, repeatList = sortItemList)
                            val findIdDuplicateCartId = duplicateInfo?.itemId
                            var quantity =  duplicateInfo?.quantity ?: 0
                            val menuItemId =   duplicateInfo?.menuItemId ?: 0
                            quantity++
                            editRepeatCart(findIdDuplicateCartId ?: 0,quantity,menuItemId,adapterPosition,addCartBt,clAddSub,itemCount,false)
                            dialog.dismiss()
                        }
                        else{
                            SvaggyApplication.progressBarLoader.start(context)
                            placeOrder(itemId,set,adapterPosition,addCartBt,clAddSub,itemCount)
                            dialog.dismiss()
                            item.menuAddOns.forEach { menuAddOn ->
                                menuAddOn.addOnsRelations
                                menuAddOn.addOnsRelations.forEach { addOnRelation ->
                                    addOnRelation.isSelected = false
                                }
                            }

                        }


                    }else{
                        SvaggyApplication.progressBarLoader.start(context)
                        placeOrder(itemId,set,adapterPosition,addCartBt,clAddSub,itemCount)
                        dialog.dismiss()
                        item.menuAddOns.forEach { menuAddOn ->
                            menuAddOn.addOnsRelations
                            menuAddOn.addOnsRelations.forEach { addOnRelation ->
                                addOnRelation.isSelected = false
                            }
                        }

                    }
                }

            }
            dialog.setContentView(viewDialog)
            dialog.show()
        }
        else{
            SvaggyApplication.progressBarLoader.start(context)
            set = ArrayList()
            placeOrder(itemId,set,adapterPosition,addCartBt,clAddSub,itemCount)
            item.menuAddOns.forEach { menuAddOn ->
                menuAddOn.addOnsRelations
                menuAddOn.addOnsRelations.forEach { addOnRelation ->
                    addOnRelation.isSelected = false
                } }
        }
    }


    private fun findDuplicateInfo(newSet: List<Int>, repeatList: List<RepeatOrderModel>): DuplicateInfo? {
        return repeatList.find { existingItem ->
            val existingAddOnIds = existingItem.addOns.map { it.addOnId }
            newSet.containsAll(existingAddOnIds) && existingAddOnIds.containsAll(newSet)
        }?.let { duplicateItem ->
            DuplicateInfo(
                itemId = duplicateItem.itemId,
                menuItemId = duplicateItem.menuItemId,
                quantity = duplicateItem.quantity
            )
        }
    }


    // Function to check if the new add-on set is already in the repeat list
    private fun isDuplicate(newSet: List<Int>, repeatList: List<RepeatOrderModel>): Boolean {
        return repeatList.any { existingItem ->
            val existingAddOnIds = existingItem.addOns.map { it.addOnId }
            newSet.containsAll(existingAddOnIds) && existingAddOnIds.containsAll(newSet)
        }
    }
    private fun findDuplicateId(newSet: List<Int>, repeatList: List<RepeatOrderModel>): Int? {
        return repeatList.find { existingItem ->
            val existingAddOnIds = existingItem.addOns.map { it.addOnId }
            newSet.containsAll(existingAddOnIds) && existingAddOnIds.containsAll(newSet)
        }?.itemId
    }
    private fun findDuplicateMenuItemId(newSet: List<Int>, repeatList: List<RepeatOrderModel>): Int? {
        return repeatList.find { existingItem ->
            val existingAddOnIds = existingItem.addOns.map { it.addOnId }
            newSet.containsAll(existingAddOnIds) && existingAddOnIds.containsAll(newSet)
        }?.menuItemId
    }
    private fun findDuplicateQuantity(newSet: List<Int>, repeatList: List<RepeatOrderModel>): Int? {
        return repeatList.find { existingItem ->
            val existingAddOnIds = existingItem.addOns.map { it.addOnId }
            newSet.containsAll(existingAddOnIds) && existingAddOnIds.containsAll(newSet)
        }?.quantity
    }

    }