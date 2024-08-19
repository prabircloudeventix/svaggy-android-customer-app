package com.svaggy.ui.fragments.payment.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.app.SvaggyApplication
import com.svaggy.databinding.FragmentCardPaymentScreenBinding
import com.svaggy.ui.fragments.payment.adapter.CardListAdapter
import com.svaggy.ui.fragments.payment.model.GetCard
import com.svaggy.ui.fragments.payment.viewmodel.PaymentViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.BEARER
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CardPaymentScreen : Fragment() {

    private var _binding: FragmentCardPaymentScreenBinding? = null
    private val binding get() = _binding!!

    private var cardListAdapter: CardListAdapter? = null
    private val mViewModel by viewModels<PaymentViewModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCardPaymentScreenBinding.inflate(inflater, container, false)
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = getString(R.string.paymentMethod)
    //    (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility=View.GONE
    //    PrefUtils.instance.setString(Constants.CurrentDestinationId,(activity as MainActivity).navController.currentDestination?.id.toString())
        binding.recyclerCards.isNestedScrollingEnabled = false
        binding.recyclerCards.setHasFixedSize(false)
        getUserPayCard()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })

        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }



        binding.txtAddNewCard.setOnClickListener {
            findNavController().navigate(R.id.action_cardPaymentScreen_to_addNewCardScreen)
        }
    }

    private fun getUserPayCard(){
        lifecycleScope.launch {
            mViewModel.getUserCard(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}").collect{
                when (it) {
                    is ApiResponse.Loading -> {
                       binding.progressBar.show()
                    }
                    is ApiResponse.Success -> {
                      binding.progressBar.hide()
                        val data = it.data
                        if (data != null){
                            val list = data.data
                            if (list.isNotEmpty()){
                                cardListAdapter = CardListAdapter(
                                    context = requireContext(),
                                    arrayList = list,
                                    clickCard = {stripeId, cardNumber, cardExpiry, cardName ->
                                        findNavController().navigate(CardPaymentScreenDirections.actionCardPaymentScreenToManageCard(
                                            stripeToken = stripeId,
                                            cardNumber =cardNumber,
                                            cardDate = cardExpiry,
                                            cardName = cardName))

                                    }
                                   )
                                binding.recyclerCards.adapter = cardListAdapter
                            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        // Nullify the binding object to avoid memory leaks
        _binding = null
    }


}