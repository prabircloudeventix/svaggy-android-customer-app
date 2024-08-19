package com.svaggy.ui.fragments.cart.screens


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.svaggy.R
import com.svaggy.databinding.FragmentCartMoreItemScreenBinding
import com.svaggy.ui.activities.MainActivity
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
import com.svaggy.utils.Constants.BEARER
import com.svaggy.utils.Constants.IS_SHOW_TIME_BAR
import com.svaggy.utils.Constants.Restaurant_is_available
import com.svaggy.utils.Constants.order_placed
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.onBackPressedDispatcher
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


@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class CartMoreItemScreen : Fragment() {
   private var _binding: FragmentCartMoreItemScreenBinding? = null
    private val binding get() = _binding!!
    private val bundle = Bundle()
    private var moreItemAdapter: MoreItemAdapter? = null
    var dialog: BottomSheetDialog? = null
    private var viewDialog: View? = null
    private var deliveryDate: String? = null
    var deliveryTime: String = ""
    private var checkoutId: String? = null
    var restaurantId: String?= null
    private var broadCastId: String? = null
    private lateinit var paymentSheet: PaymentSheet
    private var paymentIntentClientSecret: String? = ""
    private var paymentIntentId: String? = ""
    private lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    private val mViewModel by viewModels<CartViewModel>()
    private val viewModel by viewModels<PaymentViewModel>()
    private val mViewModelOrder by viewModels<PaymentViewModel>()
    private  var couponId:String? = null
    private lateinit var userAddressAdapter: UserAddressAdapter
    private lateinit var itemCount:TextView
    private lateinit var countLayout: ConstraintLayout
    private  var totalAmount:Double? = null
    private var deliveryType: String? = null
    private lateinit var orderSlotAdapter: OrderSlotAdapter
    private var timeList:ArrayList<String> = ArrayList()
    private  var dateSelectDialog:BottomSheetDialog? = null
    private var selectDeliveryTime = ""
    private var isOrderTrack = true
    private var addressId:String? = null
    private var pubBoosterArray:ArrayList<Int>? = null
    private lateinit var contentView: View






    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartMoreItemScreenBinding.inflate(inflater, container, false)
      //  (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility = View.GONE
        restaurantId = arguments?.getString("item_id").toString()
        broadCastId = arguments?.getString(Constants.BROADCAST_ID)
        deliveryType = arguments?.getString(Constants.DELIVERY_TYPE)
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("key")?.observe(viewLifecycleOwner) { coupon ->
            couponId = coupon
        }
        couponGetObserver()
        orderCreateObserver()
        // For Clear Cart Count  Icon
        itemCount = requireActivity().findViewById(R.id.tv_count_text)
        countLayout= requireActivity().findViewById(R.id.rl_cart_count_num)
        orderSlotAdapter = OrderSlotAdapter()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        getCheckoutData()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        onBackPressedDispatcher {
            PrefUtils.instance.setBoolean(IS_SHOW_TIME_BAR,false)
            if (arguments?.getString("pop_back") == "RestaurantMenuScreen") {
                findNavController().popBackStack(R.id.restaurantMenuScreen,false)
            } else {
                findNavController().popBackStack(R.id.cartScreen,false)
            }

        }


        val dateToday = PrefUtils.instance.getCurrentDateTime()
        val locale: Locale = Locale.getDefault()
        val formatter = SimpleDateFormat("dd-MM-yyyy", locale)
        deliveryDate = formatter.format(dateToday)
        deliveryTime = PrefUtils.instance.getCurrentTime24HourFormat()
        binding.dateSelect.text = deliveryDate
       binding.backButton.setOnClickListener {
            PrefUtils.instance.setBoolean(IS_SHOW_TIME_BAR,false)
            if (arguments?.getString("pop_back") == "RestaurantMenuScreen") {
                findNavController().popBackStack(R.id.restaurantMenuScreen,false)
            } else {
                findNavController().popBackStack(R.id.cartScreen,false)
            }

        }

        initBinding(formatter,dateToday)

        if ( PrefUtils.instance.getBoolean(IS_SHOW_TIME_BAR)){
            binding.dateSelect.show()
            binding.timingSelect.show()
            binding.selectTimeType.text = requireContext().getString(R.string.schedule)
            if (timeList.isNotEmpty()){
                binding.timingSelect.text = timeList[0]
                selectDeliveryTime = timeList[0]
            }
        }
        else{
            binding.selectTimeType.text = requireContext().getString(R.string.now)
            binding.dateSelect.hide()
            binding.timingSelect.hide()
        }


    }



    @SuppressLint("SuspiciousIndentation")
    private fun orderCreateObserver() {
        mViewModelOrder.addCartDataLive.observe(viewLifecycleOwner) {
            if (it.is_success == true) {
                PrefUtils.instance.clearCartItemsFromSharedPreferences()
                PrefUtils.instance.setString(Constants.DeliveryDate,"")
                PrefUtils.instance.setString(Constants.DeliveryTime,"")
                val orderId = it.data?.order_id
                if (broadCastId != null && !broadCastId.equals("null"))
                setBroadCast(broadCastId!!,orderId)


               // setBoosterAction(boosterArray,orderId)
                if (!pubBoosterArray.isNullOrEmpty()){
                    val action = CartMoreItemScreenDirections.actionCartMoreItemScreenToOrderConfirmedScreen(
                        orderId = orderId.toString(),
                        checkOrderType = isOrderTrack,
                        boosterArray = pubBoosterArray!!.toIntArray(),
                        restaurantId = restaurantId!!)
                    findNavController().navigate(action)
                }else{
                    val action = CartMoreItemScreenDirections.actionCartMoreItemScreenToOrderConfirmedScreen(
                        orderId = orderId.toString(),
                        checkOrderType = isOrderTrack,
                        restaurantId = restaurantId!!)
                    findNavController().navigate(action)

                }
                countLayout.hide()
                PrefUtils.instance.setBoolean(IS_SHOW_TIME_BAR,false)
            }
            else {
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun couponGetObserver() {
        mViewModel.setOffersDataLive.observe(viewLifecycleOwner) {
            if (it.isSuccess == true) {
                getCheckoutData()

            } else {
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    //Preet

    private fun updateCart(cartId: Int, quantity: Int, position: Int,menuItemId:Int?) {
        lifecycleScope.launch {
            mViewModel.updateCartItem(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                cartId = cartId,
                quantity = quantity).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response != null) {
                            if (response.isSuccess!!){
                                if (quantity == 0){
                                    PrefUtils.instance.removeMenuItemsList("$menuItemId")
                                    countLayout.hide()
                                }
                            }
                                getCheckoutData()
                        }
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }

    }

    @SuppressLint("SuspiciousIndentation")
    private fun getCheckoutData() {

        val map = mutableMapOf(
            "latitude" to (PrefUtils.instance.getString(Constants.Latitude) ?: ""),
            "longitude" to (PrefUtils.instance.getString(Constants.Longitude) ?: ""),
            "restaurant_id" to (PrefUtils.instance.getString("restaurantId") ?: "")
        )

        if (addressId != null)
            map["address_id"] = addressId!!


        lifecycleScope.launch {
            mViewModel.getCheckOutDetailsNew(
                token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                       binding.progressBarView.show()
                    }
                    is ApiResponse.Success -> {
                     binding.progressBarView.hide()
                        val response = it.data
                        if (response != null) {
                            if (response.is_success) {
                               binding.titleTv.text = response.data.cart_data?.restaurant_details?.restaurant_name
                                bundle.putString("checkout_id", response.data.checkout_id.toString())
                                checkoutId = response.data.checkout_id.toString()
                                deliveryType = response.data.cart_data?.restaurant_details?.delivery_type.toString()
                                val restaurantAmount = response.data.cart_data?.restaurant_details?.delivery_cart_minimum_amount

                                when(deliveryType){
                                    "DELIVERY_BY_SVAGGY"  ->{
                                        binding.tv.show()
                                        binding.cartAddressCl.show()
                                        binding.showPickText.hide()
                                    }
                                    "DELIVERY_BY_RESTAURANT" ->{
                                        binding.tv.show()
                                        binding.cartAddressCl.show()
                                        binding.showPickText.hide()

                                    }

                                    "PICKUP_ONLY" ->{
                                        isOrderTrack = false
                                        binding.tv.hide()
                                        binding.totalAmountCl.show()
                                        binding.cartAddressCl.hide()
                                        binding.showPickText.show()
                                    }
                                }

                                if (response.data.cart_data?.cart_items != null) {
                                    response.data.cart_data.cart_items.forEach {item ->
                                        val listOf = listOf(
                                            SharedPrefModel(item.dishName!!,
                                                item.dishType!!,
                                                item.price!!,
                                                item.isActive!!,
                                                item.menuItemId!!,
                                                item.actualPrice!!,
                                                item.quantity!!,
                                                item.id!!)
                                        )
                                        PrefUtils.instance.saveMenuItemsList("${item.menuItemId}",listOf)
                                    }



//                                    moreItemAdapter =
//                                        MoreItemAdapter(
//                                            context =  requireContext(),
//                                            arrayList =  response.data.cart_data.cart_items,
//                                            updateCartItem =  ::updateCart,
//                                            updateAddOn = {cartId, quantity, addOns ->
//                                                updateAddOn(cartId = cartId, quantity = quantity, addOns =addOns) },
//                                            dataReset = { getCheckoutData() }
//                                        )


                                    binding.txtWalletBalance.text = context?.getString(
                                        R.string.priceRtn,
                                        "Balance " + response.data.currency_key + " " + response.data.wallet_amount
                                    )
//                                    if (response.data.coupon.id == 0) {
//                                        binding.applyCouponCl.visibility = View.VISIBLE
//                                        binding.couponSelectConfirm.visibility = View.GONE
//                                    } else {
//                                        binding.applyCouponCl.visibility = View.GONE
//                                        binding.couponSelectConfirm.visibility = View.VISIBLE
//                                        binding.couponCodeName.text = response.data.coupon.coupon_code
//                                        binding.couponDiscountAmount.text = context?.getString(
//                                            R.string.priceRtn, response.data.currency_key + " " + response.data.coupon.amount
//                                        )
//                                        couponId = response.data.coupon.id.toString()
//                                    }

                                    binding.recyclerMoreItem.adapter = moreItemAdapter

                                    bundle.putString("pop_back", context?.getString(R.string.cart_more_item_screen))
                                    bundle.putString("item_id", response.data.cart_data.restaurant_details.id.toString())
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
                                    } else {
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
                                      requireContext(),response.data.bill_details,:: walletAvailable)

                                    binding.recyclerBillDetails.adapter = billDetailsAdapter
                                    totalAmount = response.data.cart_data.total_amount.toString().prettyPrint(response.data.cart_data.total_amount).toDouble()
                                    binding.txtTotalAmount.text = totalAmount.toString()
                                    binding.txtTotalPrice.text = response.data.cart_data.total_amount.toString().prettyPrint(response.data.cart_data.total_amount)
                                    bundle.putString("total_amount",binding.txtTotalPrice.text.toString())
                                    if (restaurantAmount!! <= totalAmount!!){
                                        binding.totalAmountCl.show()
                                    }else{
                                        binding.totalAmountCl.hide()
                                        binding.root.showSnackBar(requireContext().getString(R.string.low_amount))

                                    }
                                }
                                else {
                                    (activity as MainActivity).mViewModel.getCartData(
                                        "Bearer ${
                                            PrefUtils.instance.getString(
                                                Constants.Token
                                            ).toString()
                                        }"
                                    )
                                    findNavController().navigate(R.id.cartScreen)
                                }
                            }
                            else {
                              binding.progressBarView.hide()
                                Toast.makeText(context, "" + response.message, Toast.LENGTH_SHORT).show()
                            }
                            if (!response.data.cart_data!!.restaurant_details.booster_ids.isNullOrEmpty()){
                                pubBoosterArray = ArrayList()
                                val boosterArray = response.data.cart_data.restaurant_details.booster_ids
                                boosterArray?.forEach {booster ->
                                    pubBoosterArray!!.add(booster)
                                }
                            }
                        }


                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }
                }
                }
        }
    }

    private fun updateAddOn(cartId: Int, quantity: Int,addOns: ArrayList<Int>){

        lifecycleScope.launch {
            mViewModel.updateAddOn(token ="$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                cartId = cartId,
                quantity = quantity,
                addOns =addOns).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response != null){
                            if (response.isSuccess!!) {
                                getCheckoutData()
                            }
                        }
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
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
                      binding.progressBarView.hide()
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }
            }


        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun addNewAddressDialog(){
        val viewDialog = layoutInflater.inflate(R.layout.add_address_dialog, FrameLayout(requireContext()), false)
      val   dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(viewDialog)
        dialog.setOnShowListener {
            val bottomSheetDialogFragment =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            (viewDialog?.parent as View).setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.transparent
                )
            )
            val behavior = bottomSheetDialogFragment?.let {
                BottomSheetBehavior.from(it)
            }
            behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.setCancelable(false)


        val btnAddress  =viewDialog.findViewById<TextView>(R.id.addNewAddress)
        btnAddress.setOnClickListener {
            dialog.dismiss()
            val bundle = Bundle()
            bundle.putString("isFrom", "CheckOut")
            findNavController().navigate(R.id.action_cartMoreItemScreen_to_currentLocationFragment, bundle)

        }

        dialog.show()


    }


    private fun showAddressSheet(list: ArrayList<GetAddress.Data>,view: View?) {
        if (viewDialog == null){
            viewDialog = layoutInflater.inflate(R.layout.sheet_checkout_address,FrameLayout(requireContext()), false)
            dialog = BottomSheetDialog(requireContext())

            dialog?.setOnShowListener {
                val bottomSheetDialogFragment =
                    dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                (viewDialog?.parent as View).setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.transparent
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


            userAddressAdapter = UserAddressAdapter(context = requireContext(),
                list = list,
                selectAddress = {addressId ->
                    this.addressId = addressId
                    dialog?.dismiss()
                    getCheckoutData()
                    // call api one time
                    setDeliveryType(deliveryType = deliveryType!!,false)

                })
            addressRecyclerView?.adapter = userAddressAdapter


            addNewAddress?.setOnClickListener {
                dialog?.dismiss()
                val bundle = Bundle()
                bundle.putString("isFrom", "CheckOut")
                findNavController().navigate(R.id.action_cartMoreItemScreen_to_currentLocationFragment, bundle)
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
        isApiCall: Boolean= false){
        val map = mutableMapOf(
            "restaurant_id" to "$restaurantId",
            "date" to toDayDate.toString())

        if (selectDeliveryTime.isNotEmpty())
            map["time"] = selectDeliveryTime

        if (text != null && text == "Proceed to Pay")
            map["is_cart_check"] = true.toString()


        if (binding.dateSelect.isVisible){
            map["delivery"] = "SCHEDULED"
        }else{
            map["delivery"] = "NOW"
        }


        if (totalAmount != null && totalAmount!! == 0.0)
        map["payment_status"] = "COMPLETE_BY_WALLET"

        lifecycleScope.launch {
            mViewModel.isRestaurantOpen(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        if (data != null){
                            if (data.is_success!!) {
                                if (data.message== Restaurant_is_available){
                                    dateSelectDialog?.dismiss()
                                    binding.dateSelect.text = deliveryDate
                                    val timeArray = data.data?.time_interval
                                    timeList.clear()
                                    timeArray?.forEach {timeValue ->
                                        timeList.add(timeValue!!)
                                    }
                                    if (!isApiCall){
                                        if (!timeArray.isNullOrEmpty())
                                        binding.timingSelect.text = timeArray[0] ?: ""
                                    }
                                    if (!timeArray.isNullOrEmpty())
                                    selectDeliveryTime = timeArray[0] ?: ""
                                    orderSlotAdapter.listUpdate(timeList)
                                    if (binding.txtTotalPrice.text.toString()>"0"){
                                            bundle.putString("isFrom", "CartScreen")
                                            bundle.putString("delivery_instruction", "delivery_instruction")
                                            bundle.putString("cooking_instruction", binding.edtCookingInstruction.text.toString())
                                            bundle.putString("delivery_date", deliveryDate!!.trim())
                                            bundle.putString("delivery_time", deliveryTime.trim())
                                            //bundle.putString("date_today", formatter.format(dateToday))
                                            if (isApiCall){
                                                if (PrefUtils.instance.getString(Constants.UserFirstName) != "" &&
                                                    PrefUtils.instance.getString(Constants.UserLastName) != "" &&
                                                    PrefUtils.instance.getString(Constants.UserEmail) != "" &&
                                                    PrefUtils.instance.getString(Constants.UserMobile) != "") {
                                                    setStripePayment()
                                                } else {
                                                    bundle.putString("isFrom", "CartMoreItemScreen")
                                                    bundle.putString("cartBundle", "CartMoreItemScreen")
                                                    findNavController().navigate(R.id.action_cartMoreItemScreen_to_editProfile, bundle)
                                                }

                                            }

                                        }
                                        else{
                                            if (isApiCall){
                                                if (PrefUtils.instance.getString(Constants.UserFirstName) != "" &&
                                                    PrefUtils.instance.getString(Constants.UserLastName) != "" &&
                                                    PrefUtils.instance.getString(Constants.UserEmail) != "" &&
                                                    PrefUtils.instance.getString(Constants.UserMobile) != "") {
                                                    setStripePayment()
                                                }

                                            } else {
                                                bundle.putString("isFrom", "CartMoreItemScreen")
                                                bundle.putString("cartBundle", "CartMoreItemScreen")
                                                findNavController().navigate(R.id.action_cartMoreItemScreen_to_editProfile, bundle)
                                            }
                                        }

                                    }
                                    else if (data.message == order_placed) {
                                        PrefUtils.instance.clearCartItemsFromSharedPreferences()
                                        PrefUtils.instance.setString(Constants.DeliveryDate,"")
                                        PrefUtils.instance.setString(Constants.DeliveryTime,"")
                                        val orderId = data.data?.order_id
                                        if (broadCastId != null && !broadCastId.equals("null"))
                                            setBroadCast(broadCastId!!,orderId)

                                    if (!pubBoosterArray.isNullOrEmpty()){
                                        val action = CartMoreItemScreenDirections.actionCartMoreItemScreenToOrderConfirmedScreen(
                                            orderId = orderId.toString(),
                                            checkOrderType = isOrderTrack,
                                            boosterArray = pubBoosterArray!!.toIntArray(),
                                            restaurantId = restaurantId!!)
                                        findNavController().navigate(action)
                                    }else{
                                        val action = CartMoreItemScreenDirections.actionCartMoreItemScreenToOrderConfirmedScreen(
                                            orderId = orderId.toString(),
                                            checkOrderType = isOrderTrack,
                                            restaurantId = restaurantId!!)
                                        findNavController().navigate(action)

                                    }
                                    // if order is success then hide cart count layout
                                        countLayout.hide()


                                    }
                                }else{
                                Toast.makeText(context, data.message, Toast.LENGTH_SHORT).show()

                            }

                        }
                    }
                    is ApiResponse.Failure -> {
                   binding.progressBarView.hide()
                        AlertDialog.Builder(context)
                            .setMessage(it.msg)
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }.show() }

                }

            }

        }

    }

     private fun setStripePayment(){
         val mMap = mapOf(
             "checkout_id" to "$checkoutId"
         )
        lifecycleScope.launch {
            viewModel.setStripePayment(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map = mMap).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        if (data != null) {
                            paymentIntentClientSecret = data.data?.paymentIntent
                                paymentIntentId = data.data?.intentId
                                customerConfig = PaymentSheet.CustomerConfiguration(
                                    data.data?.customer.toString(),
                                    data.data?.ephemeralKey.toString()
                                )
                                PaymentConfiguration.init(requireContext(),data.data?.publishableKey.toString())
                                presentPaymentSheet()
                        }
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }
            }

        }
    }

    private fun presentPaymentSheet() {

            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret!!,
                PaymentSheet.Configuration(
                    merchantDisplayName = "My merchant name",
                    allowsDelayedPaymentMethods = true,
                    customer = customerConfig
                )
            )


    }



    private fun onPaymentSheetResult(paymentResult: PaymentSheetResult) {
        val type = binding.selectTimeType.text
        when (paymentResult) {
            is PaymentSheetResult.Completed -> {

                viewModel.orderPlaced(
                    token = "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                    cartRestaurantId = restaurantId?.toInt()?:0,
                    checkout_id = checkoutId?.toInt()?:0,
                   delivery_instruction =  binding.edtDeliveryInstruction.text.toString(),
                   cooking_instruction =  binding.edtCookingInstruction.text.toString(),
                    delivery_date =    deliveryDate!!.trim(),
                  deliveryTime =   selectDeliveryTime,
                   paymentIntent =  paymentIntentId.toString(),
                    paymentStatus = "SUCCESS",
                    type.toString()
                )
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(context, "Payment canceled!", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(context, "Payment failed"+ paymentResult.error.localizedMessage, Toast.LENGTH_SHORT).show()
                viewModel.orderPlaced(
                    "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                    restaurantId?.toInt()?:0,
                    checkoutId?.toInt()?:0,
                    binding.edtDeliveryInstruction.text.toString(),
                    binding.edtCookingInstruction.text.toString(),
                    deliveryDate!!.trim(),
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
                val routs = CartMoreItemScreenDirections.actionCartMoreItemScreenToWalletAddFund(isFrom = "CartScreen")
                findNavController().navigate(routs)
            }

            clAddMoreItems.setOnClickListener {
                findNavController().navigate(R.id.restaurantMenuScreen, bundle)
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
                binding.progressBarView.show()
                isRestaurantOpen(toDayDate = deliveryDate,text = it.tooltipText.toString(),true)
            }
            btRemoveCoupon.setOnClickListener {
                lifecycleScope.launch {
                    couponId?.let { it1 ->
                        mViewModel.setCoupon(authToken = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                            checkoutId = checkoutId!!,
                            couponId = it1,
                            isCouponAdded = false).collect{res ->
                            when (res) {
                                is ApiResponse.Loading -> {

                                }
                                is ApiResponse.Success -> {
                                    getCheckoutData()
                                    // getCheckOutObserver()
                                }
                                is ApiResponse.Failure -> {
                                    Toast.makeText(requireContext(),res.msg,Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

            }

            applyCouponCl.setOnClickListener {
                if (restaurantId != null && checkoutId != null){
                    val action = CartMoreItemScreenDirections.actionCartMoreItemScreenToCouponCodeScreen(restaurantId!!,checkoutId!!)
                    findNavController().navigate(action)

                }

            }
            walletCl.setOnClickListener {
                walletCheck.isChecked = !walletCheck.isChecked
                mViewModel.setWallet(
                    token = "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                    checkWallet = walletCheck.isChecked,
                    checkoutId =  checkoutId.toString()
                )
            }

//            walletCheck.setOnCheckedChangeListener { _, isChecked ->
//                // Perform the desired action when the checkbox state changes
//                if (isChecked) {
//                    binding.walletCheck.isChecked = true
//                    // Checkbox is checked on the first click
//                    // Perform action for checked state
//                    mViewModel.setWallet(
//                        token = "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
//                        checkWallet = true,
//                        checkoutId =  checkoutId.toString()
//                    )
//                } else {
//                    binding.walletCheck.isChecked = false
//                    // Checkbox is unchecked on the second click
//                    // Perform action for unchecked state
//                    mViewModel.setWallet(
//                        token = "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
//                        checkWallet =false,
//                        checkoutId = checkoutId.toString()
//                    )
//                }
//            }

            timingSelect.setOnClickListener {
                val viewDialog = layoutInflater.inflate(R.layout.sheet_delivery_time, null)
                val dialog = BottomSheetDialog(requireContext())
                dialog.setOnShowListener {
                    val bottomSheetDialogFragment = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    (viewDialog.parent as View).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
                    val behavior = bottomSheetDialogFragment?.let {
                        BottomSheetBehavior.from(it)
                    }
                    behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
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
             dateSelectDialog = BottomSheetDialog(requireContext())



                dateSelectDialog?.setOnShowListener {
                    val bottomSheetDialogFragment = dateSelectDialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    (viewDialog.parent as View).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
                    val behavior = bottomSheetDialogFragment?.let {
                        BottomSheetBehavior.from(it)
                    }
                    behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
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
                        deliveryDate = dateString
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
                isOrderTrack = true
                addressBar.show()
                checkBoxDelivery.setCompoundDrawablesWithIntrinsicBounds(requireContext().getDrawable(R.drawable.ic_red_checbox),null,null,null)
                checkBoxPick.setCompoundDrawablesWithIntrinsicBounds(requireContext().getDrawable(R.drawable.ic_checbox),null,null,null)
                setDeliveryType(deliveryType = deliveryType!!,true)

            }
            checkBoxPick.setOnClickListener {
                isOrderTrack = false
                checkBoxPick.setCompoundDrawablesWithIntrinsicBounds(requireContext().getDrawable(R.drawable.ic_red_checbox),null,null,null)
                checkBoxDelivery.setCompoundDrawablesWithIntrinsicBounds(requireContext().getDrawable(R.drawable.ic_checbox),null,null,null)
                addressBar.hide()
                setDeliveryType(deliveryType = "PICKUP_ONLY",true)

            }

        }


    }

    private fun showScheduleDialog() {

        val viewDialog = layoutInflater.inflate(R.layout.sehedule_type, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setOnShowListener {
            val bottomSheetDialogFragment = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            (viewDialog.parent as View).setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.transparent
                )
            )
            val behavior = bottomSheetDialogFragment?.let {
                BottomSheetBehavior.from(it)
            }
            behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        }
        val nowText = viewDialog.findViewById<TextView>(R.id.nowText)
        val scheduleText = viewDialog.findViewById<TextView>(R.id.scheduleText)
        val cancelButton: ImageView = viewDialog.findViewById(R.id.cancel_button)

        nowText.setOnClickListener {
            binding.selectTimeType.text = requireContext().getString(R.string.now)
            binding.dateSelect.hide()
            binding.timingSelect.hide()
            dialog.dismiss()
            PrefUtils.instance.setBoolean(IS_SHOW_TIME_BAR,false)
            val dateToday = PrefUtils.instance.getCurrentDateTime()
            val  locale: Locale = Locale.getDefault()
            val formatter = SimpleDateFormat("dd-MM-yyyy", locale)
            deliveryDate = formatter.format(dateToday)
            selectDeliveryTime = ""

        }
        scheduleText.setOnClickListener {
            binding.dateSelect.show()
            binding.timingSelect.show()
            binding.selectTimeType.text = requireContext().getString(R.string.schedule)
            dialog.dismiss()
            isRestaurantOpen(toDayDate = deliveryDate, text = it.tooltipText.toString(), isApiCall = false)
            PrefUtils.instance.setBoolean(IS_SHOW_TIME_BAR,true)

        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setContentView(viewDialog)
        dialog.show()

    }

    private fun setBroadCast(broadCastId: String, orderId: Int?){
        val json = JsonObject()
        json.addProperty("broadcast_id",broadCastId)
        json.addProperty("type","CONVERSION")
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
                        Toast.makeText(requireContext(),data!!.message.toString(),Toast.LENGTH_LONG).show()

                    }
                    is ApiResponse.Failure -> {

                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }
            }

        }
    }


    private fun setBoosterAction(boosterArray: ArrayList<Int>, orderId: Int?) {
        val json = JsonObject()
        val boosterJsonArray = JsonArray()
        for (boosterId in boosterArray) {
            boosterJsonArray.add(boosterId)
        }
        json.add("booster_ids",boosterJsonArray)
        json.addProperty("type","CONVERSION")
        json.addProperty("restaurant_id", restaurantId!!.toInt())
        json.addProperty("order_id",orderId)

        lifecycleScope.launch {
            mViewModelOrder.setBoosterConversion(token =  "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                jsonObject = json).collect{

                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        if (data != null){
                            if (data.is_success!!) {
                                //Remove Shared Pref Booster List
                                PrefUtils.instance.removeBoosterList("Booster")

                            }
                            else {
                                Toast.makeText(context, "" + data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
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
            mViewModel.setDeliveryType(token =  "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
              map = map).collect{

                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        if (data != null && data.isSuccess!!) {
                           binding.totalAmountCl.show()
                            if (isApiCall)
                            getCheckoutData()
                        }
                        else{
                            binding.totalAmountCl.hide()
                            binding.root.showSnackBar(data?.message.toString())

                        }


                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
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