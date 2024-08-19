package com.svaggy.ui.fragments.location.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.svaggy.R
import com.svaggy.databinding.FragmentCurrentLocationBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.location.adapter.LocationSearchAdapter
import com.svaggy.utils.Constants
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.onBackPressedDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class CurrentLocationFragment : Fragment(), LocationSearchAdapter.ClickListener, OnMapReadyCallback {
    lateinit var binding: FragmentCurrentLocationBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var markerLatitude: Double? = 0.0
    private var markerLongitude: Double? = 0.0
    private lateinit var locationCity: String
    private lateinit var locationRegion: String
    private lateinit var locationPostal: String
    private lateinit var mMap: GoogleMap
    private lateinit var marker: LatLng
    private lateinit var mapFragment: SupportMapFragment
    private var mAutoCompleteAdapter: LocationSearchAdapter? = null
    private lateinit var isFrom:String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrentLocationBinding.inflate(inflater, container, false)
     //   PrefUtils.instance.setString(Constants.CurrentDestinationId,(activity as MainActivity).navController.currentDestination?.id.toString())
        isFrom = arguments?.getString("isFrom").toString()
        if (isFrom.isNullOrEmpty() || isFrom == "null"){
            isFrom = PrefUtils.instance.getString("isFrom").toString()

        }
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.GONE
        (activity as MainActivity).binding.toolbar.titleTv.text = ""
  //      (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility=View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        onBackPressedDispatcher {
            if (isFrom == "AddAddress")
                findNavController().popBackStack(R.id.addressScreen,false)
            else
                findNavController().popBackStack()

        }



        binding.imgBack.setOnClickListener {
            if (isFrom == "AddAddress")
                findNavController().popBackStack(R.id.addressScreen,false)
            else
                findNavController().popBackStack()

        }

        binding.saveLocation.setOnClickListener {
            val bundle = Bundle()
            if (isFrom == "EditAddress") {
                bundle.putInt("addressId", requireArguments().getInt("addressId"))
                bundle.putString("isFrom", arguments?.getString("isFrom"))
                bundle.putString("residence", arguments?.getString("residence"))
                bundle.putString("city", locationCity)
                bundle.putString("region", arguments?.getString("region"))
                bundle.putString("postal", locationPostal)
                bundle.putDouble("latitude", marker.latitude)
                bundle.putDouble("longitude", marker.longitude)
                bundle.putString("addressType", arguments?.getString("addressType"))
                findNavController().popBackStack(R.id.currentLocationFragment, false)
                findNavController().navigate(R.id.action_currentLocationFragment_to_editAddressScreen, bundle)
            }
            else if (isFrom == "AddAddress") {
              //  bundle.putInt("addressId", requireArguments().getInt("addressId"))
                bundle.putString("isFrom", arguments?.getString("isFrom"))
                bundle.putString("city", locationCity)
                bundle.putString("region", locationRegion)
                bundle.putString("postal", locationPostal)
                bundle.putDouble("latitude", marker.latitude)
                bundle.putDouble("longitude", marker.longitude)
                bundle.putString("isFrom","AddAddress")
                findNavController().popBackStack(R.id.currentLocationFragment, false)
                findNavController().navigate(R.id.action_currentLocationFragment_to_editAddressScreen, bundle)
            }

            else if (isFrom == "CheckOut") {
                bundle.putInt("addressId", requireArguments().getInt("addressId"))
                bundle.putString("isFrom", arguments?.getString("isFrom"))
                bundle.putString("city", locationCity)
                bundle.putString("region", locationRegion)
                bundle.putString("postal", locationPostal)
                bundle.putDouble("latitude", marker.latitude)
                bundle.putDouble("longitude", marker.longitude)
                findNavController().popBackStack(R.id.currentLocationFragment, false)
                findNavController().navigate(R.id.action_currentLocationFragment_to_editAddressScreen, bundle)
            }
            else {
                PrefUtils.instance.setString(Constants.Latitude,marker.latitude.toString())
                PrefUtils.instance.setString(Constants.Longitude,marker.longitude.toString())
                bundle.putString("current_address", arguments?.getString("address"))
                PrefUtils.instance.setString(Constants.Address,binding.locationText.text.toString())
              //  findNavController().navigate(R.id.action_currentLocationFragment_to_homeFragment, bundle)
            }
//            findNavController().previousBackStackEntry?.savedStateHandle?.set("current_address", arguments?.getString("address"))
//            findNavController().popBackStack(R.id.homeFragment, true)
        }

        binding.edtSearch.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString() != "") {
                    mAutoCompleteAdapter?.filter?.filter(p0.toString())
                    binding.recyclerPlaceSearch.visibility = View.VISIBLE
                } else {
                    binding.recyclerPlaceSearch.visibility = View.GONE
                }
            }
        })

        mAutoCompleteAdapter = LocationSearchAdapter(requireContext())
        binding.recyclerPlaceSearch.layoutManager = LinearLayoutManager(requireContext())
        mAutoCompleteAdapter!!.setClickListener(this)
        binding.recyclerPlaceSearch.adapter = mAutoCompleteAdapter
        mAutoCompleteAdapter!!.notifyDataSetChanged()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        // Initialize Location manager
        val locationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check condition
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            mFusedLocationClient.lastLocation.addOnCompleteListener {
                // Check condition
                if (it.result != null) {
                    // Initialize location
                    markerLatitude = it.result.latitude
                    markerLongitude = it.result.longitude
                    locationShowMap()
                    val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
                    val list: MutableList<Address>? = geocoder?.getFromLocation(markerLatitude!!, markerLongitude!!, 1)

                    locationCity = "${list?.get(0)?.locality}"
                    locationRegion = "${list?.get(0)?.getAddressLine(0)}"
                    locationPostal = "${list?.get(0)?.postalCode}"
                    if (arguments?.getString("isFrom") == "EditAddress")
                    {
                        binding.locationText.text = arguments?.getString("address")
                    }
                    else {
                        binding.apply {
                            locationText.text = "${list?.get(0)?.getAddressLine(0)}"
                        }
                    }

                } else {
                    val locationRequest: LocationRequest = LocationRequest()
                        .setPriority(
                            LocationRequest.PRIORITY_HIGH_ACCURACY
                        )
                        .setInterval(10000)
                        .setFastestInterval(
                            1000
                        )
                        .setNumUpdates(1)

                    // Initialize location call back
                    val locationCallback: LocationCallback = object : LocationCallback() {
                        override fun onLocationResult(
                            locationResult: LocationResult) {
                            markerLatitude = locationResult.lastLocation?.latitude
                            markerLongitude = locationResult.lastLocation?.longitude
                            // Set latitude
                            locationShowMap()
                            val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
                            val list: MutableList<Address>? = geocoder?.getFromLocation(markerLatitude!!, markerLongitude!!, 1)
                            list?.get(0)?.getAddressLine(0)?.let { it1 ->  }
                            locationCity = "${list?.get(0)?.locality}"
                            locationRegion = "${list?.get(0)?.getAddressLine(0)}"
                            locationPostal = "${list?.get(0)?.postalCode}"
                            if (arguments?.getString("isFrom") == "EditAddress")
                            {
                                binding.locationText.text = arguments?.getString("address")
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
            val latlang = LatLng(markerLatitude!!, markerLongitude!!)
            googleMap.addMarker(MarkerOptions().position(latlang).title("My Current Location"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlang))
        }
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

//    fun onMapReady1(p0: GoogleMap) {
//        val callback = OnMapReadyCallback { googleMap ->
//            val latLng = LatLng(location1?.latitude ?: 0.0, location1?.longitude ?: 0.0)
//            //val latLng = LatLng(30.6571891, 76.6814621)
//            val markerOptions = MarkerOptions().position(latLng).title("I am here!")
//            p0.animateCamera(CameraUpdateFactory.newLatLng(latLng))
//            p0.moveCamera(CameraUpdateFactory.newLatLng(latLng))
//            p0.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f))
//            p0.addMarker(markerOptions)
//        }
//        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
//        mapFragment?.getMapAsync(callback)
//    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set an initial marker at a specific location (e.g., center of the map)
        marker = LatLng(markerLatitude!!, markerLongitude!!)
        mMap.addMarker(
            MarkerOptions().icon(
                bitmapDescriptorFromVector(
                    requireContext(),
                    R.drawable.ic_current_location_bg
                )
            ).position(marker).title("Marker")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 15F))
        mMap.projection.toScreenLocation(marker)
        // Set a listener to get the location when the map is clicked
        mMap.setOnMapClickListener { latLng ->
            marker = latLng
            mMap.clear()
            mMap.addMarker(
                MarkerOptions().icon(
                    bitmapDescriptorFromVector(
                        requireContext(),
                        R.drawable.ic_current_location_bg
                    )
                ).position(marker).title("Marker"))
            CoroutineScope(Dispatchers.IO).launch {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val list: MutableList<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                withContext(Dispatchers.Main) {
                    handleGeocodingResult(list)
                }
            }

        }
        mMap.setOnCameraIdleListener {
            val latLng: LatLng = mMap.cameraPosition.target
            marker = latLng
            mMap.clear()
            mMap.addMarker(
                MarkerOptions().icon(
                    bitmapDescriptorFromVector(
                        requireContext(),
                        R.drawable.ic_current_location_bg
                    )
                ).position(marker).title("Marker")
            )
            // Perform geocoding asynchronously using coroutines
            CoroutineScope(Dispatchers.IO).launch {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val list: MutableList<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                withContext(Dispatchers.Main) {
                    handleGeocodingResult(list)
                }
            }
//            val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
//            val list: MutableList<Address>? = geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 1)
//            if (!list.isNullOrEmpty()){
//                locationCity = list[0].locality
//                locationRegion = list[0].getAddressLine(0)
//                locationPostal = list[0].postalCode ?: "N/A"
//
//            }

//            if (arguments?.getString("isFrom") == "EditAddress") {
//                binding.locationText.text = arguments?.getString("address")
//            }
//            else {
//                binding.apply {
//                    if (!list.isNullOrEmpty())
//                    locationText.text = list[0].getAddressLine(0)
//                }
//            }
        }
    }

    private fun handleGeocodingResult(list: List<Address>?) {
        if (!list.isNullOrEmpty()) {
            locationCity = list[0].locality ?: "N/A"
            locationRegion = list[0].getAddressLine(0) ?: "N/A"
            locationPostal = list[0].postalCode ?: "N/A"
        }

        if (arguments?.getString("isFrom") == "EditAddress") {
            binding.locationText.text = arguments?.getString("address")
        } else {
            binding.locationText.text = list?.getOrNull(0)?.getAddressLine(0) ?: "N/A"
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

    override fun onResume() {
        super.onResume()
        if (arguments?.getString("isFrom") == "EditAddress") {
            markerLatitude = arguments?.getString("latitude")!!.toDouble()
            markerLongitude = arguments?.getString("longitude")!!.toDouble()
        }else if (arguments?.getString("isFrom") == "HomeLocation") {
            markerLatitude = arguments?.getString("latitude")!!.toDouble()
            markerLongitude = arguments?.getString("longitude")!!.toDouble()
        } else {

            val locationManager =
                activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            ) {
                //PrefUtils.instance.setString(Constants.FragmentBackName, "CurrentLocationFragment")
                getCurrentLocation()
            }
        }
    }

    override fun click(place: Place?) {
        markerLatitude = place?.latLng?.latitude
        markerLongitude = place?.latLng?.longitude
        onMapReady(mMap)
        binding.edtSearch.setText("")
        binding.recyclerPlaceSearch.visibility = View.GONE
        hideSoftKeyboard(requireActivity())
    }

    private fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        if (inputMethodManager.isAcceptingText) {
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken,
                0
            )
        }
    }
}