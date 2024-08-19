package com.svaggy.ui.activities.savePref

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivitySavePrefBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.guestuser.GuestHomeActivity
import com.svaggy.ui.fragments.editAuth.viewmodel.AuthViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.ProgressBarLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavePrefActivity : BaseActivity<ActivitySavePrefBinding>(ActivitySavePrefBinding::inflate) {


    private var preferencesAdapter: PreferencesAdapter? = null
    private var selectedItems = ArrayList<Int>()
    private val mViewModel by viewModels<AuthViewModel>()
    private lateinit var isFrom:String
    private val progressBarLoader by lazy {
        ProgressBarLoader()
    }



    override fun ActivitySavePrefBinding.initialize() {
        mViewModel.getPreferences("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}")
        initObserver()

        binding.continueBt.setOnClickListener {
            if (binding.continueBt.alpha == 1f)
            addGoals()
         //   startActivity(Intent(this@SavePrefActivity,GuestHomeActivity::class.java))
          //  finish()
        }
        isFrom = intent.getStringExtra("isFrom").toString()
    }



    @SuppressLint("HardwareIds")
    fun getAndroidId(): String {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun addGoals() {
        selectedGoals()
        if (selectedItems.size > 0) {
            if (isFrom == "Guest"){
                setGuestPref(deviceId = getAndroidId(), list = selectedItems)
            }else{
                mViewModel.setPreferences("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}", selectedItems)
            }
        }
        else {
            Toast.makeText(this, "Please Select Minimum One Preference", Toast.LENGTH_SHORT).show()
        }
    }
    private fun selectedGoals() {
        if (preferencesAdapter != null) {
            if (preferencesAdapter!!.getSelected().size > 0) {
                val stringBuilder = StringBuilder()
                selectedItems.clear()
                for (i in 0 until preferencesAdapter!!.getSelected().size) {
                    stringBuilder.append(preferencesAdapter!!.getSelected()[i].id)
                    selectedItems.add(preferencesAdapter!!.getSelected()[i].id!!)
                }
            }
        }
    }

    private fun initObserver() {
        mViewModel.getPreferenceLiveData.observe(this) {
            if (it.isSuccess == true) {
              progressBarLoader.dialog?.dismiss()
                binding.continueBt.alpha = 1f
                binding.recyclerPreference.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
                preferencesAdapter = PreferencesAdapter(this, it.data)
                binding.recyclerPreference.adapter = preferencesAdapter


            } else {
                progressBarLoader.dialog?.dismiss()
            }
        }
        mViewModel.getCommonLiveData.observe(this) {
            if (it.isSuccess!!) {
                progressBarLoader.dialog?.dismiss()
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            } else {
                progressBarLoader.dialog?.dismiss()
            }
        }
        mViewModel.errorMessage.observe(this) {
            progressBarLoader.dialog?.dismiss()
        }
    }

    private fun setGuestPref(deviceId:String,list: ArrayList<Int>){
        lifecycleScope.launch {
            mViewModel.setGuestPreferences(deviceId = deviceId, preferences = list).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        progressBarLoader.showProgressBar(this@SavePrefActivity)
                    }
                    is ApiResponse.Success -> {
                        progressBarLoader.dialog?.dismiss()
                        val data = it.data
                        if (data != null){
                            PrefUtils.instance.setString(Constants.IsGuestUser,"true")
                            PrefUtils.instance.setString(Constants.Token, data.data?.guest_user_token)
                            startActivity(Intent(this@SavePrefActivity,GuestHomeActivity::class.java))
                            finish()



                        }
                    }
                    is ApiResponse.Failure -> {
                        progressBarLoader.dialog?.dismiss()
                        Toast.makeText(this@SavePrefActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }
    }

}