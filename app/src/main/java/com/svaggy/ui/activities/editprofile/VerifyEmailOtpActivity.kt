package com.svaggy.ui.activities.editprofile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityVerifyEmailOtpBinding
import com.svaggy.databinding.FragmentVerifyEmailBinding
import com.svaggy.ui.fragments.editAuth.screens.VerifyOtpScreen
import com.svaggy.ui.fragments.editAuth.viewmodel.AuthViewModel
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.onBackPressedDispatcher
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.text.NumberFormat

@AndroidEntryPoint
class VerifyEmailOtpActivity : BaseActivity<ActivityVerifyEmailOtpBinding>(ActivityVerifyEmailOtpBinding::inflate) {
    private val authViewModel by viewModels<AuthViewModel>()
    private var allOtpAdd: String? = null
    private lateinit var userMail:String


    override fun ActivityVerifyEmailOtpBinding.initialize(){
        userMail = intent.getStringExtra("email").toString()
        binding.txtLoginEdit.text = userMail
        timer()
        runObserver()


        binding.imgLoginEdit.setOnClickListener {
        onBackPressed()
        }
        binding.btnResend.setOnClickListener {
            authViewModel.reSendEmailOtp(email = userMail)
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
                authViewModel.verifyOtp(
                    token = "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                    otpString = allOtpAdd ?: "",
                    type = "email",
                    emailId = userMail
                )


            } else {
                binding.otpContinue.isClickable = false
            }
        }

        addTextWatcher()

    }
    private fun runObserver() {
        authViewModel.otpResponseLiveData.observe(this) {
            if (it.isSuccess!!) {
                binding.progressBarMenu.hide()
                PrefUtils.instance.setString(Constants.Token, it.data?.userDetails?.accessToken)
                PrefUtils.instance.setString(Constants.UserId, it.data?.userDetails?.id.toString())
                PrefUtils.instance.setString(Constants.UserFirstName, it.data?.userDetails?.firstName)
                PrefUtils.instance.setString(Constants.UserLastName, it.data?.userDetails?.lastName)
                PrefUtils.instance.setString(Constants.UserEmail, it.data?.userDetails?.email)
                PrefUtils.instance.setString(Constants.UserMobile, it.data?.userDetails?.phoneNumber)
//                    Toast.LENGTH_SHORT).show()
                PrefUtils.instance.setString(Constants.PK_Test, it.data?.userDetails?.configuration?.publishableKey)
                PrefUtils.instance.setString(Constants.IsLogin, "true")
               // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
              //  onBackPressed()
                //val intent = Intent(this, EditProfileActivity::class.java)
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                //startActivity(intent)
                startActivity(Intent(this,EditProfileActivity::class.java).
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).
                putExtra("isFrom","VerifyEmailOtpActivity"))
               finish()
               // findNavController().popBackStack(R.id.editProfile,false)

            } else {
                binding.progressBarMenu.hide()
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        }
        authViewModel.sendEmailOtpLiveData.observe(this){
            timer()
        }
        authViewModel.errorMessage.observe(this) {
            binding.progressBarMenu.hide()
            Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
        }
    }



    private fun timer() {
        object : CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                binding.btnResend.isClickable = false
                binding.btnResend.setTextColor(

                    ContextCompat.getColor(this@VerifyEmailOtpActivity, R.color.light_gray)
                )
                val f: NumberFormat = DecimalFormat("00")
                val min = millisUntilFinished / 60000 % 60
                val sec = millisUntilFinished / 1000 % 60
                binding.timer.text = (f.format(min)) + ":" + f.format(sec)
                binding.timer.setTextColor(ContextCompat.getColor(this@VerifyEmailOtpActivity, R.color.primaryColor))
            }

            override fun onFinish() {
                binding.btnResend.isClickable = true
                binding.btnResend.setTextColor(
                    ContextCompat.getColor(this@VerifyEmailOtpActivity, R.color.primaryColor)
                )
                binding.timer.text = getString(R.string.end_timer_value)
                binding.timer.setTextColor(ContextCompat.getColor(this@VerifyEmailOtpActivity, R.color.light_gray))
            }
        }.start()
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
        private val activity: Activity,
        private val context: Context,
        private val binding: ActivityVerifyEmailOtpBinding,
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
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
                !binding.otpSix.text.isNullOrEmpty()) {
                binding.otpContinue.isClickable = true
                binding.otpContinue.background = ContextCompat.getDrawable(context, R.drawable.continue_bt)
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


}