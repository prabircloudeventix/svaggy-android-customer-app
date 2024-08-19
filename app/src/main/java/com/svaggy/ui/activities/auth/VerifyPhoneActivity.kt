package com.svaggy.ui.activities.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityVerifyPhoneBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.savePref.SavePrefActivity
import com.svaggy.ui.fragments.editAuth.screens.VerifyOtpScreen
import com.svaggy.ui.fragments.editAuth.viewmodel.AuthViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import com.svaggy.utils.updateStatusBarColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat

@AndroidEntryPoint
class VerifyPhoneActivity : BaseActivity<ActivityVerifyPhoneBinding>(ActivityVerifyPhoneBinding::inflate)  {
    private var allOtpAdd: String? = null
    private val authViewModel by viewModels<AuthViewModel>()
    private lateinit var num:String






    override fun ActivityVerifyPhoneBinding.initialize() {
        this@VerifyPhoneActivity.updateStatusBarColor("#F6F6F6", true)
        binding.otpContinue.isClickable = false
        runObserver()
        num = intent.getStringExtra(Constants.NUM).toString()
        binding.txtLoginEdit.text = num
        binding.txtOtpText.text = getString(R.string.otp_msg_phone)

        binding.imgLoginEdit.setOnClickListener {
            finish()

        }
        binding.btnResend.setOnClickListener {
            resendOtp(num)

        }

        binding.otpContinue.setOnClickListener {
            if (!binding.otpOne.text.isNullOrEmpty() &&
                !binding.otpTwo.text.isNullOrEmpty() &&
                !binding.otpThree.text.isNullOrEmpty() &&
                !binding.otpFour.text.isNullOrEmpty() &&
                !binding.otpFive.text.isNullOrEmpty() &&
                !binding.otpSix.text.isNullOrEmpty()
            ) {
                binding.otpContinue.isClickable = true
              binding.progressBarMenu.show()
                allOtpAdd = binding.otpOne.text.toString() +
                        binding.otpTwo.text.toString() +
                        binding.otpThree.text.toString() +
                        binding.otpFour.text.toString() +
                        binding.otpFive.text.toString() +
                        binding.otpSix.text.toString()


                if (PrefUtils.instance.getString(Constants.IsGuestUser) == "true"){
                    authViewModel.verifyOtp(
                        "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                        allOtpAdd ?: "",
                        (PrefUtils.instance.getString(Constants.Type) ?: ""),
                        (PrefUtils.instance.getString(Constants.EmailOrPhone) ?: "")
                    )

                }else{
                    authViewModel.verifyOtp(
                        "",
                        allOtpAdd ?: "",
                        (PrefUtils.instance.getString(Constants.Type) ?: ""),
                        (PrefUtils.instance.getString(Constants.EmailOrPhone) ?: ""))

                }



            } else {
                binding.otpContinue.isClickable = false
            }
        }

        addTextWatcher()


    }
    private fun resendOtp(mobileNumber: String) {
        val map = mapOf("mobileNumber" to mobileNumber)
        lifecycleScope.launch {
            authViewModel.reSendMobileOtp(map =map).collect{res ->
                when (res) {
                    is ApiResponse.Loading -> {
                        binding.progressBarMenu.show()

                    }
                    is ApiResponse.Success -> {
                        if (res.data?.isSuccess == true){
                            timer()
                            binding.otpOne.isFocusableInTouchMode = true
                            binding.otpOne.requestFocus()
                            binding.otpOne.setText("")
                            binding.otpTwo.setText("")
                            binding.otpThree.setText("")
                            binding.otpFour.setText("")
                            binding.otpFive.setText("")
                            binding.otpSix.setText("")
                        }
                        binding.progressBarMenu.hide()

                    }
                    is ApiResponse.Failure -> {
                        binding.progressBarMenu.hide()
                        Toast.makeText(this@VerifyPhoneActivity, "${res.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }


        }

    }
    private fun addTextWatcher() {
        binding.otpOne.addTextChangedListener(
            GenericTextWatcher(
               this,
                this,
                binding,
                binding.otpOne,
                binding.otpTwo
            )
        )
        binding.otpTwo.addTextChangedListener(
          GenericTextWatcher(
               this,
               this,
                binding,
                binding.otpTwo,
                binding.otpThree
            )
        )
        binding.otpThree.addTextChangedListener(
            GenericTextWatcher(
               this,
                this,
                binding,
                binding.otpThree,
                binding.otpFour
            )
        )
        binding.otpFour.addTextChangedListener(
            GenericTextWatcher(
               this,
               this,
                binding,
                binding.otpFour,
                binding.otpFive
            )
        )
        binding.otpFive.addTextChangedListener(
            GenericTextWatcher(
               this,
               this,
                binding,
                binding.otpFive,
                binding.otpSix
            )
        )
        binding.otpSix.addTextChangedListener(
           GenericTextWatcher(
               this,
               this,
                binding,
                binding.otpSix,
                null
            )
        )

        binding.otpOne.setOnKeyListener(VerifyOtpScreen.GenericKeyEvent(binding.otpOne, null))
        binding.otpTwo.setOnKeyListener(
            VerifyOtpScreen.GenericKeyEvent(
                binding.otpTwo,
                binding.otpOne
            )
        )
        binding.otpThree.setOnKeyListener(
            VerifyOtpScreen.GenericKeyEvent(
                binding.otpThree,
                binding.otpTwo
            )
        )
        binding.otpFour.setOnKeyListener(
            VerifyOtpScreen.GenericKeyEvent(
                binding.otpFour,
                binding.otpThree
            )
        )
        binding.otpFive.setOnKeyListener(
            VerifyOtpScreen.GenericKeyEvent(
                binding.otpFive,
                binding.otpFour
            )
        )
        binding.otpSix.setOnKeyListener(
            VerifyOtpScreen.GenericKeyEvent(
                binding.otpSix,
                binding.otpFive
            )
        )
    }



    class GenericTextWatcher internal constructor(
        private val activity: FragmentActivity,
        private val context: Context,
        private val binding: ActivityVerifyPhoneBinding,
        private val currentView: View,
        private val nextView: View?
    ) :
        TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            val text = editable.toString()
            when (currentView.id) {
                R.id.otp_one -> if (text.length == 1) {
                    currentView.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_otp_fil)
                    nextView!!.requestFocus()
                } else {
                    currentView.background =
                        ContextCompat.getDrawable(context, R.drawable.otp_curve)
                }

                R.id.otp_two -> if (text.length == 1) {
                    currentView.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_otp_fil)
                    nextView!!.requestFocus()
                } else {
                    currentView.background =
                        ContextCompat.getDrawable(context, R.drawable.otp_curve)
                }

                R.id.otp_three -> if (text.length == 1) {
                    currentView.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_otp_fil)
                    nextView!!.requestFocus()
                } else {
                    currentView.background =
                        ContextCompat.getDrawable(context, R.drawable.otp_curve)
                }

                R.id.otp_four -> if (text.length == 1) {
                    currentView.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_otp_fil)
                    nextView!!.requestFocus()
                } else {
                    currentView.background =
                        ContextCompat.getDrawable(context, R.drawable.otp_curve)
                }

                R.id.otp_five -> if (text.length == 1) {
                    currentView.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_otp_fil)
                    nextView!!.requestFocus()
                } else {
                    currentView.background =
                        ContextCompat.getDrawable(context, R.drawable.otp_curve)
                }

                R.id.otp_six -> if (text.length == 1) {
                    currentView.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_otp_fil)
                    val inputManager =
                        context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(
                        activity.currentFocus!!.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                } else {
                    currentView.background =
                        ContextCompat.getDrawable(context, R.drawable.otp_curve)
                }
            }
            otpcheck()
        }

        private fun otpcheck() {
            if (!binding.otpOne.text.isNullOrEmpty() &&
                !binding.otpTwo.text.isNullOrEmpty() &&
                !binding.otpThree.text.isNullOrEmpty() &&
                !binding.otpFour.text.isNullOrEmpty() &&
                !binding.otpFive.text.isNullOrEmpty() &&
                !binding.otpSix.text.isNullOrEmpty()
            ) {
                binding.otpContinue.isClickable = true
                binding.otpContinue.background =
                    ContextCompat.getDrawable(context, R.drawable.continue_bt)
            } else {
                binding.otpContinue.isClickable = false
                binding.otpContinue.background =
                    ContextCompat.getDrawable(context, R.drawable.line_bg)
            }
        }

        override fun beforeTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) {
        }

        override fun onTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) {
        }

    }

    override fun onResume() {
        super.onResume()
        timer()
    }

    private fun timer() {
        object : CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                binding.btnResend.isClickable = false
                binding.btnResend.setTextColor(

                    ContextCompat.getColor(
                       this@VerifyPhoneActivity,
                        R.color.light_gray
                    )
                )
                val f: NumberFormat = DecimalFormat("00")
                val min = millisUntilFinished / 60000 % 60
                val sec = millisUntilFinished / 1000 % 60
                binding.timer.text = (f.format(min)) + ":" + f.format(sec)
                binding.timer.setTextColor(ContextCompat.getColor(this@VerifyPhoneActivity, R.color.primaryColor))
            }

            override fun onFinish() {
                binding.btnResend.isClickable = true
                binding.btnResend.setTextColor(
                    ContextCompat.getColor(
                        this@VerifyPhoneActivity,
                        R.color.primaryColor
                    )
                )
                binding.timer.text = getString(R.string.end_timer_value)
                binding.timer.setTextColor(ContextCompat.getColor(this@VerifyPhoneActivity, R.color.light_gray))
            }
        }.start()
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





    private fun runObserver() {
        authViewModel.sendMobileOtpLiveData.observe(this) {
            if (it.isSuccess!!) {
                binding.progressBarMenu.hide()
                binding.otpOne.isFocusableInTouchMode = true
                binding.otpOne.requestFocus()
                binding.otpOne.setText("")
                binding.otpTwo.setText("")
                binding.otpThree.setText("")
                binding.otpFour.setText("")
                binding.otpFive.setText("")
                binding.otpSix.setText("")

            } else {
                binding.progressBarMenu.hide()
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }

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








}