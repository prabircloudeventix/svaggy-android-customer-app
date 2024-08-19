package com.svaggy.ui.fragments.order.ordertabs

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.livechatinc.inappchat.ChatWindowActivity
import com.livechatinc.inappchat.ChatWindowConfiguration
import com.svaggy.R
import com.svaggy.databinding.FragmentTodayBinding
import com.svaggy.ui.activities.track.OrderDetailsActivity
import com.svaggy.ui.activities.track.TrackOrderActivity
import com.svaggy.ui.activities.track.TrackPickOrderActivity
import com.svaggy.ui.fragments.order.adapter.OrdersAdapter
import com.svaggy.ui.fragments.order.viewmodel.OrdersViewModel
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TodayFragment : Fragment()
{
    lateinit var binding: FragmentTodayBinding
    private val bundle = Bundle()
    private lateinit var ordersAdapter: OrdersAdapter
    private var isLoaderVisible : Boolean = false
    private val mViewModel by viewModels<OrdersViewModel>()
 //   private lateinit var progressBarHelper: ProgressBarHelper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTodayBinding.inflate(inflater, container, false)
  //      progressBarHelper = ProgressBarHelper(requireContext())
        initObserver()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mViewModel.ordersList(
            "Bearer ${PrefUtils.instance.getString(Constants.Token)}",
            "TODAY"
        )
    }

    private fun initObserver() {
        mViewModel.getOrdersListDataMutable.observe(viewLifecycleOwner) {
            if (it.isSuccess!!)
            {
                if (!it?.data?.orderData.isNullOrEmpty()) {
                    isLoaderVisible = true
                    binding.noOrder.visibility =View.GONE
                    binding.recyclerOrdersList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    ordersAdapter = OrdersAdapter(requireContext(),it.data?.orderData, it.data?.type ?: "", ::getOrdersClick,
                        clickSupport = {
                            initChatWindow()
                        })
                    binding.recyclerOrdersList.adapter = ordersAdapter
                //   progressBarHelper.dismissProgressBar()
                }
                else {
               //     progressBarHelper.dismissProgressBar()
                    binding.noOrder.visibility = View.VISIBLE
                    binding.recyclerOrdersList.visibility = View.GONE
                }
            }
            else {
                isLoaderVisible = true
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
             //   progressBarHelper.dismissProgressBar()
            }
        }
        mViewModel.reOrderDataMutable.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
         //    progressBarHelper.dismissProgressBar()
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.cartScreen)
            } else {
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
          //    progressBarHelper.dismissProgressBar()
            }
        }
        mViewModel.cancelOrderDataMutable.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
                mViewModel.ordersList(
                    "Bearer ${PrefUtils.instance.getString(Constants.Token)}",
                    "TODAY"
                )
            } else {
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
        mViewModel.errorMessage.observe(viewLifecycleOwner) {
      //    progressBarHelper.dismissProgressBar()
            Toast.makeText(context, "" + it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getOrdersClick(orderId: Int, cancelClick: Boolean, trackClick: Boolean, reOrderClick: Boolean, restaurantClick: Boolean, giveReviewClick:Boolean,orderStatus:String,deliveryType:String) {
        if (cancelClick) {
            if (orderStatus == "ORDER_PREPARING"){
                PrefUtils.instance.showCustomDialog(
                    requireContext(),
                   "",
                    "",
                    "",
                    "",
                    false,
                    isNegativeShow = false,
                    negativeAlert = {
                        it.dismiss()
                    },
                    positiveAlert = {},
                    changeLayout = true
                )

            }else{
                PrefUtils.instance.showCustomDialog(
                    requireContext(),
                    "Are you sure you want to cancel your order?",
                    "No",
                    "Yes, Cancel",
                    "",
                    false,
                    isNegativeShow = true,
                    negativeAlert = {
                        it.dismiss()
                    },
                    positiveAlert = {
                        mViewModel.cancelOrder(
                            "Bearer ${PrefUtils.instance.getString(Constants.Token)}",
                            orderId.toString()
                        )
                        it.dismiss()
                    }
                )

            }

        } else if (trackClick) {
          //  bundle.putString("order_id", orderId.toString())
          //  bundle.putString("isFrom", "OrdersScreen")
            if (deliveryType == "DELIVERY_BY_SVAGGY"){
                val intent =  Intent(requireContext(),TrackOrderActivity::class.java)
                intent.putExtra("order_id", orderId.toString())
                intent.putExtra("isFrom","OrdersScreen")
                startActivity(intent)

            }
            else{
                val intent =  Intent(requireContext(),TrackPickOrderActivity::class.java)
                intent.putExtra("order_id", orderId.toString())
                intent.putExtra("isFrom","OrdersScreen")
                startActivity(intent)
            }

              //  findNavController().navigate(R.id.action_ordersScreen_to_trackPickOrder, bundle)
        }
        else if (restaurantClick) {
          //  bundle.putString("order_id", orderId.toString())
          //  bundle.putString("isFrom", "OrdersScreen")
           // findNavController().navigate(R.id.action_ordersScreen_to_orderDetails, bundle)
            val intent = Intent(requireContext(),OrderDetailsActivity::class.java)
            intent.putExtra("order_id",orderId.toString())
            intent.putExtra("isFrom","TodayOrder")
            startActivity(intent)

        }
    }
    private fun initChatWindow() {
        val config = ChatWindowConfiguration.Builder()
            .setLicenceNumber("18238530")
          //  .setLicenceNumber("18087297")
            .build()

        startChatActivity(config)


    }

    private fun startChatActivity(config: ChatWindowConfiguration) {
        val intent = Intent(requireContext(), ChatWindowActivity::class.java)
        intent.putExtras(config.asBundle())
        startActivity(intent)
    }
}