package com.svaggy.ui.activities.editprofile

import android.content.Intent
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityEditPhoneNumberBinding
import com.svaggy.ui.fragments.editAuth.screens.EditPhoneFragmentDirections
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
class EditPhoneNumberActivity : BaseActivity<ActivityEditPhoneNumberBinding>(ActivityEditPhoneNumberBinding::inflate) {
    private val authViewModel by viewModels<AuthViewModel>()
    var phoneValid : Boolean =false

    override fun ActivityEditPhoneNumberBinding.initialize(){
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val countryCode: String = "+" + binding.ccp.selectedCountryCode
                val phoneNumber = binding.etPhoneNumber.text.toString().trim { it <= ' ' }
                if (countryCode.isNotEmpty() && phoneNumber.isNotEmpty())
                {
                    val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.createInstance(this@EditPhoneNumberActivity)

                    val isoCode = phoneNumberUtil.getRegionCodeForCountryCode(countryCode.toInt())

                    val exampleNumber = phoneNumberUtil.getExampleNumber(isoCode).nationalNumber.toString()

                    val phoneLength = exampleNumber.length
                    binding.etPhoneNumber.filters = arrayOf<InputFilter>(
                        InputFilter.LengthFilter(
                            phoneLength
                        )
                    )
                    phoneValid = binding.etPhoneNumber.length() == phoneLength
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.btnSendOtp.setOnClickListener {
            val mobileNumber="+"+binding.ccp.selectedCountryCode+binding.etPhoneNumber.text.toString()
            if(phoneValid) {
                // authViewModel.sendMobileOtp("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}", mobileNumber!!)
                updateNumber(mobileNumber)
            }
            else
            {
                PrefUtils.instance.alertDialog(
                    this@EditPhoneNumberActivity,
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
                                val intent = Intent(this@EditPhoneNumberActivity,VerifyPhoneOtpActivity::class.java)
                                intent.putExtra("number",userNumber)
                                startActivity(intent)
                                //   val routes = EditPhoneFragment.actionEditEmailFragmentToVerifyEmailFragment(email = binding.etPhoneNumber.text.toString().trim())
//                                findNavController().navigate(routes)
                               // findNavController().navigate(EditPhoneFragmentDirections.actionEditPhoneFragmentToEmailVerifyOTP(number = userNumber))
                            }
                            else {
                                Toast.makeText(this@EditPhoneNumberActivity, "" + data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                    is ApiResponse.Failure -> {
                        binding.progressBarMenu.hide()
                        Toast.makeText(this@EditPhoneNumberActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }


}