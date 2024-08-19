package com.svaggy.ui.activities.auth

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityEmailLoginBinding
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
class EmailLoginActivity :  BaseActivity<ActivityEmailLoginBinding>(ActivityEmailLoginBinding::inflate) {
    private val authViewModel by viewModels<AuthViewModel>()
    private var emailAddress: String?=null




    override fun ActivityEmailLoginBinding.initialize() {
        this@EmailLoginActivity.updateStatusBarColor("#F6F6F6",true)
        PrefUtils.instance.setString(Constants.FragmentBackName, "LoginEmailScreen")
        binding.icBack.setOnClickListener {
            finish()
        }

        binding.btnSendOtp.setOnClickListener {
            emailAddress = binding.edtEmail.text.toString().trim()
            val validationResult = validateLoginInput()
            if (validationResult.first) {
                updateEmail(emailAddress!!)
            }
        }



    }
    private fun validateLoginInput(): Pair<Boolean, String> {
        emailAddress=binding.edtEmail.text.toString()
        return authViewModel.loginValidation(emailAddress!!)
    }

    private fun updateEmail(emailAddress: String) {
        val map = mapOf(
            "email" to emailAddress)
        lifecycleScope.launch {
            authViewModel.updateEmail(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                      binding.progressBarMenu.show()
                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        if (data != null){
                           binding.progressBarMenu.hide()
                            if (data.isSuccess!!) {
                                val intent = Intent(this@EmailLoginActivity,VerifyEmailActivity::class.java)
                                intent.putExtra("email",emailAddress)
                                startActivity(intent)
                            }
                            else {
                                Toast.makeText(this@EmailLoginActivity, "" + data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                    is ApiResponse.Failure -> {
                     binding.progressBarMenu.hide()
                        Toast.makeText(this@EmailLoginActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }
}