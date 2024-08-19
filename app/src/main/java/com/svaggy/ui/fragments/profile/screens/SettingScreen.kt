package com.svaggy.ui.fragments.profile.screens

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.svaggy.R
import com.svaggy.databinding.FragmentSettingScreenBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.profile.viewmodel.ProfileViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.BEARER
import com.svaggy.utils.Constants.CZECH
import com.svaggy.utils.Constants.ENGLISH
import com.svaggy.utils.Constants.OFF
import com.svaggy.utils.Constants.ON
import com.svaggy.utils.Constants.ORDER_DELIVERY
import com.svaggy.utils.Constants.PROMOTIONAL
import com.svaggy.utils.Constants.en
import com.svaggy.utils.PrefUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingScreen : Fragment() {
    lateinit var binding: FragmentSettingScreenBinding
    private val mViewModel by viewModels<ProfileViewModel>()
    private var lan = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingScreenBinding.inflate(inflater, container, false)
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = requireContext().getText(R.string.setting)
        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        )

        getSettingsDetails()
        initBinding()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initBinding() {
        binding.apply {
            consOrderDelivery.setOnClickListener {
                if (switchOrderDelivery.isChecked){
                    val map = mapOf(ORDER_DELIVERY to OFF)
                    setStrings(map,it)
                }else{
                    val map = mapOf(ORDER_DELIVERY to ON)
                    setStrings(map, it)
                }

            }
            consPromotional.setOnClickListener {
              if (switchPromotional.isChecked){
                    val map = mapOf(PROMOTIONAL to OFF)
                    setStrings(map, it)
                }else{
                    val map = mapOf(PROMOTIONAL to ON)
                    setStrings(map, it)
                }

            }
            consLanguage.setOnClickListener {
                if (lan.isNotEmpty()){
                    val action = SettingScreenDirections.actionSettingScreenToLanguageScreen(lan)
                    findNavController().navigate(action)
                }
            }
        }
    }


    private fun getSettingsDetails() {
        lifecycleScope.launch {
            mViewModel.getSettings(
                "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}"
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
                                binding.switchOrderDelivery.isChecked = orderDelivery == ON
                                binding.switchPromotional.isChecked = promotional == ON
                                if (lan == en){
                                    binding.txtSelectedLang.text = ENGLISH
                                }else{
                                    binding.txtSelectedLang.text = CZECH
                                }
                            }
                        }

                    }

                    is ApiResponse.Failure -> {
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setStrings(map: Map<String, String>, view: View){
        lifecycleScope.launch {
            mViewModel.setSettings(authToken = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}", map = map).collect{
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
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }
}