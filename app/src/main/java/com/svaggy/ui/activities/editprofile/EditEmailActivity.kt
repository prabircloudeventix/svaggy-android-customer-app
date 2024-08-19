package com.svaggy.ui.activities.editprofile

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityEditEmailBinding
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
class EditEmailActivity : BaseActivity<ActivityEditEmailBinding>(ActivityEditEmailBinding::inflate) {
    private val authViewModel by viewModels<AuthViewModel>()
    private var emailAddress: String?=null



    override fun ActivityEditEmailBinding.initialize(){
        updateStatusBarColor("#F6F6F6",true)
        binding.edtEmail.visibility = View.VISIBLE
        binding.btnSendOtp.setOnClickListener {
            emailAddress = binding.edtEmail.text.toString().trim()
            val validationResult = validateLoginInput()
            if (validationResult.first) {
                updateEmail(emailAddress!!)
            }
        }
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        binding.edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.errorMsg.hide()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })




    }
    private fun updateEmail(emailAddress: String) {
        val map = mapOf(
            "email" to emailAddress,
            "type" to "UPDATE"
        )
        lifecycleScope.launch {
            authViewModel.updateEmail(token = "${Constants.BEARER} ${
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
                            if (data.isSuccess == true) {
                                val  intent = Intent(this@EditEmailActivity,VerifyEmailOtpActivity::class.java)
                                intent.putExtra("email",binding.edtEmail.text.toString().trim())
                                startActivity(intent)
                                finish()
                               // val routes = EditEmailFragmentDirections.actionEditEmailFragmentToVerifyEmailFragment(email = binding.edtEmail.text.toString().trim())
                             //   findNavController().navigate(routes)
                            }
                            else {
                                binding.errorMsg.show()
                                binding.errorMsg.text = data.message.toString()
                                //Toast.makeText(this@EditEmailActivity, "" + data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                    is ApiResponse.Failure -> {
                        binding.progressBarMenu.hide()
                        Toast.makeText(this@EditEmailActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }

    private fun validateLoginInput(): Pair<Boolean, String> {
        emailAddress=binding.edtEmail.text.toString()
        return authViewModel.loginValidation(emailAddress!!)
    }

}