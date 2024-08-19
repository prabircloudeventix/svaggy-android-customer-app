package com.svaggy.ui.fragments.order.screens

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.svaggy.BuildConfig
import com.svaggy.R
import com.svaggy.databinding.FragmentTrackPickOrderBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.order.adapter.CancelOrderAdapter
import com.svaggy.ui.fragments.order.viewmodel.OrdersViewModel
import com.svaggy.ui.fragments.profile.model.Data
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.invisible
import com.svaggy.utils.onBackPressedDispatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

@AndroidEntryPoint
class TrackPickOrder : Fragment() {
    private val mViewModel by viewModels<OrdersViewModel>()

    private var _binding: FragmentTrackPickOrderBinding? = null
    private val binding get() = _binding!!
    private var orderID: String = ""
    private var getReason:String = ""
    private var getRestName:String = ""
    private var restaurantLatitude:String = ""
    private var restaurantLongitude:String = ""
    private val reasonList:ArrayList<Data> = ArrayList()
    private var orderStatus:String = ""
    private lateinit var cancelOrderAdapter: CancelOrderAdapter
    private lateinit var pusher: Pusher
    private lateinit var channel: Channel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            getOrderDetails()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pusher.unsubscribe("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT")
        pusher.disconnect()

    }

    override fun onDetach() {
        super.onDetach()
        pusher.unsubscribe("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT")
        pusher.disconnect()

    }

    override fun onDestroy() {
        super.onDestroy()
        channel.unbind("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT",{})
        pusher.disconnect()

    }

    fun showCustomDialog(context: Context, message: String,orderConfirm:Boolean =false) {
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
            findNavController().popBackStack(R.id.homeFragment,false)
        }

        dialog.show()
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       _binding  = FragmentTrackPickOrderBinding.inflate(inflater, container, false)
        orderID = arguments?.getString("order_id").toString()
        (activity as MainActivity).binding.toolbar.toolbarId.hide()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
     //   progressBarHelper = ProgressBarHelper(requireContext())
        getOrderDetails()
        onBackPressedDispatcher {
            if (arguments?.getString("isFrom") == context?.getString(R.string.order_screen))
                findNavController().popBackStack(R.id.ordersScreen, false)
            else
                findNavController().navigate(R.id.profileScreen)


        }
       binding.btOrderDetails.setOnClickListener {
            findNavController().navigate(TrackPickOrderDirections.actionTrackPickOrderToOrderDetails(orderId = orderID))
        }
        binding.backBtn.setOnClickListener {
            if (arguments?.getString("isFrom") == context?.getString(R.string.order_screen))
                findNavController().popBackStack(R.id.ordersScreen, false)
            else
                findNavController().navigate(R.id.profileScreen)

        }
        binding.icLoc.setOnClickListener {
            val destination = "$restaurantLatitude,$restaurantLongitude" // Example: "37.7749,-122.4194"
            openGoogleMaps(destination)

        }
        binding.cancelOrderBtn.setOnClickListener {
            if (reasonList.isNotEmpty())
                showReasonDialog(reasonList)
            else
                getCancelReason()
        }
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

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun getOrderDetails() {
        lifecycleScope.launch {
            mViewModel.orderDetails(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token)}",
                orderID
            ).collect{

                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                    ////    progressBarHelper.dismissProgressBar()
                        val response = it.data
                        if (response != null){
                            val orderDetails = response.data.order_details
                            val restaurantDetails = response.data.cart_data.restaurant_details
                            val trackOrder = response.data.order_tracking
                            val trackDriver = response.data.driver_map_details
                            getRestName = restaurantDetails.restaurant_name



                            binding.getRestaurantName.text = getRestName

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
                                if (data.text == "Order ID"){
                                    binding.getOderId.text = data.value

                                }

                            }


                            if (trackOrder.isNotEmpty()){
                                trackOrder.forEach {value ->

                                    orderStatus = value?.order_status.toString()
                                    when(orderStatus){

                                        "ORDER_PENDING" ->{


                                        }
                                        "ORDER_PREPARING" ->{
                                            binding.icOrderPreparing.setImageResource(R.drawable.ic_track_order_done)
                                            binding.cancelOrderBtn.invisible()

                                        }
                                        "ORDER_READY" ->{
                                            binding.icReadyPick.setImageResource(R.drawable.ic_track_order_done)

                                        }
                                        "ORDER_OUT_FOR_DELIVERY" ->{
                                            binding.icOrderOut.setImageResource(R.drawable.ic_track_order_done)

                                        }
                                        "ORDER_DELIVERED" ->{
                                            binding.icOrderOut.setImageResource(R.drawable.ic_track_order_done)
                                          showCustomDialog(requireContext(),
                                              "Your food is here! Enjoy your meal from ${response.data.cart_data.restaurant_details.restaurant_name} Praha Kaprova and don't forget to rate your experience in the app!"
                                          ,true)

                                        }
                                    }


                                }
                            }


                        }

                    }
                    is ApiResponse.Failure -> {
                    //    progressBarHelper.dismissProgressBar()
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }

    }


    // Function to open Google Maps with a specific destination
//    private fun openGoogleMaps(destination: String) {
//        // Create a Uri from the destination string
//        val gmmIntentUri = Uri.parse("google.navigation:q=$destination")
//
//        // Create an Intent with the action ACTION_VIEW and the Uri
//        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//
//        // Set the package to Google Maps
//        mapIntent.setPackage("com.google.android.apps.maps")
//
//        // Attempt to start the activity if the Google Maps app is installed
//        if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
//            startActivity(mapIntent)
//        } else {
//            // If the Google Maps app is not installed, you can handle it here
//            // For example, you can open the destination in a web browser
//            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$destination"))
//            startActivity(browserIntent)
//        }
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
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }
    }

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
                            Toast.makeText(requireContext(), "${response.message}", Toast.LENGTH_LONG).show()
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