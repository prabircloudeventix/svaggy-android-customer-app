package com.svaggy.ui.fragments.payment.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.svaggy.databinding.FragmentManageCardBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.payment.viewmodel.PaymentViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageCard : Fragment() {

    // Declare the binding object
    private var _binding: FragmentManageCardBinding? = null
    private val binding get() = _binding!!
    private lateinit var stripeToken:String
    private lateinit var cardNum:String
    private lateinit var cardDate:String
    private lateinit var cardName:String
    private val mViewModel by viewModels<PaymentViewModel>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentManageCardBinding.inflate(inflater, container, false)
        stripeToken = arguments?.getString("stripeToken").toString()
        cardNum = arguments?.getString("cardNumber").toString()
        cardDate = arguments?.getString("cardDate").toString()
        cardName = arguments?.getString("cardName").toString()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        initBinding()

    }

    private fun initBinding() {
        binding.apply {
            getCardNumber.text = cardNum
            getExpDate.text = cardDate
            getNickName.setText(cardName)
            addCard.setOnClickListener {
                updateCard()
            }
            imgDelete.setOnClickListener {
                deleteCard()
            }
        }

    }

    private fun updateCard(){
        if (!binding.getNickName.text.isNullOrEmpty()){
            lifecycleScope.launch {
                mViewModel.editCard(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                    stripeToken = stripeToken,
                    cardNickname = binding.getNickName.text.toString().trim()).collect{
                    when (it) {
                        is ApiResponse.Loading -> {
                           binding.progressBar.show()
                        }
                        is ApiResponse.Success -> {
                          binding.progressBar.hide()
                            val response = it.data
                            if (response!!.is_success!!){
                                findNavController().popBackStack()
                            }

                        }
                        is ApiResponse.Failure -> {
                           binding.progressBar.hide()
                            Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                        }

                    }

                }

            }


        }




    }

    private fun deleteCard() {
        PrefUtils.instance.showCustomDialog(
            requireContext(),
            "Are You Sure You Want Delete This Card?",
            "No",
            "Yes",
            "",
            false,
            true,
            negativeAlert = {
                it.dismiss()
            },
            positiveAlert = {

                val map = mapOf("card_id" to stripeToken)
                lifecycleScope.launch {
                    mViewModel.deleteUserCard(token ="${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                        map = map).collect{res ->
                            when (res) {
                            is ApiResponse.Loading -> {
                               binding.progressBar.show()
                            }
                            is ApiResponse.Success -> {
                               binding.progressBar.hide()
                                val response = res.data
                                if (response!!.isSuccess!!){
                                    findNavController().popBackStack()
                                }

                            }
                            is ApiResponse.Failure -> {
                               binding.progressBar.hide()
                                Toast.makeText(requireContext(), "${res.msg}", Toast.LENGTH_LONG).show()
                            }

                        }

                    }

                }




                it.dismiss()
            }
        )
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}