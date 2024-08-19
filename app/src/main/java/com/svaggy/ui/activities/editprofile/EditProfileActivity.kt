package com.svaggy.ui.activities.editprofile

import android.annotation.SuppressLint
import android.content.Intent
import android.text.InputFilter
import android.widget.Toast
import androidx.activity.viewModels
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityEditProfileBinding
import com.svaggy.ui.fragments.profile.viewmodel.ProfileViewModel
import com.svaggy.utils.Constants
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileActivity : BaseActivity<ActivityEditProfileBinding>(ActivityEditProfileBinding::inflate) {

    private val mViewModel by viewModels<ProfileViewModel>()
    private lateinit var isFrom:String



    @SuppressLint("SuspiciousIndentation")
    override fun ActivityEditProfileBinding.initialize(){
        isFrom = intent.getStringExtra("isFrom").toString()
        initObserver()
        //  First Type Spacing Remove Automatic
        binding.edtFirstName.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtLastName.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtEmail.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtMobile.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
//        if (isFrom == "VerifyEmailOtpActivity"){
//            if (!PrefUtils.instance.getString("userFName").isNullOrEmpty()){
//                binding.edtFirstName.setText(PrefUtils.instance.getString("userFName").toString())
//            }
//            if (!PrefUtils.instance.getString("userLName").isNullOrEmpty()){
//                binding.edtLastName.setText(PrefUtils.instance.getString("userLName").toString())
//            }
//
//        }else{


     //   }

        binding.edtFirstName.setText(PrefUtils.instance.getString(Constants.UserFirstName) ?: "")
        binding.edtLastName.setText(PrefUtils.instance.getString(Constants.UserLastName) ?: "")







       binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.edtEmail.setOnClickListener {
            PrefUtils.instance.setString("userFName",binding.edtFirstName.text.toString())
            PrefUtils.instance.setString("userLName",binding.edtLastName.text.toString())
            startActivity(Intent(this@EditProfileActivity,EditEmailActivity::class.java))
          //  val routes = EditProfileScreenDirections.actionEditProfileToEditEmailFragment()
          //  findNavController().navigate(routes)

        }

        binding.edtMobile.setOnClickListener {
            startActivity(Intent(this@EditProfileActivity,EditPhoneNumberActivity::class.java))
       //     val routes = EditProfileScreenDirections.actionEditProfileToEditPhoneFragment()
        //    findNavController().navigate(routes)

        }

        binding.btnSaveChange.setOnClickListener {
            if (binding.edtFirstName.text.isNullOrEmpty()) {
                Toast.makeText(this@EditProfileActivity, getString(R.string.empty_first_mail), Toast.LENGTH_SHORT).show()
            }
            else if (binding.edtLastName.text.isNullOrEmpty())
            {
                Toast.makeText(this@EditProfileActivity, getString(R.string.empty_last_mail), Toast.LENGTH_SHORT).show()
            }else if (binding.edtEmail.text.isNullOrEmpty()){
                Toast.makeText(this@EditProfileActivity, getString(R.string.empty_mail), Toast.LENGTH_SHORT).show()
            }
            else {
                mViewModel.editProfile(
                    "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                    binding.edtFirstName.text.toString(),
                    binding.edtLastName.text.toString()
                )
            }
        }
    }
    private fun initObserver() {
        mViewModel.editProfileDataSingle.observe(this) {
            if (it.isSuccess!!) {
                PrefUtils.instance.setString(Constants.UserFirstName, it.data?.firstName)
                PrefUtils.instance.setString(Constants.UserLastName, it.data?.lastName)
                PrefUtils.instance.setString(Constants.UserEmail, it.data?.email)
                PrefUtils.instance.setString(Constants.UserMobile, it.data?.phoneNumber)
              //  Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                binding.edtFirstName.setText(
                    PrefUtils.instance.getString(Constants.UserFirstName) ?: ""
                )
                binding.edtLastName.setText(
                    PrefUtils.instance.getString(Constants.UserLastName) ?: ""
                )
                binding.edtEmail.text = PrefUtils.instance.getString(Constants.UserEmail) ?: ""
                binding.edtMobile.text = PrefUtils.instance.getString(Constants.UserMobile) ?: ""
                onBackPressed()

            }

        }
    }

    override fun onResume() {
        super.onResume()
       // PrefUtils.instance.setString(Constants.UserEmail, it.data?.userDetails?.email)

        binding.edtEmail.text = PrefUtils.instance.getString(Constants.UserEmail) ?: ""
        binding.edtMobile.text = PrefUtils.instance.getString(Constants.UserMobile) ?: ""
    }

    override fun onBackPressed() {
        super.onBackPressed()
        PrefUtils.instance.setString("userFName","")
        PrefUtils.instance.setString("userLName","")


    }

}