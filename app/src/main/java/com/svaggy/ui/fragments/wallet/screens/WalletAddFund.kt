package com.svaggy.ui.fragments.wallet.screens

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.app.SvaggyApplication
import com.svaggy.databinding.FragmentWalletAddFundBinding
import com.svaggy.ui.fragments.home.model.SortByFilter
import com.svaggy.ui.fragments.wallet.adpter.SelectAmountAdapter
import com.svaggy.ui.fragments.wallet.adpter.SelectPaymentMethod
import com.svaggy.ui.fragments.wallet.viewmodels.WalletViewModels
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.setSafeOnClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalletAddFund : Fragment() {
    lateinit var binding: FragmentWalletAddFundBinding
    private var selectAmountAdapter: SelectAmountAdapter? = null
    private var selectAmountArraylist: ArrayList<SortByFilter> = ArrayList()
    private var paymentSheet: PaymentSheet? = null
    private var paymentIntentClientSecret: String? = ""
    private var paymentIntentId: String? = ""
    private lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    private val mViewModel by viewModels<WalletViewModels>()
    private val bundle = Bundle()
    private var isFrom = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentWalletAddFundBinding.inflate(inflater, container, false)
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = requireContext().getString(R.string.add_Funds)
    //    (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility=View.GONE
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        isFrom = arguments?.getString("isFrom").toString()
        initObserver()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectAmountArraylist.add(SortByFilter("50","",false))
        selectAmountArraylist.add(SortByFilter("100","",false))
        selectAmountArraylist.add(SortByFilter("200","",false))
        selectAmountArraylist.add(SortByFilter("Other","",false))

        binding.recyclerSelectAmount.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        selectAmountAdapter = SelectAmountAdapter((activity as MainActivity),selectAmountArraylist,binding.btWalletAmountAdd,::getPriceClicked)
        binding.recyclerSelectAmount.adapter = selectAmountAdapter

        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btWalletAmountAdd.setSafeOnClickListener {
            bundle.putString("isFrom", "WalletScreen")
            mViewModel.stripeCustomerWallet(
                "Bearer ${
                    PrefUtils.instance.getString(Constants.Token).toString()
                }",
                binding.etAmountAdd.text.toString()
            )
        }
    }
    private fun getPriceClicked(getShortName: String, selectSort:Boolean, shortByTextView: TextView, getShortKey:String) {
        if (getShortName != "Other") {
            binding.etAmountAdd.setText(getShortName)
            binding.etAmountAdd.clearFocus()
        }
        else {
            binding.etAmountAdd.requestFocus()
            val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(binding.etAmountAdd, InputMethodManager.SHOW_IMPLICIT)
            binding.etAmountAdd.setText("")

        }
    }
    private fun initObserver() {
        mViewModel.walletAmountAddMutable.observe(viewLifecycleOwner) {
            if (it.isSuccess == true) {
                binding.etAmountAdd.setText("")

                val routs = WalletAddFundDirections.actionWalletAddFundToMoneyAddedSuccessfully(isFrom = isFrom,
                    addedAmount =it.data?.addedAmount.toString(), updatedBalance =  it.data?.updatedBalance.toString() )
                findNavController().navigate(routs)
            } else {
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }

        mViewModel.stripeCustomerWalletData.observe(viewLifecycleOwner) {
            if (it.isSuccess == true) {
                paymentIntentClientSecret = it.data?.paymentIntent
                paymentIntentId = it.data?.intentId
                customerConfig = PaymentSheet.CustomerConfiguration(
                    it.data?.customer.toString(),
                    it.data?.ephemeralKey.toString()
                )
                PaymentConfiguration.init(requireContext(),it.data?.publishableKey.toString())
                presentPaymentSheet()
            }
            else {
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun presentPaymentSheet() {

        paymentSheet!!.presentWithPaymentIntent(
            paymentIntentClientSecret!!,
            PaymentSheet.Configuration(
                merchantDisplayName = "My merchant name",
                allowsDelayedPaymentMethods = true,
                customer = customerConfig
            )
        )
    }

    private fun onPaymentSheetResult(paymentResult: PaymentSheetResult) {
        when (paymentResult) {
            is PaymentSheetResult.Completed -> {
                mViewModel.walletAmountAdd(
                    "Bearer ${
                        PrefUtils.instance.getString(Constants.Token).toString()
                    }",
                    binding.etAmountAdd.text.toString(),
                    paymentIntentId.toString(),
                    "SUCCESS"
                )
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(context, "Payment canceled!", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(context, "Payment failed"+ paymentResult.error.localizedMessage, Toast.LENGTH_SHORT).show()
                mViewModel.walletAmountAdd(
                    "Bearer ${
                        PrefUtils.instance.getString(Constants.Token).toString()
                    }",
                    binding.etAmountAdd.text.toString(),
                    paymentIntentId.toString(),
                    "FAIL"
                )
            }
        }
    }
}