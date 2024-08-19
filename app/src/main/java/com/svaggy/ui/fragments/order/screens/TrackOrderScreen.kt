package com.svaggy.ui.fragments.order.screens

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.maps.android.PolyUtil
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.svaggy.BuildConfig
import com.svaggy.R
import com.svaggy.databinding.FragmentTrackOrderScreenBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.review.DeliveryReviewActivity
import com.svaggy.ui.fragments.order.adapter.CancelOrderAdapter
import com.svaggy.ui.fragments.order.adapter.OtpAdapter
import com.svaggy.ui.fragments.order.viewmodel.OrdersViewModel
import com.svaggy.ui.fragments.profile.model.Data
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.BEARER
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.invisible
import com.svaggy.utils.onBackPressedDispatcher
import com.svaggy.utils.show
import com.svaggy.utils.updateStatusBarColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.Timer
import java.util.TimerTask


@AndroidEntryPoint
class TrackOrderScreen : Fragment() , OnMapReadyCallback {
    lateinit var binding: FragmentTrackOrderScreenBinding
    private val mViewModel by viewModels<OrdersViewModel>()
    private var orderScreen: String? = ""
    private var orderID: String = ""
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var peekHeight = 0.0
    private lateinit var pusher: Pusher
    private lateinit var channel: Channel
    private  var googleMap: GoogleMap? = null
    private lateinit var  mapFragment:SupportMapFragment
     private  var origin : LatLng? = null
    private  var restaurantLat : LatLng? = null
    private lateinit var otpAdapter: OtpAdapter
    private lateinit var cancelOrderAdapter: CancelOrderAdapter

    private val reasonList:ArrayList<Data> = ArrayList()
    private var getReason:String = ""
    private var getRestName:String = ""
    private var orderStatus:String = ""
    private var timeStamp:String = ""
    private var marker:Marker? = null
    private var driverMarker: Marker? = null
    private var timer: Timer? = null
    private var restaurantMarker: Marker? = null
    private var userMarker: Marker? = null
    private var restaurantNum:String? = null
    private var driverNum:String? = null










    fun showCustomDialog(context: Context, message: String) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.auto_reject_dialog)

        val dialogTitle = dialog.findViewById<TextView>(R.id.txtMessage)
        val dialogButton = dialog.findViewById<TextView>(R.id.btnDialogPositive)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogTitle.text = message


        dialogButton.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
            findNavController().popBackStack(R.id.homeFragment,false)
        }

        dialog.show()
    }



    override fun onDestroy() {
        super.onDestroy()
        pusher.unsubscribe("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT")
        pusher.disconnect()
        timer?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pusher.unsubscribe("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT")
        pusher.disconnect()
        timer?.cancel()
    }

    override fun onDetach() {
        pusher.unsubscribe("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT")
        pusher.disconnect()
        super.onDetach()
    }








    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        requireActivity().updateStatusBarColor("#F6F6F6",true)
        binding = FragmentTrackOrderScreenBinding.inflate(inflater, container, false)
  //      PrefUtils.instance.setString(Constants.CurrentDestinationId, (activity as MainActivity).navController.currentDestination?.id.toString())
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.GONE
    //    (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility = View.GONE
        orderScreen = arguments?.getString("isFrom")
        timer = Timer()
        orderID = arguments?.getString("order_id").toString()
        mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val options = PusherOptions()
        options.setCluster("ap2")
        pusher = Pusher(BuildConfig.PUSHER_KEY, options)
        channel = pusher.subscribe(BuildConfig.PUSHER_CHANNEL)
        pusher.connect()
        channel.bind("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT") { event ->
            val json = JSONObject(event.data)
            val  updateType = json.getString("type")
            if (updateType.equals("ORDER_CANCELLED")){
                val msg = json.getString("message")
                lifecycleScope.launch {
                    showCustomDialog(requireContext(),msg)
                }

            }
            if (json.has("delivery_otp")){
                val  deliveryOtp = json.getString("delivery_otp")
                val digitList = deliveryOtp.toString().map { it.toString().toInt() }
                lifecycleScope.launch (Dispatchers.Main){
                    otpAdapter.updateAdapter(digitList)
                }
            }
            getOrderDetails(this)

            //  updateIcon(updateType)

        }

        onBackPressedDispatcher {
            if (arguments?.getString("isFrom") == context?.getString(R.string.order_screen))
                findNavController().popBackStack(R.id.ordersScreen, false)
            else
                findNavController().navigate(R.id.profileScreen)


        }


        initBinding()
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels / 1.4
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        peekHeight = height / 2.1
        bottomSheetBehavior.peekHeight = peekHeight.toInt()
        bottomSheetBehavior.maxHeight = 2000

        binding.cancelOrderBtn.setOnClickListener {
            if (reasonList.isNotEmpty())
                showReasonDialog(reasonList)
            else
           getCancelReason()
        }

        otpAdapter = OtpAdapter()
        binding.otpRc.adapter = otpAdapter




    }
    override fun onResume() {
        super.onResume()
        getOrderDetails(this)

    }


    private fun initBinding() {
        binding.apply {
          imgBack.setOnClickListener {
                if (arguments?.getString("isFrom") == context?.getString(R.string.order_screen)) {
                    findNavController().popBackStack(R.id.ordersScreen, false)
                } else {
                    findNavController().navigate(R.id.profileScreen)

                }



            }
            btOrderDetails.setOnClickListener {
                findNavController().navigate(TrackOrderScreenDirections.actionTrackOrderScreenToOrderDetails(orderID ?:""))
            }
            imageView9.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
              intent.setData(Uri.parse("tel:$restaurantNum"))
               startActivity(intent)

            }
            binding.callDriver.setOnClickListener {

            }
        }

    }





    private fun updateIcon(updateType: String) {

        when(updateType){
            "ORDER_PREPARING" ->{
                binding.icOrderPreparing.setImageResource(R.drawable.ic_track_order_done)
                binding.cancelOrderBtn.invisible()
                lifecycleScope.launch {

                }

            }
            "ORDER_READY" ->{
                binding.cancelOrderBtn.invisible()
                binding.icReadyPick.setImageResource(R.drawable.ic_track_order_done)


            }
            "ORDER_OUT_FOR_DELIVERY" ->{
                binding.cancelOrderBtn.invisible()
                binding.icOrderOut.setImageResource(R.drawable.ic_track_order_done)

            }
            "ORDER_DELIVERED" ->{
                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        val navController = activity?.findNavController(R.id.fragmentContainerView)
                        navController?.navigate(R.id.deliveryReview, bundleOf("order_id" to orderID))
                    } catch (e: Exception) {
                        // Handle the exception
                        Log.e("NavigationError", "Navigation failed: ${e.message}")
                    }
                }
            }
        }

    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n", "SuspiciousIndentation")
    private fun getOrderDetails(trackOrderScreen: TrackOrderScreen) {
        lifecycleScope.launch {
            mViewModel.orderDetails(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token)}",
                orderID
            ).collect{

                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                   //   progressBarHelper.dismissProgressBar()
                        val response = it.data
                        if (response != null){
                            val orderDetails = response.data.order_details
                            val restaurantDetails = response.data.cart_data.restaurant_details
                            val trackOrder = response.data.order_tracking
                            val trackDriver = response.data.driver_map_details
                             getRestName = restaurantDetails.restaurant_name



                            val restaurantLatitude =  restaurantDetails.latitude.toDouble()
                            val restaurantLongitude =  restaurantDetails.longitude.toDouble()
                            val deliveryTime =  restaurantDetails.delivery_time
                            binding.getRestaurantName.text = getRestName
                            restaurantLat = LatLng(restaurantLatitude, restaurantLongitude)
                           orderDetails.forEach { data ->
                               if (data.text == "Date"){
                                   val orderDate = data.value
                                   val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                   sdf.timeZone = TimeZone.getTimeZone("UTC")
                                   val currentDate = orderDate.let { sdf.parse(it) }
                                   currentDate?.let {
                                       val calendar = Calendar.getInstance()
                                       calendar.time = currentDate
                                       val formattedDate = SimpleDateFormat("dd MMM yyyy").format(calendar.time)
                                       val formattedTime = SimpleDateFormat("h:mm a").format(calendar.time)
                                       binding.getOrderDate.text = "$formattedDate, $formattedTime"
                                   }
                               }
                               if (data.text == "Deliver To"){
                                   val myLatitude = data.latitude?.toDouble()
                                   val myLongitude = data.longitude?.toDouble()
                                   origin = LatLng(myLatitude!!,myLongitude!!)

                               }
                               if (data.text == "Phone Number"){
                                   restaurantNum = data.value

                               }

                           }


                            if (!trackOrder.isNullOrEmpty()){
                                trackOrder.forEach {value ->
                                    timeStamp = value?.timestamp.toString()
                                   orderStatus = value?.order_status.toString()
                                    when(orderStatus){

                                        "ORDER_PENDING" ->{
                                            val formattedTimestamp = convertTimestampToCustomFormat(timeStamp)
                                            binding.getPendingTime.text = formattedTimestamp

                                        }
                                        "ORDER_PREPARING" ->{
                                            binding.icOrderPreparing.setImageResource(R.drawable.ic_track_order_done)
                                            binding.cancelOrderBtn.invisible()
                                            val formattedTimestamp = convertTimestampToCustomFormat(timeStamp)
                                            binding.getPreparingTime.text = formattedTimestamp


                                        }
                                        "ORDER_READY" ->{
                                            binding.icReadyPick.setImageResource(R.drawable.ic_track_order_done)
                                            val formattedTimestamp = convertTimestampToCustomFormat(timeStamp)
                                            binding.getReadyTime.text = formattedTimestamp


                                        }
                                        "ORDER_OUT_FOR_DELIVERY" ->{
                                            binding.icOrderOut.setImageResource(R.drawable.ic_track_order_done)
                                            val formattedTimestamp = convertTimestampToCustomFormat(timeStamp)
                                            binding.callDriver.show()
                                            binding.getOutDeliveryTime.text = formattedTimestamp


                                        }
                                        "ORDER_DELIVERED" ->{
                                           // binding.cancelOrderBtn.text = requireContext().getString(R.string.goReview)
//                                            lifecycleScope.launch(Dispatchers.Main) {
//                                                try {
//                                                    val navController = activity?.findNavController(R.id.fragmentContainerView)
//                                                    navController?.navigate(R.id.deliveryReview, bundleOf("order_id" to orderID))
//                                                } catch (e: Exception) {
//                                                    // Handle the exception
//                                                    Log.e("NavigationError", "Navigation failed: ${e.message}")
//                                                }
//                                            }

                                          //  startActivity(Intent(this@TrackOrderScreen,DeliveryReviewActivity::class.java))

                                        }
                                    }


                                }
                            }

                            val deliveryOtp = response.data.order_confirmation_otp
                            val digitList = deliveryOtp.map { it.toString().toInt() }
                            if (digitList.isNotEmpty())
                                withContext(Dispatchers.Main){
                                    otpAdapter.updateAdapter(digitList)

                                }
                            trackDriver?.restaurant_to_user_polyline?.let { point ->
                                if (response.data.order_status == "ORDER_OUT_FOR_DELIVERY")
                                    drawPolylineNew(point)


                            }
                            trackDriver?.driver_phone_number?.let { phoneNum ->
                                if (response.data.order_status == "ORDER_OUT_FOR_DELIVERY")
                                driverNum = phoneNum
                            }

                        }
                        if (restaurantMarker == null)
                        addMarkerWithStatus("")
                    }
                    is ApiResponse.Failure -> {
                   //   progressBarHelper.dismissProgressBar()
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }

    }

    @SuppressLint("SimpleDateFormat")
    fun convertTimestampToCustomFormat(timestamp: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("MMMM d, HH:mm", Locale.getDefault())
        val date = inputFormat.parse(timestamp)
        return outputFormat.format(date)
    }

    private fun getDriverLocation(){
        lifecycleScope.launch {
            mViewModel.getDriverDetails(token ="$BEARER ${PrefUtils.instance.getString(Constants.Token)}" ,
                orderId = orderID.toInt()).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response != null){
                            val latLong =  LatLng(response.data!!.current_latitude!!.toDouble(),
                                response.data.current_longitude!!.toDouble())
                            updateDriverMarker(latLong)
                        }


                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }

    private fun updateDriverMarker(latLng: LatLng) {
        if (driverMarker == null) {
            // Create and add the marker with the default icon
            val markerOptions = MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker())
            driverMarker = googleMap?.addMarker(markerOptions)
        } else {
            // Update the position of the existing marker
            driverMarker?.position = latLng
        }
//        val vectorDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_driver_new)
//        vectorDrawable?.setBounds(
//            0,
//            0,
//            vectorDrawable.intrinsicWidth,
//            vectorDrawable.intrinsicHeight)
//        val bitmap = Bitmap.createBitmap(
//            vectorDrawable?.intrinsicWidth ?: 0,
//            vectorDrawable?.intrinsicHeight ?: 0,
//            Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//        vectorDrawable?.draw(canvas)
//
//        if (driverMarker == null) {
//            val markerOptions = MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(bitmap))
//            driverMarker = googleMap?.addMarker(markerOptions)
//
//        } else
//            driverMarker?.position = latLng


    }

//    private fun animateMarker(marker: Marker, newLatLng: LatLng, hideMarker: Boolean) {
//        val startPosition = marker.position
//        val endPosition = newLatLng
//        val startRotation = marker.rotation
//
//        val latLngInterpolator = LatLngInterpolator.LinearFixed()
//        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
//        valueAnimator.duration = 1000 // Animation duration in milliseconds
//
//        valueAnimator.addUpdateListener { animation ->
//            val v = animation.animatedFraction
//            val newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition)
//            marker.position = newPosition
//            marker.rotation = getRotation(startPosition, endPosition)
//        }
//
//        if (hideMarker) {
//            valueAnimator.addListener(object : Animator.AnimatorListener {
//
//                override fun onAnimationStart(animation: Animator) {
//
//                }
//
//                override fun onAnimationEnd(animation: Animator) {
//                    marker.isVisible = false
//                }
//
//                override fun onAnimationCancel(animation: Animator) {
//
//                }
//
//                override fun onAnimationRepeat(animation: Animator) {
//
//                }
//            })
//        }
//        valueAnimator.start()
//    }
//
//    private fun getRotation(start: LatLng, end: LatLng): Float {
//        val deltaLat = end.latitude - start.latitude
//        val deltaLng = end.longitude - start.longitude
//        val angle = (Math.atan2(deltaLng, deltaLat) * (180 / Math.PI)).toFloat()
//        return angle
//    }

    // Interface for LatLngInterpolator
//    interface LatLngInterpolator {
//        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng
//
//        class LinearFixed : LatLngInterpolator {
//            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
//                val lat = (b.latitude - a.latitude) * fraction + a.latitude
//                val lng = (b.longitude - a.longitude) * fraction + a.longitude
//                return LatLng(lat, lng)
//            }
//        }
//    }














    private fun drawPolylineNew(point: String) {
        val decodedPolyline: List<LatLng> = PolyUtil.decode(point)
         googleMap?.clear()
        googleMap?.addPolyline(
            PolylineOptions()
                .addAll(decodedPolyline)
                .color(Color.RED) // Set polyline color
                .width(10f)       // Set polyline width
        )

// Optionally, zoom the camera to fit the polyline

        val bounds = LatLngBounds.builder()
        decodedPolyline.forEach { bounds.include(it) }
       // googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),100),
            2000, // Animation duration in milliseconds
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    // Animation finished
                }

                override fun onCancel() {
                    // Animation cancelled
                }
            })

        val startIcon = BitmapFactory.decodeResource(resources, R.drawable.restaurnat_img)
        val endIcon = BitmapFactory.decodeResource(resources, R.drawable.customer_img)
        val startPosition = decodedPolyline.first()
        val startMarkerOptions = MarkerOptions().position(startPosition)
        startMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(startIcon))
        googleMap?.addMarker(startMarkerOptions)
        //restaurantMarker?.position = startPosition

        //
        // Add marker for the end position
        val endPosition = decodedPolyline.last()
        val endMarkerOptions = MarkerOptions().position(endPosition)
        endMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(endIcon))
       // userMarker?.position = endPosition
        googleMap?.addMarker(endMarkerOptions)

        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Call the function to get driver location
                getDriverLocation()
            }
        }, 0, 5000) // 20 seconds


    }


    @SuppressLint("SuspiciousIndentation")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isCompassEnabled = false
        googleMap?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.silver_style)
        )

    }



 private suspend fun addMarkerWithStatus(status: String) {
        if (marker == null){

            if (googleMap == null){
                delay(2000)
            }
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(restaurantLat!!, 16f),
                1500,
                null)




            val vectorDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_restaurant)
            val vectorDrawableUser = ContextCompat.getDrawable(requireContext(), R.drawable.ic_user)
            vectorDrawable?.setBounds(
                0,
                0,
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight)

            vectorDrawableUser?.setBounds(
                0,
                0,
                vectorDrawableUser.intrinsicWidth,
                vectorDrawableUser.intrinsicHeight)

            val bitmap = Bitmap.createBitmap(
                vectorDrawable?.intrinsicWidth ?: 0,
                vectorDrawable?.intrinsicHeight ?: 0,
                Bitmap.Config.ARGB_8888)

            val bitmapUser = Bitmap.createBitmap(
                vectorDrawableUser?.intrinsicWidth ?: 0,
                vectorDrawableUser?.intrinsicHeight ?: 0,
                Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val canvasUser = Canvas(bitmapUser)
            vectorDrawable?.draw(canvas)
            vectorDrawableUser?.draw(canvasUser)



            if (restaurantMarker == null) {
                val markerOptions = MarkerOptions().position(restaurantLat!!).icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                restaurantMarker = googleMap?.addMarker(markerOptions)
            } else {
                restaurantMarker?.position = restaurantLat!!
            }

            if (userMarker ==null){
             val mark =  MarkerOptions().position(origin!!).icon(BitmapDescriptorFactory.fromBitmap(bitmapUser))
                restaurantMarker = googleMap?.addMarker(mark)

            }else{
                userMarker?.position = origin!!

            }

        }


 }




    // Function to add a marker with status above the restaurant location

    // Function to create a custom marker with status text above it









//    private fun getDirections(): DirectionsResult? {
//        val context = GeoApiContext.Builder()
//            .apiKey(getString(R.string.google_maps_key))
//            .build()
//
//        return DirectionsApi.newRequest(context)
//            .mode(TravelMode.DRIVING)
//            .origin(com.google.maps.model.LatLng(origin!!.latitude, origin!!.longitude))
//            .destination(com.google.maps.model.LatLng(destination!!.latitude, destination!!.longitude))
//            .await()
//    }

    private fun drawPath(directions: DirectionsResult) {
        val decodedPath = PolylineEncoding.decode(directions.routes[0].overviewPolyline.encodedPath)

        val options = PolylineOptions()
            .color(Color.BLUE) // Set the color of the track line
            .width(12f) // Set the width of the track line

        for (point in decodedPath) {
            options.add(LatLng(point.lat, point.lng))
        }

        googleMap?.addPolyline(options)

        // Add start and stop point icons
        val startMarkerOptions = MarkerOptions()
            .position(LatLng(directions.routes[0].legs[0].startLocation.lat, directions.routes[0].legs[0].startLocation.lng))
            .title("Start")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) // Set the color of the start marker
        googleMap?.addMarker(startMarkerOptions)

        val endMarkerOptions = MarkerOptions()
            .position(LatLng(directions.routes[0].legs[0].endLocation.lat, directions.routes[0].legs[0].endLocation.lng))
            .title("End")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) // Set the color of the end marker
        googleMap?.addMarker(endMarkerOptions)
    }


  @SuppressLint("MissingInflatedId")
  private fun showReasonDialog(data: List<Data?>?) {
      val viewDialog = layoutInflater.inflate(R.layout.cancel_order_view, null)
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
      val reasonRc = viewDialog.findViewById<RecyclerView>(R.id.reasonRecyclerView)
      val cancelOrderBtn = viewDialog.findViewById<TextView>(R.id.btnCancelOrder)
      val setRestName = viewDialog.findViewById<TextView>(R.id.getRestName)
      setRestName.text = getRestName
      cancelOrderAdapter = CancelOrderAdapter( requireContext(), data!!) { reason ->
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

    private fun getCancelReason(){
        lifecycleScope.launch {
            mViewModel.deleteAccountReason(authToken = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
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
                      //  progressBarHelper.dismissProgressBar()
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }
    }


    private fun cancelOder(reason: String) {
        val map= mapOf(
            "order_id" to orderID.toString(),
            "cancel_reason" to reason)
        lifecycleScope.launch {
            mViewModel.canceledOrder(token =  "$BEARER ${PrefUtils.instance.getString(Constants.Token)}",
                map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {
               //         progressBarHelper.showProgressBar()

                    }
                    is ApiResponse.Success -> {
                   //     progressBarHelper.dismissProgressBar()
                        val response = it.data
                        if (response != null && response.isSuccess!!){
                            findNavController().popBackStack(R.id.homeFragment,false)



                        }


                    }
                    is ApiResponse.Failure -> {
                     //   progressBarHelper.dismissProgressBar()
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }

    }

}
