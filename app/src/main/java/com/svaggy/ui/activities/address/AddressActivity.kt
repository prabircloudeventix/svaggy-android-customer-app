package com.svaggy.ui.activities.address

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityAdressBinding
import com.svaggy.ui.activities.home_location.OnMapLocationActivity
import com.svaggy.ui.fragments.cart.viewmodel.CartViewModel
import com.svaggy.ui.fragments.location.adapter.AddressAdapter
import com.svaggy.ui.fragments.location.model.GetAddress
import com.svaggy.ui.fragments.location.viewmodel.AddressViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddressActivity : BaseActivity<ActivityAdressBinding>(ActivityAdressBinding::inflate) {
    private var addressList: ArrayList<GetAddress.Data>? = null
    private var addressAdapter: AddressAdapter? = null
    private var deletePosition:Int?=0
    private val mViewModel by viewModels<AddressViewModel>()
    private val mviewModel by viewModels<CartViewModel>()


    override fun  ActivityAdressBinding.initialize(){
        initObserver()
        mViewModel.getAddress("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}")
      binding.backButton.setOnClickListener {
          onBackPressed()
        }

        binding.txtAddNewAddress.setOnClickListener {
//            val bundle = Bundle()
//            bundle.putString("isFrom", "AddAddress")
//            PrefUtils.instance.setString("isFrom","AddAddress")
            val intent = Intent(this@AddressActivity,OnMapLocationActivity::class.java)
            intent.putExtra("isFrom","AddAddress")
            startActivity(intent)

        }


    }
    private fun initObserver() {

        mViewModel.getAddressDataLive.observe(this) {
            if (it.isSuccess!!) {
                binding.progressBarMenu.hide()
                if (it.data.size > 0) {
                    binding.recyclerAddress.visibility = View.VISIBLE
                    binding.consEmptyAddress.visibility = View.GONE
                    binding.recyclerAddress.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                    binding.recyclerAddress.isNestedScrollingEnabled = true
                    addressList = it.data
                    addressAdapter = AddressAdapter(this, "AddressScreen",addressList!!, ::getSelectAddressData,
                        clickEditAdd ={ googlePin: String, isForSelf: String, addressType: String,
                                        phoneNum: String, recipientName: String, floor: String,
                                        companyName: String, streetName: String,lat:String,long:String,addressId:String ->
                            val intent = Intent(this,OnMapLocationActivity::class.java)
        intent.putExtra("googlePin",googlePin)
        intent.putExtra("isForSelf",isForSelf)
        intent.putExtra("addressType",addressType)
        intent.putExtra("isFrom","EditAddress")
        intent.putExtra("phoneNum", phoneNum)
        intent.putExtra("recipientName", recipientName)
        intent.putExtra("floor", floor)
        intent.putExtra("companyName", companyName)
        intent.putExtra("streetName", streetName)
        intent.putExtra("latitude", lat)
        intent.putExtra("longitude", long)
        intent.putExtra("addressId", addressId)
        startActivity(intent)

                        },
                        updateAddress = { googlePin: String, isForSelf: String, addressType: String,
                                          phoneNum: String, recipientName: String, floor: String,
                                          companyName: String, streetName: String,lat:String,long:String,addressId:String , isDefault,adapterPos->
                            updateAddress(googlePin,isForSelf,addressType,phoneNum,streetName,lat,long,addressId,isDefault,adapterPos,recipientName,floor,companyName)

                        }
                    )
                    binding.recyclerAddress.adapter = addressAdapter
                }
                else {
                    PrefUtils.instance.setString(Constants.AddressId,"")
                    binding.recyclerAddress.visibility = View.GONE
                    binding.consEmptyAddress.visibility = View.VISIBLE
                }
            } else {
//                (activity as MainActivity).hideLoader()
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }

        mViewModel.deleteAddressDataLive.observe(this) {
            if (it.isSuccess!!) {
                binding.progressBarMenu.hide()
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
//                addressAdapter?.deleteItem(addressList!!, deletePosition!!)
                addressList?.removeAt(deletePosition!!)
                addressAdapter?.notifyItemRangeRemoved(deletePosition!!, addressList?.size!!-1)
                mViewModel.getAddress("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}")

            } else {
//                (activity as MainActivity).hideLoader()
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }

        mViewModel.editAddressDataSingle.observe(this) {
            if (it.isSuccess!!) {
                addressAdapter?.notifyDataSetChanged() }
            else
            {
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }

        mviewModel.setCurrentAddressDataLive.observe(this) {
            if (it.isSuccess == true) {
                binding.progressBarMenu.hide()
                onBackPressed()
            } else {
                binding.progressBarMenu.hide()
                //Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSelectAddressData(position:Int, addressId:Int, address:String, isClicked:String){
        deletePosition = position
        if (isClicked == "ForDelete") {
            if (addressId != 0) {
                PrefUtils.instance.showCustomDialog(
                    context = this,
                    message ="Are you sure you want delete this address?",
                    txtNegative ="No",
                    txtPositive = "Yes",
                    note = "",
                    isNoteShow = false,
                    isNegativeShow = true,
                    negativeAlert = {
                        it.dismiss()
                    },
                    positiveAlert = {
                        mViewModel.deleteAddress("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}", addressId)
                        it.dismiss()
                    }
                )
            }
        }
        else if (isClicked == "ForDefault") {
            mViewModel.editAddress(
                "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                addressId,
                isForSelf = false,
                isDefault = true,
                recipientName = "",
                phoneNumber = "",
                residenceInfo = "",
                regionInfo = "",
                postalCode = "",
                city = "",
                latitude = "",
                longitude = "",
                addressType = "",
                isEditFromDefault = true
            )
        } else
        {
          //  mviewModel.setCurrentAddress(
           //     "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}", addressId)
           // findNavController().previousBackStackEntry?.savedStateHandle?.set("current_address", address)
        }
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
            mViewModel.editUserAddress(token ="Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
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