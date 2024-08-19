package com.svaggy.ui.activities.address

import android.content.Intent
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityEditAddressBinding
import com.svaggy.ui.activities.checkout.CheckOutActivity
import com.svaggy.ui.fragments.banner.adapter.CompareFoodAdapter
import com.svaggy.ui.fragments.location.viewmodel.AddressViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.setSafeOnClickListener
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditAddressActivity : BaseActivity<ActivityEditAddressBinding>(ActivityEditAddressBinding::inflate) {
    private var addressType: String? = null
    private val mViewModel by viewModels<AddressViewModel>()
    private lateinit var isFrom:String
    private lateinit var googlePinAddreess:String
    private lateinit var latitude:String
    private lateinit var longitude:String
    private lateinit var addressId:String

//    intent.putExtra("isFrom",isFrom)
//    intent.putExtra("residence",intent.getStringExtra("residence"))
//    intent.putExtra("city",locationCity)
//    intent.putExtra("region",intent.getStringExtra("region"))
//    intent.putExtra("postal",locationPostal)
//    intent.putExtra("latitude",marker.latitude)
//    intent.putExtra("longitude",marker.longitude)
//    intent.putExtra("addressType",intent.getStringExtra("addressType"))


    override fun ActivityEditAddressBinding.initialize(){
        isFrom = intent.getStringExtra("isFrom").toString()
       // addAddressObserver()
       // mViewModel.getAddress("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}")
        binding.radioSelf.isChecked = true
        binding.txtName.visibility = View.GONE
        binding.edtName.visibility = View.GONE
        binding.txtPhone.visibility = View.GONE
        binding.edtPhone.visibility = View.GONE

        googlePinAddreess = intent.getStringExtra("region").toString()
        latitude = intent.getDoubleExtra("lat",0.0).toString()
        longitude = intent.getDoubleExtra("long",0.0).toString()
        val parts = googlePinAddreess.split(",")
        val part1 = if (parts.isNotEmpty()) parts[0] else ""
        val part2 = if (parts.size > 1) parts.subList(1, parts.size).joinToString(",").trim() else ""
        binding.getPin.text = part1
        binding.getPinPint.text = part2

        binding.icEdit.setOnClickListener {
            onBackPressed()
        }

        when(isFrom){

            "EditAddress" ->{
                updateUi()
            }
            "AddAddress" ->{}
            else ->{
//                addressType = "HOME"
//                binding.edtHome.setBackgroundResource(R.drawable.bg_preference_fil)
            }
        }



        backButton.setOnClickListener {
           onBackPressed()
        }

        binding.radioSelf.setOnClickListener {
            binding.radioSelf.isChecked = true
            binding.radioSomeone.isChecked = false
            binding.txtName.visibility = View.GONE
            binding.edtName.visibility = View.GONE
            binding.txtPhone.visibility = View.GONE
            binding.edtPhone.visibility = View.GONE
            binding.txtType.show()
            binding.edtHome.show()
            binding.edtWork.show()
            binding.edtOther.show()
        }

        binding.radioSomeone.setOnClickListener {
            binding.radioSelf.isChecked = false
            binding.radioSomeone.isChecked = true
            binding.txtName.visibility = View.VISIBLE
            binding.edtName.visibility = View.VISIBLE
            binding.txtPhone.visibility = View.VISIBLE
            binding.edtPhone.visibility = View.VISIBLE
            binding.txtType.visibility = View.GONE
            binding.edtHome.visibility = View.GONE
            binding.edtWork.visibility = View.GONE
            binding.edtOther.visibility = View.GONE

        }

        binding.edtHome.setOnClickListener {
            addressType = "HOME"
            binding.edtHome.setBackgroundResource(R.drawable.bg_preference_fil)
            binding.edtWork.setBackgroundResource(R.drawable.radius_black)
            binding.edtOther.setBackgroundResource(R.drawable.radius_black)
        }

        binding.edtWork.setOnClickListener {
            addressType = "WORK"
            binding.edtHome.setBackgroundResource(R.drawable.radius_black)
            binding.edtWork.setBackgroundResource(R.drawable.bg_preference_fil)
            binding.edtOther.setBackgroundResource(R.drawable.radius_black)
        }

        binding.edtOther.setOnClickListener {
            addressType = "OTHER"
            binding.edtHome.setBackgroundResource(R.drawable.radius_black)
            binding.edtWork.setBackgroundResource(R.drawable.radius_black)
            binding.edtOther.setBackgroundResource(R.drawable.bg_preference_fil)
        }




//
//        binding.txtSaveLocation.setSafeOnClickListener {
//            if (isFrom == "EditAddress") {
//                if (binding.radioSomeone.isChecked) {
//                    if (binding.edtName.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Name")
//                        binding.edtName.requestFocus()
//                    } else if (binding.edtPhone.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Phone")
//                        binding.edtPhone.requestFocus()
//                    } else if (binding.edtFlat.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence")
//                        binding.edtFlat.requestFocus()
//                    } else if (binding.edtArea.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence Area")
//                        binding.edtArea.requestFocus()
//                    } else if (binding.edtPostalCode.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence Postal Code")
//                        binding.edtPostalCode.requestFocus()
//                    } else if (binding.edtCity.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your City")
//                        binding.edtCity.requestFocus()
//                    } else {
//                        mViewModel.editAddress("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
//                            intent?.getIntExtra("addressId",0)!!,
//                            false,
//                            false,
//                            binding.edtName.text.toString(),
//                            binding.edtPhone.text.toString(),
//                            binding.edtFlat.text.toString(),
//                            binding.edtArea.text.toString(),
//                            binding.edtPostalCode.text.toString(),
//                            binding.edtCity.text.toString(),
//                            intent.getDoubleExtra("latitude",0.0).toString(),
//                            intent.getDoubleExtra("longitude",0.0).toString(),
//                            addressType!!,
//                            false
//                        )
//                    }
//                }
//                else {
//                    if (binding.edtFlat.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence")
//                        binding.edtFlat.requestFocus()
//                    }
//                    else if (binding.edtArea.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence Area")
//                        binding.edtArea.requestFocus()
//                    }
//                    else if (binding.edtPostalCode.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence Postal Code")
//                        binding.edtPostalCode.requestFocus()
//                    } else if (binding.edtCity.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your City")
//                        binding.edtCity.requestFocus()
//                    } else {
//                        mViewModel.editAddress(
//                            "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
//                            intent?.getIntExtra("addressId",0)!!,
//                            true,
//                            false,
//                            "",
//                            "",
//                            binding.edtFlat.text.toString(),
//                            binding.edtArea.text.toString(),
//                            binding.edtPostalCode.text.toString(),
//                            binding.edtCity.text.toString(),
//                            intent.getDoubleExtra("latitude",0.0).toString(),
//                            intent.getDoubleExtra("longitude",0.0).toString(),
//                            addressType!!,
//                            false
//                        )
//                    }
//                }
//            }
//            else if (isFrom   =="CheckOut"){
//                if (binding.radioSomeone.isChecked) {
//                    if (binding.edtName.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Name")
//                        binding.edtName.requestFocus()
//                    } else if (binding.edtPhone.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Phone")
//                        binding.edtPhone.requestFocus()
//                    } else if (binding.edtFlat.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence")
//                        binding.edtFlat.requestFocus()
//                    } else if (binding.edtArea.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence Area")
//                        binding.edtArea.requestFocus()
//                    } else if (binding.edtPostalCode.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence Postal Code")
//                        binding.edtPostalCode.requestFocus()
//                    } else if (binding.edtCity.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your City")
//                        binding.edtCity.requestFocus()
//                    } else {
//                        mViewModel.addAddress(
//                            "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
//                            false,
//                            false,
//                            binding.edtName.text.toString(),
//                            binding.edtPhone.text.toString(),
//                            binding.edtFlat.text.toString(),
//                            binding.edtArea.text.toString(),
//                            binding.edtPostalCode.text.toString(),
//                            binding.edtCity.text.toString(),
//                            intent.getDoubleExtra("latitude",0.0).toString(),
//                            intent.getDoubleExtra("longitude",0.0).toString(),
//                            addressType!!
//                        )
//                    }
//                }
//                else {
//                    if (binding.edtFlat.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence")
//                        binding.edtFlat.requestFocus()
//                    } else if (binding.edtArea.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence Area")
//                        binding.edtArea.requestFocus()
//                    } else if (binding.edtPostalCode.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence Postal Code")
//                        binding.edtPostalCode.requestFocus()
//                    } else if (binding.edtCity.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your City")
//                        binding.edtCity.requestFocus()
//                    } else {
//                        mViewModel.addAddress(
//                            "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
//                            true,
//                            false,
//                            "",
//                            "",
//                            binding.edtFlat.text.toString(),
//                            binding.edtArea.text.toString(),
//                            binding.edtPostalCode.text.toString(),
//                            binding.edtCity.text.toString(),
//                            intent.getDoubleExtra("latitude",0.0).toString(),
//                            intent.getDoubleExtra("longitude",0.0).toString(),
//                            addressType!!
//                        )
//                    }
//                }
//            }
//            else {
//                if (binding.radioSomeone.isChecked) {
//                    if (binding.edtName.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Name")
//                        binding.edtName.requestFocus()
//                    } else if (binding.edtPhone.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Phone")
//                        binding.edtPhone.requestFocus()
//                    } else if (binding.edtFlat.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence")
//                        binding.edtFlat.requestFocus()
//                    } else if (binding.edtArea.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence Area")
//                        binding.edtArea.requestFocus()
//                    } else if (binding.edtPostalCode.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence Postal Code")
//                        binding.edtPostalCode.requestFocus()
//                    } else if (binding.edtCity.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your City")
//                        binding.edtCity.requestFocus()
//                    } else {
//                        mViewModel.addAddress(
//                            "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
//                            false,
//                            false,
//                            binding.edtName.text.toString(),
//                            binding.edtPhone.text.toString(),
//                            binding.edtFlat.text.toString(),
//                            binding.edtArea.text.toString(),
//                            binding.edtPostalCode.text.toString(),
//                            binding.edtCity.text.toString(),
//                            intent.getDoubleExtra("latitude",0.0).toString(),
//                            intent.getDoubleExtra("longitude",0.0).toString(),
//                            addressType!!
//                        )
//                    }
//                }
//                else {
//                    if (binding.edtFlat.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence")
//                        binding.edtFlat.requestFocus()
//                    } else if (binding.edtArea.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence Area")
//                        binding.edtArea.requestFocus()
//                    } else if (binding.edtPostalCode.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your Residence Postal Code")
//                        binding.edtPostalCode.requestFocus()
//                    } else if (binding.edtCity.text.isNullOrEmpty()) {
//                        PrefUtils.instance.showToast(this@EditAddressActivity, "Please Enter Your City")
//                        binding.edtCity.requestFocus()
//                    } else {
//                        mViewModel.addAddress(
//                            "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
//                            true,
//                            false,
//                            "",
//                            "",
//                            binding.edtFlat.text.toString(),
//                            binding.edtArea.text.toString(),
//                            binding.edtPostalCode.text.toString(),
//                            binding.edtCity.text.toString(),
//                            intent.getDoubleExtra("latitude",0.0).toString(),
//                            intent.getDoubleExtra("longitude",0.0).toString(),
//                            addressType ?: "OTHER"
//                        )
//                    }
//                }
//            }
//
//
//            }



        binding.txtSaveLocation.setOnClickListener {

            when(isFrom){


                "EditAddress" ->{
                    when(binding.radioSelf.isChecked){
                        true ->{
                            if (!googlePinAddreess.isNullOrEmpty() && googlePinAddreess != "null"   && !latitude.isNullOrEmpty() && latitude != "null"){
                                if (binding.editStreet.text.trim().toString().isNullOrEmpty()){
                                    binding.editStreet.error = "Required"
                                }else{

                                    if (addressType != null){
                                        mProgressBar().showProgressBar(this@EditAddressActivity)
                                        updateAddressFoSelf()

                                    }else{
                                        Toast.makeText(this@EditAddressActivity, "Please Select Address Type", Toast.LENGTH_LONG).show()
                                    }

                                }
                            }

                        }
                        false ->{

                            if (!googlePinAddreess.isNullOrEmpty() && googlePinAddreess != "null"   && !latitude.isNullOrEmpty() && latitude != "null"){
                                if (binding.edtName.text.trim().toString().isNullOrEmpty()){
                                    binding.edtName.error = "Required"
                                }else{

                                    if (binding.edtPhone.text.trim().toString().isNullOrEmpty()){
                                        binding.edtPhone.error = "Required"
                                    }
                                    else{

                                        if (binding.editStreet.text.trim().toString().isNullOrEmpty()){
                                            binding.editStreet.error = "Required"
                                        }else{
                                            mProgressBar().showProgressBar(this@EditAddressActivity)
                                            updateAddressFoRandom()


                                        }

                                    }

                                }
                            }

                        }
                    }

                }

                else ->{
                    when(binding.radioSelf.isChecked){
                        true ->{
                            if (!googlePinAddreess.isNullOrEmpty() && googlePinAddreess != "null"   && !latitude.isNullOrEmpty() && latitude != "null"){
                                if (binding.editStreet.text.trim().toString().isNullOrEmpty()){
                                    binding.editStreet.error = "Required"
                                }else{

                                    if (addressType != null){
                                        mProgressBar().showProgressBar(this@EditAddressActivity)
                                        addMySelfAddress()

                                    }else{
                                        Toast.makeText(this@EditAddressActivity, "Please Select Address Type", Toast.LENGTH_LONG).show()
                                    }

                                }
                            }

                        }
                        false ->{

                            if (!googlePinAddreess.isNullOrEmpty() && googlePinAddreess != "null"   && !latitude.isNullOrEmpty() && latitude != "null"){
                                if (binding.edtName.text.trim().toString().isNullOrEmpty()){
                                    binding.edtName.error = "Required"
                                }else{

                                    if (binding.edtPhone.text.trim().toString().isNullOrEmpty()){
                                        binding.edtPhone.error = "Required"
                                    }
                                    else{

                                        if (binding.editStreet.text.trim().toString().isNullOrEmpty()){
                                            binding.editStreet.error = "Required"
                                        }else{
                                            mProgressBar().showProgressBar(this@EditAddressActivity)
                                            addRandomAddress()


                                        }

                                    }

                                }
                            }

                        }
                    }


                }
            }


        }

    }

    private fun updateUi() {

        binding.edtName.setText(intent.getStringExtra("recipientName"))
        binding.edtPhone.setText(intent.getStringExtra("phoneNum"))
        binding.editStreet.setText(intent.getStringExtra("streetName"))
        binding.editFloor.setText(intent.getStringExtra("floor"))
        binding.editCompany.setText(intent.getStringExtra("companyName"))
        addressType = intent.getStringExtra("addressType").toString()
        addressId = intent.getStringExtra("addressId").toString()
        when(intent.getStringExtra("addressType")){
            "Home" ->{ binding.edtHome.setBackgroundResource(R.drawable.bg_preference_fil)}
            "Work" ->{binding.edtWork.setBackgroundResource(R.drawable.bg_preference_fil)}
            "Other" ->{binding.edtOther.setBackgroundResource(R.drawable.bg_preference_fil)}

        }
        when(intent.getStringExtra("isForSelf")){


            "true" ->{
                binding.radioSelf.isChecked = true
                binding.radioSomeone.isChecked = false
                binding.txtName.visibility = View.GONE
                binding.edtName.visibility = View.GONE
                binding.txtPhone.visibility = View.GONE
                binding.edtPhone.visibility = View.GONE
            }
            "false" ->{
                binding.radioSelf.isChecked = false
                binding.radioSomeone.isChecked = true
                binding.txtName.visibility = View.VISIBLE
                binding.edtName.visibility = View.VISIBLE
                binding.txtPhone.visibility = View.VISIBLE
                binding.edtPhone.visibility = View.VISIBLE
                binding.txtType.visibility = View.GONE
                binding.edtHome.visibility = View.GONE
                binding.edtWork.visibility = View.GONE
                binding.edtOther.visibility = View.GONE
            }

        }


    }

    private fun addRandomAddress() {
        val map = mapOf(
            "is_for_self" to false.toString(),
            "is_default" to false.toString(),
            "recipient_name" to  binding.edtName.text.trim().toString(),
            "phone_number" to  binding.edtPhone.text.trim().toString(),
            "is_default" to false.toString(),
            "latitude" to latitude,
            "longitude" to longitude,
            "address_type" to "OTHER",
            "street_number" to binding.editStreet.text.trim().toString(),
            "floor" to binding.editFloor.text.trim().toString(),
            "company_name" to binding.editCompany.text.trim().toString(),
            "google_pinned_address" to googlePinAddreess,
        )
        lifecycleScope.launch {
            mViewModel.addUserAddress(token ="Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map =map).collect{

                when (it) {
                    is ApiResponse.Loading -> {



                    }
                    is ApiResponse.Success -> {
                        mProgressBar().dialog?.dismiss()
                        if (isFrom =="CheckOut"){
                            startActivity(Intent(this@EditAddressActivity,CheckOutActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            finish()
                        }
                        else {
                            startActivity(Intent(this@EditAddressActivity, AddressActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            finish()
                        }




                    }
                    is ApiResponse.Failure -> {
                        mProgressBar().dialog?.dismiss()

                    }

                }

            }
        }
    }


//    mMap["is_for_self"] = isForSelf.toString()
//    mMap["is_default"] = isDefault.toString()
//    mMap["recipient_name"] = recipientName
//    mMap["phone_number"] = phoneNumber
//    mMap["residence_info"] = residenceInfo
//    mMap["region_info"] = regionInfo
//    mMap["postal_code"] = postalCode
//    mMap["city"] = city
//    mMap["latitude"] = latitude
//    mMap["longitude"] = longitude
//    mMap["address_type"] = addressType


    private fun addMySelfAddress(){
        val map = mapOf(
            "is_for_self" to true.toString(),
            "is_default" to false.toString(),
            "latitude" to latitude,
            "longitude" to longitude,
            "address_type" to addressType.toString(),
            "street_number" to binding.editStreet.text.trim().toString(),
            "floor" to binding.editFloor.text.trim().toString(),
            "company_name" to binding.editCompany.text.trim().toString(),
            "google_pinned_address" to googlePinAddreess,
        )
        lifecycleScope.launch {
            mViewModel.addUserAddress(token ="Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map =map).collect{

                when (it) {
                    is ApiResponse.Loading -> {



                    }
                    is ApiResponse.Success -> {
                        mProgressBar().dialog?.dismiss()
                        if (isFrom =="CheckOut"){
                            startActivity(Intent(this@EditAddressActivity,CheckOutActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            finish()
                        }
                        else {
                            startActivity(Intent(this@EditAddressActivity, AddressActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            finish()
                        }




                    }
                    is ApiResponse.Failure -> {
                        mProgressBar().dialog?.dismiss()

                    }

                }

            }
        }
    }
    private fun updateAddressFoSelf(){
        val map = mapOf(
            "is_for_self" to true.toString(),
            "is_default" to false.toString(),
            "recipient_name" to  binding.edtName.text.trim().toString(),
            "phone_number" to  binding.edtPhone.text.trim().toString(),
            "latitude" to latitude,
            "longitude" to longitude,
            "address_type" to addressType.toString(),
            "street_number" to binding.editStreet.text.trim().toString(),
            "floor" to binding.editFloor.text.trim().toString(),
            "company_name" to binding.editCompany.text.trim().toString(),
            "google_pinned_address" to googlePinAddreess,
            "id" to addressId)
        lifecycleScope.launch {
            mViewModel.editUserAddress(token ="Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map =map).collect{

                when (it) {
                    is ApiResponse.Loading -> {



                    }
                    is ApiResponse.Success -> {
                        mProgressBar().dialog?.dismiss()
                        if (isFrom =="CheckOut"){
                            startActivity(Intent(this@EditAddressActivity,CheckOutActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            finish()
                        }
                        else {
                            startActivity(Intent(this@EditAddressActivity, AddressActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            finish()
                        }




                    }
                    is ApiResponse.Failure -> {
                        mProgressBar().dialog?.dismiss()

                    }

                }

            }
        }
    }

    private fun updateAddressFoRandom(){
        val map = mapOf(
            "is_for_self" to false.toString(),
            "recipient_name" to  binding.edtName.text.trim().toString(),
            "phone_number" to  binding.edtPhone.text.trim().toString(),
            "is_default" to false.toString(),
            "latitude" to latitude,
            "longitude" to longitude,
            "address_type" to "OTHER",
            "street_number" to binding.editStreet.text.trim().toString(),
            "floor" to binding.editFloor.text.trim().toString(),
            "company_name" to binding.editCompany.text.trim().toString(),
            "google_pinned_address" to googlePinAddreess,
            "id" to addressId
        )
        lifecycleScope.launch {
            mViewModel.editUserAddress(token ="Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map =map).collect{

                when (it) {
                    is ApiResponse.Loading -> {



                    }
                    is ApiResponse.Success -> {
                        mProgressBar().dialog?.dismiss()
                        if (isFrom =="CheckOut"){
                            startActivity(Intent(this@EditAddressActivity,CheckOutActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            finish()
                        }
                        else {
                            startActivity(Intent(this@EditAddressActivity, AddressActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            finish()
                        }




                    }
                    is ApiResponse.Failure -> {
                        mProgressBar().dialog?.dismiss()

                    }

                }

            }
        }
    }



}