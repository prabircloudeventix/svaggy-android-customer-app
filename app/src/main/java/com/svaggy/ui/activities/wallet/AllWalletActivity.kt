package com.svaggy.ui.activities.wallet

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityAllWalletBinding
import com.svaggy.ui.fragments.home.adapter.SortByAdapter
import com.svaggy.ui.fragments.home.model.SortByFilter
import com.svaggy.ui.fragments.wallet.adpter.WalletTxnHistoryAdapter
import com.svaggy.ui.fragments.wallet.viewmodels.WalletViewModels
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.setSafeOnClickListener
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllWalletActivity : BaseActivity<ActivityAllWalletBinding>(ActivityAllWalletBinding::inflate) {

    private var sortByList: ArrayList<SortByFilter>? =  null
    private lateinit var adapterWalletHistory: WalletTxnHistoryAdapter
    private val mViewModel by viewModels<WalletViewModels>()


    override fun ActivityAllWalletBinding.initialize(){

        sortByList = ArrayList()
        sortByList!!.add(SortByFilter("Last 10 Transactions","LAST_10_TRANSACTIONS",false))
        sortByList!!.add(SortByFilter("View Last Week","LAST_WEEK",false))
        sortByList!!.add(SortByFilter("View Last Month","LAST_MONTH",false))

       binding.backButton.setOnClickListener {
          onBackPressed()
        }

        binding.btnAddFunds.setSafeOnClickListener {
          //  findNavController().navigate(R.id.action_myWalletFragment_to_walletAddFund)
            startActivity(Intent(this@AllWalletActivity,AddWalletActivity::class.java))

        }


        binding.txtSortBy.setOnClickListener{
            val viewDialog = layoutInflater.inflate(R.layout.sheet_shortby_filter, null)
            val   dialog = BottomSheetDialog(this@AllWalletActivity)
            dialog.setOnShowListener {
                val bottomSheetDialogFragment = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                (viewDialog.parent as View).setBackgroundColor(ContextCompat.getColor(this@AllWalletActivity, R.color.transparent))
                val behavior = bottomSheetDialogFragment?.let {
                    BottomSheetBehavior.from(it)
                }
                behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            }

            val recyclerSortBy=viewDialog.findViewById<RecyclerView>(R.id.recyclerSortBy)
            val cancelImage=viewDialog.findViewById<ImageView>(R.id.cancelImage)

            cancelImage.setOnClickListener {
                dialog.dismiss()
            }

            recyclerSortBy?.adapter = SortByAdapter(context = this@AllWalletActivity,
                sortByList = sortByList!!, getRestaurantFilter = {value ->
                    dialog.dismiss()
                    getWalletData(value)

                })

            dialog.setContentView(viewDialog)
            dialog.show()
        }





    }


    override fun onResume() {
        super.onResume()
        getWalletData(value = null)
    }
    @SuppressLint("StringFormatInvalid")
    private fun getWalletData(value: String?) {
        val map: MutableMap<String, String> = mutableMapOf()

        if (value != null)
            map["type"] = value
        lifecycleScope.launch {
            mViewModel.getWallet(token ="Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        binding.progressBar.show()
                    }
                    is ApiResponse.Success -> {
                        binding.progressBar.hide()
                        val response = it.data
                        binding.txtWalletBalance.text = getString(R.string.curencyWith, response?.data?.walletAmount?.toInt().toString())
                        if (response != null && response.data?.transactions!!.isNotEmpty()){
                            adapterWalletHistory = WalletTxnHistoryAdapter(context = this@AllWalletActivity,
                                arrayList = response.data!!.transactions)
                            binding.recyclerWalletHistory.adapter = adapterWalletHistory
                            binding.recyclerWalletHistory.show()
                            binding.emptyWallet.hide()
                            binding.tvEmpty.hide()
                            binding.empty.hide()

                        }else{
                            binding.recyclerWalletHistory.hide()
                            binding.tvEmpty.show()
                            binding.empty.show()
                            binding.emptyWallet.show()
                        }

                    }
                    is ApiResponse.Failure -> {
                        binding.progressBar.hide()
                        Toast.makeText(this@AllWalletActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }



}