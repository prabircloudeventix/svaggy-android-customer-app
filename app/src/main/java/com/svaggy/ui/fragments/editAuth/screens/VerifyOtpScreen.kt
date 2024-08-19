package com.svaggy.ui.fragments.editAuth.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.svaggy.R
import com.svaggy.databinding.FragmentVerifyOtpScreenBinding
import com.svaggy.ui.activities.MainActivity
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
class VerifyOtpScreen : Fragment() {
    lateinit var binding: FragmentVerifyOtpScreenBinding
    private var allOtpAdd: String? = null
    private val authViewModel by viewModels<AuthViewModel>()
    val bundle = Bundle()
    private lateinit var userNum:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().updateStatusBarColor("#F6F6F6", true)
        binding = FragmentVerifyOtpScreenBinding.inflate(inflater, container, false)
      //  PrefUtils.instance.setString(Constants.CurrentDestinationId, (activity as MainActivity).navController.currentDestination?.id.toString())
        PrefUtils.instance.setString(Constants.FragmentBackName, "VerifyOtpScreen")
        userNum = arguments?.getString("number").toString()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (arguments?.getString("isFrom") == "EditProfileEmail") {
                        findNavController().popBackStack(R.id.emailVerifyOTP, true)
                    } else if (arguments?.getString("isFrom") == "EditProfileMobile") {
                        findNavController().popBackStack(R.id.emailVerifyOTP, true)
                    } else {
                      //  findNavController().navigate(R.id.loginPhnNo)
                    }
                }
            }
            )

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
            if (PrefUtils.instance.getString(Constants.Type) == "phone_number") {
                findNavController().popBackStack()
            } else if (PrefUtils.instance.getString(Constants.Type) == "email") {
                findNavController().popBackStack()
            }
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

        addTextWatcher()
    }

    private fun addTextWatcher() {
        binding.otpOne.addTextChangedListener(
            GenericTextWatcher(
                requireActivity(),
                requireContext(),
                binding,
                binding.otpOne,
                binding.otpTwo
            )
        )
        binding.otpTwo.addTextChangedListener(
            GenericTextWatcher(
                requireActivity(),
                requireContext(),
                binding,
                binding.otpTwo,
                binding.otpThree
            )
        )
        binding.otpThree.addTextChangedListener(
            GenericTextWatcher(
                requireActivity(),
                requireContext(),
                binding,
                binding.otpThree,
                binding.otpFour
            )
        )
        binding.otpFour.addTextChangedListener(
            GenericTextWatcher(
                requireActivity(),
                requireContext(),
                binding,
                binding.otpFour,
                binding.otpFive
            )
        )
        binding.otpFive.addTextChangedListener(
            GenericTextWatcher(
                requireActivity(),
                requireContext(),
                binding,
                binding.otpFive,
                binding.otpSix
            )
        )
        binding.otpSix.addTextChangedListener(
            GenericTextWatcher(
                requireActivity(),
                requireContext(),
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
        private val activity: FragmentActivity,
        private val context: Context,
        private val binding: FragmentVerifyOtpScreenBinding,
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
                        context!!,
                        R.color.light_gray
                    )
                )
                val f: NumberFormat = DecimalFormat("00")
                val min = millisUntilFinished / 60000 % 60
                val sec = millisUntilFinished / 1000 % 60
                binding.timer.text = (f.format(min)) + ":" + f.format(sec)
                binding.timer.setTextColor(ContextCompat.getColor(context!!, R.color.primaryColor))
            }

            override fun onFinish() {
                binding.btnResend.isClickable = true
                binding.btnResend.setTextColor(
                    ContextCompat.getColor(
                        context!!,
                        R.color.primaryColor
                    )
                )
                binding.timer.text = getString(R.string.end_timer_value)
                binding.timer.setTextColor(ContextCompat.getColor(context!!, R.color.light_gray))
            }
        }.start()
    }

    private fun runObserver() {
        authViewModel.sendMobileOtpLiveData.observe(viewLifecycleOwner) {
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
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.otpResponseLiveData.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
             binding.progressBarMenu.hide()
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
                PrefUtils.instance.setString(Constants.Token, it.data?.userDetails?.accessToken)
                PrefUtils.instance.setString(Constants.UserId, it.data?.userDetails?.id.toString())
                PrefUtils.instance.setString(Constants.UserFirstName, it.data?.userDetails?.firstName)
                PrefUtils.instance.setString(Constants.UserLastName, it.data?.userDetails?.lastName)
                PrefUtils.instance.setString(Constants.UserEmail, it.data?.userDetails?.email)
                PrefUtils.instance.setString(Constants.UserMobile, it.data?.userDetails?.phoneNumber)
                Toast.makeText(requireContext(),it.data?.userDetails?.configuration?.publishableKey.toString(),Toast.LENGTH_SHORT).show()
                PrefUtils.instance.setString(Constants.PK_Test, it.data?.userDetails?.configuration?.publishableKey)
                PrefUtils.instance.setString(Constants.IsLogin, "true")
                findNavController().popBackStack(R.id.profileScreen,false)
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
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
        authViewModel.errorMessage.observe(viewLifecycleOwner) {
           binding.progressBarMenu.hide()
            Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}