package com.svaggy.ui.fragments.location.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.app.SvaggyApplication
import com.svaggy.databinding.FragmentHomeLocationBinding
import com.svaggy.ui.fragments.cart.viewmodel.CartViewModel
import com.svaggy.ui.fragments.location.adapter.AddressAdapter
import com.svaggy.ui.fragments.location.adapter.LocationSearchAdapter
import com.svaggy.ui.fragments.location.model.GetAddress
import com.svaggy.ui.fragments.location.viewmodel.AddressViewModel
import com.svaggy.utils.Constants
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale


@AndroidEntryPoint
class HomeLocationScreen : Fragment(),LocationSearchAdapter.ClickListener {
    lateinit var binding: FragmentHomeLocationBinding
    var location1: Location?=null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mAutoCompleteAdapter: LocationSearchAdapter? = null

    private var deletePosition:Int?=0
    private var addressList: ArrayList<GetAddress.Data>? = null
    private var addressAdapter: AddressAdapter? = null

    private val mViewModel by viewModels<AddressViewModel>()
    private val mViewModelCart by viewModels<CartViewModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeLocationBinding.inflate(inflater, container, false)
        PrefUtils.instance.setString(Constants.FragmentBackName,"HomeLocationScreen")
        initObserver()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    //findNavController().popBackStack(R.id.addressScreen,true)
                    findNavController().navigate(R.id.homeFragment)
                }
            }
            )

        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.consDetectLocation.setOnClickListener {
            PrefUtils.instance.setString(Constants.FragmentBackName,"LocationFragment")
            findNavController().previousBackStackEntry?.savedStateHandle?.set("current_address", binding.txtCurrentAddress.text.toString())
            findNavController().popBackStack()
        }

        binding.edtSearchLocation.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtSearchLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString() != "") {
                    mAutoCompleteAdapter?.getFilter()?.filter(p0.toString())
                    binding.recyclerPlaceSearch.setVisibility(View.VISIBLE)
                    binding.consDetectLocation.visibility = View.GONE
                    binding.recyclerAddress.visibility = View.GONE
                } else {
                    binding.recyclerPlaceSearch.setVisibility(View.GONE)
                    binding.consDetectLocation.visibility = View.VISIBLE
                    binding.recyclerAddress.visibility = View.VISIBLE
                }
            }
        })

        mAutoCompleteAdapter = LocationSearchAdapter(requireContext())
        binding.recyclerPlaceSearch.layoutManager = LinearLayoutManager(requireContext())
        mAutoCompleteAdapter!!.setClickListener(this)
        binding.recyclerPlaceSearch.adapter = mAutoCompleteAdapter
        mAutoCompleteAdapter!!.notifyDataSetChanged()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )
        if (requestCode == 100 && grantResults.isNotEmpty()
            && (grantResults[0] + grantResults[1]
                    == PackageManager.PERMISSION_GRANTED)
        ) {
            locationServiceCheck()
        } else {
            Toast
                .makeText(
                    activity,
                    "Permission denied",
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }
    private fun locationServiceCheck() {
        val locationManager = activity
            ?.getSystemService(
                Context.LOCATION_SERVICE
            ) as LocationManager
        if (locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )
            || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        ) {
            findNavController().navigate(R.id.action_homeLocationScreen_to_currentLocationFragment)
        } else {
            startActivity(
                Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
                )
                    .setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        getCurrentLocation()
        mViewModel.getAddress("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}")
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        // Initialize Location manager
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check condition
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            mFusedLocationClient.lastLocation.addOnCompleteListener {
                // Initialize location
                location1 = it.result
                // Check condition
                if (location1 != null) {
                    val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
                    val list: MutableList<Address>? = geocoder?.getFromLocation(location1?.latitude?:0.0, location1?.longitude?:0.0, 1)
                    binding.apply {
                        binding.txtCurrentAddress.text = "${list?.get(0)?.getAddressLine(0)}"
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
                            locationResult: LocationResult
                        ) {
                            // Initialize
                            // location
                            location1 = locationResult
                                .lastLocation
                            // Set latitude
//                            locationShowMap()
                            val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
                            val list: MutableList<Address>? =
                                geocoder?.getFromLocation(
                                    location1?.latitude ?: 0.0,
                                    location1?.longitude ?: 0.0,
                                    1
                                )
                            binding.apply {
                                binding.txtCurrentAddress.text = "${list?.get(0)?.getAddressLine(0)}"
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
            // When location service is not enabled
            // open location setting
            startActivity(
                Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
                )
                    .setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
            )
        }
    }

    private fun initObserver() {

        mViewModel.getAddressDataLive.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
               binding.progressBarMenu.hide()
                if (it.data.size > 0)
                {
                    binding.recyclerAddress.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    binding.recyclerAddress.isNestedScrollingEnabled = true
                    addressList = it.data
//                    addressAdapter = AddressAdapter(requireContext(),"HomeLocation", addressList!!, ::getSelectAddressData,
//                        clickEditAdd = {googlePin: String, isForSelf: String,
//                                        addressType: String, phoneNum: String,
//                                        recipientName: String, floor: String,
//                                        companyName: String, streetName: String ,lat:String,long:String,_->
//
//                        } )
                    binding.recyclerAddress.adapter = addressAdapter
                }
            } else {
//                (activity as MainActivity).hideLoader()
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
        mViewModel.deleteAddressDataLive.observe(viewLifecycleOwner) {
            if (it.isSuccess!!)
            {
              binding.progressBarMenu.hide()
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
//                addressAdapter?.deleteItem(addressList!!, deletePosition!!)
                addressList?.removeAt(deletePosition!!)
                addressAdapter?.notifyItemRangeRemoved(deletePosition!!, addressList?.size!!-1)

            } else {
//                (activity as MainActivity).hideLoader()
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
        mViewModelCart.setCurrentAddressDataLive.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
               binding.progressBarMenu.hide()
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
//                findNavController().previousBackStackEntry?.savedStateHandle?.set("current_address", address)
                findNavController().navigateUp()
            } else {
              binding.progressBarMenu.hide()
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSelectAddressData(position:Int, addressId:Int, address:String, isClicked:String){
        deletePosition = position
        if (isClicked == "ForDelete")
        {
            if (addressId != 0)
            {
                PrefUtils.instance.showCustomDialog(
                    requireContext(),
                    "Are You Sure You Want Delete This Address?",
                    "No",
                    "Yes",
                    "",
                    false,
                    true,
                    negativeAlert = {
                        it.dismiss()
                    },
                    positiveAlert = {
                        mViewModel.deleteAddress("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",addressId)
                        it.dismiss()
                    }
                )
            }
        }
        else
        {
            mViewModelCart.setCurrentAddress(
                "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}", addressId)
            PrefUtils.instance.setString(Constants.Address,address)
//            findNavController().previousBackStackEntry?.savedStateHandle?.set("current_address", address)
//            findNavController().navigateUp()
        }
    }

    override fun click(place: Place?) {
        val bundle = Bundle()
        bundle.putString("address", place?.address)
        bundle.putString("isFrom", "HomeLocation")
        bundle.putString("latitude", place?.latLng?.latitude.toString())
        bundle.putString("longitude", place?.latLng?.longitude.toString())
        findNavController().navigate(R.id.action_homeLocationScreen_to_currentLocationFragment,bundle)

    }

    private fun editAddressItem(position:Int, isClicked:Boolean, addressId:Int, residence: String, region: String, postal: String, latitude: String, longitude: String, addressType: String)
    {
//        val bundle = Bundle()
//        bundle.putInt("position", position)
//        bundle.putBoolean("isClicked", isClicked)
//        bundle.putInt("addressId", addressId)
//        bundle.putString("residence", residence)
//        bundle.putString("region", region)
//        bundle.putString("postal", postal)
//        findNavController().navigate(R.id.action_addressScreen_to_editAddressScreen,bundle)
    }
}