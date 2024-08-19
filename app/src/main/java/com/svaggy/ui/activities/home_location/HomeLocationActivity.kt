package com.svaggy.ui.activities.home_location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.model.Place
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityHomeLocationBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.guestuser.GuestHomeActivity
import com.svaggy.ui.fragments.cart.viewmodel.CartViewModel
import com.svaggy.ui.fragments.location.adapter.AddressAdapter
import com.svaggy.ui.fragments.location.adapter.LocationSearchAdapter
import com.svaggy.ui.fragments.location.model.GetAddress
import com.svaggy.ui.fragments.location.viewmodel.AddressViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.BEARER
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class HomeLocationActivity : BaseActivity<ActivityHomeLocationBinding>(ActivityHomeLocationBinding::inflate),LocationSearchAdapter.ClickListener {

    var meLocation: Location?=null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mAutoCompleteAdapter: LocationSearchAdapter? = null

    private var deletePosition:Int?=0
    private var addressList: ArrayList<GetAddress.Data>? = null
    private var addressAdapter: AddressAdapter? = null

    private val mViewModel by viewModels<AddressViewModel>()
    private val mViewModelCart by viewModels<CartViewModel>()


    @SuppressLint("NotifyDataSetChanged")
    override fun ActivityHomeLocationBinding.initialize(){
        binding.consDetectLocation.setOnClickListener {
            if (meLocation != null){
                if (PrefUtils.instance.getString(Constants.IsGuestUser).equals("true")){
                    PrefUtils.instance.setString(Constants.Latitude,meLocation?.latitude.toString())
                    PrefUtils.instance.setString(Constants.Longitude,meLocation?.longitude.toString())
                    PrefUtils.instance.setString(Constants.Address,txtCurrentAddress.text.toString())
                    startActivity(Intent(this@HomeLocationActivity,GuestHomeActivity::class.java))
                    finish()

                }else{
                    PrefUtils.instance.setString(Constants.Latitude,meLocation?.latitude.toString())
                    PrefUtils.instance.setString(Constants.Longitude,meLocation?.longitude.toString())
                    PrefUtils.instance.setString(Constants.Address,txtCurrentAddress.text.toString())
                    startActivity(Intent(this@HomeLocationActivity,MainActivity::class.java))
                    finish()

                }


            }


        }
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        initObserver()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this@HomeLocationActivity)
        getCurrentLocation()
        mViewModel.getAddress("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}")


        binding.edtSearchLocation.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtSearchLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString() != "") {
                    mAutoCompleteAdapter?.filter?.filter(p0.toString())
                    binding.recyclerPlaceSearch.visibility = View.VISIBLE
                    binding.consDetectLocation.visibility = View.GONE
                    binding.recyclerAddress.visibility = View.GONE
                } else {
                    binding.recyclerPlaceSearch.visibility = View.GONE
                    binding.consDetectLocation.visibility = View.VISIBLE
                    binding.recyclerAddress.visibility = View.VISIBLE
                }
            }
        })
        mAutoCompleteAdapter = LocationSearchAdapter(this@HomeLocationActivity)
        binding.recyclerPlaceSearch.layoutManager = LinearLayoutManager(this@HomeLocationActivity)
        mAutoCompleteAdapter?.setClickListener(this@HomeLocationActivity)
        binding.recyclerPlaceSearch.adapter = mAutoCompleteAdapter
        mAutoCompleteAdapter?.notifyDataSetChanged()

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
                   this,
                    "Permission denied",
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }
    private fun locationServiceCheck() {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
          //  findNavController().navigate(R.id.action_homeLocationScreen_to_currentLocationFragment)
        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        // Initialize Location manager
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check condition
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            mFusedLocationClient.lastLocation.addOnCompleteListener {
                // Initialize location
                meLocation = it.result
                // Check condition
                if (meLocation != null) {
                    val geocoder = this.let { Geocoder(it, Locale.getDefault()) }
                    val list: MutableList<Address>? = geocoder.getFromLocation(meLocation?.latitude?:0.0, meLocation?.longitude?:0.0, 1)
                    binding.apply {
                        binding.txtCurrentAddress.text = "${list?.get(0)?.getAddressLine(0)}"
                    }

                } else {
                    val   locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                        .setWaitForAccurateLocation(false)
                        .setMinUpdateIntervalMillis(1000)
                        .setMaxUpdateDelayMillis(1000)
                        .build()

//

                    // Initialize location call back
                    val locationCallback: LocationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            // Initialize
                            // location
                            meLocation = locationResult
                                .lastLocation
                            // Set latitude
//                            locationShowMap()
                            val geocoder =  Geocoder(this@HomeLocationActivity, Locale.getDefault())
                            val list: MutableList<Address>? = geocoder.getFromLocation(meLocation?.latitude ?: 0.0, meLocation?.longitude ?: 0.0, 1)
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
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private fun initObserver() {

        mViewModel.getAddressDataLive.observe(this) {
            if (it.isSuccess == true) {
                binding.progressBarMenu.hide()
                if (it.data.size > 0)
                {
                    binding.recyclerAddress.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                    binding.recyclerAddress.isNestedScrollingEnabled = true
                    addressList = it.data
                    addressAdapter = AddressAdapter(this,"HomeLocation", addressList!!, ::getSelectAddressData,
                        clickEditAdd ={ _, _, _, _, _, _, _, _, _, _, _->

                        },
                        updateAddress = { googlePin: String, isForSelf: String, addressType: String,
                                          phoneNum: String, recipientName: String, floor: String,
                                          companyName: String, streetName: String,lat:String,long:String,addressId:String , isDefault,adapterPos->
                            updateAddress(googlePin,isForSelf,addressType,phoneNum,streetName,lat,long,addressId,isDefault,adapterPos,recipientName,floor,companyName)

                        }
                    )
                    binding.recyclerAddress.adapter = addressAdapter
                }
            } else {
              //  Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
        mViewModel.deleteAddressDataLive.observe(this) {
            if (it.isSuccess == true) {
                binding.progressBarMenu.hide()
//                addressAdapter?.deleteItem(addressList!!, deletePosition!!)
             //   addressList?.removeAt(deletePosition!)
               // addressAdapter?.notifyItemRangeRemoved(deletePosition!!, addressList?.size!!-1)

            } else {
              //  Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
        mViewModelCart.setCurrentAddressDataLive.observe(this) {
            if (it.isSuccess == true) {
                binding.progressBarMenu.hide()
              //  Constants.offerRestaurant.clear()
             //   Constants.allRestaurant.clear()
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,MainActivity::class.java))
                finish()

//                findNavController().previousBackStackEntry?.savedStateHandle?.set("current_address", address)
              //  findNavController().navigateUp()
            } else {
                binding.progressBarMenu.hide()
              //  Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSelectAddressData(position:Int, addressId:Int, address:String, isClicked:String){
        deletePosition = position
        if (isClicked == "ForDelete") {
            if (addressId != 0)
            {
                PrefUtils.instance.showCustomDialog(
                   this,
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
                        mViewModel.deleteAddress(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",addressId)
                        addressAdapter?.removeItem(position)
                        it.dismiss()
                    }
                )
            }
        }
        else {

         //   mViewModelCart.setCurrentAddress(
         //       "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}", addressId)
            PrefUtils.instance.setString(Constants.Address,address)
            startActivity(Intent(this,MainActivity::class.java))
            finish()
//            findNavController().previousBackStackEntry?.savedStateHandle?.set("current_address", address)
//            findNavController().navigateUp()
        }
    }

    override fun click(place: Place?) {
//        val bundle = Bundle()
//        bundle.putString("address", place?.address)
//        bundle.putString("isFrom", "HomeLocation")
//        bundle.putString("latitude", place?.latLng?.latitude.toString())
//        bundle.putString("longitude", place?.latLng?.longitude.toString())
        val intent = Intent(this,OnMapLocationActivity::class.java)
        intent.putExtra("address",place?.address)
        intent.putExtra("isFrom","HomeLocation")
        intent.putExtra("latitude",place?.latLng?.latitude.toString())
        intent.putExtra("longitude",place?.latLng?.longitude.toString())
        startActivity(intent)
      //  findNavController().navigate(R.id.action_homeLocationScreen_to_currentLocationFragment,bundle)

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


    @SuppressLint("NotifyDataSetChanged")
    private fun updateAddress(
        googlePin: String,
        isForSelf: String,
        addressType: String,
        phoneNum: String,
        streetName: String,
        lat: String,
        long: String,
        addressId: String,
        isDefault: String,
        adapterPos: Int,
        recipientName: String,
        floor: String,
        companyName: String
    ) {
        val map = mapOf(
            "is_for_self" to isForSelf,
            "is_default" to isDefault,
            "recipient_name" to  recipientName,
            "phone_number" to  phoneNum,
            "latitude" to lat,
            "longitude" to long,
            "address_type" to addressType,
            "street_number" to streetName,
            "floor" to floor,
            "company_name" to companyName,
            "google_pinned_address" to googlePin,
            "id" to addressId)
        lifecycleScope.launch {
            mViewModel.editUserAddress(token ="$BEARER  ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map =map).collect{

                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        mProgressBar().dialog?.dismiss()
                        addressAdapter?.notifyDataSetChanged()
                    }
                    is ApiResponse.Failure -> {
                        mProgressBar().dialog?.dismiss()

                    }

                }

            }
        }
    }

}