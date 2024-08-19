package com.svaggy.ui.activities.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityPhoneLogInBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.savePref.SavePrefActivity
import com.svaggy.ui.fragments.editAuth.viewmodel.AuthViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.countrycodepicker.Country
import com.svaggy.utils.countrycodepicker.CountryCodePicker
import com.svaggy.utils.countrycodepicker.CountryUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import com.svaggy.utils.updateStatusBarColor
import dagger.hilt.android.AndroidEntryPoint
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhoneLogInActivity : BaseActivity<ActivityPhoneLogInBinding>(ActivityPhoneLogInBinding::inflate), CountryCodePicker.OnCountryRegister  {


    private lateinit var countries: List<Country>
    private var phoneValid: Boolean = false
    private var mobileNumber: String? = null
    private var email: String? = ""
    private val requestCodeGmailLogin = 1
    private val authViewModel by viewModels<AuthViewModel>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { data: Boolean ->
        if (data){
            PrefUtils.instance.setBoolean(Constants.IS_NOTIFICATION,true)

        }else{
            PrefUtils.instance.setBoolean(Constants.IS_NOTIFICATION,false)
        }

    }





    override fun ActivityPhoneLogInBinding.initialize() {
        this@PhoneLogInActivity.updateStatusBarColor("#CE221E",false)
        countries = CountryUtils.getAllCountries(this@PhoneLogInActivity)
        initObserver()
        binding.etPhoneNumber.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        binding.etPhoneNumber.keyListener = DigitsKeyListener.getInstance("0123456789")
        binding.ccp.setDefaultCountryUsingNameCode("cz")
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val countryCode: String = "+" + binding.ccp.selectedCountryCode.toString()
                val phoneNumber = binding.etPhoneNumber.text.toString().trim { it <= ' ' }
                if (countryCode.isNotEmpty() && phoneNumber.isNotEmpty()) {
                    val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.createInstance(this@PhoneLogInActivity)

                    val isoCode = phoneNumberUtil.getRegionCodeForCountryCode(countryCode.toInt())

                    val exampleNumber =
                        phoneNumberUtil.getExampleNumber(isoCode).nationalNumber.toString()

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
        binding.txtLoginButton.setOnClickListener {
            mobileNumber = "+" + binding.ccp.selectedCountryCode + binding.etPhoneNumber.text.toString()
            if (phoneValid) {
             binding.progressBarMenu.show()
              //  authViewModel.sendMobileOtp("", mobileNumber!!)
                sendMobileOtp(mobileNumber!!)
            } else {
                PrefUtils.instance.alertDialog(
                    this@PhoneLogInActivity,
                    getString(R.string.app_name),
                    "Phone Number is Invalid $mobileNumber",
                    okAlert = {
                        it.dismiss()
                    }, cancelAlert = {
                        it.dismiss()
                    },
                    false
                )
            }
        }
        binding.imgEmailLogin.setOnClickListener {
            startActivity(Intent(this@PhoneLogInActivity,EmailLoginActivity::class.java))
        }
        binding.ccp.setOnClickListener {
            binding.ccp.setCountryCode
        }
        binding.imgGmailLogin.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this@PhoneLogInActivity, gso)
            startActivityForResult(googleSignInClient.signInIntent, requestCodeGmailLogin)
        }
        notificationPermission()

        when(intent.getStringExtra("isFrom")){
            "GuestHome"->{
                binding.skipBtn.hide()
                binding.backButton.show()
            }
            "GuestCart"->{
                binding.skipBtn.hide()
                binding.backButton.hide()
            }
        }
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.skipBtn.setOnClickListener {
            PrefUtils.instance.setString(Constants.IsLogin, "false")
            val intent = Intent(this@PhoneLogInActivity,SavePrefActivity::class.java)
            intent.putExtra("isFrom","Guest")
            startActivity(intent)

        }
        binding.txtPrivacyPolicy.paintFlags =  binding.txtPrivacyPolicy.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.txtTermService.paintFlags =  binding.txtTermService.paintFlags or Paint.UNDERLINE_TEXT_FLAG



        binding.txtTermService.setOnClickListener {
            openLinkInBrowser("https://svaggy.com/terms-conditions")

        }
        binding.txtPrivacyPolicy.setOnClickListener {
            openLinkInBrowser("https://svaggy.com/data-protection-terms")
        }



    }

    private fun openLinkInBrowser(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }
    private fun sendMobileOtp(mobileNumber: String) {
        val map = mapOf(
            "mobileNumber" to mobileNumber
        )
        lifecycleScope.launch {
            authViewModel.sendMobileOtp(token = "", map =map).collect{res ->
                when (res) {
                    is ApiResponse.Loading -> {
                        binding.progressBarMenu.show()

                    }
                    is ApiResponse.Success -> {
                        if (res.data?.isSuccess == true){
                            PrefUtils.instance.setString(Constants.EmailOrPhone, "+" + binding.ccp.selectedCountryCode + binding.etPhoneNumber.text.toString())
                            val intent = Intent(this@PhoneLogInActivity,VerifyPhoneActivity::class.java)
                            intent.putExtra(Constants.NUM,mobileNumber)
                            startActivity(intent)

                        }
                        binding.progressBarMenu.hide()

                    }
                    is ApiResponse.Failure -> {
                        binding.progressBarMenu.hide()
                        Toast.makeText(this@PhoneLogInActivity, "${res.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }


        }

    }
    private fun notificationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_DENIED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }else{
            PrefUtils.instance.setBoolean(Constants.IS_NOTIFICATION,true)
        }

    }

    private fun initObserver() {
        authViewModel.sendMobileOtpLiveData.observe(this) {
            if (it.isSuccess!!) {
            binding.progressBarMenu.hide()
                Toast.makeText(this, "Otp Send Successful", Toast.LENGTH_SHORT).show()
                PrefUtils.instance.setString(Constants.Type, "phone_number")
                PrefUtils.instance.setString(Constants.EmailOrPhone, "+" + binding.ccp.selectedCountryCode + binding.etPhoneNumber.text.toString())


                startActivity(Intent(this,VerifyPhoneActivity::class.java))
            } else {
              binding.progressBarMenu.hide()
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
//        authViewModel.otpResponseLiveData.observe(this) {
//            if (it.isSuccess!!) {
//                PrefUtils.instance.setString(Constants.EmailOrPhone, email)
//                PrefUtils.instance.setString(Constants.Token, it.data?.userDetails?.accessToken)
//                PrefUtils.instance.setString(Constants.UserId, it.data?.userDetails?.id.toString())
//                PrefUtils.instance.setString(Constants.UserFirstName, it.data?.userDetails?.firstName)
//                PrefUtils.instance.setString(Constants.UserLastName, it.data?.userDetails?.lastName)
//                PrefUtils.instance.setString(Constants.UserEmail, it.data?.userDetails?.email)
//                PrefUtils.instance.setString(Constants.stripeKey, it.data?.userDetails?.configuration?.stripeKey)
//                PrefUtils.instance.setString(Constants.GOOGLE_MAP_KEY, it.data?.userDetails?.configuration?.googleMapKey)
//                PrefUtils.instance.setString(Constants.PUSHER_KEY, it.data?.userDetails?.configuration?.PUSHER_KEY)
//                PrefUtils.instance.setString(Constants.PUSHER_CLUSTER, it.data?.userDetails?.configuration?.PUSHER_CLUSTER)
//                PrefUtils.instance.setString(Constants.UserMobile, it.data?.userDetails?.phoneNumber)
//                if (it.data?.userDetails?.currentLanguage.equals("en")){
//                    PrefUtils.instance.setLang(Constants.currentLocale,"en")
//                }else{
//                    PrefUtils.instance.setLang(Constants.currentLocale,"cs")
//
//                }
//                PrefUtils.instance.setString(Constants.IsLogin, "true")
//
//                if (it.data?.userDetails?.preferences?.size!! > 0) {
//                    startActivity(Intent(this,MainActivity::class.java))
//                    finish()
//                } else {
//                    startActivity(Intent(this,SavePrefActivity::class.java))
//                    finish()
//
//                }
//              binding.progressBarMenu.hide()
//            } else {
//              binding.progressBarMenu.hide()
//                Toast.makeText(this, "Sign In Failed!", Toast.LENGTH_SHORT).show()
//            }
//        }
        authViewModel.otpResponseLiveData.observe(this) {
            if (it.isSuccess!!) {
                binding.progressBarMenu.hide()
                PrefUtils.instance.setString(Constants.Token, it.data?.userDetails?.accessToken)
                PrefUtils.instance.setString(Constants.UserId, it.data?.userDetails?.id.toString())
                PrefUtils.instance.setString(Constants.UserFirstName, it.data?.userDetails?.firstName)
                PrefUtils.instance.setString(Constants.UserLastName, it.data?.userDetails?.lastName)
                PrefUtils.instance.setString(Constants.UserEmail, it.data?.userDetails?.email)
                PrefUtils.instance.setString(Constants.UserMobile, it.data?.userDetails?.phoneNumber)
                PrefUtils.instance.setString(Constants.IsGuestUser,"false")
                PrefUtils.instance.setString(Constants.stripeKey, it.data?.userDetails?.configuration?.stripeKey)
                PrefUtils.instance.setString(Constants.GOOGLE_MAP_KEY, it.data?.userDetails?.configuration?.googleMapKey)
                PrefUtils.instance.setString(Constants.PUSHER_KEY, it.data?.userDetails?.configuration?.PUSHER_KEY)
                PrefUtils.instance.setString(Constants.PUSHER_CLUSTER, it.data?.userDetails?.configuration?.PUSHER_CLUSTER)
                if (it.data?.userDetails?.currentLanguage.equals("en")){
                    PrefUtils.instance.setLang(Constants.currentLocale,"en")
                }else{
                    PrefUtils.instance.setLang(Constants.currentLocale,"cs")

                }
                PrefUtils.instance.setString(Constants.IsLogin, "true")
                //bundle.putString("cartBundle", "CartMoreItemScreen")

                setToken()
                if (it.data?.userDetails?.preferences?.size!! > 0) {
                    startActivity(Intent(this,MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }else{
                    startActivity(Intent(this,SavePrefActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()


                }

            } else {
                binding.progressBarMenu.hide()
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        }
        authViewModel.errorMessage.observe(this) {
           binding.progressBarMenu.hide()
            Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
        }
    }


    private fun setToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                PrefUtils.instance.setString(Constants.DeviceToken, task.result).toString()
                val map = mapOf("device_token" to PrefUtils.instance.getString(Constants.DeviceToken).toString())
                lifecycleScope.launch {
                    authViewModel.setToken(
                        "Bearer ${
                            PrefUtils.instance.getString(Constants.Token).toString()
                        }", map
                    ).collect {
                        when (it) {
                            is ApiResponse.Loading -> {

                            }

                            is ApiResponse.Success -> {

                            }

                            is ApiResponse.Failure -> {

                            }

                        }

                    }

                }
            }
        }





    }









    private fun checkGooglePlayServices(): Boolean {
        val status =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        return status == ConnectionResult.SUCCESS
    }



    override fun onCountrySelected(selectedCountry: Country?) {

    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCodeGmailLogin) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                // Handle the signed-in user here
                email = account.email
                val givenName = account.givenName
                val familyName = account.familyName
                val id = account.id

              binding.progressBarMenu.show()
                authViewModel.socialLogin(
                    email.toString(),
                    givenName.toString(),
                    familyName.toString(),
                    id.toString(),
                    "google"
                )
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Login failed due to an unexpected problem, please try again later.", Toast.LENGTH_LONG).show()
        }
    }


}