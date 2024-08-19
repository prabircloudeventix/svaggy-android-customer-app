package com.svaggy.ui.activities.track

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.maps.android.PolyUtil
import com.livechatinc.inappchat.ChatWindowActivity
import com.livechatinc.inappchat.ChatWindowConfiguration
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.svaggy.BuildConfig
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityTrackOrderBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.review.DeliveryReviewActivity
import com.svaggy.ui.fragments.order.adapter.CancelOrderAdapter
import com.svaggy.ui.fragments.order.adapter.OtpAdapter
import com.svaggy.ui.fragments.order.adapter.TrackIconAdapter
import com.svaggy.ui.fragments.order.viewmodel.OrdersViewModel
import com.svaggy.ui.fragments.profile.model.Data
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.invisible
import com.svaggy.utils.show
import com.svaggy.utils.updateStatusBarColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.Timer
import java.util.TimerTask

@AndroidEntryPoint
class TrackOrderActivity : BaseActivity<ActivityTrackOrderBinding>(ActivityTrackOrderBinding::inflate),OnMapReadyCallback {
    private val mViewModel by viewModels<OrdersViewModel>()
    private var orderID: String = ""
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var peekHeight = 0.0
    private  var googleMap: GoogleMap? = null
    private lateinit var  mapFragment: SupportMapFragment
    private  var origin : LatLng? = null
    private  var restaurantLat : LatLng? = null
    private lateinit var otpAdapter: OtpAdapter
    private lateinit var cancelOrderAdapter: CancelOrderAdapter
    private lateinit var trackIconAdapter: TrackIconAdapter
    private lateinit var pusher: Pusher // Declare Pusher instance
    private lateinit var channel: Channel // Declare Pusher channel

    private val reasonList:ArrayList<Data> = ArrayList()
    private var getReason:String = ""
    private var getRestName:String = ""
    private var driverMarker: Marker? = null
    private var timer: Timer? = null
    private var restaurantMarker: Marker? = null
    private var userMarker: Marker? = null
    private var restaurantNum:String? = null
    private lateinit var isFrom:String
    private var polyline: Polyline? = null
    private lateinit var warningDialog: Dialog
    private var isGoogleMapInit = false
    private var statusCheckOrderId = ""




    override fun ActivityTrackOrderBinding.initialize(){
       updateStatusBarColor("#F6F6F6",true)
        orderID = intent?.getStringExtra("order_id").toString()
        isFrom = intent?.getStringExtra("isFrom").toString()
        mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this@TrackOrderActivity)
        MapsInitializer.initialize(applicationContext)
        initBinding()
        val displayMetrics = DisplayMetrics()
      windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels / 1.4
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        peekHeight = height / 2.1
        bottomSheetBehavior.peekHeight = peekHeight.toInt()
        bottomSheetBehavior.maxHeight = 3000

        binding.cancelOrderBtn.setOnClickListener {
            if (!reasonList.isNullOrEmpty())
                showReasonDialog(reasonList)
            else
                getCancelReason()
        }

        otpAdapter = OtpAdapter()
        binding.otpRc.adapter = otpAdapter
        //
        trackIconAdapter = TrackIconAdapter()
        binding.trackIconRc.adapter = trackIconAdapter
        binding.contactSupport.setOnClickListener {
            initChatWindow()
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
           // .setLicenceNumber("18087297")
            .setVisitorName(PrefUtils.instance.getString(Constants.UserFirstName))
            .setVisitorEmail(PrefUtils.instance.getString(Constants.UserEmail))
            .build()
        startChatActivity(config)


    }

    private fun setGoogleMapsApiKey(apiKey: String) {
        try {
            val googleMapsApiKeyField = MapsInitializer::class.java.getDeclaredField("apiKey")
            googleMapsApiKeyField.isAccessible = true
            googleMapsApiKeyField.set(null, apiKey)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        timer = Timer()
        val options = PusherOptions()
        options.setCluster(PrefUtils.instance.getString(Constants.PUSHER_CLUSTER))
        if (!::pusher.isInitialized){
            pusher = Pusher( PrefUtils.instance.getString(Constants.PUSHER_KEY), options)
            //  pusher = Pusher(BuildConfig.PUSHER_KEY, options)
            channel = pusher.subscribe(BuildConfig.PUSHER_CHANNEL)
            pusher.connect()
            channel.bind("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT") { event ->
                val json = JSONObject(event.data)
                Log.d("preet181",json.toString())
                val updateType = json.getString("type")
                if (updateType.equals("ORDER_CANCELLED")) {
                    val msg = json.getString("message")
                    val pattern = Regex("Order ID: (OD[0-9]+)")
                    val matchResult = pattern.find(msg)
                    val orderId = matchResult?.groups?.get(1)?.value
                    Log.d("preet",orderId.toString())
                    lifecycleScope.launch {
                        showCustomDialog(msg)
                    }

                }else
                    gerOrderDetailsWithTrack()
//            if (json.has("delivery_otp")) {
//                val deliveryOtp = json.getString("delivery_otp")
//                val digitList = deliveryOtp.toString().map { it.toString().toInt() }
//                lifecycleScope.launch(Dispatchers.Main) {
//                    otpAdapter.updateAdapter(digitList)
//                }
//            }

                //  getDeliveryTime()

            }

        }
        if (isGoogleMapInit)
        gerOrderDetailsWithTrack()


      //  getDeliveryTime()
    }
    fun showCustomDialog( message: String) {
       warningDialog = Dialog(this)
        warningDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        warningDialog.setContentView(R.layout.auto_reject_dialog)

        val dialogTitle = warningDialog.findViewById<TextView>(R.id.txtMessage)
        val dialogButton = warningDialog.findViewById<TextView>(R.id.btnDialogPositive)
        warningDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogTitle.text = message


        dialogButton.setOnClickListener {
            warningDialog.dismiss()
            warningDialog.cancel()
          //  findNavController().popBackStack(R.id.homeFragment,false)
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

        warningDialog.show()
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



    override fun onDestroy() {
        super.onDestroy()
        pusher.unsubscribe("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT")
        pusher.disconnect()
        channel.unbind("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT") {}
        timer?.cancel()


    }

    override fun onStop() {
        super.onStop()
        channel.unbind("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT") {}
        pusher.unsubscribe("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT")
        pusher.disconnect()
        timer?.cancel()
        if (::warningDialog.isInitialized){
            warningDialog.dismiss()
        }
    }

    override fun onBackPressed() {
        if (isFrom == "OrderConfirmed" || isFrom == "Splash"){
                startActivity(Intent(this@TrackOrderActivity,MainActivity::class.java))
             finish()

                } else
                super.onBackPressed()

    }


    private fun initBinding() {
        binding.apply {
            imgBack.setOnClickListener {
                if (isFrom == "OrderConfirmed" || isFrom == "Splash"){
                    startActivity(Intent(this@TrackOrderActivity,MainActivity::class.java))
                    finish()
                }
                else{
                onBackPressed()}


            }
            btOrderDetails.setOnClickListener {
                val intent = Intent(this@TrackOrderActivity,OrderDetailsActivity::class.java)
                intent.putExtra("order_id", orderID)
                startActivity(intent)
              //  findNavController().navigate(TrackOrderScreenDirections.actionTrackOrderScreenToOrderDetails(orderID ?:""))
            }
            callRest.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.setData(Uri.parse("tel:$restaurantNum"))
                startActivity(intent)

            }
            contactSupportFull.setOnClickListener {
                initChatWindow()
            }
        }
    }



    @SuppressLint("SimpleDateFormat")
    private fun gerOrderDetailsWithTrack(){
        lifecycleScope.launch {
            mViewModel.orderDetailsWithTrack(token ="${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token)}",
                orderId = orderID, type = "ORDER_TRACKING").collect{orderRes ->
                    when(orderRes){
                        is ApiResponse.Loading -> {
                            mProgressBar().showProgressBar(this@TrackOrderActivity)

                        }
                        is ApiResponse.Success -> {
                            if (orderRes.data?.is_success!!){
                                val trackList = orderRes.data.data?.order_tracking
                                val orderData = orderRes.data.data
                                if (!trackList.isNullOrEmpty()){
                                    binding.restaurantName.text = orderData?.restaurant_name
                                    getRestName = orderData?.restaurant_name.toString()
                                    restaurantNum = orderData?.restaurant_phone_number.toString()
                                trackIconAdapter.setTrackList(trackList)
                                    binding.getOrderStatus.text = orderData?.current_order_text
                                    if (orderData?.current_order_text.equals("Awaiting restaurant confirmation")){
                                        binding.cancelOrderBtn.show()
                                        binding.contactSupport.show()
                                        binding.contactSupportFull.hide()
                                    }else{
                                        binding.cancelOrderBtn.invisible()
                                        binding.contactSupport.hide()
                                        binding.contactSupportFull.show()

                                    }
                                    if (!orderData?.order_confirmation_otp.isNullOrEmpty()){
                                        val digitList = orderData?.order_confirmation_otp.toString().map { it.toString().toInt() }
                                         withContext(Dispatchers.Main) {
                                            otpAdapter.updateAdapter(digitList)
                                        }
                                    }

                                    if (orderData?.order_status.equals("ORDER_DELIVERED")){
                                        val intent = Intent(this@TrackOrderActivity, DeliveryReviewActivity::class.java)
                                        intent.putExtra("order_id",orderID)
                                        startActivity(intent)
                                        finish()
                                    }
                                    if (orderData?.order_status.equals("ORDER_CANCELLED")){
                                        if (!orderData?.current_order_text.isNullOrEmpty()){
                                            showCustomDialog(orderData?.cancelled_order_popup_text.toString())
                                        }
                                    }

                                    if (!orderData?.current_order_text.equals("Awaiting restaurant confirmation")){
                                        getDeliveryTime()

                                    }
                                    if (orderData?.current_order_text.equals("Order is begin prepared")){

                                    }
                                    if (orderData?.current_order_text.equals("Out for delivery")){
                                        timer?.scheduleAtFixedRate(object : TimerTask() {
                                            override fun run() {
                                                // Call the function to get driver location
                                                //  getDriverLocation()
                                                getDeliveryTime()
                                            }
                                        }, 0, 10000) // 20 seconds


                                    }
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
                                    val restLat = orderData?.restaurant_latitide.toString().toDouble()
                                    val restLong = orderData?.restaurant_longitude.toString().toDouble()
                                    val userLat = orderData?.users_latitide.toString().toDouble()
                                    val userLong = orderData?.users_longitude.toString().toDouble()
                                    restaurantLat = LatLng(restLat,restLong)
                                    origin = LatLng(userLat,userLong)
                                    addMarkerWithStatus()

                                }



                            }

                            mProgressBar().dialog?.dismiss()


                        }
                        is ApiResponse.Failure -> {
                            mProgressBar().dialog?.dismiss()
                            Toast.makeText(this@TrackOrderActivity, "${orderRes.msg}", Toast.LENGTH_LONG).show()
                        }

                    }

            }
        }
    }

//Constants.UserEmail

//
private fun convertTimestampToCustomFormat(timestamp: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(timestamp)
        return outputFormat.format(date)
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
                        val polyline = dataObject.optString("polyline","")
                        val driverName = dataObject.optString("driver_name","")
                        val driverPhoneNumber = dataObject.optString("driver_phone_number","")
                        val expectedDeliveryTime = dataObject.getString("expected_delivery_time")
                        if (polyline.isNotEmpty() && polyline != "null")
                        drawPolylineNew(polyline)
                        if (expectedDeliveryTime.isNotEmpty() && expectedDeliveryTime != "null"){
                            binding.tvTime.show()
                            binding.getEstimateTime.show()
                            binding.getEstimateTime.text = convertTimestampToCustomFormat(expectedDeliveryTime)}
                        if (driverName.isNotEmpty() && driverName != "null"){
                            binding.getDriverName.show()
                            binding.tvTime.show()
                            binding.getDriverName.text = driverName
                        }
                        binding.getDriverName.setOnClickListener {
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.setData(Uri.parse("tel:$driverPhoneNumber"))
                            startActivity(intent)

                        }

                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(this@TrackOrderActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }


    private fun drawPolylineNew(point: String) {
        val decodedPolyline: List<LatLng> = PolyUtil.decode(point)
        polyline?.remove()

        // Add new polyline to the map
        polyline = googleMap?.addPolyline(
            PolylineOptions()
                .addAll(decodedPolyline)
                .color(Color.RED) // Set polyline color
                .width(10f)       // Set polyline width
        )



// Optionally, zoom the camera to fit the polyline

        val bounds = LatLngBounds.builder()
        decodedPolyline.forEach { bounds.include(it) }
        // googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngBounds(bounds.build(),100),
            2000, // Animation duration in milliseconds
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    // Animation finished
                }

                override fun onCancel() {
                    // Animation cancelled
                }
            })

        // Add or update marker for the driver's position
        val startIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_driver_new)
        val startPosition = decodedPolyline.first()
        if (driverMarker == null) {
            val startMarkerOptions = MarkerOptions().position(startPosition)
                .icon(BitmapDescriptorFactory.fromBitmap(startIcon))
            driverMarker = googleMap?.addMarker(startMarkerOptions)
        } else {
            driverMarker?.position = startPosition
        }

//        val startIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_driver_new)
//        val startPosition = decodedPolyline.first()
//        val startMarkerOptions = MarkerOptions().position(startPosition)
//        startMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(startIcon))
//        if (driverMark ==null){
//            driverMarker = googleMap?.addMarker(startMarkerOptions)
//        }else{
//            driverMarker?.position = startPosition
//        }
       // googleMap?.addMarker(startMarkerOptions)
        //restaurantMarker?.position = startPosition

        //
        // Add marker for the end position
        val endIcon = BitmapFactory.decodeResource(resources, R.drawable.customer_img)
        val endPosition = decodedPolyline.last()
        if (userMarker == null){
            val endMarkerOptions = MarkerOptions().position(endPosition)
            endMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(endIcon))
            userMarker = googleMap?.addMarker(endMarkerOptions)
        }else{
            userMarker?.position = endPosition
        }


    }


    @SuppressLint("SuspiciousIndentation")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isCompassEnabled = false
        googleMap?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
               this,
                R.raw.silver_style))
        isGoogleMapInit = true
        gerOrderDetailsWithTrack()

    }



    private suspend fun addMarkerWithStatus() {


            if (googleMap == null){
                delay(2000)
            }
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(restaurantLat!!, 16f),
                1500,
                null)




            val restIcon = BitmapFactory.decodeResource(resources, R.drawable.restaurnat_img)
            val userIcon = BitmapFactory.decodeResource(resources, R.drawable.customer_img)



            if (restaurantMarker == null) {
                val markerOptions = MarkerOptions().position(restaurantLat!!).icon(BitmapDescriptorFactory.fromBitmap(restIcon))
                restaurantMarker = googleMap?.addMarker(markerOptions)
            } else {
                restaurantMarker?.position = restaurantLat!!
            }

            if (userMarker ==null){
                val mark =  MarkerOptions().position(origin!!).icon(BitmapDescriptorFactory.fromBitmap(userIcon))
                userMarker = googleMap?.addMarker(mark)
            }else{
                userMarker?.position = origin!!
            }

    }


    private fun showReasonDialog(data: List<Data?>?) {
        val viewDialog = layoutInflater.inflate(R.layout.cancel_order_view, null)
        val dialog = BottomSheetDialog(this)
        dialog.setOnShowListener {
            val bottomSheetDialogFragment = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            (viewDialog.parent as View).setBackgroundColor(
                ContextCompat.getColor(this, R.color.transparent)
            )
            val behavior = bottomSheetDialogFragment?.let {
                BottomSheetBehavior.from(it)
            }
            behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        }
        val reasonRc = viewDialog.findViewById<RecyclerView>(R.id.reasonRecyclerView)
        val cancelOrderBtn = viewDialog.findViewById<TextView>(R.id.btnCancelOrder)
        val setRestName = viewDialog.findViewById<TextView>(R.id.getRestName)
        val getItem = viewDialog.findViewById<TextView>(R.id.getItem)
        val cancelImg = viewDialog.findViewById<ImageView>(R.id.cancelImg)
        setRestName.text = getRestName
        cancelOrderAdapter = CancelOrderAdapter( this, data!!) { reason ->
            getReason = reason

        }
        cancelImg.setOnClickListener {
            dialog.dismiss()

        }
        cancelOrderBtn.setOnClickListener {
            mProgressBar().dialog?.show()
           cancelOder(getReason)
       //     cancelOder("")
        //    dialog.dismiss()
        }
        reasonRc.adapter = cancelOrderAdapter
        dialog.setContentView(viewDialog)
        dialog.show()

    }

    private fun getCancelReason(){
        lifecycleScope.launch {
            mViewModel.deleteAccountReason(authToken = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                reasonFor = "USER", reasonType = "ORDER_CANCELLATION").collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        mProgressBar().dialog?.dismiss()

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
                       mProgressBar().dialog?.dismiss()
                        Toast.makeText(this@TrackOrderActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }
    }


    private fun cancelOder(reason: String) {

        val map= mapOf(
            "order_id" to orderID,
            "cancel_reason" to reason)

        lifecycleScope.launch {
            mViewModel.canceledOrder(token =  "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token)}",
                map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        //         progressBarHelper.showProgressBar()

                    }
                    is ApiResponse.Success -> {
                        //     progressBarHelper.dismissProgressBar()
                        val response = it.data
                        if (response != null && response.isSuccess!!){
                            rejectOrderConformation()

                        }


                    }
                    is ApiResponse.Failure -> {
                        //   progressBarHelper.dismissProgressBar()
                        Toast.makeText(this@TrackOrderActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }

    }


}