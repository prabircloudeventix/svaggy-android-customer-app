package com.svaggy.ui.activities.payment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityPaymentCardBinding
import com.svaggy.databinding.FragmentCardPaymentScreenBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.payment.adapter.CardListAdapter
import com.svaggy.ui.fragments.payment.screens.CardPaymentScreenDirections
import com.svaggy.ui.fragments.payment.viewmodel.PaymentViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class PaymentCardActivity :BaseActivity<ActivityPaymentCardBinding>(ActivityPaymentCardBinding::inflate) {

    private var cardListAdapter: CardListAdapter? = null
    private val mViewModel by viewModels<PaymentViewModel>()



    override fun ActivityPaymentCardBinding.initialize(){
        binding.recyclerCards.isNestedScrollingEnabled = false
        binding.recyclerCards.setHasFixedSize(false)



       binding.backButton.setOnClickListener {
           onBackPressed()
        }

        binding.txtAddNewCard.setOnClickListener {
            startActivity(Intent(this@PaymentCardActivity,AddCardActivity::class.java))
        }
        binding.addNewPayment.setOnClickListener {
            startActivity(Intent(this@PaymentCardActivity,AddCardActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        getUserPayCard()
    }

    private fun getUserPayCard(){
        lifecycleScope.launch {
            mViewModel.getUserCard(token = "${Constants.BEARER} ${
                PrefUtils.instance.getString(
                    Constants.Token).toString()}").collect{
                when (it) {
                    is ApiResponse.Loading -> {
                       mProgressBar().showProgressBar(this@PaymentCardActivity)
                    }
                    is ApiResponse.Success -> {
                     mProgressBar().dialog?.dismiss()
                        val data = it.data
                        if (data != null){
                            val list = data.data
                            if (list.isNotEmpty()){
                                cardListAdapter = CardListAdapter(
                                    context =this@PaymentCardActivity,
                                    arrayList = list,
                                    clickCard = {stripeId, cardNumber, cardExpiry, cardName ->
                                      val intent = Intent(this@PaymentCardActivity,EditCardActivity::class.java)
                                        intent.putExtra("stripeToken",stripeId)
                                        intent.putExtra("cardNumber",cardNumber)
                                        intent.putExtra("cardDate",cardExpiry)
                                        intent.putExtra("cardName",cardName)
                                        startActivity(intent)
//                                        findNavController().navigate(
//                                            CardPaymentScreenDirections.actionCardPaymentScreenToManageCard(
//                                            stripeToken = stripeId,
//                                            cardNumber =cardNumber,
//                                            cardDate = cardExpiry,
//                                            cardName = cardName))

                                    }
                                )
                                binding.recyclerCards.adapter = cardListAdapter
                                binding.nstScroll.show()
                                binding.emptyView.hide()
                            }else{
                                binding.nstScroll.hide()
                                binding.emptyView.show()
                            }
                        }
                    }
                    is ApiResponse.Failure -> {
                        mProgressBar().dialog?.dismiss()
                        Toast.makeText(this@PaymentCardActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }
            }
        }
    }

}