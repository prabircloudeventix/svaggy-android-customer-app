package com.svaggy.ui.fragments.wallet.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.FragmentMyWalletBinding
import com.svaggy.ui.fragments.home.adapter.SortByAdapter
import com.svaggy.ui.fragments.home.model.SortByFilter
import com.svaggy.ui.fragments.wallet.adpter.WalletTxnHistoryAdapter
import com.svaggy.ui.fragments.wallet.viewmodels.WalletViewModels
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.onBackPressedDispatcher
import com.svaggy.utils.setSafeOnClickListener
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyWalletFragment : Fragment() {

    private var _binding: FragmentMyWalletBinding? = null
    private val binding get() = _binding!!
    private var sortByList: ArrayList<SortByFilter>? =  null
    private lateinit var adapterWalletHistory: WalletTxnHistoryAdapter
    private val mViewModel by viewModels<WalletViewModels>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyWalletBinding.inflate(inflater, container, false)
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = requireContext().getString(R.string.my_wallet)
      //  (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility=View.GONE
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         getWalletData(value = null)
        onBackPressedDispatcher{
            findNavController().popBackStack()

        }
        sortByList = ArrayList()
        sortByList!!.add(SortByFilter("Last 10 Transactions","LAST_10_TRANSACTIONS",false))
        sortByList!!.add(SortByFilter("View Last Week","LAST_WEEK",false))
        sortByList!!.add(SortByFilter("View Last Month","LAST_MONTH",false))

        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAddFunds.setSafeOnClickListener {
            findNavController().navigate(R.id.action_myWalletFragment_to_walletAddFund)
        }

        binding.txtSortBy.setOnClickListener{
            val viewDialog = layoutInflater.inflate(R.layout.sheet_shortby_filter, null)
          val   dialog = BottomSheetDialog(requireContext())
            dialog.setOnShowListener {
                val bottomSheetDialogFragment = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                (viewDialog.parent as View).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
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

            recyclerSortBy?.adapter = SortByAdapter(context = requireContext(),
                sortByList = sortByList!!, getRestaurantFilter = {value ->
                    dialog.dismiss()
                    getWalletData(value)

                })

            dialog.setContentView(viewDialog)
            dialog.show()
        }
    }
//

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
                        binding.txtWalletBalance.text = requireContext().getString(R.string.curencyName, response?.data?.walletAmount.toString())
                        if (response != null && response.data?.transactions!!.isNotEmpty()){
                            adapterWalletHistory = WalletTxnHistoryAdapter(context = requireContext(),
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
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }

}