package com.svaggy.ui.fragments.profile.screens

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.JsonObject
import com.svaggy.R
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
class AccountDeleteFragment : Fragment() {
    private var _binding:FragmentAccountDeleteBinding? = null
    private val binding get() = _binding!!
    private val mViewModel by viewModels<ProfileViewModel>()
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
    private lateinit var reasonListAdapter: ReasonListAdapter
    private var getReason:String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountDeleteBinding.inflate(inflater, container, false)
     //   progressBarHelper = ProgressBarHelper(requireContext())
        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = requireContext().getString(R.string.delete_account)
     //   (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility=View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getReason()
        binding.btnDeleteAccount.setOnClickListener {
            if (getReason != null)
                deleteAccount()
            else
                binding.root.showSnackBar("Please select at least one reason")
        }
        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun getReason(){
        lifecycleScope.launch {
            mViewModel.deleteAccountReason(authToken = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
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
                                reasonListAdapter = ReasonListAdapter(context = requireContext(),
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
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
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
            mViewModel.deleteMe(authToken = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
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
                                (activity as MainActivity).startActivity(
                                    Intent(requireContext(), PhoneLogInActivity::class.java)
                                )
                                (activity as MainActivity).finish()

                            }


                        }
                    }
                    is ApiResponse.Failure -> {
                    //    progressBarHelper.dismissProgressBar()
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }



}