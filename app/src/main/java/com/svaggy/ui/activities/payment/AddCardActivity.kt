package com.svaggy.ui.activities.payment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.Address
import com.stripe.android.model.CardParams
import com.stripe.android.model.Token
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityAddCardBinding
import com.svaggy.databinding.FragmentAddNewCardScreenBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.payment.viewmodel.PaymentViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.setSafeOnClickListener
import com.svaggy.utils.show
import com.svaggy.utils.validateCardExpiryDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.StringTokenizer
@AndroidEntryPoint
class AddCardActivity : BaseActivity<ActivityAddCardBinding>(ActivityAddCardBinding::inflate) {

    private val mViewModel by viewModels<PaymentViewModel>()
    private var pkTestKey:String? = null



    override fun ActivityAddCardBinding.initialize(){



       binding.backButton.setOnClickListener {
           onBackPressed()
        }

        binding.addCard.setSafeOnClickListener {
            mProgressBar().showProgressBar(this@AddCardActivity)
            addCard()
        }
    }


    private fun addCard() {
        if (!PrefUtils.instance.getString(Constants.PK_Test).isNullOrEmpty())
            pkTestKey = PrefUtils.instance.getString(Constants.PK_Test)
        val validExpDate = binding.edtExpDate.text.toString()
        if (validateCardExpiryDate(validExpDate)) {
            val stringTokenizer = StringTokenizer(validExpDate, "/")
            val cardNumber: String = binding.edtCardNumber.text.toString()
            val cardExpMonth = stringTokenizer.nextToken().toInt()
            val cardExpYear = stringTokenizer.nextToken().toInt()
            val cardCVC: String = binding.edtCardCvv.text.toString()
            val cardName: String = binding.edtCardName.text.toString()
            val stripe = Stripe(this,PrefUtils.instance.getString(Constants.stripeKey).toString())
            val address = Address(null, null, null, null, null, null)

            val params =
                CardParams(cardNumber, cardExpMonth, cardExpYear, cardCVC, cardName, address)
            stripe.createCardToken(params, callback = object : ApiResultCallback<Token> {
                override fun onError(e: Exception) {
                    mProgressBar().dialog?.dismiss()
                    Toast.makeText(
                       this@AddCardActivity,
                        "Please enter the valid card details!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                override fun onSuccess(result: Token) {
                    val cardId = result.id
                    lifecycleScope.launch {
                        mViewModel.addUserCard(token ="Bearer ${
                            PrefUtils.instance.getString(
                                Constants.Token).toString()}",
                            cardToken = cardId,
                            cardNickname = binding.edtCardName.text.toString().trim()).collect{
                            when (it) {
                                is ApiResponse.Loading -> {

                                }
                                is ApiResponse.Success -> {
                                    mProgressBar().dialog?.dismiss()
                                    val response = it.data
                                    if (response!!.is_success!!){
                                        onBackPressed()

                                    } else {
                                        mProgressBar().dialog?.dismiss()
                                        Toast.makeText(this@AddCardActivity, "" + response.message, Toast.LENGTH_SHORT).show()
                                    }


                                }
                                is ApiResponse.Failure -> {
                                    mProgressBar().dialog?.dismiss()
                                    Toast.makeText(this@AddCardActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                                }

                            }
                        }

                    }

                }
            })
        }
    }


}