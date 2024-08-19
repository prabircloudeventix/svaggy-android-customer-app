package com.svaggy.ui.activities.settings

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityAppSettingBinding
import com.svaggy.databinding.FragmentSettingScreenBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.profile.screens.SettingScreenDirections
import com.svaggy.ui.fragments.profile.viewmodel.ProfileViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AppSettingActivity :BaseActivity<ActivityAppSettingBinding>(ActivityAppSettingBinding::inflate) {
    private val mViewModel by viewModels<ProfileViewModel>()
    private var lan = ""



    @RequiresApi(Build.VERSION_CODES.O)
    override fun ActivityAppSettingBinding.initialize(){
       binding.backButton.setOnClickListener {
           onBackPressed()
        }
        initBinding()
        getSettingsDetails()

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initBinding() {
        binding.apply {
            consOrderDelivery.setOnClickListener {
                if (switchOrderDelivery.isChecked){
                    val map = mapOf(Constants.ORDER_DELIVERY to Constants.OFF)
                    setStrings(map,it)
                }else{
                    val map = mapOf(Constants.ORDER_DELIVERY to Constants.ON)
                    setStrings(map, it)
                }

            }
            consPromotional.setOnClickListener {
                if (switchPromotional.isChecked){
                    val map = mapOf(Constants.PROMOTIONAL to Constants.OFF)
                    setStrings(map, it)
                }else{
                    val map = mapOf(Constants.PROMOTIONAL to Constants.ON)
                    setStrings(map, it)
                }

            }
            consLanguage.setOnClickListener {
                if (lan.isNotEmpty()){
                    val intent = Intent(this@AppSettingActivity,LanguageActivity::class.java)
                    intent.putExtra(Constants.en,lan)
                    startActivity(intent)
                 //   val action = SettingScreenDirections.actionSettingScreenToLanguageScreen(lan)
                  //  findNavController().navigate(action)
                }
            }
        }
    }


    private fun getSettingsDetails() {
        lifecycleScope.launch {
            mViewModel.getSettings(
                "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}"
            ).collect {
                when (it) {
                    is ApiResponse.Loading -> {


                    }

                    is ApiResponse.Success -> {
                        if (it.data != null){
                            if (it.data.isSuccess!!){
                                val orderDelivery = it.data.data!!.ORDERDELIVERY ?: ""
                                val promotional = it.data.data!!.PROMOTIONAL
                                lan = it.data.data!!.LANGUAGE ?: ""
                                binding.switchOrderDelivery.isChecked = orderDelivery == Constants.ON
                                binding.switchPromotional.isChecked = promotional == Constants.ON
                                if (lan == Constants.en){
                                    binding.txtSelectedLang.text = Constants.ENGLISH
                                }else{
                                    binding.txtSelectedLang.text = Constants.CZECH
                                }
                            }
                        }

                    }

                    is ApiResponse.Failure -> {
                        Toast.makeText(this@AppSettingActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setStrings(map: Map<String, String>, view: View){
        lifecycleScope.launch {
            mViewModel.setSettings(authToken = "${Constants.BEARER} ${
                PrefUtils.instance.getString(
                    Constants.Token).toString()}", map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {


                    }

                    is ApiResponse.Success -> {
                        if (it.data?.isSuccess!!){
                            if (view.tooltipText == "order"){
                                binding.switchOrderDelivery.isChecked = !binding.switchOrderDelivery.isChecked
                            }else{
                                binding.switchPromotional.isChecked = ! binding.switchPromotional.isChecked
                            }

                        }


                    }

                    is ApiResponse.Failure -> {
                        Toast.makeText(this@AppSettingActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }

}