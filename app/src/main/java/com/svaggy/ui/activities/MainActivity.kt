package com.svaggy.ui.activities

//import com.google.android.gms.tasks.
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.badge.BadgeDrawable
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.svaggy.BuildConfig
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityMainBinding
import com.svaggy.imageslider.ImageModel
import com.svaggy.ui.activities.odercollection.OrderCollectionActivity
import com.svaggy.ui.activities.review.DeliveryReviewActivity
import com.svaggy.ui.activities.track.TrackOrderActivity
import com.svaggy.ui.activities.track.TrackPickOrderActivity
import com.svaggy.ui.fragments.cart.model.ViewCart
import com.svaggy.ui.fragments.cart.viewmodel.CartViewModel
import com.svaggy.ui.fragments.editAuth.viewmodel.AuthViewModel
import com.svaggy.ui.fragments.home.model.SharedPrefModel
import com.svaggy.ui.fragments.location.viewmodel.AddressViewModel
import com.svaggy.ui.fragments.order.viewmodel.OrdersViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.BEARER
import com.svaggy.utils.Constants.DELIVERY_BY_SVAGGY
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.defaultIfNullOrNullString
import com.svaggy.utils.hide
import com.svaggy.utils.invisible
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject


@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(inflateMethod =ActivityMainBinding::inflate ){



    private lateinit var navController: NavController
    val mViewModel by viewModels<CartViewModel>()
    val mCartViewModel by viewModels<AddressViewModel>()
    private val viewModel by viewModels<OrdersViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()
    private var type = ""
    private  var restaurantId:String = ""
    private  var broadCastId:String = ""
    lateinit var icon: BadgeDrawable
    private  var orderId:Int? = null
    private  var deliveryType:String? = null
    private lateinit var pusher: Pusher // Declare Pusher instance
    private lateinit var channel: Channel // Declare Pusher channel
    private lateinit var isFrom:String
    private lateinit var warningDialog:Dialog
    lateinit var badgeDrawable: BadgeDrawable
    private lateinit var appUpdateManager: AppUpdateManager
    val imageUrl:ArrayList<ImageModel> = ArrayList()
   val cartDataList:ArrayList<ViewCart.Data.CartItems> = ArrayList()
    var cartItemRestaurantName = ""
    var cartItemTotalPrices = ""














    override fun ActivityMainBinding.initialize() {
        navController = this@MainActivity.findNavController(R.id.fragmentContainerView)
        supportActionBar?.hide()
        badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.cartScreen)
        badgeDrawable.badgeTextColor = getColor(R.color.white)
        badgeDrawable.backgroundColor = getColor(R.color.primaryColor)
        badgeDrawable.isVisible = false
        type = intent.getStringExtra("type").toString()
        restaurantId = intent.getStringExtra("restaurant_id").toString()
        broadCastId = intent.getStringExtra("broadcast_id").toString()
        isFrom = intent.getStringExtra("isFrom").toString()

        initBinding()
      //  deleteAllCartData()
        checkPlayStoreUpdate()
       // getAddress()
       // initHostDefaultFragment()
        if (isFrom == "ReOrderScreen"){
            navController.navigate(R.id.cartScreen)
            binding.bottomNavigationView.selectedItemId = R.id.cartScreen
        }
        setToken()

    }




    fun getCartItem() {
        lifecycleScope.launch {
            mViewModel.getCartItem(token = "$BEARER ${
                PrefUtils.instance.getString(
                    Constants.Token).toString()}").collect{
                when (it) {
                    is ApiResponse.Loading -> {
                    }
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response?.isSuccess == true) {
                            cartDataList.clear()
                            response.data?.cartItems?.let { res ->
                                cartDataList.addAll(res)
                            }
                            PrefUtils.instance.clearCartItemsFromSharedPreferences()
                            if ((response.data?.cartItems?.size ?: 0) > 0) {
                                response.data?.cartItems?.forEach { item ->
                                    val listOf = listOf(
                                        SharedPrefModel(
                                            item.dishName ?: "",
                                            item.dishType ?: "",
                                            item.price ?: 0.0,
                                            item.isActive ?: false,
                                            item.menuItemId ?: 0,
                                            item.actualPrice ?: 0.0,
                                            item.quantity ?: 0,
                                            item.id ?: 0
                                        )
                                    )
                                    PrefUtils.instance.saveMenuItemsList("${item.menuItemId}", listOf)
                                    badgeDrawable.isVisible = true
                                    badgeDrawable.number = response.data?.totalCartQuantity ?: 0
                                    cartItemRestaurantName = response.data?.restaurantDetails?.restaurantName.toString()
                                    PrefUtils.instance.setString(Constants.RestaurantName,response.data?.restaurantDetails?.restaurantName)
                                    cartItemTotalPrices = "CZK " +PrefUtils.instance.formatDouble(response.data?.totalAmount ?: 0.0)
                                }

                            } else {
                                badgeDrawable.isVisible = false
                            }
                            PrefUtils.instance.setString(Constants.CartRestaurantId, response.data?.restaurantDetails?.id.toString())

                        }
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(this@MainActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }
            }


        }

    }





    private fun trackOrder(orderId: Int, deliveryType: String?,listSize:Int){
        lifecycleScope.launch {
            viewModel.getDeliveryTime(orderId = orderId).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {

                        val response = it.data
                        val jsonObject = JSONObject(response.toString())
                        val dataObject = jsonObject.getJSONObject("data")
                      //  val orderId = dataObject.getInt("order_id")
                       // val currentOrderStatus = dataObject.getString("current_order_status")
                        val orderStatusText = dataObject.getString("current_order_text")
                       // val appOrderId = dataObject.getString("app_order_id")
                        val remainingTime = dataObject.getString("remaining_time")
                        val restaurantName = dataObject.getString("restaurant_name")
                      //  val driverName = dataObject.optString("driver_name","")
                      //  val driverPhoneNumber = dataObject.optString("driver_phone_number","")
                     //   val driverLatitude = dataObject.optString("driver_latitude","")
                     //   val driverLongitude = dataObject.optString("driver_longitude","")
                     //   val createdTime = dataObject.getString("created_time")
                    //    val expectedDeliveryTime = dataObject.getString("expected_delivery_time")
                    //    val createdAt = dataObject.getString("createdAt")
                     //   val updatedAt = dataObject.getString("updatedAt")
                        val cleanTime = remainingTime.replace(" mins", "")

                        if (listSize > 1){
                            binding.trackOrderCard.show()
                            binding.timeLayout.invisible()
                            binding.getReamingTime.invisible()
                            binding.getRestaurantTime.invisible()
                            binding.tvTrack.show()
                            binding.icMulti.show()
                            binding.tvTrack.text = getString(R.string.track_your_2_orders,listSize.toString())


                        }
                        else{
                            if (orderStatusText.equals("Awaiting restaurant confirmation")){
                                binding.trackOrderCard.hide()


                            }
                            else{

                                when(navController.currentDestination?.id) {
                                    R.id.homeFragment ->{
                                        binding.trackOrderCard.show()
                                        binding.timeLayout.show()
                                    }
                                    else ->{
                                        binding.trackOrderCard.hide()
                                    }
                                }




                            }
                            binding.tvTrack.invisible()
                            binding.icMulti.invisible()
                            binding.getReamingTime.show()
                            binding.getRestaurantTime.show()
                            binding.tvFood.show()
                            binding.getReamingTime.text = cleanTime.defaultIfNullOrNullString()
                            binding.getRestaurantTime.text = restaurantName
                            if (!orderStatusText.equals("Awaiting restaurant confirmation")){
                            binding.tvFood.text = orderStatusText
                         }

                        }
                    }
                    is ApiResponse.Failure -> {
                        binding.trackOrderCard.hide()
                        Toast.makeText(this@MainActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

     fun getTodayOrder(){
        val map = mapOf(
            "type" to "TODAY"
        )
        lifecycleScope.launch {
            viewModel.getTodayOrder(authToken ="$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map =map).collect{
                when (it) {
                    is ApiResponse.Loading -> {


                    }
                    is ApiResponse.Success -> {
                        val response = it.data?.data?.orderData

                       if (!response.isNullOrEmpty()){
                           for (i in 0..response.size){
                               orderId = response[0].orderId
                               deliveryType = response[0].deliveryType.toString()
                           }
                           if (orderId != null){
                               trackOrder(orderId ?: 0,deliveryType,response.size)

                           }
                       }else{
                           orderId = null
                           binding.trackOrderCard.hide()
                       }

                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(this@MainActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }

    }
    private fun initBinding() {
        binding.apply {
            hideBar.setOnClickListener {
                binding.ratingOrderBar.hide()
                orderId = null

            }
           goTracking.setOnClickListener {
                if (binding.tvTrack.isVisible){
                    val intent = Intent(this@MainActivity, OrderCollectionActivity::class.java)
                    intent.putExtra("order_id", orderId.toString())
                    intent.putExtra("isFrom", "MainActivity")
                    startActivity(intent)

                }else {
                    if (deliveryType == DELIVERY_BY_SVAGGY) {
                        val intent = Intent(this@MainActivity, TrackOrderActivity::class.java)
                        intent.putExtra("order_id", orderId.toString())
                        intent.putExtra("isFrom", "MainActivity")
                        startActivity(intent)

                    } else {
                        val intent = Intent(
                            this@MainActivity,
                            TrackPickOrderActivity::class.java
                        )
                        intent.putExtra("order_id", orderId.toString())
                        intent.putExtra("isFrom", "MainActivity")
                        startActivity(intent)
                    }
                }

            }
           goOrderRate.setOnClickListener {
                val intent = Intent(this@MainActivity, DeliveryReviewActivity::class.java)
                intent.putExtra("order_id", orderId.toString())
                intent.putExtra("isFrom", "MainActivity")
                startActivity(intent)

            }
//            bottomNavigationView.setOnItemSelectedListener { item ->
//                when (item.itemId) {
//                    R.id.homeFragment -> {
//                        if (navController.currentDestination?.id != R.id.homeFragment) {
//                            navController.navigate(R.id.homeFragment)
//                        }
//                        true
//                    }
//
//                    R.id.cartScreen -> {
//                        binding.trackOrderCard.hide()
//                        if (navController.currentDestination?.id != R.id.cartScreen) {
//                            navController.navigate(R.id.cartScreen) }
//                        true
//                    }
//
//                    R.id.profileScreen -> {
//                        binding.trackOrderCard.hide()
//                        if (navController.currentDestination?.id != R.id.profileScreen) {
//                            navController.navigate(R.id.profileScreen) }
//                        true
//                    }
//
//                    else -> false
//                }
//            }
       }





    }
//    private fun getAddress() {
//        mCartViewModel.getAddressDataLive.observe(this) {
//            if (it.isSuccess!!) {
//                if (it.data.size > 0)
//                {
//                    addressList = it.data
//                }
//            } else {
//                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

//    fun showBottomNavigationHome() {
//        if (PrefUtils.instance.getString(Constants.FragmentBackName)=="HomeFragment") {
//            if (orderId != null){
//                binding.trackOrderCard.show()
//            }else{
//                binding.trackOrderCard.hide()
//
//            }
//           binding.bottomNavigationView.bottomNavigationMain.visibility = View.VISIBLE
//           binding.bottomNavigationView.ivHome.setColorFilter(ContextCompat.getColor(this, R.color.primaryColor))
//            this.getColor(R.color.primaryColor).let {binding.bottomNavigationView.tvHomeText.setTextColor(it) }
//           binding.bottomNavigationView.tvHomeText.typeface = Typeface.DEFAULT_BOLD
//
//           binding.bottomNavigationView.bottomNavigationMain.visibility = View.VISIBLE
//           binding.bottomNavigationView.ivCart.setColorFilter(
//                ContextCompat.getColor(this, R.color.bottom_nav_txt_color))
//            this.getColor(R.color.bottom_nav_txt_color)
//                .let {binding.bottomNavigationView.tvCartText.setTextColor(it) }
//           binding.bottomNavigationView.tvCartText.typeface = Typeface.DEFAULT
//
//           binding.bottomNavigationView.bottomNavigationMain.visibility = View.VISIBLE
//           binding.bottomNavigationView.ivProfile.setColorFilter(
//                ContextCompat.getColor(
//                    this,
//                    R.color.bottom_nav_txt_color
//                )
//            )
//            this.getColor(R.color.bottom_nav_txt_color)
//                .let {binding.bottomNavigationView.tvProfile.setTextColor(it) }
//           binding.bottomNavigationView.tvProfile.typeface = Typeface.DEFAULT
//        }
//
//        else if (PrefUtils.instance.getString(Constants.FragmentBackName)=="CartScreen") {
//            binding.trackOrderCard.hide()
//           binding.bottomNavigationView.bottomNavigationMain.visibility = View.VISIBLE
//           binding.bottomNavigationView.ivHome.setColorFilter(
//                ContextCompat.getColor(
//                    this,
//                    R.color.bottom_nav_txt_color
//                )
//            )
//            this.getColor(R.color.bottom_nav_txt_color)
//                .let {binding.bottomNavigationView.tvHomeText.setTextColor(it) }
//           binding.bottomNavigationView.tvHomeText.typeface = Typeface.DEFAULT
//
//           binding.bottomNavigationView.bottomNavigationMain.visibility = View.VISIBLE
//           binding.bottomNavigationView.ivCart.setColorFilter(
//                ContextCompat.getColor(
//                    this,
//                    R.color.primaryColor
//                )
//            )
//            this.getColor(R.color.primaryColor)
//                .let {binding.bottomNavigationView.tvCartText.setTextColor(it) }
//           binding.bottomNavigationView.tvCartText.typeface = Typeface.DEFAULT_BOLD
//
//           binding.bottomNavigationView.bottomNavigationMain.visibility = View.VISIBLE
//           binding.bottomNavigationView.ivProfile.setColorFilter(
//                ContextCompat.getColor(
//                    this,
//                    R.color.bottom_nav_txt_color
//                )
//            )
//            this.getColor(R.color.bottom_nav_txt_color)
//                .let {binding.bottomNavigationView.tvProfile.setTextColor(it) }
//           binding.bottomNavigationView.tvProfile.typeface = Typeface.DEFAULT
//        }
//
//        else if (PrefUtils.instance.getString(Constants.FragmentBackName)=="ProfileScreen") {
//            binding.trackOrderCard.hide()
//           binding.bottomNavigationView.bottomNavigationMain.visibility = View.VISIBLE
//           binding.bottomNavigationView.ivHome.setColorFilter(
//                ContextCompat.getColor(
//                    this,
//                    R.color.bottom_nav_txt_color
//                )
//            )
//            this.getColor(R.color.bottom_nav_txt_color)
//                .let {binding.bottomNavigationView.tvHomeText.setTextColor(it) }
//           binding.bottomNavigationView.tvHomeText.typeface = Typeface.DEFAULT
//
//           binding.bottomNavigationView.bottomNavigationMain.visibility = View.VISIBLE
//           binding.bottomNavigationView.ivCart.setColorFilter(
//                ContextCompat.getColor(
//                    this,
//                    R.color.bottom_nav_txt_color
//                )
//            )
//            this.getColor(R.color.bottom_nav_txt_color)
//                .let {binding.bottomNavigationView.tvCartText.setTextColor(it) }
//           binding.bottomNavigationView.tvCartText.typeface = Typeface.DEFAULT
//
//           binding.bottomNavigationView.bottomNavigationMain.visibility = View.VISIBLE
//           binding.bottomNavigationView.ivProfile.setColorFilter(
//                ContextCompat.getColor(
//                    this,
//                    R.color.primaryColor
//                )
//            )
//            this.getColor(R.color.primaryColor)
//                .let {binding.bottomNavigationView.tvProfile.setTextColor(it) }
//           binding.bottomNavigationView.tvProfile.typeface = Typeface.DEFAULT_BOLD
//        }
//    }

//    private fun deleteAllCartData() {
//        mViewModel.deleteAllCartDataLive.observe(this) {
//            if (it.isSuccess == true) {
//                PrefUtils.instance.setString(Constants.CartRestaurantId,"")
//                mViewModel.getCartData(
//                    "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}"
//                )
//                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
//            }
//        }
//    }


    override fun onResume() {
        super.onResume()
        Constants.isInitBottomNav = true
        val options = PusherOptions()
        options.setCluster( PrefUtils.instance.getString(Constants.PUSHER_CLUSTER))
        if (!::pusher.isInitialized){
            pusher = Pusher( PrefUtils.instance.getString(Constants.PUSHER_KEY), options)
            channel = pusher.subscribe(BuildConfig.PUSHER_CHANNEL)
            pusher.connect()
            channel.bind("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT") { event ->
                val json = JSONObject(event.data)
                when(json.getString("type")){

                    "ORDER_CANCELLED" ->{
                        lifecycleScope.launch {
                            binding.trackOrderCard.hide()
                            binding.timeLayout.hide()
                            orderId = null
                            if (json.has("message")){
                                val cancelOrderMsg = json.getString("message")
                                showCustomDialog(cancelOrderMsg)
                            }
                        }
                    }
                    "ORDER_DELIVERED" ->{
                        lifecycleScope.launch {
                            binding.trackOrderCard.hide()
                            binding.timeLayout.hide()
                            binding.ratingOrderBar.show()
                        }

                    }
                    else ->{getTodayOrder()}
                }
            }

        }
        getCartItem()

    }
    fun showCustomDialog( message: String) {
        warningDialog = Dialog(this)
        warningDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        warningDialog.setContentView(R.layout.auto_reject_dialog)

        val dialogTitle = warningDialog.findViewById<TextView>(R.id.txtMessage)
        val dialogButton = warningDialog.findViewById<TextView>(R.id.btnDialogPositive)
        warningDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogButton.text = getString(R.string.ok)

        dialogTitle.text = message


        dialogButton.setOnClickListener {
            warningDialog.dismiss()
        }

        warningDialog.show()
    }






    override fun onStop() {
        super.onStop()
        pusher.unsubscribe("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT")
        channel.unbind("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT") {}
        pusher.disconnect()
    }

    override fun onPause() {
        super.onPause()
        Constants.isInitBottomNav = false
        if (::warningDialog.isInitialized){
            warningDialog.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pusher.unsubscribe("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT")
        channel.unbind("${PrefUtils.instance.getString(Constants.UserId)}_USER_EVENT") {}
        pusher.disconnect()
    }


//    private fun initHostDefaultFragment() {
//        val graph = navController.navInflater.inflate(R.navigation.navgraph)
//        when {
//            intent?.getBooleanExtra(PATH, false)!! -> {
//                graph.setStartDestination(R.id.homeFragment)
//            }
//
//        }
//        navController.graph = graph
//    }

    private fun setToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
               val newToken = task.result
                val oldToken = PrefUtils.instance.getString(Constants.DeviceToken)
                if (newToken == oldToken){
                  //  Toast.makeText(this,"Token Not Update",Toast.LENGTH_SHORT).show()
                }else{
                    PrefUtils.instance.setString(Constants.DeviceToken, newToken).toString()
                    val map = mapOf("device_token" to newToken)
                    lifecycleScope.launch {
                        authViewModel.setToken(
                            "Bearer ${
                                PrefUtils.instance.getString(Constants.Token).toString()
                            }", map
                        ).collect {
                            when (it) {
                                is ApiResponse.Loading -> {

                                }

                                is ApiResponse.Success -> {

                                }

                                is ApiResponse.Failure -> {

                                }

                            }

                        }

                    }

                }

            }
        }





    }
//    private fun checkAppUpdate() {
//        val versionCode = BuildConfig.VERSION_CODE
//        val versionName = BuildConfig.VERSION_NAME
//        lifecycleScope.launch {
//            mViewModel.getAppUpdate(deviceOs = "ANDROID", appType = "CUSTOMER_APP", buildVersion =versionName ).collect{
//                when (it) {
//                    is ApiResponse.Loading -> {
//
//                    }
//                    is ApiResponse.Success -> {
//                        val data = it.data.toString()
//
//                        val jsonObject = JSONObject(data)
//                        if (jsonObject.has("data")){
//                            val dataObject = jsonObject.getJSONObject("data")
//                            val isMendatory = dataObject.getBoolean("is_mendatory")
//                            val isUpdateAvailable = dataObject.getBoolean("is_update_available")
//                            if (isUpdateAvailable){
//                                when(isMendatory){
//                                    true ->{
//                                       showMendatoryDialog()
//                                    }
//                                    false ->{showSimpleUpdate()}
//
//                                }
//
//                            }else{
//                              //  Constants.checkAppUpdateCounter = 1
//                            }
//                        }
//                    }
//                    is ApiResponse.Failure -> {
//                    }
//
//                }
//
//            }
//        }
//
//    }
    @SuppressLint("SuspiciousIndentation")
//    private fun showMendatoryDialog() {
//      val   updateDialog = Dialog(this)
//        updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        updateDialog.setContentView(R.layout.update_dialog)
//
//    //    val dialogTitle = updateDialog.findViewById<TextView>(R.id.txtMessage)
//        val dialogButton = updateDialog.findViewById<TextView>(R.id.btnDialogPositive)
//        updateDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//        updateDialog.setCancelable(false)
//
//        dialogButton.setOnClickListener {
//            updateDialog.dismiss()
//            openAppInPlayStore()
//        }
//
//        updateDialog.show()
//
//    }

//    private fun showSimpleUpdate() {
//        val   updateDialog = Dialog(this)
//        updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        updateDialog.setContentView(R.layout.simple_update)
//
//      //  val dialogTitle = updateDialog.findViewById<TextView>(R.id.txtMessage)
//        val dialogButton = updateDialog.findViewById<TextView>(R.id.btnDialogPositive)
//        val maybeBtn = updateDialog.findViewById<TextView>(R.id.maybe)
//        updateDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//        dialogButton.setOnClickListener {
//            updateDialog.dismiss()
//            openAppInPlayStore()
//        }
//        maybeBtn.setOnClickListener {
//            updateDialog.dismiss()
//
//        }
//        updateDialog.setOnDismissListener {
//         //   Constants.checkAppUpdateCounter = 1
//        }
//
//        updateDialog.show()
//
//    }
//    private fun openAppInPlayStore() {
//        try {
//            // Try to open the Play Store app with the specific app package
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.svaggy"))
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        } catch (e: ActivityNotFoundException) {
//            // If the Play Store app is not installed, open the Play Store website
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        }
//    }
    private fun checkPlayStoreUpdate(){
      appUpdateManager = AppUpdateManagerFactory.create(this)

// Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

// Checks that the platform will allow the specified type of update.

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.

                appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    // an activity result launcher registered via registerForActivityResult
                    this,
                    // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                    // flexible updates.
                    1001)
            }
        }

    }






//    private val  appUpdateLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
//        if (result.resultCode != RESULT_OK) {
//            Log.d("preet21", "Update failed! Please try again")
//        }
//    }





}
