package com.svaggy.ui.activities.payment

import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityEditCardBinding
import com.svaggy.ui.fragments.payment.viewmodel.PaymentViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditCardActivity : BaseActivity<ActivityEditCardBinding>(ActivityEditCardBinding::inflate) {

    private lateinit var stripeToken:String
    private lateinit var cardNum:String
    private lateinit var cardDate:String
    private lateinit var cardName:String
    private val mViewModel by viewModels<PaymentViewModel>()



    override fun ActivityEditCardBinding.initialize(){
        stripeToken = intent?.getStringExtra("stripeToken").toString()
        cardNum = intent?.getStringExtra("cardNumber").toString()
        cardDate = intent?.getStringExtra("cardDate").toString()
        cardName = intent?.getStringExtra("cardName").toString()
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
            backButton.setOnClickListener {
                onBackPressed()
            }
        }

    }

    private fun updateCard(){
        if (!binding.getNickName.text.isNullOrEmpty()){
            lifecycleScope.launch {
                mViewModel.editCard(token = "${Constants.BEARER} ${
                    PrefUtils.instance.getString(
                        Constants.Token).toString()}",
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
                               onBackPressed()
                            }

                        }
                        is ApiResponse.Failure -> {
                            binding.progressBar.hide()
                            Toast.makeText(this@EditCardActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                        }

                    }

                }

            }


        }




    }

    private fun deleteCard() {
        PrefUtils.instance.showCustomDialog(
           this,
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
                    mViewModel.deleteUserCard(token ="${Constants.BEARER} ${
                        PrefUtils.instance.getString(
                            Constants.Token).toString()}",
                        map = map).collect{res ->
                        when (res) {
                            is ApiResponse.Loading -> {
                                binding.progressBar.show()
                            }
                            is ApiResponse.Success -> {
                                binding.progressBar.hide()
                                val response = res.data
                                if (response!!.isSuccess!!){
                                   onBackPressed()
                                }

                            }
                            is ApiResponse.Failure -> {
                                binding.progressBar.hide()
                                Toast.makeText(this@EditCardActivity, "${res.msg}", Toast.LENGTH_LONG).show()
                            }

                        }

                    }

                }




                it.dismiss()
            }
        )
    }

}