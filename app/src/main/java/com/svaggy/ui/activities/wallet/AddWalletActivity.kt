package com.svaggy.ui.activities.wallet

import android.content.Context
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityAddWalletActivtyBinding
import com.svaggy.ui.fragments.home.model.SortByFilter
import com.svaggy.ui.fragments.wallet.adpter.SelectAmountAdapter
import com.svaggy.ui.fragments.wallet.viewmodels.WalletViewModels
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.setSafeOnClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddWalletActivity : BaseActivity<ActivityAddWalletActivtyBinding>(ActivityAddWalletActivtyBinding::inflate) {
    private var selectAmountAdapter: SelectAmountAdapter? = null
    private var selectAmountArraylist: ArrayList<SortByFilter> = ArrayList()
    private var paymentSheet: PaymentSheet? = null
    private var paymentIntentClientSecret: String? = ""
    private var paymentIntentId: String? = ""
    private lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    private val mViewModel by viewModels<WalletViewModels>()

    override fun ActivityAddWalletActivtyBinding.initialize(){
        paymentSheet = PaymentSheet(this@AddWalletActivity, ::onPaymentSheetResult)
        initObserver()
        selectAmountArraylist.add(SortByFilter("50","",false))
        selectAmountArraylist.add(SortByFilter("100","",false))
        selectAmountArraylist.add(SortByFilter("200","",false))
        selectAmountArraylist.add(SortByFilter("Other","",false))

        binding.recyclerSelectAmount.layoutManager = LinearLayoutManager(this@AddWalletActivity, LinearLayoutManager.HORIZONTAL, false)
        selectAmountAdapter = SelectAmountAdapter(this@AddWalletActivity,selectAmountArraylist,binding.btWalletAmountAdd,::getPriceClicked)
        binding.recyclerSelectAmount.adapter = selectAmountAdapter

      binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.btWalletAmountAdd.setSafeOnClickListener {
            mProgressBar().showProgressBar(this@AddWalletActivity)
            mViewModel.stripeCustomerWallet("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
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
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(binding.etAmountAdd, InputMethodManager.SHOW_IMPLICIT)
            binding.etAmountAdd.setText("")

        }
    }
    private fun initObserver() {
        mViewModel.walletAmountAddMutable.observe(this) {
            if (it.isSuccess == true) {
                binding.etAmountAdd.setText("")
               // Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
              //  val routs = WalletAddFundDirections.actionWalletAddFundToMoneyAddedSuccessfully(isFrom = isFrom,
                 //   addedAmount =it.data?.addedAmount.toString(), updatedBalance =  it.data?.updatedBalance.toString() )
               // findNavController().navigate(routs)
                val intent = Intent(this,MoneyAddedActivity::class.java)
                intent.putExtra("added_amount",it.data?.addedAmount.toString())
                intent.putExtra("updated_balance",it.data?.updatedBalance.toString())
                startActivity(intent)
                finish()
            } else {
                mProgressBar().dialog?.dismiss()
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }

        mViewModel.stripeCustomerWalletData.observe(this) {
            if (it.isSuccess == true) {
                paymentIntentClientSecret = it.data?.paymentIntent
                paymentIntentId = it.data?.intentId
                customerConfig = PaymentSheet.CustomerConfiguration(
                    it.data?.customer.toString(),
                    it.data?.ephemeralKey.toString()
                )
                PaymentConfiguration.init(this,PrefUtils.instance.getString(Constants.stripeKey).toString())
                presentPaymentSheet()
            }
            else {
                mProgressBar().dialog?.dismiss()
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun presentPaymentSheet() {


        val defaultBillingDetails = PaymentSheet.BillingDetails(
            address = PaymentSheet.Address(country = "CZ"))

        paymentSheet!!.presentWithPaymentIntent(
            paymentIntentClientSecret!!,
            PaymentSheet.Configuration(
                merchantDisplayName = "My merchant name",
                allowsDelayedPaymentMethods = true,
                customer = customerConfig,
                defaultBillingDetails = defaultBillingDetails  // Set default billing details
            )
        )
    }

    private fun onPaymentSheetResult(paymentResult: PaymentSheetResult) {
        when (paymentResult) {
            is PaymentSheetResult.Completed -> {
                mProgressBar().dialog?.dismiss()
                Toast.makeText(this, "Payment complete!", Toast.LENGTH_SHORT).show()
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
                mProgressBar().dialog?.dismiss()
                Toast.makeText(this, "Payment canceled!", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                mProgressBar().dialog?.dismiss()
                Toast.makeText(this, "Payment failed"+ paymentResult.error.localizedMessage, Toast.LENGTH_SHORT).show()
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