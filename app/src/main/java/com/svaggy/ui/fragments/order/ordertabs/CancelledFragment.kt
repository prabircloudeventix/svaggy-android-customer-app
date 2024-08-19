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
import com.svaggy.databinding.FragmentCancelledBinding
import com.svaggy.ui.activities.track.OrderDetailsActivity
import com.svaggy.ui.activities.track.TrackOrderActivity
import com.svaggy.ui.activities.track.TrackPickOrderActivity
import com.svaggy.ui.fragments.order.adapter.OrdersAdapter
import com.svaggy.ui.fragments.order.viewmodel.OrdersViewModel
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CancelledFragment : Fragment()
{
    lateinit var binding: FragmentCancelledBinding
    private val bundle = Bundle()
    private lateinit var ordersAdapter: OrdersAdapter
    private var isLoaderVisible : Boolean = false
    private val mViewModel by viewModels<OrdersViewModel>()
    //chat
    private var licenceNumber: String = "17868564"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCancelledBinding.inflate(inflater, container, false)
        initObserver()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mViewModel.ordersList(
            "Bearer ${PrefUtils.instance.getString(Constants.Token)}",
            "CANCELLED"
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
                  binding.progressBarMenu.hide()
                }
                else {
                    isLoaderVisible = true
                    binding.progressBarMenu.hide()
                    binding.noOrder.visibility = View.VISIBLE
                    binding.recyclerOrdersList.visibility = View.GONE
                }
            }
            else {
                isLoaderVisible = true
              binding.progressBarMenu.hide()
            }
        }
        mViewModel.reOrderDataMutable.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
              binding.progressBarMenu.hide()
                findNavController().navigate(R.id.cartScreen)
            } else {
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
              binding.progressBarMenu.hide()
            }
        }
        mViewModel.cancelOrderDataMutable.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
                mViewModel.ordersList(
                    "Bearer ${PrefUtils.instance.getString(Constants.Token)}",
                    "TODAY"
                )
             binding.progressBarMenu.hide()
            } else {
             binding.progressBarMenu.hide()
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
        mViewModel.errorMessage.observe(viewLifecycleOwner) {
           binding.progressBarMenu.hide()
            Toast.makeText(context, "" + it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getOrdersClick(orderId: Int, cancelClick: Boolean, trackClick: Boolean, reOrderClick: Boolean, restaurantClick: Boolean, giveReviewClick:Boolean,status:String,deliveryType:String)
    {
        if (cancelClick)
        {
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
        } else if (trackClick) {
            if (deliveryType == "DELIVERY_BY_SVAGGY"){
                val intent =  Intent(requireContext(), TrackOrderActivity::class.java)
                intent.putExtra("order_id", orderId.toString())
                intent.putExtra("isFrom","OrdersScreen")
                startActivity(intent)
            }
        } else if (restaurantClick) {
            val intent = Intent(requireContext(), OrderDetailsActivity::class.java)
            intent.putExtra("order_id",orderId.toString())
            intent.putExtra("isFrom","Cancelled")
            startActivity(intent)

        } else{
            val intent =  Intent(requireContext(), TrackPickOrderActivity::class.java)
            intent.putExtra("order_id", orderId.toString())
            intent.putExtra("isFrom","OrdersScreen")
            startActivity(intent)
        }
    }

    private fun initChatWindow() {
        val config = ChatWindowConfiguration.Builder()
            //
            .setLicenceNumber("18238530")
           // .setLicenceNumber("18087297")
            .build()

        startChatActivity(config)


    }

    private fun startChatActivity(config: ChatWindowConfiguration) {
        val intent = Intent(requireContext(), ChatWindowActivity::class.java)
        intent.putExtras(config.asBundle())
        startActivity(intent)
    }
}
