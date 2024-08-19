package com.svaggy.ui.fragments.order.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.FragmentOrderDetailsBinding
import com.svaggy.ui.fragments.cart.adapter.BillDetailAdapter
import com.svaggy.ui.fragments.order.adapter.OrderItemDetailsAdapter
import com.svaggy.ui.fragments.order.viewmodel.OrdersViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import com.svaggy.utils.updateStatusBarColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderDetails : Fragment() {

    private val mViewModel by viewModels<OrdersViewModel>()
    lateinit var binding: FragmentOrderDetailsBinding
    private var orderID: String? = ""
    var ordersscreen: String? = ""
    private lateinit var ordersAdapter: OrderItemDetailsAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text=context?.getString(R.string.order_details)
        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    //    PrefUtils.instance.setString(Constants.CurrentDestinationId, (activity as MainActivity).navController?.currentDestination?.id.toString())
        orderID = arguments?.getString("order_id")
        ordersscreen=arguments?.getString("isFrom")
        requireActivity().updateStatusBarColor("#F6F6F6",true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when (ordersscreen) {
                        context?.getString(R.string.track_order_screen) -> {
                            findNavController().popBackStack(R.id.trackOrderScreen,false)
                        }
                        context?.getString(R.string.order_screen) -> {
                            findNavController().popBackStack(R.id.ordersScreen,false)
                        }
                        else -> {
                            findNavController().popBackStack()
                        }
                    }

                }
            })


        if (!orderID.isNullOrEmpty()) {
            orderDetailsObserver()
        }

        binding.btTrackOrder.setOnClickListener {
            findNavController().popBackStack(R.id.trackOrderScreen,false)
        }
    }

    private fun orderDetailsObserver() {
        lifecycleScope.launch {
            mViewModel.orderDetails("Bearer ${PrefUtils.instance.getString(Constants.Token)}", orderID.toString()).collect{

                when (it) {
                    is ApiResponse.Loading -> {
                      binding.progressBarMenu.show()
                    }
                    is ApiResponse.Success -> {

                        ordersAdapter = OrderItemDetailsAdapter(
                            requireContext(),
                            it.data?.data?.cart_data?.cart_items,
                            it.data?.data?.currency_key ?: "")
                        binding.recyclerProductItem.adapter = ordersAdapter
                        binding.itemAddCt.visibility = View.VISIBLE
                        binding.tvOrder.visibility = View.VISIBLE

                        val billDetailsAdapter = BillDetailAdapter(requireContext(),it.data?.data!!.bill_details,:: walletAvailable)
                        binding.recyclerBillDetails.adapter = billDetailsAdapter
                        binding.billDetailsLayout.visibility = View.VISIBLE
                        binding.tvBill.visibility = View.VISIBLE

                        val orderDetailsAdapter = OrderDetailsAdapter((activity as MainActivity),it.data.data.order_details)
                        binding.recyclerOrderDetails.adapter = orderDetailsAdapter
                        binding.recyclerOrderDetails.visibility = View.VISIBLE
                        binding.tvOrderDetails.visibility = View.VISIBLE

                        binding.txtTotalAmount.text = it.data.data.cart_data.total_amount.toString()

                        binding.restaurantAddress.text = it.data.data.cart_data.restaurant_details.address?:""

                        binding.txtRestaurantName.text=it.data.data.cart_data.restaurant_details.restaurant_name
                        binding.btTrackOrder.visibility = View.VISIBLE
                      binding.progressBarMenu.hide()


                    }
                    is ApiResponse.Failure -> {
                       binding.progressBarMenu.hide()
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }













        mViewModel.getOrderDetailsDataMutable.observe(viewLifecycleOwner) {
            if (it.is_success) {
                binding.progressBarMenu.hide()




            } else {
                binding.progressBarMenu.hide()
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun walletAvailable(b: Boolean) {

    }

}