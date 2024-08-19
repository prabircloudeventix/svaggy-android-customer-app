package com.svaggy.ui.fragments.editAuth.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.FragmentEditEmailBinding
import com.svaggy.ui.fragments.editAuth.viewmodel.AuthViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import com.svaggy.utils.updateStatusBarColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditEmailFragment : Fragment() {
    lateinit var binding: FragmentEditEmailBinding
    private val authViewModel by viewModels<AuthViewModel>()
    private var emailAddress: String?=null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        requireActivity().updateStatusBarColor("#F6F6F6",true)
        binding = FragmentEditEmailBinding.inflate(inflater, container, false)
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
    //    (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility=View.GONE
        binding.txtHead.text = context?.getString(R.string.email)
        binding.edtEmail.visibility =View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text =  context?.getString(R.string.edit_profile)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
            )





//        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                val countryCode: String = "+" + binding.ccp.selectedCountryCode
//                val phoneNumber = binding.etPhoneNumber.text.toString().trim { it <= ' ' }
//                if (countryCode.length > 0 && phoneNumber.length > 0)
//                {
//                    val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.createInstance(context)
//
//                    val isoCode = phoneNumberUtil.getRegionCodeForCountryCode(countryCode.toInt())
//
//                    val exampleNumber = phoneNumberUtil.getExampleNumber(isoCode).nationalNumber.toString()
//
//                    val phoneLength = exampleNumber.length
//                    binding.etPhoneNumber.filters = arrayOf<InputFilter>(
//                        InputFilter.LengthFilter(
//                            phoneLength
//                        )
//                    )
//                    if(binding.etPhoneNumber.length() == phoneLength)
//                    {
//                        phoneValid=true
//                    }
//                    else
//                    {
//                        phoneValid=false
//                    }
//                }
//            }
//            override fun afterTextChanged(s: Editable) {}
//        })

        binding.btnSendOtp.setOnClickListener {
            emailAddress = binding.edtEmail.text.toString().trim()
                val validationResult = validateLoginInput()
                if (validationResult.first) {
                    updateEmail(emailAddress!!)
                }
        }
    }

    private fun updateEmail(emailAddress: String) {
        val map = mapOf(
            "email" to emailAddress,
            "type" to "UPDATE"
        )
        lifecycleScope.launch {
            authViewModel.updateEmail(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
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
                                val routes = EditEmailFragmentDirections.actionEditEmailFragmentToVerifyEmailFragment(email = binding.edtEmail.text.toString().trim())
                                findNavController().navigate(routes)
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

    private fun runObserver() {
//        val bundle = Bundle()
//        authViewModel.sendEmailOtpLiveData.observe(viewLifecycleOwner) {
//            if (it.isSuccess!!) {
//                (activity as MainActivity).hideLoader()
//                Toast.makeText(context, "Otp Send Successful", Toast.LENGTH_SHORT).show()
//                if (arguments?.getString("cartBundle") == "CartMoreItemScreen") {
//                    bundle.putString("cartBundle", "CartMoreItemScreen")
//                } else if (arguments?.getString("cartBundle") == "ProfileScreen") {
//                    bundle.putString("cartBundle", "ProfileScreen")
//                }
//                if (arguments?.getString("isFrom") == "EditProfileEmail")
//                    bundle.putString("isFrom", "EditProfileEmail")
//                PrefUtils.instance.setString(Constants.Type, "email")
//                PrefUtils.instance.setString(
//                    Constants.EmailOrPhone,
//                    binding.edtEmail.text.toString()
//                )
//                findNavController().navigate(
//                    R.id.action_secondLoginFragment_to_emailVerifyOTP,
//                    bundle
//                )
//            } else {
//                Toast.makeText(context, it.message.toString(), Toast.LENGTH_SHORT).show()
//            }
//        }
//        authViewModel.sendMobileOtpLiveData.observe(viewLifecycleOwner) {
//            if (it.isSuccess!!) {
//                (activity as MainActivity).hideLoader()
//                Toast.makeText(context, "Otp Send Successful", Toast.LENGTH_SHORT).show()
//                bundle.putString("isFrom", "EditProfileMobile")
//                bundle.putString("cartBundle", "CartMoreItemScreen")
//                PrefUtils.instance.setString(Constants.Type,"phone_number")
//                PrefUtils.instance.setString(Constants.EmailOrPhone, "+"+binding.ccp.selectedCountryCode + binding.etPhoneNumber.text.toString())
//                findNavController().navigate(R.id.action_secondLoginFragment_to_emailVerifyOTP,bundle)
//            }
//            else{
//                (activity as MainActivity).hideLoader()
//                Toast.makeText(context, ""+ it.message, Toast.LENGTH_SHORT).show()
//            }
//        }
//        authViewModel.errorMessage.observe(viewLifecycleOwner) {
//            (activity as MainActivity).hideLoader()
//            Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
//        }
    }

    private fun validateLoginInput(): Pair<Boolean, String> {
        emailAddress=binding.edtEmail.text.toString()
        return authViewModel.loginValidation(emailAddress!!)
    }
}