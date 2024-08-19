package com.svaggy.ui.activities.home_location

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityOnMapLocationBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.address.EditAddressActivity
import com.svaggy.ui.activities.guestuser.GuestHomeActivity
import com.svaggy.ui.fragments.location.adapter.LocationSearchAdapter
import com.svaggy.utils.Constants
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Locale
import java.util.concurrent.TimeoutException

class OnMapLocationActivity :  BaseActivity<ActivityOnMapLocationBinding>(ActivityOnMapLocationBinding::inflate) ,LocationSearchAdapter.ClickListener, OnMapReadyCallback {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var markerLatitude: Double = 0.0
    private var markerLongitude: Double = 0.0
    private var locationCity  = ""
    private  var locationRegion: String = ""
    private  var locationPostal: String = ""
    private lateinit var mMap: GoogleMap
    private lateinit var marker: LatLng
    private lateinit var mapFragment: SupportMapFragment
    private var mAutoCompleteAdapter: LocationSearchAdapter? = null
    private var isFrom:String =""
    private  var googlePin:String = ""
    private  var isForSelf:String = ""
    private  var phoneNum:String =""
    private  var recipientName:String = ""
    private  var addressType:String = ""
    private  var companyName:String = ""
    private  var streetName:String = ""
    private  var floor:String = ""
    private  var addressId:String = ""
    private  var selectLat:String = ""
    private  var selectLong:String = ""

    @SuppressLint("SuspiciousIndentation")
    override fun ActivityOnMapLocationBinding.initialize(){
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@OnMapLocationActivity)




        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this@OnMapLocationActivity)
        isFrom = intent.getStringExtra("isFrom").toString()
        googlePin = intent.getStringExtra("googlePin").toString()
        isForSelf = intent.getStringExtra("isForSelf").toString()
        phoneNum = intent.getStringExtra("phoneNum").toString()
        recipientName = intent.getStringExtra("recipientName").toString()
        addressType = intent.getStringExtra("addressType").toString()
        companyName = intent.getStringExtra("companyName").toString()
        streetName = intent.getStringExtra("streetName").toString()
        floor = intent.getStringExtra("floor").toString()
        addressId = intent.getStringExtra("addressId").toString()
        binding.locationText.text = googlePin
        binding.saveLocation.setOnClickListener {

            if (binding.saveLocation.alpha == 1f)
            when(isFrom){
                "EditAddress" ->{
                    val intent =    Intent(this@OnMapLocationActivity,EditAddressActivity::class.java)
                    intent.putExtra("isFrom","EditAddress")
                    intent.putExtra("googlePin",locationRegion)
                    intent.putExtra("isForSelf",isForSelf)
                    intent.putExtra("phoneNum",phoneNum)
                    intent.putExtra("recipientName",recipientName)
                    intent.putExtra("addressType",addressType)
                    intent.putExtra("floor",floor)
                    intent.putExtra("companyName",companyName)
                    intent.putExtra("streetName",streetName)
                    intent.putExtra("region",locationRegion)
                    intent.putExtra("lat",marker.latitude)
                    intent.putExtra("long",marker.longitude)
                    intent.putExtra("addressId",addressId)
                    startActivity(intent)

                }
                "AddAddress","CheckOut" ->{
                    val intent =    Intent(this@OnMapLocationActivity,EditAddressActivity::class.java)
                    intent.putExtra("isFrom",isFrom)
                    intent.putExtra("residence",intent.getStringExtra("residence"))
                    intent.putExtra("city",locationCity)
                    intent.putExtra("region",locationRegion)
                    intent.putExtra("postal",locationPostal)
                    intent.putExtra("lat",marker.latitude)
                    intent.putExtra("long",marker.longitude)
                    intent.putExtra("addressType",intent.getStringExtra("addressType"))
                    startActivity(intent)

                }
                else ->{
                    PrefUtils.instance.setString(Constants.Latitude,marker.latitude.toString())
                    PrefUtils.instance.setString(Constants.Longitude,marker.longitude.toString())
                    PrefUtils.instance.setString(Constants.Address,binding.locationText.text.toString())
                    if (PrefUtils.instance.getString(Constants.IsGuestUser) == "true"){
                        startActivity(Intent(this@OnMapLocationActivity,GuestHomeActivity::class.java))
                    }
                    else{
                        startActivity(Intent(this@OnMapLocationActivity,MainActivity::class.java))
                    }
                    finish()
                }

            }

//
        }
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.edtSearch.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString() != "") {
                    binding.icLoc.hide()
                    binding.centerMarker.hide()
                    mAutoCompleteAdapter?.filter?.filter(p0.toString())
                    binding.recyclerPlaceSearch.visibility = View.VISIBLE
                } else {
                    binding.centerMarker.show()
                    binding.icLoc.show()
                    binding.recyclerPlaceSearch.visibility = View.GONE
                }
            }
        })
        mAutoCompleteAdapter = LocationSearchAdapter(this@OnMapLocationActivity)
        binding.recyclerPlaceSearch.layoutManager = LinearLayoutManager(this@OnMapLocationActivity)
        mAutoCompleteAdapter?.setClickListener(this@OnMapLocationActivity)
        binding.recyclerPlaceSearch.adapter = mAutoCompleteAdapter
        mAutoCompleteAdapter?.notifyDataSetChanged()
        //
        if (intent?.getStringExtra("isFrom") == "EditAddress") {
            markerLatitude = intent?.getStringExtra("latitude")!!.toDouble()
            markerLongitude = intent?.getStringExtra("longitude")!!.toDouble()
        }else if (intent?.getStringExtra("isFrom") == "HomeLocation") {
            markerLatitude = intent?.getStringExtra("latitude")!!.toDouble()
            markerLongitude = intent?.getStringExtra("longitude")!!.toDouble()
        } else {

            val locationManager = this@OnMapLocationActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                //PrefUtils.instance.setString(Constants.FragmentBackName, "CurrentLocationFragment")
                getCurrentLocation()
            }
        }
        binding.icLoc.setOnClickListener {
            getCurrentLocation()
        }

    }

    private fun setGoogleMapsApiKey() {
        try {

            val googleMapsApiKeyField = MapsInitializer::class.java.getDeclaredField("apiKey")
            googleMapsApiKeyField.isAccessible = true
            googleMapsApiKeyField.set(null, PrefUtils.instance.getString(Constants.GOOGLE_MAP_KEY)!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        // Initialize Location manager
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check condition
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mFusedLocationClient.lastLocation.addOnCompleteListener {
                // Check condition
                if (it.result != null) {
                    // Initialize location
                    markerLatitude = it.result.latitude
                    markerLongitude = it.result.longitude
                    locationShowMap()
                    val geocoder =  Geocoder(this, Locale.getDefault())

                    try {
                        val list: MutableList<Address>? = geocoder.getFromLocation(markerLatitude ?: 0.0, markerLongitude ?: 0.0, 1)
                        locationCity = "${list?.get(0)?.locality}"
                        locationRegion = "${list?.get(0)?.getAddressLine(0)}"
                        locationPostal = "${list?.get(0)?.postalCode}"
                        if (intent?.getStringExtra("isFrom") == "EditAddress") {
                            binding.locationText.text = intent?.getStringExtra("address")
                        }
                        else {
                            binding.apply {
                                Log.d("preet250","${list?.get(0)?.getAddressLine(0)}")
                                locationText.text = "${list?.get(0)?.getAddressLine(0)}"
                            }
                        }

                    }catch (e:Exception){
                        e.printStackTrace()

                    }catch (e: TimeoutException) {
                        e.printStackTrace()

                        }


                } else {
                    val locationRequest: LocationRequest = LocationRequest()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(10000)
                        .setFastestInterval(1000)
                        .setNumUpdates(1)

                    // Initialize location call back
                    val locationCallback: LocationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            markerLatitude = locationResult.lastLocation?.latitude ?: 0.0
                            markerLongitude = locationResult.lastLocation?.longitude ?: 0.0
                            // Set latitude
                            locationShowMap()
                            val geocoder =  Geocoder(this@OnMapLocationActivity, Locale.getDefault())
                            val list: MutableList<Address>? = geocoder.getFromLocation(markerLatitude ?: 0.0, markerLongitude ?: 0.0, 1)
                            list?.get(0)?.getAddressLine(0)?.let { _ ->  }
                            locationCity = "${list?.get(0)?.locality}"
                            locationRegion = "${list?.get(0)?.getAddressLine(0)}"
                            locationPostal = "${list?.get(0)?.postalCode}"
                            if (intent?.getStringExtra("isFrom") == "EditAddress") {
                                binding.locationText.text = intent?.getStringExtra("address")
                            }
                            else {
                                binding.apply {
                                    locationText.text = "${list?.get(0)?.getAddressLine(0)}"
                                }
                            }
                        }
                    }

                    // Request location updates
                    mFusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.myLooper()
                    )
                }
            }
        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun locationShowMap() {
        val callback = OnMapReadyCallback { googleMap ->
            val latlang = LatLng(markerLatitude ?: 0.0, markerLongitude ?: 0.0)
           // googleMap.addMarker(MarkerOptions().position(latlang).title("My Current Location"))
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latlang))
        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }


    override fun click(place: Place?) {
        markerLatitude = place?.latLng?.latitude ?: 0.0
        markerLongitude = place?.latLng?.longitude ?: 0.0
        onMapReady(mMap)
        binding.edtSearch.setText("")
        binding.recyclerPlaceSearch.visibility = View.GONE
        hideSoftKeyboard(this)

    }

    private fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        if (inputMethodManager.isAcceptingText) {
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus?.windowToken, 0)
        }
    }

//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//
//        // Set an initial marker at a specific location (e.g., center of the map)
//        marker = LatLng(markerLatitude!!, markerLongitude!!)
//        mMap.addMarker(
//            MarkerOptions().icon(
//                bitmapDescriptorFromVector(
//                    this,
//                    R.drawable.ic_current_location_bg
//                )
//            ).position(marker).title("Marker")
//        )
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 15F))
//        mMap.projection.toScreenLocation(marker)
//        // Set a listener to get the location when the map is clicked
//        mMap.setOnMapClickListener { latLng ->
//            marker = latLng
//            mMap.clear()
//            mMap.addMarker(
//                MarkerOptions().icon(
//                    bitmapDescriptorFromVector(
//                       this,
//                        R.drawable.ic_current_location_bg
//                    )
//                ).position(marker).title("Marker"))
//            CoroutineScope(Dispatchers.IO).launch {
//                val geocoder = Geocoder(this@OnMapLocationActivity, Locale.getDefault())
//                val list: MutableList<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
//
//                withContext(Dispatchers.Main) {
//                    handleGeocodingResult(list)
//                }
//            }
//
//        }
//        mMap.setOnCameraIdleListener {
//            val latLng: LatLng = mMap.cameraPosition.target
//            marker = latLng
//            mMap.clear()
//            mMap.addMarker(
//                MarkerOptions().icon(
//                    bitmapDescriptorFromVector(
//                        this,
//                        R.drawable.ic_current_location_bg
//                    )
//                ).position(marker).title("Marker")
//            )
//            // Perform geocoding asynchronously using coroutines
//            CoroutineScope(Dispatchers.IO).launch {
//                val geocoder = Geocoder(this@OnMapLocationActivity, Locale.getDefault())
//                val list: MutableList<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
//
//                withContext(Dispatchers.Main) {
//                    handleGeocodingResult(list)
//                }
//            }
////            val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
////            val list: MutableList<Address>? = geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 1)
////            if (!list.isNullOrEmpty()){
////                locationCity = list[0].locality
////                locationRegion = list[0].getAddressLine(0)
////                locationPostal = list[0].postalCode ?: "N/A"
////
////            }
//
////            if (arguments?.getString("isFrom") == "EditAddress") {
////                binding.locationText.text = arguments?.getString("address")
////            }
////            else {
////                binding.apply {
////                    if (!list.isNullOrEmpty())
////                    locationText.text = list[0].getAddressLine(0)
////                }
////            }
//        }
//    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Move the camera to the initial position
        marker = LatLng(markerLatitude ?: 0.0, markerLongitude ?: 0.0)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 15F))

        // Set a listener for when the camera stops moving
        mMap.setOnCameraIdleListener {
            val centerLatLng = mMap.cameraPosition.target
           // val roundedLat = String.format("%.7f", centerLatLng.latitude).toDouble()
           // val roundedLng = String.format("%.7f", centerLatLng.longitude).toDouble()
            marker = LatLng(centerLatLng.latitude,centerLatLng.longitude)
            // Perform geocoding asynchronously using coroutines
//            CoroutineScope(Dispatchers.IO).launch {
//                val geocoder = Geocoder(this@OnMapLocationActivity, Locale.getDefault())
//                val addressList: MutableList<Address>? = geocoder.getFromLocation(centerLatLng.latitude, centerLatLng.longitude, 1)
//
//                withContext(Dispatchers.Main) {
//                    handleGeocodingResult(addressList)
//                }
//            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Set a timeout for the geocoding request
                    val addressList: MutableList<Address>? = withTimeout(5000) { // 5000 milliseconds = 5 seconds
                        val geocoder = Geocoder(this@OnMapLocationActivity, Locale.getDefault())
                        geocoder.getFromLocation(centerLatLng.latitude, centerLatLng.longitude, 1)
                    }

                    withContext(Dispatchers.Main) {
                        handleGeocodingResult(addressList)
                    }
                } catch (e: Exception) {

                    withContext(Dispatchers.Main) {
                        e.printStackTrace()
                        // Optionally, show a toast or handle the error appropriately

                    }
                }
            }
        }


        mMap.setOnMapClickListener { latLng ->
            marker = latLng

        }

        // Optional: Set a listener for map clicks if needed
//        mMap.setOnMapClickListener { latLng ->
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
//        }
    }

    private fun handleGeocodingResult(list: List<Address>?) {
        if (!list.isNullOrEmpty()) {
            locationCity = list[0].locality ?: "N/A"
            locationRegion = list[0].getAddressLine(0) ?: "N/A"
            locationPostal = list[0].postalCode ?: "N/A"
            selectLat = list[0].latitude.toString()
            selectLong = list[0].longitude.toString()

        }

        binding.locationText.text = list?.getOrNull(0)?.getAddressLine(0) ?: "N/A"
        if (binding.locationText.text == "N/A"){
            binding.saveLocation.alpha = 0.5f
        }else{
            binding.saveLocation.alpha = 1f

        }

    }


    private fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorResId: Int
    ): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

}