package com.svaggy.ui.fragments.payment.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.Address
import com.stripe.android.model.CardParams
import com.stripe.android.model.Token
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.FragmentAddNewCardScreenBinding
import com.svaggy.ui.fragments.payment.viewmodel.PaymentViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import com.svaggy.utils.validateCardExpiryDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.StringTokenizer

@AndroidEntryPoint
class AddNewCardScreen : Fragment() {
    lateinit var binding: FragmentAddNewCardScreenBinding
    private val mViewModel by viewModels<PaymentViewModel>()
    private var pkTestKey:String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddNewCardScreenBinding.inflate(inflater, container, false)
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = getString(R.string.cardType)
      //  (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility = View.GONE



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),
            object : OnBackPressedCallback(true) { override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })


        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.addCard.setOnClickListener {
            addCard()
           binding.progressBarMenu.show()



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
            val stripe = Stripe(requireContext(),pkTestKey ?: getString(R.string.stripe_payment_key))
            val address = Address(null, null, null, null, null, null)

            val params =
                CardParams(cardNumber, cardExpMonth, cardExpYear, cardCVC, cardName, address)
            stripe.createCardToken(params, callback = object : ApiResultCallback<Token> {
                override fun onError(e: Exception) {
                    Toast.makeText(
                        context,
                        "Please enter the valid card details!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                override fun onSuccess(result: Token) {
                    val cardId = result.id
                    lifecycleScope.launch {
                        mViewModel.addUserCard(token ="Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                            cardToken = cardId,
                            cardNickname = binding.edtCardName.text.toString().trim()).collect{
                            when (it) {
                                is ApiResponse.Loading -> {

                                }
                                is ApiResponse.Success -> {
                                  binding.progressBarMenu.show()
                                    val response = it.data
                                    if (response!!.is_success!!){
                                        findNavController().popBackStack()
                                        } else {
                                            Toast.makeText(context, "" + response.message, Toast.LENGTH_SHORT).show()
                                        }


                                }
                                is ApiResponse.Failure -> {
                                  binding.progressBarMenu.hide()
                                    Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                                }

                            }
                        }

                    }

                }
            })
        }
    }


}