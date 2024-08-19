package com.svaggy.ui.activities.track

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.livechatinc.inappchat.ChatWindowActivity
import com.livechatinc.inappchat.ChatWindowConfiguration
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.svaggy.BuildConfig
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityTrackPickOrderBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.order.adapter.CancelOrderAdapter
import com.svaggy.ui.fragments.order.adapter.PickOrderAdapter
import com.svaggy.ui.fragments.order.viewmodel.OrdersViewModel
import com.svaggy.ui.fragments.profile.model.Data
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.invisible
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class TrackPickOrderActivity : BaseActivity<ActivityTrackPickOrderBinding>(ActivityTrackPickOrderBinding::inflate) {
    private val mViewModel by viewModels<OrdersViewModel>()
    private var orderID: String = ""
    private var getReason:String = ""
    private var getRestName:String = ""
    private val reasonList:ArrayList<Data> = ArrayList()
    private lateinit var cancelOrderAdapter: CancelOrderAdapter
    private var restaurantNum:String? = null
    private lateinit var isFrom:String
    private lateinit var pickOrderAdapter: PickOrderAdapter
    private lateinit var pusher: Pusher // Declare Pusher instance
    private lateinit var channel: Channel // Declare Pusher channel



    @SuppressLint("SuspiciousIndentation")
    override fun ActivityTrackPickOrderBinding.initialize(){
        orderID = intent.getStringExtra("order_id").toString()
        isFrom = intent.getStringExtra("isFrom").toString()
        pickOrderAdapter = PickOrderAdapter()
        binding.trackIconRc.adapter = pickOrderAdapter


      getOrderDetailsWithTrack()
       // getDeliveryTime()
        binding.btOrderDetails.setOnClickListener {
            val intent = Intent(this@TrackPickOrderActivity,OrderDetailsActivity::class.java)
            intent.putExtra("order_id", orderID)
            startActivity(intent)

        }
        binding.backBtn.setOnClickListener {
            if (isFrom == "OrderConfirmed")
                startActivity(Intent(this@TrackPickOrderActivity,MainActivity::class.java))
            else
                onBackPressed()

        }
//        binding.icLoc.setOnClickListener {
//            val destination = "$restaurantLatitude,$restaurantLongitude" // Example: "37.7749,-122.4194"
//            openGoogleMaps(destination)
//
//        }
        binding.cancelOrderBtn.setOnClickListener {
            if (reasonList.isNotEmpty())
                showReasonDialog(reasonList)
            else
                getCancelReason()
        }
        binding.support.setOnClickListener {
            initChatWindow()
        }
        binding.callRest.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.setData(Uri.parse("tel:$restaurantNum"))
            startActivity(intent)

        }



    }


    private fun startChatActivity(config: ChatWindowConfiguration) {
        val intent = Intent(this, ChatWindowActivity::class.java)
        intent.putExtras(config.asBundle())
        startActivity(intent)
    }
    private fun initChatWindow() {
        val config = ChatWindowConfiguration.Builder()
            .setLicenceNumber("18238530")
            //.setLicenceNumber("18087297")
            .setVisitorName(PrefUtils.instance.getString(Constants.UserFirstName))
            .setVisitorEmail(PrefUtils.instance.getString(Constants.UserEmail))
            .build()
        startChatActivity(config)


    }

    override fun onResume() {
        super.onResume()
        val options = PusherOptions()
        options.setCluster(PrefUtils.instance.getString(Constants.PUSHER_CLUSTER))
        if (!::pusher.isInitialized){
            pusher = Pusher( PrefUtils.instance.getString(Constants.PUSHER_KEY), options)
            channel = pusher.subscribe(BuildConfig.PUSHER_CHANNEL)
            pusher.connect()
            channel.bind("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT") { event ->
                val json = JSONObject(event.data)

                val  updateType = json.getString("type")
                if (updateType.equals("ORDER_CANCELLED")){
                    val msg = json.getString("message")
                    lifecycleScope.launch {
                        showCustomDialog(this@TrackPickOrderActivity,msg)
                    }
                }
                // getDeliveryTime()
                // getOrderDetails()
                getOrderDetailsWithTrack()
            }

        }
        getOrderDetailsWithTrack()


    }

    override fun onDestroy() {
        super.onDestroy()
        pusher.unsubscribe("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT")
        pusher.disconnect()

    }

    override fun onStop() {
        super.onStop()
        pusher.unsubscribe("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT")
        pusher.disconnect()
    }

    private fun rejectOrderConformation() {
        val dial  = Dialog(this)
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dial.setContentView(R.layout.order_cancel_conformation)

        val dialogButton = dial.findViewById<TextView>(R.id.btnDialogPositive)
        dial.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dial.setOnDismissListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }


        dialogButton.setOnClickListener {
            dial.dismiss()

        }

        dial.show()
    }



    private fun getOrderDetailsWithTrack(){
        lifecycleScope.launch {
            mViewModel.orderDetailsWithTrack(token ="${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token)}",
                orderId = orderID, type = "ORDER_TRACKING").collect{orderRes ->
                when(orderRes){
                    is ApiResponse.Loading -> {
                        mProgressBar().showProgressBar(this@TrackPickOrderActivity)

                    }
                    is ApiResponse.Success -> {
                        if (orderRes.data?.is_success!!){
                            val trackList = orderRes.data.data?.order_tracking
                            val orderData = orderRes.data.data
                            if (!trackList.isNullOrEmpty()){
                                binding.restaurantName.text = orderData?.restaurant_name
                                restaurantNum  = orderData?.restaurant_phone_number
                                getRestName = orderData?.restaurant_name.toString()
                                pickOrderAdapter.setTrackList(trackList)
                                binding.getOrderStatus.text = orderData?.current_order_text
                                if (!orderData?.current_order_text.equals("Awaiting restaurant confirmation")){
                                    binding.cancelOrderBtn.invisible()
                                    binding.tvTime.show()
                                    binding.getEstimateTime.show()
                                }

                                if (orderData?.current_order_text.equals("Order delivered")){
//                                    val intent = Intent(this@TrackPickOrderActivity, DeliveryReviewActivity::class.java)
//                                    intent.putExtra("order_id",orderID)
//                                    startActivity(intent)
                                    showCustomDialog(this@TrackPickOrderActivity, "Your food is here! Enjoy your meal from ${orderData?.restaurant_name} Praha Kaprova and don't forget to rate your experience in the app!",true)
                                }
                                if (!orderData?.current_order_text.equals("Awaiting restaurant confirmation")){
                                    getDeliveryTime()

                                }
                                if (orderData?.current_order_text.equals("Order is begin prepared")){

                                }
                                if (orderData?.current_order_text.equals("Out for delivery")){ }
//                                    val orderDate = data.value
//                                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//                                    sdf.timeZone = TimeZone.getTimeZone("UTC")
//                                    val currentDate = orderDate.let { sdf.parse(it) }
//                                    currentDate?.let {
//                                        val calendar = Calendar.getInstance()
//                                        calendar.time = currentDate
//                                        val formattedDate = SimpleDateFormat("dd MMM yyyy").format(calendar.time)
//                                        val formattedTime = SimpleDateFormat("h:mm a").format(calendar.time)
//                                        binding.getOrderDate.text = "$formattedDate, $formattedTime"
//                                    }
                                if (orderData?.order_status.equals("ORDER_CANCELLED")){
                                    showCustomDialog(this@TrackPickOrderActivity,"",false)

                                }



                            }



                        }

                        mProgressBar().dialog?.dismiss()


                    }
                    is ApiResponse.Failure -> {
                        mProgressBar().dialog?.dismiss()
                        Toast.makeText(this@TrackPickOrderActivity, "${orderRes.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }

    fun showCustomDialog(context: Context, message: String, orderConfirm:Boolean =false) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.auto_reject_dialog)

        val dialogTitle = dialog.findViewById<TextView>(R.id.txtMessage)
        val dialogButton = dialog.findViewById<TextView>(R.id.btnDialogPositive)
        val img = dialog.findViewById<ImageView>(R.id.imgIcon)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogTitle.text = message
        if (orderConfirm)
            img.setImageResource(R.drawable.ic_right)
        else
            img.setImageResource(R.drawable.ic_alert_info)


        dialogButton.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
            startActivity(Intent(this,MainActivity::class.java))
            finish()
          //  findNavController().popBackStack(R.id.homeFragment,false)
        }

        dialog.show()
    }

    private fun openGoogleMaps(destination: String) {
        val uri = "geo:$destination?q=$destination"
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        mapIntent.setPackage("com.google.android.apps.maps")

        try {
            startActivity(mapIntent)
        } catch (e:Exception) {
            // Google Maps app is not installed, fallback to opening in a web browser
            val webUri = "https://www.google.com/maps/search/?api=1&query=$destination"
            val webMapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUri))
            startActivity(webMapIntent)
        }
    }

    private fun getDeliveryTime(){
        lifecycleScope.launch {
            mViewModel.getDeliveryTime(orderId = orderID.toInt()).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val response = it.data
                        val jsonObject = JSONObject(response.toString())
                        val dataObject = jsonObject.getJSONObject("data")
                        val orderId = dataObject.getInt("order_id")
                        val currentOrderStatus = dataObject.getString("current_order_status")
                        val appOrderId = dataObject.getString("app_order_id")
                        val remainingTime = dataObject.getString("remaining_time")
                        val remainingDistance = dataObject.optString("remaining_distance","")
                        val restaurantName = dataObject.getString("restaurant_name")
                        val expectedDeliveryTime = dataObject.getString("expected_delivery_time")
                        val createdAt = dataObject.getString("createdAt")
                        val updatedAt = dataObject.getString("updatedAt")
                        val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        utcFormat.timeZone = TimeZone.getTimeZone("UTC")


                        val utcDate = utcFormat.parse(expectedDeliveryTime)
                        val localFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        localFormat.timeZone = TimeZone.getDefault()
                        val localTimeString = localFormat.format(utcDate)
                        binding.getEstimateTime.text = localTimeString.toString()
                        binding.getOderId.text = appOrderId
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(this@TrackPickOrderActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }


    @SuppressLint("SimpleDateFormat", "SetTextI18n")
//    private fun getOrderDetails() {
//        lifecycleScope.launch {
//            mViewModel.orderDetails(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token)}",
//                orderID
//            ).collect{
//
//                when (it) {
//                    is ApiResponse.Loading -> {
//
//                    }
//                    is ApiResponse.Success -> {
//                        ////    progressBarHelper.dismissProgressBar()
//                        val response = it.data
//                        if (response != null){
//                            val orderDetails = response.data.order_details
//                            val restaurantDetails = response.data.cart_data.restaurant_details
//                            val trackOrder = response.data.order_tracking
//                            val trackDriver = response.data.driver_map_details
//                            getRestName = restaurantDetails.restaurant_name
//
//
//
//                            binding.getRestaurantName.text = getRestName
//
//                            orderDetails.forEach { data ->
//                                if (data.text == "Date"){
//                                    val orderDate = data.value
//                                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//                                    sdf.timeZone = TimeZone.getTimeZone("UTC")
//                                    val currentDate = orderDate.let { sdf.parse(it) }
//                                    currentDate?.let {
//                                        val calendar = Calendar.getInstance()
//                                        calendar.time = currentDate
//                                        val formattedDate = SimpleDateFormat("dd MMM yyyy").format(calendar.time)
//                                        val formattedTime = SimpleDateFormat("h:mm a").format(calendar.time)
//                                        binding.getOrderDate.text = "$formattedDate, $formattedTime"
//                                    }
//                                }
//                                if (data.text == "Order ID"){
//                                    binding.getOderId.text = data.value
//
//                                }
//
//                            }
//
//
//                            if (trackOrder.isNotEmpty()){
//                                trackOrder.forEach {value ->
//                                    timeStamp = value?.timestamp.toString()
//                                    orderStatus = value?.order_status.toString()
//
//                                    when(orderStatus){
//
//                                        "ORDER_PENDING" ->{
//                                            val formattedTimestamp = convertTimestampToCustomFormat(timeStamp)
//                                            binding.getPendingTime.text = formattedTimestamp
//
//
//                                        }
//                                        "ORDER_PREPARING" ->{
//                                            binding.icOrderPreparing.setImageResource(R.drawable.ic_track_order_done)
//                                            binding.cancelOrderBtn.invisible()
//                                            val formattedTimestamp = convertTimestampToCustomFormat(timeStamp)
//                                            binding.getPreparingTime.text = formattedTimestamp
//
//                                        }
//                                        "ORDER_READY" ->{
//                                            binding.icReadyPick.setImageResource(R.drawable.ic_track_order_done)
//                                            val formattedTimestamp = convertTimestampToCustomFormat(timeStamp)
//                                            binding.getReadyTime.text = formattedTimestamp
//
//                                        }
//                                        "ORDER_OUT_FOR_DELIVERY" ->{
//                                            binding.icOrderOut.setImageResource(R.drawable.ic_track_order_done)
//
//                                        }
//                                        "ORDER_DELIVERED" ->{
//                                            binding.icOrderOut.setImageResource(R.drawable.ic_track_order_done)
//                                            showCustomDialog(this@TrackPickOrderActivity,
//                                                "Your food is here! Enjoy your meal from ${response.data.cart_data.restaurant_details.restaurant_name} Praha Kaprova and don't forget to rate your experience in the app!"
//                                                ,true)
//
//                                        }
//                                    }
//
//
//                                }
//                            }
//
//
//                        }
//
//                    }
//                    is ApiResponse.Failure -> {
//                        //    progressBarHelper.dismissProgressBar()
//                        Toast.makeText(this@TrackPickOrderActivity, "${it.msg}", Toast.LENGTH_LONG).show()
//                    }
//
//                }
//
//            }
//
//        }
//
//    }

    private fun getCancelReason(){
        lifecycleScope.launch {
            mViewModel.deleteAccountReason(authToken = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                reasonFor = "USER", reasonType = "ORDER_CANCELLATION").collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {

                        val data = it.data
                        if (data != null){
                            if (data.is_success!!) {
                                reasonList.clear()
                                data.data?.forEach {modelData ->
                                    reasonList.add(modelData!!)
                                }
                                showReasonDialog(reasonList)

                            }
                        }
                    }
                    is ApiResponse.Failure -> {
                        //    progressBarHelper.dismissProgressBar()
                        Toast.makeText(this@TrackPickOrderActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }
    }

    private fun showReasonDialog(data: List<Data?>?) {
        val viewDialog = layoutInflater.inflate(R.layout.cancel_order_view, null)
        val dialog = BottomSheetDialog(this)
        dialog.setOnShowListener {
            val bottomSheetDialogFragment = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            (viewDialog.parent as View).setBackgroundColor(
                ContextCompat.getColor(
                   this, R.color.transparent
                )
            )
            val behavior = bottomSheetDialogFragment?.let {
                BottomSheetBehavior.from(it)
            }
            behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        }
        val reasonRc = viewDialog.findViewById<RecyclerView>(R.id.reasonRecyclerView)
        val cancelOrderBtn = viewDialog.findViewById<TextView>(R.id.btnCancelOrder)
        val setRestName = viewDialog.findViewById<TextView>(R.id.getRestName)
        setRestName.text = getRestName
        cancelOrderAdapter = CancelOrderAdapter( this, data!!) { reason ->
            getReason = reason

        }
        cancelOrderBtn.setOnClickListener {
            cancelOder(getReason)
            dialog.dismiss()
        }
        reasonRc.adapter = cancelOrderAdapter
        dialog.setContentView(viewDialog)
        dialog.show()

    }

    private fun convertTimestampToCustomFormat(timestamp: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("MMMM d, HH:mm", Locale.getDefault())
        val date = inputFormat.parse(timestamp)
        return outputFormat.format(date)
    }




    private fun cancelOder(reason: String) {
        val map= mapOf(
            "order_id" to orderID.toString(),
            "cancel_reason" to reason)
        lifecycleScope.launch {
            mViewModel.canceledOrder(token =  "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token)}",
                map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        //      progressBarHelper.showProgressBar()

                    }
                    is ApiResponse.Success -> {
                        //    progressBarHelper.dismissProgressBar()
                        val response = it.data
                        if (response != null && response.isSuccess!!){
                            rejectOrderConformation()
                        }
                    }
                    is ApiResponse.Failure -> {
                        //   progressBarHelper.dismissProgressBar()
                        Toast.makeText(this@TrackPickOrderActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }

    }


}