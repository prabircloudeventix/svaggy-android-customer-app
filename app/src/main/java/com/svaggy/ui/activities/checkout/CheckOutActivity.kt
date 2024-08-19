package com.svaggy.ui.activities.checkout

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.DatePicker
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.stripe.android.PaymentConfiguration
import com.stripe.android.googlepaylauncher.GooglePayPaymentMethodLauncher
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.app.SvaggyApplication
import com.svaggy.client.models.AddOnsModel
import com.svaggy.client.models.MenuItem
import com.svaggy.databinding.ActivityCheckOutBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.editprofile.EditProfileActivity
import com.svaggy.ui.activities.home_location.OnMapLocationActivity
import com.svaggy.ui.activities.restaurant.CouponActivity
import com.svaggy.ui.activities.restaurant.RestaurantMenuActivity
import com.svaggy.ui.activities.track.OrderConfirmedActivity
import com.svaggy.ui.activities.wallet.AddWalletActivity
import com.svaggy.ui.fragments.cart.adapter.BillDetailAdapter
import com.svaggy.ui.fragments.cart.adapter.MoreItemAdapter
import com.svaggy.ui.fragments.cart.adapter.OrderSlotAdapter
import com.svaggy.ui.fragments.cart.scroll_choice.ScrollChoice
import com.svaggy.ui.fragments.cart.viewmodel.CartViewModel
import com.svaggy.ui.fragments.home.model.SharedPrefModel
import com.svaggy.ui.fragments.location.adapter.UserAddressAdapter
import com.svaggy.ui.fragments.location.model.GetAddress
import com.svaggy.ui.fragments.payment.viewmodel.PaymentViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.ADD_PROMO
import com.svaggy.utils.Constants.COMPLETE_BY_WALLET
import com.svaggy.utils.Constants.CONVERSION
import com.svaggy.utils.Constants.DELIVERY_BY_RESTAURANT
import com.svaggy.utils.Constants.DELIVERY_BY_SVAGGY
import com.svaggy.utils.Constants.NOW
import com.svaggy.utils.Constants.PICKUP_ONLY
import com.svaggy.utils.Constants.REMOVE_PROMO
import com.svaggy.utils.Constants.SCHEDULED
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.invisible
import com.svaggy.utils.prettyPrint
import com.svaggy.utils.setSafeOnClickListener
import com.svaggy.utils.show
import com.svaggy.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class CheckOutActivity : BaseActivity<ActivityCheckOutBinding>(ActivityCheckOutBinding::inflate) {
    private var moreItemAdapter: MoreItemAdapter? = null
    var dialog: BottomSheetDialog? = null
    private var viewDialog: View? = null
    private var deliveryDate: String = ""
    var deliveryTime: String = ""
    private var checkoutId: String? = null
    var restaurantId: String= ""
    private var broadCastId: String? = null
    private lateinit var paymentSheet: PaymentSheet
    private var paymentIntentClientSecret: String = ""
    private var paymentIntentId: String  = ""
    private lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    private val mViewModel by viewModels<CartViewModel>()
    private val viewModel by viewModels<PaymentViewModel>()
    private val mViewModelOrder by viewModels<PaymentViewModel>()
    private  var couponId:String? = null
    private lateinit var userAddressAdapter: UserAddressAdapter
    private  var totalAmount:Double? = null
    private var deliveryType: String? = null
    private var currentDeliveryType: String = ""
    private lateinit var orderSlotAdapter: OrderSlotAdapter
    private var timeList:ArrayList<String> = ArrayList()
    private  var dateSelectDialog: BottomSheetDialog? = null
    private var selectDeliveryTime = ""
    private var isOrderTrack = true
    private var addressId:String? = null
    private var pubBoosterArray:ArrayList<Int>? = null
    //



    private val addWalletLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        getCheckoutData(isSetDeliveryType = false)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun ActivityCheckOutBinding.initialize(){
        restaurantId = intent.getStringExtra("item_id").toString()
        broadCastId = intent.getStringExtra(Constants.BROADCAST_ID)
        deliveryType = intent.getStringExtra(Constants.DELIVERY_TYPE)
        paymentSheet = PaymentSheet(this@CheckOutActivity, ::onPaymentSheetResult)

        couponGetObserver()
        orderCreateObserver()
        // For Clear Cart Count  Icon

        orderSlotAdapter = OrderSlotAdapter()
        val dateToday = PrefUtils.instance.getCurrentDateTime()
        val locale: Locale = Locale.getDefault()
        val formatter = SimpleDateFormat("dd-MM-yyyy", locale)
        deliveryDate = formatter.format(dateToday)
        deliveryTime = PrefUtils.instance.getCurrentTime24HourFormat()
        binding.dateSelect.text = deliveryDate
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        initBinding(formatter,dateToday)
        getCheckoutData(isSetDeliveryType = true)


    }

    override fun onResume() {
        super.onResume()
        mProgressBar().dialog?.dismiss()

    }
    private fun orderCreateObserver() {
        mViewModelOrder.addCartDataLive.observe(this) {
            if (it.is_success == true) {
                mProgressBar().dialog?.dismiss()
                PrefUtils.instance.clearCartItemsFromSharedPreferences()
                PrefUtils.instance.setString(Constants.DeliveryDate,"")
                PrefUtils.instance.setString(Constants.DeliveryTime,"")
                val orderId = it.data?.order_id
                if (broadCastId != null && !broadCastId.equals("null")){
                    setBroadCast(broadCastId ?: "",orderId)}


                // setBoosterAction(boosterArray,orderId)

//                    val action = CartMoreItemScreenDirections.actionCartMoreItemScreenToOrderConfirmedScreen(
//                        orderId = orderId.toString(),
//                        checkOrderType = isOrderTrack,
//                        boosterArray = pubBoosterArray!!.toIntArray(),
//                        restaurantId = restaurantId!!)
                    val intent = Intent(this,OrderConfirmedActivity::class.java)
                    intent.putExtra("orderId",orderId.toString())
                    intent.putExtra("checkOrderType",isOrderTrack)
                    if (!pubBoosterArray.isNullOrEmpty()){
                    intent.putExtra("boosterArray",pubBoosterArray)}
                   intent.putExtra("restaurantId",restaurantId)
                 intent.putExtra("deliveryType",deliveryType)
                    startActivity(intent)
                    finish()
                   // findNavController().navigate(action)


            }
            else {
                mProgressBar().dialog?.dismiss()
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun couponGetObserver() {
        mViewModel.setOffersDataLive.observe(this) {
            if (it.isSuccess == true) {
                getCheckoutData()

            } else {
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateCart(cartId: Int, quantity: Int, position: Int,menuItemId:Int?) {
        lifecycleScope.launch {
            mViewModel.updateCartItem(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                cartId = cartId,
                quantity = quantity).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        mProgressBar().showProgressBar(this@CheckOutActivity)


                    }
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response != null) {
                            if (response.isSuccess == true){
                                getCheckoutData()
//                                if (quantity == 0){
//                                    PrefUtils.instance.removeMenuItemsList("$menuItemId")
//                                    startActivity(Intent(this@CheckOutActivity,MainActivity::class.java)
//                                        .putExtra("isFrom","ReOrderScreen")
//                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//                                }else{
//                                    getCheckoutData()
//
//                                }
                            }


                        }
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(this@CheckOutActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }

    }
    @SuppressLint("SuspiciousIndentation", "SetTextI18n", "UseCompatLoadingForDrawables")
    private fun getCheckoutData(isSetDeliveryType:Boolean = false) {

        val map = mutableMapOf(
            "latitude" to (PrefUtils.instance.getString(Constants.Latitude) ?: ""),
            "longitude" to (PrefUtils.instance.getString(Constants.Longitude) ?: ""),
            "restaurant_id" to (PrefUtils.instance.getString("restaurantId") ?: "")
        )

        if (addressId != null)
            map["address_id"] = addressId ?: ""


        lifecycleScope.launch {
            mViewModel.getCheckOutDetailsNew(
                token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                       SvaggyApplication.progressBarLoader.start(this@CheckOutActivity)
                    }
                    is ApiResponse.Success -> {
                        SvaggyApplication.progressBarLoader.stop()
                        val response = it.data
                        if (response != null) {
                            if (response.is_success) {
                                binding.titleTv.text = response.data.cart_data?.restaurant_details?.restaurant_name
                                checkoutId = response.data.checkout_id.toString()
                                deliveryType = response.data.cart_data?.restaurant_details?.delivery_type.toString()
                                currentDeliveryType = response.data.current_delivery_type.toString()
                                val restaurantAmount = response.data.cart_data?.restaurant_details?.delivery_cart_minimum_amount

//                                when(currentDeliveryType){
//                                    "PICKUP_ONLY" ->{
//                                        if (isSetDeliveryType)
//                                            setDeliveryType(deliveryType =  "PICKUP_ONLY",false)
//                                            binding.checkBoxPick.setCompoundDrawablesWithIntrinsicBounds(this@CheckOutActivity.getDrawable(R.drawable.ic_red_checbox),null,null,null)
//                                           binding.checkBoxDelivery.setCompoundDrawablesWithIntrinsicBounds(this@CheckOutActivity.getDrawable(R.drawable.ic_checbox),null,null,null)
//                                           binding.addressBar.hide()
//                                          binding.rushCard.hide()
//                                          binding.delayCard.hide()
//                                    }
//                                    else ->{
//                                        if (isSetDeliveryType){
//                                            setDeliveryType(deliveryType = deliveryType ?: "DELIVERY_BY_SVAGGY",false)
//                                            binding. checkBoxDelivery.setCompoundDrawablesWithIntrinsicBounds(this@CheckOutActivity.getDrawable(R.drawable.ic_red_checbox),null,null,null)
//                                            binding.checkBoxPick.setCompoundDrawablesWithIntrinsicBounds(this@CheckOutActivity.getDrawable(R.drawable.ic_checbox),null,null,null)
//                                            binding.checkBoxDelivery.isClickable = false
//
//                                        }
//                                    }
//                                }

                                when(deliveryType){
                                    DELIVERY_BY_SVAGGY  ->{
                                        binding.tv.show()
                                        binding.cartAddressCl.show()
                                        binding.showPickText.hide()
                                        binding.tvDeliverByRestaurant.hide()

                                    }
                                    DELIVERY_BY_RESTAURANT ->{
                                        binding.tv.show()
                                        binding.cartAddressCl.show()
                                        binding.showPickText.hide()
                                        binding.tvDeliverByRestaurant.show()
                                    }

                                    PICKUP_ONLY ->{
                                        isOrderTrack = false
                                        binding.tv.hide()
                                        binding.totalAmountCl.show()
                                        binding.cartAddressCl.hide()
                                        binding.showPickText.show()
                                        binding.tvDeliverByRestaurant.hide()
                                    }
                                }

                                if (response.data.cart_data?.cart_items != null) {
                                    PrefUtils.instance.clearCartItemsFromSharedPreferences()
                                    response.data.cart_data.cart_items.forEach {item ->
                                        val listOf = listOf(
                                            SharedPrefModel(item.dishName ?: "",
                                                item.dishType ?: "",
                                                item.price ?: 0.0,
                                                item.isActive ?: false,
                                                item.menuItemId ?: 0,
                                                item.actualPrice?: 0.0,
                                                item.quantity ?: 0,
                                                item.id ?: 0)
                                        )
                                        PrefUtils.instance.saveMenuItemsList("${item.menuItemId}",listOf)
                                    }



                                    moreItemAdapter = MoreItemAdapter(
                                            context =  this@CheckOutActivity,
                                            arrayList =  response.data.cart_data.cart_items,
                                            updateCartItem =  {cartId, quantity, addOns,  ->
                                                updateAddOn2(cartId = cartId, quantity = quantity, addOns =addOns)
                                                              },
                                            updateAddOn = {cartId, quantity, addOns ->
                                                updateAddOn2(cartId = cartId, quantity = quantity, addOns =addOns)
                                              //  updateAddOn(cartId = cartId, quantity = quantity, addOns =addOns)
                                                          },
                                            dataReset = { getCheckoutData() })


                                    binding.txtWalletBalance.text = getString(
                                        R.string.priceRtn,
                                        "Balance " + response.data.currency_key + " " + response.data.wallet_amount
                                    )

                                    if (response.data.coupon?.coupon_code != null){
                                        binding.applyPromo.hide()
                                        binding.applyCouponCl.hide()
                                        binding.couponSelectConfirm.visibility = View.VISIBLE
                                        binding.couponCodeName.text = response.data.coupon.coupon_code
                                        binding.couponDiscountAmount.text = getString(
                                            R.string.priceRtn, response.data.currency_key + " " + response.data.coupon.amount
                                        )
                                        if (response.data.coupon.is_promo == null)
                                        couponId = response.data.coupon.id.toString()
                                        if (response.data.coupon.is_promo == true){
                                            binding.showPromoTv.text = ContextCompat.getString(this@CheckOutActivity, R.string.debited_from_total_amount)
                                        }else{
                                            binding.showPromoTv.text = ContextCompat.getString(this@CheckOutActivity, R.string.promo_code_saving)
                                        }

                                    }
                                    else{
                                        if (response.data.promo_details?.promo_code != null){
                                            binding.applyPromo.show()
                                            binding.applyCouponCl.hide()
                                            binding.applyPromo.visibility = View.VISIBLE
                                            binding.getPromoCode.text = response.data.promo_details.promo_code
                                            binding.getPromoBalance.text = response.data.promo_details.promo_balance.toString()
                                            binding.getDiscountText.text = response.data.promo_details.text
                                            binding.couponSelectConfirm.visibility = View.GONE
                                            binding.promoCounter.text = getString(R.string.promo_couter,response.data.promo_count.toString())

                                            val text = response.data.promo_details.promo_amount.toString()
                                            val typeface = ResourcesCompat.getFont(this@CheckOutActivity, R.font.jostbold) // Ensure you have the font in your resources
                                            val colorText = SpannableStringBuilder().bold{
                                                color(ContextCompat.getColor(this@CheckOutActivity, R.color.green_clr)) {
                                                    append(text).apply {
                                                        setSpan(
                                                            object : ClickableSpan() {
                                                                override fun onClick(p0: View) {

                                                                }

                                                                override fun updateDrawState(ds: TextPaint) {

                                                                }
                                                            },
                                                            0,
                                                            length,
                                                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                                        )
                                                    }
                                                }
                                            }
                                            val s = SpannableStringBuilder()
                                                .append("You will get ")
                                                .append( colorText )
                                                .append(" off on this order")

                                            binding.getDiscountText.text = s
                                        }else{
                                            binding.applyPromo.hide()
                                            binding.couponSelectConfirm.hide()
                                            binding.applyCouponCl.show()
                                            binding.counterCoupon.text = getString(R.string.promo_couter,response.data.promo_count.toString())

                                        }


                                    }
//

                                    binding.recyclerMoreItem.adapter = moreItemAdapter

                                    restaurantId= response.data.cart_data.restaurant_details.id.toString()

                                    if (response.data.delivery_address.id != 0) {
                                        //   binding.cartAddressCl.visibility = View.VISIBLE
                                        if (deliveryType.equals("DELIVERY_BY_SVAGGY") || deliveryType.equals("DELIVERY_BY_RESTAURANT")){
                                            binding.cartAddressCl.show()
                                        }
                                        //  binding.addressBar.show()
                                        //   binding.totalAmountCl.show()
                                        if (response.data.delivery_address.address_type == "Work"){
                                            binding.imageView25.setImageResource(R.drawable.ic_work_new)
                                        }else{
                                            binding.imageView25.setImageResource(R.drawable.ic_home)
                                        }
                                        binding.tvHome.text = response.data.delivery_address.address_type
                                        binding.txtAddressShow.text = response.data.delivery_address.complete_address
                                    }
                                    else {
                                        if (deliveryType.equals("DELIVERY_BY_SVAGGY") || deliveryType.equals("DELIVERY_BY_RESTAURANT"))
                                            getUserAddress()
                                        // binding.addressBar.show()
                                        // binding.cartAddressCl.visibility = View.GONE
                                        // runAddressSelectSheet("openAddressList")
                                    }

                                    binding.tvHome.setSafeOnClickListener {btn ->
                                        getUserAddress(btn)

                                    }

                                    val billDetailsAdapter = BillDetailAdapter(
                                       this@CheckOutActivity, response.data.bill_details,:: walletAvailable)

                                    binding.recyclerBillDetails.adapter = billDetailsAdapter
                                    totalAmount = response.data.cart_data.total_amount.toString().prettyPrint(response.data.cart_data.total_amount).toDouble()
                                    binding.txtTotalAmount.text = "CZK ${totalAmount.toString()}"
                                    binding.txtTotalPrice.text = "CZK $totalAmount"
                                  //  bundle.putString("total_amount",binding.txtTotalPrice.text.toString())
                                    if (restaurantAmount!! <= totalAmount!!){
                                        binding.totalAmountCl.show()
                                    }else{
                                        binding.totalAmountCl.hide()
                                        binding.root.showSnackBar(this@CheckOutActivity.getString(R.string.low_amount))

                                    }
                                }
                                else {
                                    PrefUtils.instance.clearCartItemsFromSharedPreferences()
                                    startActivity(Intent(this@CheckOutActivity,MainActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("item_id",restaurantId.toString())
                                        .putExtra("isFrom","ReOrderScreen"))
                                    finish()
//

                                }
                                if (response.data.cart_data?.restaurant_details?.is_rush_mode != null){
                                    if (response.data.cart_data.restaurant_details.is_rush_mode){
                                        binding.rushCard.show()
                                        binding.delayCard.hide()
                                    }
                                    else{
                                        if (deliveryType != PICKUP_ONLY && isOrderTrack){
                                            binding.rushCard.invisible()
                                            binding.delayCard.show()
                                        }else{
                                            binding.rushCard.hide()
                                            binding.delayCard.hide()

                                        }

                                    }

                                }

                            }
                            else {
                               mProgressBar().dialog?.dismiss()
                                Toast.makeText(this@CheckOutActivity, "" + response.message, Toast.LENGTH_SHORT).show()
                            }
                            if (response.data.cart_data != null){
                                if (!response.data.cart_data.restaurant_details.booster_ids.isNullOrEmpty()){
                                    pubBoosterArray = ArrayList()
                                    val boosterArray = response.data.cart_data.restaurant_details.booster_ids
                                    boosterArray.forEach {booster ->
                                        pubBoosterArray!!.add(booster)
                                    }
                                }

                            }

                        }

                    }
                    is ApiResponse.Failure -> {
                        SvaggyApplication.progressBarLoader.stop()

                    }
                }
            }
        }
    }
    private fun updateAddOn(cartId: Int, quantity: Int,addOns: ArrayList<Int>){
        lifecycleScope.launch {
            mViewModel.updateAddOn(token ="${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                cartId = cartId,
                quantity = quantity,
                addOns =addOns).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response != null){
                            if (response.isSuccess == true) {
                                getCheckoutData()
                            }
                        }
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(this@CheckOutActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }

    private fun updateAddOn2(cartId: Int, quantity: Int,addOns: ArrayList<Int>){
        val addOnsToSend = if (addOns.isEmpty()) arrayListOf() else addOns



        val addOnsModel = AddOnsModel(
            menu_item = MenuItem(
                quantity = quantity,
                cart_id = cartId,
                add_ons = addOnsToSend
            )
        )



        lifecycleScope.launch {
            mViewModel.updateAddOn2(
                token ="${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                addOnModel =addOnsModel).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {

                        val response = it.data
                        if (response != null){
                            if (response.isSuccess!!) {
                                getCheckoutData(isSetDeliveryType = false)
                            }
                        }
                    }
                    is ApiResponse.Failure -> {

                        Toast.makeText(this@CheckOutActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
//            mViewModel.updateAddOn(
//                token ="$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
//                cartId = cartId,
//                quantity = quantity,
//                addOns = arrayListOf()
//            ).collect{
//                when (it) {
//                    is ApiResponse.Loading -> {
//
//                    }
//                    is ApiResponse.Success -> {
//
//                        val response = it.data
//                        if (response != null){
//                            if (response.isSuccess!!) {
//                                getCartItem()
//                            }
//                        }
//                    }
//                    is ApiResponse.Failure -> {
//
//                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
//                    }
//
//                }
//
//            }
        }
    }




    private fun getUserAddress(view: View? = null) {
        lifecycleScope.launch {
            mViewModel.getUserAddress(token = "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}").collect{
                when (it) {
                    is ApiResponse.Loading -> {}
                    is ApiResponse.Success -> {
                        val data = it.data
                        if (data != null){
                            val response = data.data
                            if (response.isNotEmpty()) {
                                showAddressSheet(response,view)
                            } else {
                                addNewAddressDialog()
                            }
                        }
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(this@CheckOutActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }
            }


        }
    }
    @SuppressLint("SuspiciousIndentation")
    private fun addNewAddressDialog(){
        val viewDialog = layoutInflater.inflate(R.layout.add_address_dialog, FrameLayout(this), false)
        val   dialog = BottomSheetDialog(this)
        dialog.setContentView(viewDialog)
        dialog.setOnShowListener {
            val bottomSheetDialogFragment = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            (viewDialog?.parent as View).setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
            val behavior = bottomSheetDialogFragment?.let {
                BottomSheetBehavior.from(it)
            }
            behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.setCancelable(false)


        val btnAddress  =viewDialog.findViewById<TextView>(R.id.addNewAddress)
        btnAddress.setSafeOnClickListener {
            dialog.dismiss()
           // val bundle = Bundle()
          //  bundle.putString("isFrom", "CheckOut")
          //  findNavController().navigate(R.id.action_cartMoreItemScreen_to_currentLocationFragment, bundle)
            val intent = Intent(this,OnMapLocationActivity::class.java)
            intent.putExtra("isFrom","CheckOut")
            startActivity(intent)

        }

        dialog.show()


    }
    private fun showAddressSheet(list: ArrayList<GetAddress.Data>, view: View?) {
        if (viewDialog == null){
            viewDialog = layoutInflater.inflate(R.layout.sheet_checkout_address,
                FrameLayout(this), false)
            dialog = BottomSheetDialog((this))

            dialog?.setOnShowListener {
                val bottomSheetDialogFragment =
                    dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                (viewDialog?.parent as View).setBackgroundColor(
                    ContextCompat.getColor(
                        this, R.color.transparent
                    )
                )
                val behavior = bottomSheetDialogFragment?.let {
                    BottomSheetBehavior.from(it)
                }
                behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            }
            if (view == null){
                dialog?.setCancelable(false)
            }


            val addressRecyclerView = viewDialog?.findViewById<RecyclerView>(R.id.addressRecyclerView)
            val addNewAddress = viewDialog?.findViewById<TextView>(R.id.addNewAddress)

            viewDialog?.let { dialog?.setContentView(it) }
            dialog?.show()


            userAddressAdapter = UserAddressAdapter(context =this,
                list = list,
                selectAddress = {addressId ->
                    this.addressId = addressId
                    dialog?.dismiss()
                    getCheckoutData()
                    // call api one time


                })
            addressRecyclerView?.adapter = userAddressAdapter


            addNewAddress?.setSafeOnClickListener {
                dialog?.dismiss()
               // val bundle = Bundle()
               // bundle.putString("isFrom", "CheckOut")
                val intent = Intent(this,OnMapLocationActivity::class.java)
                intent.putExtra("isFrom","CheckOut")
                startActivity(intent)
              //  findNavController().navigate(R.id.action_cartMoreItemScreen_to_currentLocationFragment, bundle)
            }

            dialog?.setOnDismissListener {
                viewDialog = null

            }

        }

    }



    @SuppressLint("SuspiciousIndentation")
    private fun isRestaurantOpen(
        toDayDate: String? = null,
        text: String? = null,
        isApiCall: Boolean= false,
        view: View? = null){
        val map = mutableMapOf(
            "restaurant_id" to "$restaurantId",
            "date" to toDayDate.toString())

        if (selectDeliveryTime.isNotEmpty())
            map["time"] = selectDeliveryTime

        if (text != null && text == "Proceed to Pay"){
            map["is_cart_check"] = true.toString()
            if (isOrderTrack)
                map["is_driver_available"] = "true"
        }


        if (binding.dateSelect.isVisible){
            map["delivery"] = SCHEDULED
        }else{
            map["delivery"] = NOW
        }


        if (totalAmount != null && totalAmount!! == 0.0)
            map["payment_status"] = COMPLETE_BY_WALLET



        lifecycleScope.launch {
            mViewModel.isRestaurantOpen(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        mProgressBar().showProgressBar(this@CheckOutActivity)


                    }

                    is ApiResponse.Success -> {
                        if (text != null && text != "Proceed to Pay") {
                            mProgressBar().dialog?.dismiss()
                        }

                        val data = it.data
                        if (data != null) {
                            if (data.is_success!!) {
                                if (data.message == Constants.Restaurant_is_available) {
                                    dateSelectDialog?.dismiss()
                                    binding.dateSelect.text = deliveryDate
                                    val timeArray = data.data?.time_interval
                                    timeList.clear()
                                    timeArray?.forEach { timeValue ->
                                        timeList.add(timeValue!!)
                                    }
                                    if (!isApiCall) {
                                        if (!timeArray.isNullOrEmpty())
                                            binding.timingSelect.text = timeArray[0] ?: ""
                                    }
                                    if (!timeArray.isNullOrEmpty())
                                        if (view == null)
                                        selectDeliveryTime = timeArray[0] ?: ""
                                    orderSlotAdapter.listUpdate(timeList)
                                    if (binding.txtTotalPrice.text.toString() > "0") {
                                        //  bundle.putString("isFrom", "CartScreen")
                                        //  bundle.putString("delivery_instruction", "delivery_instruction")
                                        //  bundle.putString("cooking_instruction", binding.edtCookingInstruction.text.toString())
                                        //  bundle.putString("delivery_date", deliveryDate!!.trim())
                                        //  bundle.putString("delivery_time", deliveryTime.trim())
                                        //bundle.putString("date_today", formatter.format(dateToday))
                                        if (isApiCall) {
                                            if (PrefUtils.instance.getString(Constants.UserFirstName) != "" &&
                                                PrefUtils.instance.getString(Constants.UserLastName) != "" &&
                                                PrefUtils.instance.getString(Constants.UserEmail) != "" &&
                                                PrefUtils.instance.getString(Constants.UserMobile) != ""
                                            ) {
                                                setStripePayment()
                                            } else {
                                                //   bundle.putString("isFrom", "CartMoreItemScreen")
                                                //   bundle.putString("cartBundle", "CartMoreItemScreen")
                                                //  findNavController().navigate(R.id.action_cartMoreItemScreen_to_editProfile, bundle)
                                                val intent = Intent(
                                                    this@CheckOutActivity,
                                                    EditProfileActivity::class.java
                                                )
                                                startActivity(intent)
                                            }

                                        }

                                    } else {
                                        if (isApiCall) {
                                            if (PrefUtils.instance.getString(Constants.UserFirstName) != "" &&
                                                PrefUtils.instance.getString(Constants.UserLastName) != "" &&
                                                PrefUtils.instance.getString(Constants.UserEmail) != "" &&
                                                PrefUtils.instance.getString(Constants.UserMobile) != ""
                                            ) {
                                                setStripePayment()
                                            }

                                        } else {
                                            //  bundle.putString("isFrom", "CartMoreItemScreen")
                                            //  bundle.putString("cartBundle", "CartMoreItemScreen")
                                            //  findNavController().navigate(R.id.action_cartMoreItemScreen_to_editProfile, bundle)
                                        }
                                    }

                                } else if (data.message == Constants.order_placed) {
                                    PrefUtils.instance.clearCartItemsFromSharedPreferences()
                                    PrefUtils.instance.setString(Constants.DeliveryDate, "")
                                    PrefUtils.instance.setString(Constants.DeliveryTime, "")
                                    val orderId = data.data?.order_id
                                    if (broadCastId != null && !broadCastId.equals("null"))
                                        setBroadCast(broadCastId!!, orderId)

                                    if (!pubBoosterArray.isNullOrEmpty()) {
                                        val intent = Intent(
                                            this@CheckOutActivity,
                                            OrderConfirmedActivity::class.java
                                        )
                                        intent.putExtra("orderId", orderId.toString())
                                        intent.putExtra("checkOrderType", isOrderTrack)
                                        intent.putExtra("boosterArray", pubBoosterArray)
                                        intent.putExtra("restaurantId", restaurantId)
                                        intent.putExtra("deliveryType", deliveryType)
                                        startActivity(intent)

//                                        val action = CartMoreItemScreenDirections.actionCartMoreItemScreenToOrderConfirmedScreen(
//                                            orderId = orderId.toString(),
//                                            checkOrderType = isOrderTrack,
//                                            boosterArray = pubBoosterArray!!.toIntArray(),
//                                            restaurantId = restaurantId!!)
//                                        findNavController().navigate(action)
                                    } else {
                                        val intent = Intent(
                                            this@CheckOutActivity,
                                            OrderConfirmedActivity::class.java
                                        )
                                        intent.putExtra("orderId", orderId.toString())
                                        intent.putExtra("checkOrderType", isOrderTrack)
                                        intent.putExtra("restaurantId", restaurantId)
                                        intent.putExtra("deliveryType", deliveryType)
                                        startActivity(intent)
//                                        val action = CartMoreItemScreenDirections.actionCartMoreItemScreenToOrderConfirmedScreen(
//                                            orderId = orderId.toString(),
//                                            checkOrderType = isOrderTrack,
//                                            restaurantId = restaurantId!!)
//                                        findNavController().navigate(action)

                                    }
                                    // if order is success then hide cart count layout


                                }
                            } else {
                                Toast.makeText(
                                    this@CheckOutActivity,
                                    data.message,
                                    Toast.LENGTH_SHORT
                                ).show()

                            }

                        }
                    }

                    is ApiResponse.Failure -> {
                        showErrorDialog(this@CheckOutActivity, it.msg.toString())
                        mProgressBar().dialog?.dismiss()
//                        AlertDialog.Builder(this@CheckOutActivity)
//                            .setMessage(it.msg)
//                            .setPositiveButton("OK") { dialog, _ ->
//                                dialog.dismiss()
//                            }.show() }

                    }
                }

            }

        }

    }

    private fun setStripePayment(){
        val mMap = mapOf(
            "checkout_id" to "$checkoutId"
        )
        lifecycleScope.launch {
            viewModel.setStripePayment(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map = mMap).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        if (data != null) {
                            paymentIntentClientSecret = data.data?.paymentIntent ?: ""
                            paymentIntentId = data.data?.intentId ?: ""
                            customerConfig = PaymentSheet.CustomerConfiguration(
                                data.data?.customer.toString(),
                                data.data?.ephemeralKey.toString()
                            )
                            PaymentConfiguration.init(this@CheckOutActivity,PrefUtils.instance.getString(Constants.stripeKey).toString())
                            if (!paymentIntentClientSecret.isNullOrEmpty())
                            presentPaymentSheet()
                            else{
                                Toast.makeText(this@CheckOutActivity,"Oops! Failed to initiate payment",Toast.LENGTH_LONG).show()
                                mProgressBar().dialog?.dismiss()
                            }
                        }
                    }
                    is ApiResponse.Failure -> {
                        mProgressBar().dialog?.dismiss()
                        Toast.makeText(this@CheckOutActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }
            }

        }
    }

    private fun presentPaymentSheet() {

        val googlePayConfiguration = PaymentSheet.GooglePayConfiguration(
            environment = PaymentSheet.GooglePayConfiguration.Environment.Production,
            countryCode = "CZ",
            currencyCode = "CZK")



        val defaultBillingDetails = PaymentSheet.BillingDetails(
            address = PaymentSheet.Address(country = "CZ"))

        val configuration = PaymentSheet.Configuration(
            merchantDisplayName = "Svaggy",
            allowsDelayedPaymentMethods = true,
            customer = customerConfig,
            defaultBillingDetails = defaultBillingDetails  ,// Set default billing details
            googlePay = googlePayConfiguration)

        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret = paymentIntentClientSecret ?: "", configuration)


//        paymentSheet.presentWithPaymentIntent(
//            paymentIntentClientSecret!!,
//            PaymentSheet.Configuration(
//                merchantDisplayName = "Preet",
//                allowsDelayedPaymentMethods = true,
//                googlePay = googlePayConfig,
//                customer = customerConfig,
//                defaultBillingDetails = defaultBillingDetails)
//        )



    }

    private fun onPaymentSheetResult(paymentResult: PaymentSheetResult) {
        val type = if (binding.selectTimeType.text.toString() == "Standard") {
            "Now"
        } else {
            binding.selectTimeType.text.toString()
        }

        when (paymentResult) {
            is PaymentSheetResult.Completed -> {
                viewModel.orderPlaced(
                    token = "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                    cartRestaurantId = restaurantId?.toInt()?:0,
                    checkout_id = checkoutId?.toInt()?:0,
                    delivery_instruction =  binding.edtDeliveryInstruction.text.toString(),
                    cooking_instruction =  binding.edtCookingInstruction.text.toString(),
                    delivery_date =    deliveryDate.trim(),
                    deliveryTime =   selectDeliveryTime,
                    paymentIntent =  paymentIntentId.toString(),
                    paymentStatus = "SUCCESS",
                    type
                )
            }
            is PaymentSheetResult.Canceled -> {
                mProgressBar().dialog?.dismiss()
                Toast.makeText(this, "Payment canceled!", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                mProgressBar().dialog?.dismiss()
                Toast.makeText(this, "Payment failed"+ paymentResult.error.localizedMessage, Toast.LENGTH_SHORT).show()
                mProgressBar().dialog?.dismiss()
                viewModel.orderPlaced(
                    "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                    restaurantId?.toInt()?:0,
                    checkoutId?.toInt()?:0,
                    binding.edtDeliveryInstruction.text.toString(),
                    binding.edtCookingInstruction.text.toString(),
                    deliveryDate.trim(),
                    selectDeliveryTime,
                    paymentIntentId.toString(),
                    "FAIL",
                    type.toString()
                )
            }
        }
    }



    @SuppressLint("UseCompatLoadingForDrawables", "SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initBinding(formatter: SimpleDateFormat, dateToday: Date) {


        binding.apply {
            txtAddFundBT.setOnClickListener {
                val intent = Intent(this@CheckOutActivity,AddWalletActivity::class.java)
                addWalletLauncher.launch(intent)
               //startActivity(intent)
            }

            clAddMoreItems.setOnClickListener {
             //   onBackPressed()
                val intent = Intent(this@CheckOutActivity, RestaurantMenuActivity::class.java)
                intent.putExtra("item_id",restaurantId.toString())
                intent.putExtra("pop_back","CheckOut")
                startActivity(intent)
              //  findNavController().navigate(R.id.restaurantMenuScreen, bundle)
            }

            edtCookingInstruction.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
            edtDeliveryInstruction.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())

            imgCookingInstr.setOnClickListener {
                if (binding.edtCookingInstruction.visibility == 0) {
                    binding.edtCookingInstruction.visibility = View.GONE
                } else {
                    binding.edtCookingInstruction.visibility = View.VISIBLE
                }
            }

            proceedToPay.setSafeOnClickListener {
                isRestaurantOpen(toDayDate = deliveryDate,text = it.tooltipText.toString(),true,it)
            }
            btRemoveCoupon.setOnClickListener {
                if (couponId != null){
                    lifecycleScope.launch {
                        couponId?.let { it1 ->
                            mViewModel.setCoupon(authToken = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                                checkoutId = checkoutId!!,
                                couponId = it1,
                                isCouponAdded = false).collect{res ->
                                when (res) {
                                    is ApiResponse.Loading -> {

                                    }
                                    is ApiResponse.Success -> {
                                        couponId = null
                                        getCheckoutData()
                                    }
                                    is ApiResponse.Failure -> {
                                        Toast.makeText(this@CheckOutActivity,res.msg,Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }

                }else{
                    applyPromoCoupon(promoType = REMOVE_PROMO)
                }



            }

            promoCounter.setOnClickListener {
                if (restaurantId != null && checkoutId != null) {
                    val intent = Intent(this@CheckOutActivity, CouponActivity::class.java)
                    intent.putExtra("restaurantId", restaurantId)
                    intent.putExtra("checkOutId", checkoutId)
                    addWalletLauncher.launch(intent)

                }
            }
                applyCouponCl.setSafeOnClickListener {
                    if (restaurantId != null && checkoutId != null){
                        val intent = Intent(this@CheckOutActivity,CouponActivity::class.java)
                        intent.putExtra("restaurantId",restaurantId)
                        intent.putExtra("checkOutId",checkoutId)
                      //  startActivity(intent)
                        addWalletLauncher.launch(intent)

                }

            }
            walletCl.setOnClickListener {
                if (walletCheck.isChecked){
                    walletCheck.isChecked = !walletCheck.isChecked
                    mViewModel.setWallet(
                        token = "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                        checkWallet = walletCheck.isChecked,
                        checkoutId =  checkoutId.toString()
                    )

                }else{
                    if (  binding.txtWalletBalance.text.equals("Balance CZK 0.0")){
                        Toast.makeText(this@CheckOutActivity,"Please Add Wallet Payment",Toast.LENGTH_LONG).show()
                    }
                    else{
                        walletCheck.isChecked = !walletCheck.isChecked
                        mViewModel.setWallet(
                            token = "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                            checkWallet = walletCheck.isChecked,
                            checkoutId =  checkoutId.toString()
                        )

                    }

                }





            }

            binding.applyCoupon.setOnClickListener {
                applyPromoCoupon(ADD_PROMO)
            }


            timingSelect.setSafeOnClickListener {
                val viewDialog = layoutInflater.inflate(R.layout.sheet_delivery_time, null)
                val dialog = BottomSheetDialog(this@CheckOutActivity)
                dialog.setOnShowListener {
                    val bottomSheetDialogFragment = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    (viewDialog.parent as View).setBackgroundColor(ContextCompat.getColor(this@CheckOutActivity, R.color.transparent))
                    val behavior = bottomSheetDialogFragment?.let {
                        BottomSheetBehavior.from(it)
                    }
                    behavior?.state = BottomSheetBehavior.STATE_EXPANDED
                    //slotRc.adapter = orderSlotAdapter


                }
                val  scrollChoice = viewDialog.findViewById<ScrollChoice>(R.id.scroll_choice)
                val  doneDate = viewDialog.findViewById<TextView>(R.id.cancelImage)
                scrollChoice.addItems(timeList,3)


                scrollChoice.setOnItemSelectedListener { _, _, name ->
                    selectDeliveryTime = name
                    binding.timingSelect.text = name

                }
                doneDate.setOnClickListener {
                    dialog.dismiss()

                }




                dialog.setContentView(viewDialog)
                dialog.show()
            }

            dateSelect.setOnClickListener {
                val viewDialog = layoutInflater.inflate(R.layout.sheet_delivery_date, null)
                dateSelectDialog = BottomSheetDialog(this@CheckOutActivity)



                dateSelectDialog?.setOnShowListener {
                    val bottomSheetDialogFragment = dateSelectDialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    (viewDialog.parent as View).setBackgroundColor(ContextCompat.getColor(this@CheckOutActivity, R.color.transparent))
                    val behavior = bottomSheetDialogFragment?.let {
                        BottomSheetBehavior.from(it)
                    }
                    behavior?.state = BottomSheetBehavior.STATE_EXPANDED
                }

                val    datePicker : DatePicker = viewDialog.findViewById(R.id.datePicker)
                val doneDate = viewDialog.findViewById<TextView>(R.id.cancelImage)


                val today = Calendar.getInstance()
                datePicker.minDate = today.timeInMillis



                datePicker.updateDate(
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH))


                doneDate.setOnClickListener {
                    deliveryDate = formatter.format(dateToday)
                    val selectedDay = datePicker.dayOfMonth
                    val selectedMonth = datePicker.month.plus(1)
                    val selectedYear = datePicker.year

                    val msg = "$selectedDay-$selectedMonth-$selectedYear"

                    val dateF = formatter.parse(msg)
                    val dateString = dateF?.let { it1 -> formatter.format(it1) }

                    if (deliveryDate == dateString) {
                        binding.dateSelect.text = dateString
                        //  binding.timingSelect.visibility = View.GONE
                        isRestaurantOpen(
                            toDayDate = binding.dateSelect.text.toString(),
                            text = "Schedule",
                            isApiCall = false)

                    } else {
                        deliveryDate = dateString ?: ""
                        isRestaurantOpen(
                            toDayDate = deliveryDate.toString(),
                            text = "Schedule",
                            isApiCall = false
                        )
                        PrefUtils.instance.setString(Constants.DeliveryDate, deliveryDate)
                        PrefUtils.instance.setString(Constants.DeliveryTime, "")
                        binding.timingSelect.visibility = View.VISIBLE
                    }

                }

                dateSelectDialog?.setContentView(viewDialog)
                dateSelectDialog?.show()
            }

            selectTimeType.setOnClickListener {
                showScheduleDialog()
            }


            checkBoxDelivery.setOnClickListener {
                checkBoxPick.isClickable = true
                checkBoxDelivery.isClickable = false
                isOrderTrack = true
                addressBar.show()
                checkBoxDelivery.setCompoundDrawablesWithIntrinsicBounds(this@CheckOutActivity.getDrawable(R.drawable.ic_red_checbox),null,null,null)
                checkBoxPick.setCompoundDrawablesWithIntrinsicBounds(this@CheckOutActivity.getDrawable(R.drawable.ic_checbox),null,null,null)
                setDeliveryType(deliveryType = deliveryType ?: "", isApiCall = true)

            }
            checkBoxPick.setOnClickListener {
                checkBoxPick.isClickable = false
                checkBoxDelivery.isClickable = true
                isOrderTrack = false
                checkBoxPick.setCompoundDrawablesWithIntrinsicBounds(this@CheckOutActivity.getDrawable(R.drawable.ic_red_checbox),null,null,null)
                checkBoxDelivery.setCompoundDrawablesWithIntrinsicBounds(this@CheckOutActivity.getDrawable(R.drawable.ic_checbox),null,null,null)
                addressBar.hide()
                setDeliveryType(deliveryType = PICKUP_ONLY, isApiCall = true)
                binding.rushCard.hide()
                binding.delayCard.hide()


            }

        }

    }
    private fun applyPromoCoupon(promoType:String){
        val map = mapOf(
            "checkout_id" to checkoutId.toString(),
            "promo_type" to promoType,
        )
        lifecycleScope.launch {
            mViewModel.applyPromo(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
               map = map).collect{res ->
                when (res) {
                    is ApiResponse.Loading -> {


                    }
                    is ApiResponse.Success -> {
                        if (res.data != null && res.data.isSuccess == true){
                            getCheckoutData()

                        }else{
                            binding.root.showSnackBar(res.data?.message.toString())

                        }
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(this@CheckOutActivity,res.msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showScheduleDialog() {

        val viewDialog = layoutInflater.inflate(R.layout.sehedule_type, null)
        val dialog = BottomSheetDialog(this)
        dialog.setOnShowListener {
            val bottomSheetDialogFragment = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            (viewDialog.parent as View).setBackgroundColor(
                ContextCompat.getColor(this, R.color.transparent)
            )
            val behavior = bottomSheetDialogFragment?.let {
                BottomSheetBehavior.from(it)
            }
            behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
        val nowText = viewDialog.findViewById<TextView>(R.id.nowText)
        val scheduleText = viewDialog.findViewById<TextView>(R.id.scheduleText)
        val cancelButton: ImageView = viewDialog.findViewById(R.id.cancel_button)

        nowText.setOnClickListener {
            binding.selectTimeType.text = getString(R.string.now)
            binding.dateSelect.hide()
            binding.timingSelect.hide()
            dialog.dismiss()
            PrefUtils.instance.setBoolean(Constants.IS_SHOW_TIME_BAR,false)
            val dateToday = PrefUtils.instance.getCurrentDateTime()
            val  locale: Locale = Locale.getDefault()
            val formatter = SimpleDateFormat("dd-MM-yyyy", locale)
            deliveryDate = formatter.format(dateToday)
            selectDeliveryTime = ""

        }
        scheduleText.setOnClickListener {
            binding.dateSelect.show()
            binding.timingSelect.show()
            binding.selectTimeType.text = getString(R.string.schedule)
            dialog.dismiss()
            isRestaurantOpen(toDayDate = deliveryDate, text = it.tooltipText.toString(), isApiCall = false)
            PrefUtils.instance.setBoolean(Constants.IS_SHOW_TIME_BAR,true)

        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setContentView(viewDialog)
        dialog.show()

    }

    private fun showErrorDialog(context: Context, message: String) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.warning_dialog)

        val dialogTitle = dialog.findViewById<TextView>(R.id.txtMessage)
        val dialogButton = dialog.findViewById<TextView>(R.id.btnDialogPositive)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogTitle.text = message

        dialogButton.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
            //  findNavController().popBackStack(R.id.homeFragment,false)
        }

        dialog.show()
    }


    private fun setBroadCast(broadCastId: String, orderId: Int?){
        val json = JsonObject()
        json.addProperty("broadcast_id",broadCastId)
        json.addProperty("type",CONVERSION)
        json.addProperty("restaurant_id",restaurantId!!.toInt())
        json.addProperty("order_id",orderId!!.toInt())

        lifecycleScope.launch {
            mViewModelOrder.setConversion(token = "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                jsonObject = json ).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        Toast.makeText(this@CheckOutActivity,data!!.message.toString(),Toast.LENGTH_LONG).show()

                    }
                    is ApiResponse.Failure -> {

                        Toast.makeText(this@CheckOutActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }
            }

        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setDeliveryType(deliveryType:String, isApiCall: Boolean) {
        val map = mapOf(
            "checkout_id" to "$checkoutId",
            "delivery_type" to deliveryType
        )
        lifecycleScope.launch {
            mViewModel.setDeliveryType(token =  "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map = map).collect{

                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        if (data != null && data.isSuccess!!) {
                            binding.totalAmountCl.show()
                            if (isApiCall)
                               getCheckoutData(isSetDeliveryType = false)
                        }
                        else{
                            binding.totalAmountCl.hide()
                            binding.root.showSnackBar(data?.message.toString())

                        }


                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(this@CheckOutActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }

    }

    private fun walletAvailable(b: Boolean) {
        if (b){
            binding.walletCheck.isChecked = true
        }
    }





}