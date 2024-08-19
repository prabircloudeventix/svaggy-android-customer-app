package com.svaggy.ui.activities.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityDeleteAccountBinding
import com.svaggy.databinding.FragmentAccountDeleteBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.auth.PhoneLogInActivity
import com.svaggy.ui.fragments.profile.adapter.ReasonListAdapter
import com.svaggy.ui.fragments.profile.viewmodel.ProfileViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class DeleteAccountActivity : BaseActivity<ActivityDeleteAccountBinding>(ActivityDeleteAccountBinding::inflate) {
    private val mViewModel by viewModels<ProfileViewModel>()
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
    private lateinit var reasonListAdapter: ReasonListAdapter
    private var getReason:String? = null

    override fun ActivityDeleteAccountBinding.initialize(){
        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this@DeleteAccountActivity, gso)
        getReason()
        binding.btnDeleteAccount.setOnClickListener {
            if (getReason != null)
                deleteAccount()
            else
                binding.root.showSnackBar("Please select at least one reason")
        }
       binding.backButton.setOnClickListener {
          onBackPressed()
        }

    }

    private fun getReason(){
        lifecycleScope.launch {
            mViewModel.deleteAccountReason(authToken = "${Constants.BEARER} ${
                PrefUtils.instance.getString(
                    Constants.Token).toString()}",
                reasonFor = "USER", reasonType = "ACCOUNT_DEACTIVATION").collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        //     progressBarHelper.showProgressBar()
                    }
                    is ApiResponse.Success -> {
                        //     progressBarHelper.dismissProgressBar()
                        val data = it.data
                        if (data != null){
                            if (data.is_success!!) {
                                reasonListAdapter = ReasonListAdapter(context = this@DeleteAccountActivity,
                                    reasonList = data.data!!,
                                    userSelection = {reason ->
                                        getReason = reason
                                    })
                                binding.rcView.adapter = reasonListAdapter
                            }
                        }
                    }
                    is ApiResponse.Failure -> {
                        //   progressBarHelper.dismissProgressBar()
                        Toast.makeText(this@DeleteAccountActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }
    }

    private fun deleteAccount(){
        val map = mapOf(
            "deactivation_reason" to getReason!!
        )
        lifecycleScope.launch {
            mViewModel.deleteMe(authToken = "${Constants.BEARER} ${
                PrefUtils.instance.getString(
                    Constants.Token).toString()}",
                map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        //       progressBarHelper.showProgressBar()
                    }
                    is ApiResponse.Success -> {
                        //     progressBarHelper.dismissProgressBar()
                        val data = it.data
                        if (data != null){
                            if (data.isSuccess!!) {
                                mAuth?.signOut()
                                mGoogleSignInClient?.signOut()
                                PrefUtils.instance.clearEditor()
                                val intent = Intent(this@DeleteAccountActivity,PhoneLogInActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()

                            }
                        }
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(this@DeleteAccountActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }

}