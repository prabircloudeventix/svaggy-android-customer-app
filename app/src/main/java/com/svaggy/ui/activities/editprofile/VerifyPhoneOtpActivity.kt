package com.svaggy.ui.activities.editprofile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityVerifyPhoneOtpBinding
import com.svaggy.databinding.FragmentVerifyOtpScreenBinding
import com.svaggy.ui.fragments.editAuth.viewmodel.AuthViewModel
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import com.svaggy.utils.updateStatusBarColor
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.text.NumberFormat

@AndroidEntryPoint
class VerifyPhoneOtpActivity : BaseActivity<ActivityVerifyPhoneOtpBinding>(ActivityVerifyPhoneOtpBinding::inflate) {
    private var allOtpAdd: String? = null
    private val authViewModel by viewModels<AuthViewModel>()
    val bundle = Bundle()
    private lateinit var userNum:String
    private  var timer: CountDownTimer? =null



    override fun  ActivityVerifyPhoneOtpBinding.initialize(){
       updateStatusBarColor("#F6F6F6", true)
        userNum = intent.getStringExtra("number").toString()
        binding.otpContinue.isClickable = false
        runObserver()

        binding.txtLoginEdit.text = userNum
        if (PrefUtils.instance.getString(Constants.Type) == "phone_number") {
            binding.txtOtpText.text = getString(R.string.otp_msg_phone)
        }
        else if (PrefUtils.instance.getString(Constants.Type) == "email") {
            binding.txtOtpText.text = getString(R.string.otp_msg_email)
        }

        binding.imgLoginEdit.setOnClickListener {
            onBackPressed()

        }
        binding.btnResend.setOnClickListener {
            binding.progressBarMenu.show()
            if (PrefUtils.instance.getString(Constants.Type) == "phone_number") {
                authViewModel.reSendMobileNewOtp(
                    PrefUtils.instance.getString(Constants.EmailOrPhone) ?: ""
                )
            } else if (PrefUtils.instance.getString(Constants.Type) == "email") {
                authViewModel.reSendEmailOtp(
                    PrefUtils.instance.getString(Constants.EmailOrPhone) ?: ""
                )
            }
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
                    "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                    allOtpAdd ?: "",
                    ("UPDATE"),
                    (userNum))
            } else {
                binding.otpContinue.isClickable = false
            }
        }
        timer()

        addTextWatcher()



    }
    private fun timer() {
     timer =  object : CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                binding.btnResend.isClickable = false
                binding.btnResend.setTextColor(

                    ContextCompat.getColor(
                       this@VerifyPhoneOtpActivity,
                        R.color.light_gray
                    )
                )
                val f: NumberFormat = DecimalFormat("00")
                val min = millisUntilFinished / 60000 % 60
                val sec = millisUntilFinished / 1000 % 60
                binding.timer.text = (f.format(min)) + ":" + f.format(sec)
                binding.timer.setTextColor(ContextCompat.getColor(this@VerifyPhoneOtpActivity, R.color.primaryColor))
            }

            override fun onFinish() {
                binding.btnResend.isClickable = true
                binding.btnResend.setTextColor(
                    ContextCompat.getColor(
                        this@VerifyPhoneOtpActivity,
                        R.color.primaryColor
                    )
                )
                binding.timer.text = getString(R.string.end_timer_value)
                binding.timer.setTextColor(ContextCompat.getColor(this@VerifyPhoneOtpActivity, R.color.light_gray))
            }
        }.start()
    }

    override fun onStop() {
        super.onStop()
        timer?.cancel()

    }

    private fun runObserver() {
        authViewModel.sendMobileOtpLiveData.observe(this) {
            if (it.isSuccess!!) {
                binding.otpOne.isFocusableInTouchMode = true
                binding.otpOne.requestFocus()
                binding.otpOne.setText("")
                binding.otpTwo.setText("")
                binding.otpThree.setText("")
                binding.otpFour.setText("")
                binding.otpFive.setText("")
                binding.otpSix.setText("")
                timer()
                binding.progressBarMenu.hide()
            } else {
                binding.progressBarMenu.hide()
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.otpResponseLiveData.observe(this) {
            if (it.isSuccess!!) {
                binding.progressBarMenu.hide()
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
                PrefUtils.instance.setString(Constants.Token, it.data?.userDetails?.accessToken)
                PrefUtils.instance.setString(Constants.UserId, it.data?.userDetails?.id.toString())
                PrefUtils.instance.setString(Constants.UserFirstName, it.data?.userDetails?.firstName)
                PrefUtils.instance.setString(Constants.UserLastName, it.data?.userDetails?.lastName)
                PrefUtils.instance.setString(Constants.UserEmail, it.data?.userDetails?.email)
                PrefUtils.instance.setString(Constants.UserMobile, it.data?.userDetails?.phoneNumber)
//                Toast.makeText(this,it.data?.userDetails?.configuration?.publishableKey.toString(),
//                    Toast.LENGTH_SHORT).show()
                PrefUtils.instance.setString(Constants.PK_Test, it.data?.userDetails?.configuration?.publishableKey)
                PrefUtils.instance.setString(Constants.IsLogin, "true")
              //  findNavController().popBackStack(R.id.profileScreen,false)
//                startActivity(Intent(this,EditProfileActivity::class.java).
//                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).
//                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).
//                putExtra("isFrom","VerifyEmailOtpActivity"))
//                finish()

               startActivity(Intent(this,EditProfileActivity::class.java)
                   .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                   .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
                //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                finish()
                //bundle.putString("cartBundle", "CartMoreItemScreen")
//                if (arguments?.getString("isFrom") == "EditProfileEmail") {
//                    if (arguments?.getString("cartBundle") == "CartMoreItemScreen") {
//                        bundle.putString("cartBundle", "CartMoreItemScreen")
//                    } else if (arguments?.getString("cartBundle") == "ProfileScreen") {
//                        bundle.putString("cartBundle", "ProfileScreen")
//                    }
//                    findNavController().navigate(R.id.action_emailVerifyOTP_to_editProfile, bundle)
//                } else if (arguments?.getString("isFrom") == "EditProfileMobile") {
//                    if (arguments?.getString("cartBundle") == "CartMoreItemScreen") {
//                        bundle.putString("cartBundle", "CartMoreItemScreen")
//                    } else if (arguments?.getString("cartBundle") == "ProfileScreen") {
//                        bundle.putString("cartBundle", "ProfileScreen")
//                    }
//                    findNavController().navigate(R.id.action_emailVerifyOTP_to_editProfile, bundle)
//                } else {
//                    if (it.data?.userDetails?.preferences?.size!! > 0) {
//                      //  findNavController().navigate(R.id.action_emailVerifyOTP_to_homeFragment)
//                    } else {
//                       // findNavController().navigate(R.id.action_emailVerifyOTP_to_preferencesFragment)
//                    }
//                }
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

        binding.otpOne.setOnKeyListener(GenericKeyEvent(binding.otpOne, null))
        binding.otpTwo.setOnKeyListener(GenericKeyEvent(binding.otpTwo, binding.otpOne))
        binding.otpThree.setOnKeyListener(GenericKeyEvent(binding.otpThree, binding.otpTwo))
        binding.otpFour.setOnKeyListener(GenericKeyEvent(binding.otpFour, binding.otpThree))
        binding.otpFive.setOnKeyListener(GenericKeyEvent(binding.otpFive, binding.otpFour))
        binding.otpSix.setOnKeyListener(GenericKeyEvent(binding.otpSix, binding.otpFive))
    }

    class GenericKeyEvent internal constructor(
        private val currentView: EditText,
        private val previousView: EditText?
    ) : View.OnKeyListener {
        override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
            if (event!!.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.id != R.id.otp_one && currentView.text.isEmpty()) {
                previousView!!.text = null
                previousView.requestFocus()
                return true
            }
            return false
        }
    }

    class GenericTextWatcher internal constructor(
        private val activity: Activity,
        private val context: Context,
        private val binding: ActivityVerifyPhoneOtpBinding,
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

}