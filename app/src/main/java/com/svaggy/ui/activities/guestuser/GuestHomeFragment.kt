package com.svaggy.ui.activities.guestuser

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.svaggy.R
import com.svaggy.databinding.FragmentGuestHomeBinding
import com.svaggy.imageslider.ImageModel
import com.svaggy.pagination.adapter.AllRestaurantPageAdapter
import com.svaggy.pagination.adapter.OfferPaginationAdapter
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.banner.ComboScreenActivity
import com.svaggy.ui.activities.banner.CompareFoodActivity
import com.svaggy.ui.activities.home_location.HomeLocationActivity
import com.svaggy.ui.activities.home_search.FoodSearchActivity
import com.svaggy.ui.activities.restaurant.AllRestaurantActivity
import com.svaggy.ui.activities.restaurant.RestaurantMenuActivity
import com.svaggy.ui.fragments.home.adapter.AllRestaurantAdapter
import com.svaggy.ui.fragments.home.adapter.CuisinesAdapter
import com.svaggy.ui.fragments.home.adapter.RestaurantsFilterAdapter
import com.svaggy.ui.fragments.home.adapter.SliderAdapter
import com.svaggy.ui.fragments.home.adapter.SortByAdapter
import com.svaggy.ui.fragments.home.model.RestaurantFilter
import com.svaggy.ui.fragments.home.model.SharedPrefModel
import com.svaggy.ui.fragments.home.model.SortByFilter
import com.svaggy.ui.fragments.home.viewmodel.HomeViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.onBackPressedDispatcher
import com.svaggy.utils.show
import com.svaggy.utils.updateStatusBarColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

@AndroidEntryPoint
class GuestHomeFragment : Fragment() {
    private var _binding: FragmentGuestHomeBinding? = null
    private val binding get() = _binding!!
    private val mViewModel by viewModels<HomeViewModel>()
    private var cuisinesAdapter: CuisinesAdapter? = null
    private var allRestaurantAdapter: AllRestaurantAdapter? = null
    private var restaurantFilterList:ArrayList<RestaurantFilter>? = null
    private var sortByList: ArrayList<SortByFilter>? = null
    private var latitude:String?=""
    private var longitude:String?=""
    var dialog: BottomSheetDialog?=null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private var progressDialog: Dialog? = null
    private lateinit var allRestaurantPageAdapter: AllRestaurantPageAdapter
    private lateinit var  offerPaginationAdapter: OfferPaginationAdapter
    private var  dataLoad = false
    private lateinit var imageUrl:ArrayList<ImageModel>



    private val locationPermissions = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            when(isGPSEnabled()){
                true ->{requestLocationUpdates()}
                false ->{locationServiceCheck()}
            }
        } else {
            openAppSettings()
        }
    }





    private val locationSettingsResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            requestLocationUpdates()
        } else {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            locationSettingsLauncher.launch(intent)
        }
    }

    private val locationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { _ ->
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (enabled){
            requestLocationUpdates()
        }else{
            locationServiceCheck()
        }
    }


    private val allowPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { _ ->
        if (checkLocationPermission()){
            if (isGPSEnabled()){
                requestLocationUpdates()
            }else{
                locationServiceCheck()
            }
        }else{
            showPermissionRequestDialog()
        }
    }

    private fun showPermissionRequestDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Location Permission Required")
        builder.setMessage("This app requires access to your location to function properly.")
        builder.setPositiveButton("Grant") { dialog, which ->
            openAppSettings()
            dialog.dismiss()
        }
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        requireActivity().updateStatusBarColor("#F6F6F6",true)
        _binding = FragmentGuestHomeBinding.inflate(inflater, container, false)


        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        initObserver()
        onBackPressedDispatcher {
            requireActivity().finish()
        }
        getCartItem()
        return binding.root
    }


    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.offerSlider.adapter = SliderAdapter( (activity as GuestHomeActivity).imageUrl ,::getSliderItem)
        binding.indicator.setPager(binding.offerSlider)
        binding.offerSlider.currentItem = 0

        binding.txtSearch.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        /**
         * list all data add
         */

        restaurantFilterList = ArrayList()
        val filterTextList = listOf(
            RestaurantFilter(getString(R.string.sort_by),"sort_by", ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down),false),
            RestaurantFilter(getString(R.string.featured),"is_featured", null,false),
            RestaurantFilter(getString(R.string.greatOffers),"is_greatoffer", null,false),
            RestaurantFilter(getString(R.string.nearest),"is_nearest", null,false),
            RestaurantFilter(getString(R.string.veg),"is_veg", ContextCompat.getDrawable(requireContext(), R.drawable.veg_icon),false),
            RestaurantFilter(getString(R.string.non_veg),"is_non_veg", ContextCompat.getDrawable(requireContext(), R.drawable.nonveg_icon), false),
            RestaurantFilter(getString(R.string.vegan_txt),"is_vegan", ContextCompat.getDrawable(requireContext(), R.drawable.vegan), false)
        )
        binding.recyclerRestaurantsFilter.adapter = RestaurantsFilterAdapter(
            context = requireContext(),
            restaurantFilter = filterTextList,
            getSelectType = { _, _,list ->
                restaurantFilterList!!.addAll(list)
                getAllRestaurant(list = restaurantFilterList,sortValue = null)},
            sortType = { showSortDialog()})
        //
        sortByList = ArrayList()
        sortByList?.add(SortByFilter("Relevance","relevance",false))
        sortByList?.add(SortByFilter("Rating (High to Low)","ratings",false))
        sortByList?.add(SortByFilter("Delivery Time (Low to High)","deliveryTimeLowToHigh",false))
        sortByList?.add(SortByFilter("Delivery Time (High to Low)","deliveryTimeHighToLow",false))




        binding.offerViewAllBt.setOnClickListener {
            startActivity(Intent(requireContext(), AllRestaurantActivity::class.java))

        }

        binding.locationGet.setOnClickListener {
            startActivity(Intent(requireContext(), HomeLocationActivity::class.java))
        }

        binding.txtSearch.setOnClickListener {
            startActivity(Intent(requireContext(), FoodSearchActivity::class.java))
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())


        if (!PrefUtils.instance.getString(Constants.RESTAURANT_ADDRESS).isNullOrEmpty())
            binding.txtCurrentAddress.text = PrefUtils.instance.getString(Constants.RESTAURANT_ADDRESS)

        if (Constants.getCuisines.isNotEmpty()){
            binding.recyclerCuisines.layoutManager = GridLayoutManager(activity, 2, GridLayoutManager.HORIZONTAL, false)
            cuisinesAdapter =  CuisinesAdapter(requireContext(), Constants.getCuisines,:: getCuisinesRestaurant)
            binding.recyclerCuisines.adapter = cuisinesAdapter

        }else{
            mViewModel.getCuisines("${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}")


        }



        if (checkLocationPermission()){
            if (isGPSEnabled()){
                requestLocationUpdates()
            }else{
                locationServiceCheck()
            }

        }
        else{
            requestLocationPermission()

        }
    }

    fun getCartItem() {
        lifecycleScope.launch {
            mViewModel.getCartItem(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}").collect{
                when (it) {
                    is ApiResponse.Loading -> {
                    }
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response?.isSuccess == true) {
                            PrefUtils.instance.setString(Constants.CartRestaurantId, response.data?.restaurantDetails?.id.toString())
                            if ((activity as GuestHomeActivity).imageUrl.isEmpty()){
                                (activity as GuestHomeActivity).imageUrl.clear()
                                if (response.data?.isShowPromoBanner == true && response.data?.isComboAvailable == true){
                                    (activity as GuestHomeActivity).imageUrl.add(ImageModel(R.drawable.offer1,"isShowPromoBanner"))
                                    (activity as GuestHomeActivity).imageUrl.add(ImageModel(R.drawable.offer2,"isComPare"))
                                    (activity as GuestHomeActivity).imageUrl.add(ImageModel(R.drawable.offer3,"isComboAvailable"))

                                }
                                else{
                                    if (response.data?.isShowPromoBanner == true){
                                        (activity as GuestHomeActivity).imageUrl.add(ImageModel(R.drawable.offer1,"isShowPromoBanner"))
                                        (activity as GuestHomeActivity).imageUrl.add(ImageModel(R.drawable.offer2,"isComPare"))


                                    }
                                    if (response.data?.isComboAvailable == true){
                                        //  Constants.imageUrl.add(R.drawable.offer1)
                                        (activity as GuestHomeActivity).imageUrl.add(ImageModel(R.drawable.offer2,"isComPare"))
                                        (activity as GuestHomeActivity).imageUrl.add(ImageModel(R.drawable.offer3,"isComboAvailable"))

                                    }
                                    if (response.data?.isShowPromoBanner == false && response.data?.isComboAvailable == false){
                                        (activity as GuestHomeActivity).imageUrl.add(ImageModel(R.drawable.offer2,"isComPare"))

                                    }
                                }

                                binding.offerSlider.adapter = SliderAdapter( (activity as GuestHomeActivity).imageUrl ,::getSliderItem)
                                binding.indicator.setPager(binding.offerSlider)
                                binding.offerSlider.currentItem = 0
                            }else{
                                binding.offerSlider.adapter = SliderAdapter( (activity as GuestHomeActivity).imageUrl ,::getSliderItem)
                                binding.indicator.setPager(binding.offerSlider)
                                binding.offerSlider.currentItem = 0
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








    private fun getAllRestaurant(list: List<RestaurantFilter>? = null,sortValue:String? = null,isLoaderShow:Boolean = true) {


        val json = JsonObject()
        json.addProperty("latitude",PrefUtils.instance.getString(Constants.Latitude).toString())
        json.addProperty("longitude", PrefUtils.instance.getString(Constants.Longitude).toString())

        list?.forEach {
            if (it.filterBoolean){
                // map[it.paramName] = true.toString()
                json.addProperty(it.paramName,true)
            }
        }
        if (sortValue != null){
            // map["sort_by"] = sortValue
            json.addProperty("sort_by",sortValue)
        }



        // val queryParams = mapOf("latitude" to "30.6573401", "longitude" to "76.6809003") // example parameters
        lifecycleScope.launch {
            mViewModel.getRestaurantsPagination(authToken = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",json).collect { pagingData ->
                allRestaurantPageAdapter = AllRestaurantPageAdapter(context = requireContext(), goMenuScreen = ::goMenuScreen)
                allRestaurantPageAdapter.submitData(lifecycle,pagingData)
                binding.recyclerRestaurants.adapter = allRestaurantPageAdapter
                allRestaurantPageAdapter.loadStateFlow.collectLatest { loadState ->
                    when {
                        loadState.refresh is LoadState.Loading || loadState.append is LoadState.Loading -> {
                            //  if (allRestaurantPageAdapter.itemCount == 0){
                            binding.recyclerRestaurants.hide()
                            binding.shimmerBar.show()
                            binding.consEmptyRestaurant.hide()
                            //  }
                            // Show progress bar while refreshing or appending
                            // binding.progressBar?.isVisible = true
                            // binding.noOrder.visibility = View.GONE // Hide "no order" message
                        }

                        loadState.refresh is LoadState.Error || loadState.append is LoadState.Error -> {
                            binding.recyclerRestaurants.hide()
                            binding.shimmerBar.hide()
                            binding.consEmptyRestaurant.show()

                        }

                        loadState.refresh is LoadState.NotLoading && loadState.append is LoadState.NotLoading -> {
                            if (allRestaurantPageAdapter.itemCount == 0){
                                binding.recyclerRestaurants.hide()
                                binding.shimmerBar.hide()
                                binding.consEmptyRestaurant.show()

                            }else{
                                binding.recyclerRestaurants.show()
                                binding.shimmerBar.hide()
                                binding.consEmptyRestaurant.hide()
                                dataLoad = true

                            }


                        }
                    }
                }
            }

        }



//        lifecycleScope.launch {
//            mViewModel.getAllRestaurant(
//                token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
//                map = map).collect{
//                when (it) {
//                    is ApiResponse.Loading -> {
//                        showProgressBar(requireContext())
//
//
//                    }
//                    is ApiResponse.Success -> {
//                        progressDialog?.dismiss()
//                        val response = it.data
//                        if (response != null && response.data.isNotEmpty() ){
//                            binding.recyclerRestaurants.show()
//                            binding.consEmptyRestaurant.hide()
//                            Constants.allRestaurant.clear()
//                            Constants.allRestaurant.addAll(response.data)
//                            allRestaurantAdapter =  AllRestaurantAdapter(requireContext(), response.data, ::goMenuScreen)
//                           binding.recyclerRestaurants.adapter = allRestaurantAdapter
//                        }
//                        else{
//                           binding.recyclerRestaurants.hide()
//                           binding.consEmptyRestaurant.show()
//                        }
//
//                    }
//                    is ApiResponse.Failure -> {
//                        progressDialog?.dismiss()
//                        binding.recyclerRestaurants.visibility = View.GONE
//                       binding.consEmptyRestaurant.visibility = View.VISIBLE
//                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
//                    }
//
//                }
//
//            }
//        }

    }



    private fun isGPSEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun locationServiceCheck() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(50)
            .setMaxUpdateDelayMillis(50)
            .build()
        val settingsClient = LocationServices.getSettingsClient(requireActivity())

        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        settingsClient.checkLocationSettings(locationSettingsRequest).addOnSuccessListener {}
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {

                    try {
                        locationSettingsResultLauncher.launch(IntentSenderRequest.Builder(e.resolution).build())
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Exception occurred while attempting to send the Intent, handle it here
                    }
                } else {
                    // Handle other types of exceptions
                }
            }
    }



    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {


        if (!PrefUtils.instance.getString(Constants.Latitude).isNullOrEmpty()){
            /**
             * get All Restaurant
             *
             */
            getAllRestaurant(list = null,sortValue = null, isLoaderShow = true)
            getOfferRestaurant()


        }
        else{
            //    progressBarHelper.showProgressBar()
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 50)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(50)
                .setMaxUpdateDelayMillis(50)
                .setMinUpdateDistanceMeters(10000f)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    //       progressBarHelper.dismissProgressBar()
                    val location = locationResult.lastLocation
                    latitude  = location?.latitude.toString()
                    longitude = location?.longitude.toString()
                    binding.txtCurrentAddress.text = getAddressFromLocation(requireContext(), location!!.latitude, location.longitude)
                    PrefUtils.instance.setString(Constants.Latitude,latitude)
                    PrefUtils.instance.setString(Constants.Longitude,longitude)
                    if (!PrefUtils.instance.getString(Constants.Latitude).isNullOrEmpty()){
                        /**
                         * get All Restaurant
                         */

                        getAllRestaurant(list = null,sortValue = null,isLoaderShow = false)
                        getOfferRestaurant()

                    }

                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())


        }
    }



    private fun checkLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermission() {
        locationPermissions.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        allowPermissionLauncher.launch(intent)

    }

    fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        var addressText = ""

        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                val addressParts = mutableListOf<String>()

                for (i in 0..address.maxAddressLineIndex) {
                    addressParts.add(address.getAddressLine(i))
                }

                addressText = addressParts.joinToString(separator = "\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        PrefUtils.instance.setString(Constants.RESTAURANT_ADDRESS,addressText)

        return addressText
    }

    private fun getSliderItem(type: String) {
        when (type) {

            "isShowPromoBanner" -> {
                binding.nestedScrollView.post {
                    val recyclerView = binding.recyclerRestaurants
                    val nestedScrollView = binding.nestedScrollView


                    val child = recyclerView.getChildAt(2)

                    if (child != null) {
                        nestedScrollView.smoothScrollTo(0, child.top)
                    } else {
                        // Handle case when child is null
                        Log.e("ScrollError", "Child at position 2 is null")
                    }
                }
            }

            "isComPare" -> {
                startActivity(Intent(requireContext(), CompareFoodActivity::class.java))
            }

            "isComboAvailable" -> {
                startActivity(Intent(requireContext(), ComboScreenActivity::class.java))
            }
        }

    }



    private fun initObserver() {
        mViewModel.getCuisinesLiveData.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
                Constants.getCuisines.addAll(it.data)
                binding.recyclerCuisines.layoutManager = GridLayoutManager(activity, 2, GridLayoutManager.HORIZONTAL, false)
                cuisinesAdapter =  CuisinesAdapter(requireContext(), it.data,:: getCuisinesRestaurant)
                binding.recyclerCuisines.adapter = cuisinesAdapter
            }
        }


        mViewModel.errorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCuisinesRestaurant(cuisineId: Int, cuisineName: String) {
//        val bundle = Bundle()
//        bundle.putString("cuisine_id", cuisineId.toString())
//        bundle.putString("cuisine_name", cuisineName)
//        bundle.putString("method_call", "CuisineAllRestaurants")
//         findNavController().navigate(R.id.action_guestHomeFragment_to_restaurantMenuScreen2,bundle)
        val intent = Intent(requireContext(), AllRestaurantActivity::class.java)
        intent.putExtra("cuisine_id",cuisineId.toString())
        intent.putExtra("cuisine_name",cuisineName)
        intent.putExtra("method_call","CuisineAllRestaurants")
        startActivity(intent)
    }



    private fun showSortDialog(){
        val viewDialog = layoutInflater.inflate(R.layout.sheet_shortby_filter, null)
        dialog = BottomSheetDialog(requireContext())
        dialog?.setOnShowListener {
            val bottomSheetDialogFragment = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            (viewDialog.parent as View).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
            val behavior = bottomSheetDialogFragment?.let {
                BottomSheetBehavior.from(it)
            }
            behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        }

        val recyclerSortBy=viewDialog.findViewById<RecyclerView>(R.id.recyclerSortBy)
        val cancelImage=viewDialog.findViewById<ImageView>(R.id.cancelImage)

        cancelImage.setOnClickListener {
            dialog?.dismiss()
        }

        recyclerSortBy?.adapter = SortByAdapter(
            context = requireContext(),
            sortByList = sortByList!!,
            getRestaurantFilter = { value->
                getAllRestaurant(list = restaurantFilterList,sortValue = value)
                dialog?.dismiss() })

        dialog?.setContentView(viewDialog)
        dialog?.show()

    }


    private fun goMenuScreen(id: Int, getDeliveryType: String,boosterArray: ArrayList<Int>,deliveryTimer:String) {
//        val bundle = Bundle()
//        bundle.putString("item_id", id.toString())
//        bundle.putString("deliveryType", getDeliveryType)
//        bundle.putString("pop_back", "HomeFragment")
//        findNavController().navigate(R.id.action_guestHomeFragment_to_restaurantMenuScreen2,bundle)
        val intent = Intent(requireContext(), RestaurantMenuActivity::class.java)
        PrefUtils.instance.setString("item_id",id.toString())
        PrefUtils.instance.setString("deliveryType",getDeliveryType)
        PrefUtils.instance.setString("deliveryTime",deliveryTimer)
      //  intent.putExtra("item_id",id.toString())
      //  intent.putExtra("deliveryType",getDeliveryType)
      //  intent.putExtra("deliveryTime",deliveryTimer)
       // intent.putExtra("pop_back","HomeFragment")
        startActivity(intent)

    }

    private fun getOfferRestaurant(){
//        val map = mapOf(
//            "latitude" to PrefUtils.instance.getString(Constants.Latitude).toString(),
//            "longitude" to PrefUtils.instance.getString(Constants.Longitude).toString())

        val json = JsonObject()
        json.addProperty("latitude",PrefUtils.instance.getString(Constants.Latitude).toString())
        json.addProperty("longitude", PrefUtils.instance.getString(Constants.Longitude).toString())


        lifecycleScope.launch {
            mViewModel.getOfferRestaurantsPagination(authToken = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",json).collect { pagingData ->
                offerPaginationAdapter = OfferPaginationAdapter(context = requireContext(), goMenuScreen = ::goMenuScreen)
                offerPaginationAdapter.submitData(lifecycle,pagingData)
                binding.recyclerOfferRestaurants.adapter = offerPaginationAdapter
                offerPaginationAdapter.loadStateFlow.collectLatest { loadState ->

                    when {
                        loadState.refresh is LoadState.Loading || loadState.append is LoadState.Loading -> {
                            // Show progress bar while refreshing or appending
                            // binding.progressBar?.isVisible = true
                            // binding.noOrder.visibility = View.GONE // Hide "no order" message
                        }

                        loadState.refresh is LoadState.Error || loadState.append is LoadState.Error -> {
                            binding.recyclerRestaurants.hide()
                            binding.shimmerBarOffer.hide()
                            binding.constraintLayout7.hide()

                        }

                        loadState.refresh is LoadState.NotLoading && loadState.append is LoadState.NotLoading -> {
                            if (offerPaginationAdapter.itemCount == 0){
                                binding.recyclerOfferRestaurants.hide()
                                binding.shimmerBarOffer.hide()
                                binding.constraintLayout7.hide()

                            }else{
                                binding.recyclerOfferRestaurants.show()
                                binding.shimmerBarOffer.hide()
                                binding.constraintLayout7.show()

                            }


                        }
                    }
                }
            }
        }
//        lifecycleScope.launch {
//            mViewModel.getOfferRestaurant(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
//                mMap =map ).collect{
//                when (it) {
//                    is ApiResponse.Loading -> {
//
//
//
//                    }
//                    is ApiResponse.Success -> {
//                   //     progressBarHelper.dismissProgressBar()
//                        val data = it.data
//                        if (data != null){
//                            if (data.isSuccess!!) {
//                                if (data.data.isNotEmpty()){
//                                    binding.constraintLayout7.show()
//                                    Constants.offerRestaurant.clear()
//                                    Constants.offerRestaurant.addAll(data.data)
//                                    offerRestaurantAdapter = OfferRestaurantAdapter(requireContext(), data.data, ::goMenuScreen)
//                                            binding.recyclerOfferRestaurants.adapter = offerRestaurantAdapter
//                                        }else{
//                                    binding.constraintLayout7.hide()
//                                }
//                            }
//
//
//                        }
//                    }
//                    is ApiResponse.Failure -> {
//                   //     progressBarHelper.dismissProgressBar()
//                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
//                    }
//
//                }
//
//            }
//
//        }

    }








}