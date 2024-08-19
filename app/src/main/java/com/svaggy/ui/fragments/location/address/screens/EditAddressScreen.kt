package com.svaggy.ui.fragments.location.address.screens

import android.os.Bundle
import android.text.InputFilter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.FragmentEditAddressScreenBinding
import com.svaggy.ui.fragments.location.viewmodel.AddressViewModel
import com.svaggy.utils.Constants
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditAddressScreen : Fragment()
{
    lateinit var binding: FragmentEditAddressScreenBinding
    private var addressType: String? = null
    private val mViewModel by viewModels<AddressViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditAddressScreenBinding.inflate(inflater, container, false)

        addAddressObserver()

     //   PrefUtils.instance.setString(Constants.CurrentDestinationId,(activity as MainActivity).navController.currentDestination?.id.toString())

        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = ContextCompat.getString(requireContext(), R.string.add_new_address)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mViewModel.getAddress("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack(R.id.currentLocationFragment,false)
                }
            })

        binding.radioSelf.isChecked = true
        binding.txtName.visibility = View.GONE
        binding.edtName.visibility = View.GONE
        binding.imgContacts.visibility = View.GONE
        binding.txtPhone.visibility = View.GONE
        binding.edtPhone.visibility = View.GONE

        if (arguments?.getString("isFrom") == "EditAddress") {
            binding.edtFlat.setText(arguments?.getString("residence"))
            addressType = arguments?.getString("addressType")
            if (addressType == "Home") {
                binding.edtHome.setBackgroundResource(R.drawable.bg_preference_fil)
            }
            else if (addressType == "Work")
            {
                binding.edtWork.setBackgroundResource(R.drawable.bg_preference_fil)
            }
            else if (addressType == "Other")
            {
                binding.edtOther.setBackgroundResource(R.drawable.bg_preference_fil)
            }
        }
        else if (arguments?.getString("isFrom") == "AddAddress"){

        }else{
            binding.edtFlat.setText(arguments?.getString(""))
            addressType = "HOME"
            binding.edtHome.setBackgroundResource(R.drawable.bg_preference_fil)

        }
        binding.edtName.setText(arguments?.getString(""))
        binding.edtPhone.setText(arguments?.getString(""))
        binding.edtArea.setText(arguments?.getString("region"))
        binding.edtPostalCode.setText(arguments?.getString("postal"))
        binding.edtCity.setText(arguments?.getString("city"))

        binding.edtName.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtPhone.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtFlat.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtArea.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtPostalCode.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtCity.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().navigate(R.id.currentLocationFragment)
        }

        binding.radioSelf.setOnClickListener {
            binding.radioSelf.isChecked = true
            binding.radioSomeone.isChecked = false
            binding.txtName.visibility = View.GONE
            binding.edtName.visibility = View.GONE
            binding.imgContacts.visibility = View.GONE
            binding.txtPhone.visibility = View.GONE
            binding.edtPhone.visibility = View.GONE
        }

        binding.radioSomeone.setOnClickListener {
            binding.radioSelf.isChecked = false
            binding.radioSomeone.isChecked = true
            binding.txtName.visibility = View.VISIBLE
            binding.edtName.visibility = View.VISIBLE
            binding.imgContacts.visibility = View.VISIBLE
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

        binding.txtSaveLocation.setOnClickListener {
            (activity as MainActivity).mCartViewModel.getAddress(
                "Bearer ${
                    PrefUtils.instance.getString(
                        Constants.Token
                    ).toString()
                }"
            )

            if (arguments?.getString("isFrom") == "EditAddress") {
                if (binding.radioSomeone.isChecked) {
                    if (binding.edtName.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Name")
                        binding.edtName.requestFocus()
                    } else if (binding.edtPhone.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Phone")
                        binding.edtPhone.requestFocus()
                    } else if (binding.edtFlat.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence")
                        binding.edtFlat.requestFocus()
                    } else if (binding.edtArea.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence Area")
                        binding.edtArea.requestFocus()
                    } else if (binding.edtPostalCode.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence Postal Code")
                        binding.edtPostalCode.requestFocus()
                    } else if (binding.edtCity.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your City")
                        binding.edtCity.requestFocus()
                    } else {
                        mViewModel.editAddress("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}", arguments?.getInt("addressId")!!,
                            false,
                            false,
                            binding.edtName.text.toString(),
                            binding.edtPhone.text.toString(),
                            binding.edtFlat.text.toString(),
                            binding.edtArea.text.toString(),
                            binding.edtPostalCode.text.toString(),
                            binding.edtCity.text.toString(),
                            arguments?.getDouble("latitude").toString(),
                            arguments?.getDouble("longitude").toString(),
                            addressType!!,
                            false
                        )
                    }
                }
                else {
                    if (binding.edtFlat.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence")
                        binding.edtFlat.requestFocus()
                    } else if (binding.edtArea.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence Area")
                        binding.edtArea.requestFocus()
                    } else if (binding.edtPostalCode.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence Postal Code")
                        binding.edtPostalCode.requestFocus()
                    } else if (binding.edtCity.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your City")
                        binding.edtCity.requestFocus()
                    } else {
                        mViewModel.editAddress(
                            "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                            arguments?.getInt("addressId")!!,
                            true,
                            false,
                            "",
                            "",
                            binding.edtFlat.text.toString(),
                            binding.edtArea.text.toString(),
                            binding.edtPostalCode.text.toString(),
                            binding.edtCity.text.toString(),
                            arguments?.getDouble("latitude").toString(),
                            arguments?.getDouble("longitude").toString(),
                            addressType!!,
                            false
                        )
                    }
                }
            }
            else if (arguments?.getString("isFrom")=="CheckOut"){
                if (binding.radioSomeone.isChecked)
                {
                    if (binding.edtName.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Name")
                        binding.edtName.requestFocus()
                    } else if (binding.edtPhone.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Phone")
                        binding.edtPhone.requestFocus()
                    } else if (binding.edtFlat.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence")
                        binding.edtFlat.requestFocus()
                    } else if (binding.edtArea.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence Area")
                        binding.edtArea.requestFocus()
                    } else if (binding.edtPostalCode.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence Postal Code")
                        binding.edtPostalCode.requestFocus()
                    } else if (binding.edtCity.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your City")
                        binding.edtCity.requestFocus()
                    } else {
                        mViewModel.addAddress(
                            "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                            false,
                            false,
                            binding.edtName.text.toString(),
                            binding.edtPhone.text.toString(),
                            binding.edtFlat.text.toString(),
                            binding.edtArea.text.toString(),
                            binding.edtPostalCode.text.toString(),
                            binding.edtCity.text.toString(),
                            arguments?.getDouble("latitude").toString(),
                            arguments?.getDouble("longitude").toString(),
                            addressType!!
                        )
                    }
                }
                else {
                    if (binding.edtFlat.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence")
                        binding.edtFlat.requestFocus()
                    } else if (binding.edtArea.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence Area")
                        binding.edtArea.requestFocus()
                    } else if (binding.edtPostalCode.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence Postal Code")
                        binding.edtPostalCode.requestFocus()
                    } else if (binding.edtCity.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your City")
                        binding.edtCity.requestFocus()
                    } else {
                        mViewModel.addAddress(
                            "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                            true,
                            false,
                            "",
                            "",
                            binding.edtFlat.text.toString(),
                            binding.edtArea.text.toString(),
                            binding.edtPostalCode.text.toString(),
                            binding.edtCity.text.toString(),
                            arguments?.getDouble("latitude").toString(),
                            arguments?.getDouble("longitude").toString(),
                            addressType!!
                        )
                    }
                }
            }
            else {
                if (binding.radioSomeone.isChecked) {
                    if (binding.edtName.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Name")
                        binding.edtName.requestFocus()
                    } else if (binding.edtPhone.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Phone")
                        binding.edtPhone.requestFocus()
                    } else if (binding.edtFlat.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence")
                        binding.edtFlat.requestFocus()
                    } else if (binding.edtArea.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence Area")
                        binding.edtArea.requestFocus()
                    } else if (binding.edtPostalCode.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence Postal Code")
                        binding.edtPostalCode.requestFocus()
                    } else if (binding.edtCity.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your City")
                        binding.edtCity.requestFocus()
                    } else {
                        mViewModel.addAddress(
                            "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                            false,
                            false,
                            binding.edtName.text.toString(),
                            binding.edtPhone.text.toString(),
                            binding.edtFlat.text.toString(),
                            binding.edtArea.text.toString(),
                            binding.edtPostalCode.text.toString(),
                            binding.edtCity.text.toString(),
                            arguments?.getDouble("latitude").toString(),
                            arguments?.getDouble("longitude").toString(),
                            addressType!!
                        )
                    }
                }
                else {
                    if (binding.edtFlat.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence")
                        binding.edtFlat.requestFocus()
                    } else if (binding.edtArea.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence Area")
                        binding.edtArea.requestFocus()
                    } else if (binding.edtPostalCode.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your Residence Postal Code")
                        binding.edtPostalCode.requestFocus()
                    } else if (binding.edtCity.text.isNullOrEmpty()) {
                        PrefUtils.instance.showToast(context, "Please Enter Your City")
                        binding.edtCity.requestFocus()
                    } else {
                        mViewModel.addAddress(
                            "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                            true,
                            false,
                            "",
                            "",
                            binding.edtFlat.text.toString(),
                            binding.edtArea.text.toString(),
                            binding.edtPostalCode.text.toString(),
                            binding.edtCity.text.toString(),
                            arguments?.getDouble("latitude").toString(),
                            arguments?.getDouble("longitude").toString(),
                            addressType ?: "OTHER"
                        )
                    }
                }
            }
        }
    }

    private fun addAddressObserver() {
        mViewModel.addAddressDataSingle.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
                val latitude = it.data?.latitude
                val longitude = it.data?.longitude
                val id = it.data?.id.toString()

                PrefUtils.instance.setString(Constants.Latitude,latitude)
                PrefUtils.instance.setString(Constants.Longitude,longitude)
                PrefUtils.instance.setString(Constants.AddressId,id)


                if (arguments?.getString("isFrom")=="CheckOut"){
                    findNavController().popBackStack(R.id.cartMoreItemScreen,false)
            }
                else{
                    Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_editAddressScreen_to_addressScreen)
                }
            }
            else
            {
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }

        mViewModel.editAddressDataSingle.observe(viewLifecycleOwner) {
            if (it.isSuccess!!)
            {

                if (arguments?.getString("isFrom")=="CheckOut"){
                    Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.cartMoreItemScreen)
                }
                else{
                    Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_editAddressScreen_to_addressScreen)
                }
            }
            else
            {
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}