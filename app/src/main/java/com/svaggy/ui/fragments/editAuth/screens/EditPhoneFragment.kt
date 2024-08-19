package com.svaggy.ui.fragments.editAuth.screens

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.svaggy.R
import com.svaggy.databinding.FragmentEditPhoneBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.editAuth.viewmodel.AuthViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditPhoneFragment : Fragment() {


    private var _binding:FragmentEditPhoneBinding? = null
    private val binding get() = _binding!!
    private val authViewModel by viewModels<AuthViewModel>()
    var phoneValid : Boolean =false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPhoneBinding.inflate(inflater, container, false)
        (activity as MainActivity).binding.toolbar.titleTv.text = context?.getString(R.string.change_phone_number)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val countryCode: String = "+" + binding.ccp.selectedCountryCode
                val phoneNumber = binding.etPhoneNumber.text.toString().trim { it <= ' ' }
                if (countryCode.length > 0 && phoneNumber.length > 0)
                {
                    val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.createInstance(context)

                    val isoCode = phoneNumberUtil.getRegionCodeForCountryCode(countryCode.toInt())

                    val exampleNumber = phoneNumberUtil.getExampleNumber(isoCode).nationalNumber.toString()

                    val phoneLength = exampleNumber.length
                    binding.etPhoneNumber.filters = arrayOf<InputFilter>(
                        InputFilter.LengthFilter(
                            phoneLength
                        )
                    )
                    if(binding.etPhoneNumber.length() == phoneLength) {
                        phoneValid=true }
                    else
                    {
                        phoneValid=false
                    }
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })

        binding.btnSendOtp.setOnClickListener {
            val mobileNumber="+"+binding.ccp.selectedCountryCode+binding.etPhoneNumber.text.toString()
            if(phoneValid) {
              // authViewModel.sendMobileOtp("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}", mobileNumber!!)
                updateNumber(mobileNumber)
            }
            else
            {
                PrefUtils.instance.alertDialog(
                    context,
                    getString(R.string.app_name),
                    "Phone Number is Invalid $mobileNumber",
                    okAlert = {
                        it.dismiss()
                    },cancelAlert = {
                        it.dismiss()
                    },
                    false
                )
            }
        }


    }

    private fun updateNumber(userNumber: String) {
        val map = mapOf(
            "mobileNumber" to userNumber,
            "type" to "UPDATE"
        )
        lifecycleScope.launch {
            authViewModel.updatePhone(token = "${Constants.BEARER} ${
                PrefUtils.instance.getString(
                    Constants.Token).toString()}",
                map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                       binding.progressBarMenu.show()
                    }
                    is ApiResponse.Success -> {
                        binding.progressBarMenu.hide()
                        val data = it.data
                        if (data != null){
                            if (data.isSuccess!!) {
                             //   val routes = EditPhoneFragment.actionEditEmailFragmentToVerifyEmailFragment(email = binding.etPhoneNumber.text.toString().trim())
//                                findNavController().navigate(routes)
                                findNavController().navigate(EditPhoneFragmentDirections.actionEditPhoneFragmentToEmailVerifyOTP(number = userNumber))
                            }
                            else {
                                Toast.makeText(context, "" + data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                    is ApiResponse.Failure -> {
                        binding.progressBarMenu.hide()
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }


}