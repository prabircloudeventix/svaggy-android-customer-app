package com.svaggy.ui.fragments.profile.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.livechatinc.inappchat.ChatWindowActivity
import com.livechatinc.inappchat.ChatWindowConfiguration
import com.svaggy.R
import com.svaggy.databinding.FragmentProfileScreenBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.address.AddressActivity
import com.svaggy.ui.activities.auth.PhoneLogInActivity
import com.svaggy.ui.activities.editprofile.EditProfileActivity
import com.svaggy.ui.activities.odercollection.OrderCollectionActivity
import com.svaggy.ui.activities.payment.PaymentCardActivity
import com.svaggy.ui.activities.settings.AppSettingActivity
import com.svaggy.ui.activities.settings.DeleteAccountActivity
import com.svaggy.ui.activities.settings.HelpActivity
import com.svaggy.ui.activities.wallet.AllWalletActivity
import com.svaggy.ui.fragments.cart.viewmodel.CartViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileScreen : Fragment() {
    lateinit var binding: FragmentProfileScreenBinding
    private var mAuth: FirebaseAuth? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val mViewModel by viewModels<CartViewModel>()




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileScreenBinding.inflate(inflater, container, false)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        mAuth = FirebaseAuth.getInstance()
        return binding.root
    }





    private fun initChatWindow(isAddMail:Boolean) {
        if (isAddMail){
            val config = ChatWindowConfiguration.Builder()
               // .setLicenceNumber("18087297")
                .setLicenceNumber("18238530")
                .setVisitorName("${PrefUtils.instance.getString(Constants.UserFirstName)} ${PrefUtils.instance.getString(Constants.UserLastName)}")
                .setVisitorEmail(PrefUtils.instance.getString(Constants.UserEmail))
                .build()
            startChatActivity(config)

        }else{
            val config = ChatWindowConfiguration.Builder()
              //  .setLicenceNumber("18087297")
                 .setLicenceNumber("18238530")
                .setVisitorName(" ")
                .setVisitorEmail(" ")
                .build()
             startChatActivity(config)

        }



    }

    private fun startChatActivity(config: ChatWindowConfiguration) {
        val intent = Intent(requireContext(), ChatWindowActivity::class.java)
        intent.putExtras(config.asBundle())
        startActivity(intent)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.homeFragment)
                }
            })

        if (!PrefUtils.instance.getString(Constants.UserFirstName).isNullOrEmpty()) {
            binding.userName.text = "${PrefUtils.instance.getString(Constants.UserFirstName)} ${
                PrefUtils.instance.getString(Constants.UserLastName)
            }"
        } else {
            binding.userName.text = PrefUtils.instance.getString(Constants.UserMobile) ?: ""
        }
        binding.consChat.setOnClickListener {
            if (!PrefUtils.instance.getString(Constants.UserEmail).isNullOrEmpty()){
                initChatWindow(isAddMail = true)
            }else{
                initChatWindow(isAddMail = false)
            }
        }

        binding.consProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            intent.putExtra("isFrom", "ProfileScreen")
            startActivityForResult(intent, 101)
        }

        binding.consOrder.setOnClickListener {
            startActivity(Intent(requireContext(), OrderCollectionActivity::class.java))
        }

        binding.consAddress.setOnClickListener {
            startActivity(Intent(requireContext(), AddressActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            mAuth?.signOut()
            mGoogleSignInClient?.signOut()

            PrefUtils.instance.setString(Constants.IsLogin, "false")
            userLogOut()
            PrefUtils.instance.setString(Constants.AddressId, "")
            PrefUtils.instance.setString(Constants.Address, "")
            PrefUtils.instance.setString(Constants.UserFirstName, "")
            PrefUtils.instance.setString(Constants.UserEmail,"")
            PrefUtils.instance.setString(Constants.UserLastName, "")
            PrefUtils.instance.setString(Constants.Type, "")
            PrefUtils.instance.setString(Constants.FragmentBackName, "")
            PrefUtils.instance.setString(Constants.BackPressHandel, "")
            PrefUtils.instance.setString(Constants.IsNotFirstOpen, "")
            PrefUtils.instance.setString(Constants.AddOnsArray, "")
            PrefUtils.instance.setString(Constants.CartRestaurantId, "")
            PrefUtils.instance.setString(Constants.MenutRestaurantId, "")
            PrefUtils.instance.setString(Constants.CurrentDestinationId, "")
            PrefUtils.instance.setString("IS_PROMO", "")
           // (activity as MainActivity).addressList = arrayListOf()
            PrefUtils.instance.clearEditor()

            (activity as MainActivity).startActivity(
                Intent(requireContext(), PhoneLogInActivity::class.java)
            )
            (activity as MainActivity).finish()

        }


        binding.consPayment.setOnClickListener {
            startActivity(Intent(requireContext(), PaymentCardActivity::class.java))
        }

        binding.consWallet.setOnClickListener {
            startActivity(Intent(requireContext(), AllWalletActivity::class.java))

        }

        binding.consSetting.setOnClickListener {
            startActivity(Intent(requireContext(), AppSettingActivity::class.java))
        }

        binding.consHelp.setOnClickListener {
            startActivity(Intent(requireContext(), HelpActivity::class.java))
        }

        val type = arguments?.getString("type")
        if (type != null) {
            if (type.isNotEmpty()) {
                val action = ProfileScreenDirections.actionProfileScreenToOrdersScreen(type)
                findNavController().navigate(action)
            }
        }

        binding.btnDeleteAccount.setOnClickListener {
            startActivity(Intent(requireContext(), DeleteAccountActivity::class.java))
        }
    }


    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!PrefUtils.instance.getString(Constants.UserFirstName).isNullOrEmpty()) {
            binding.userName.text = "${PrefUtils.instance.getString(Constants.UserFirstName)} ${
                PrefUtils.instance.getString(Constants.UserLastName)
            }"
        } else {
            binding.userName.text = PrefUtils.instance.getString(Constants.UserMobile) ?: ""
        }
    }

  private  fun unsubscribeFromFCMTopics() {
        FirebaseMessaging.getInstance().deleteToken()
    }
    private fun userLogOut(){
        val json = JsonObject()
        json.addProperty("device_token",   PrefUtils.instance.getString(Constants.DeviceToken))
        lifecycleScope.launch {
            mViewModel.userLogout(authToken = "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                json = json).collect{

                when (it) {
                    is ApiResponse.Loading -> {
                    }
                    is ApiResponse.Success -> {
                        unsubscribeFromFCMTopics()
                        PrefUtils.instance.setString(Constants.Token, "")
                    }
                    is ApiResponse.Failure -> {


                    }

                }


            }

        }


    }

}


