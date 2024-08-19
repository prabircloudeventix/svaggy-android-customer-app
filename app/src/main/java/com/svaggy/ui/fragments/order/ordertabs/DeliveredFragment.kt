package com.svaggy.ui.fragments.order.ordertabs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.livechatinc.inappchat.ChatWindowActivity
import com.livechatinc.inappchat.ChatWindowConfiguration
import com.svaggy.R
import com.svaggy.databinding.FragmentDeliveredBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.track.OrderDetailsActivity
import com.svaggy.ui.activities.track.TrackOrderActivity
import com.svaggy.ui.activities.track.TrackPickOrderActivity
import com.svaggy.ui.activities.review.OrderReviewActivity
import com.svaggy.ui.fragments.order.adapter.OrderDeliveredAdapter
import com.svaggy.ui.fragments.order.viewmodel.OrdersViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.BEARER
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.createCustomDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DeliveredFragment : Fragment() {
    lateinit var binding: FragmentDeliveredBinding
    private lateinit var ordersAdapter: OrderDeliveredAdapter
    private val mViewModel by viewModels<OrdersViewModel>()
    private var progressDialog: Dialog? = null
    //chat

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDeliveredBinding.inflate(inflater, container, false)
        initObserver()
    //    progressBarHelper = ProgressBarHelper(requireContext())
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mViewModel.ordersList(
            "$BEARER ${PrefUtils.instance.getString(Constants.Token)}",
            "DELIVERED"
        )
    }
    private fun initObserver() {
        mViewModel.getOrdersListDataMutable.observe(viewLifecycleOwner) {
            if (it.isSuccess!!)
            {
                if (!it?.data?.orderData.isNullOrEmpty()) {
                    binding.noOrder.visibility =View.GONE
                    binding.recyclerOrdersList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    ordersAdapter = OrderDeliveredAdapter(requireContext(),it.data?.orderData, it.data?.type ?: "",
                        ::getOrdersClick,
                        clickSupport = {
                            initChatWindow()

                        })
                    binding.recyclerOrdersList.adapter = ordersAdapter
                }
                else {
                    binding.noOrder.visibility = View.VISIBLE
                    binding.recyclerOrdersList.visibility = View.GONE
                }
            } else
            {
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
        mViewModel.reOrderDataMutable.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
                if (it.message == "Items added to cart for re-order!!"){
                    findNavController().navigate(R.id.cartScreen)
                }else{
                    val dialog = requireContext().createCustomDialog(R.layout.re_order_dialog) {}
                    dialog.show()

                    val tvMassage = dialog.findViewById<TextView>(R.id.getMessage)
                    val okBtn = dialog.findViewById<TextView>(R.id.btnDialogPositive)
                    val cancelBtn = dialog.findViewById<TextView>(R.id.btnDialogNegative)
                    okBtn.setOnClickListener {
                        dialog.hide()

                    }
                    cancelBtn.setOnClickListener {
                        dialog.hide()

                    }
                    tvMassage?.text = "${it.message}"


                }
            } else {
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
        mViewModel.cancelOrderDataMutable.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
                mViewModel.ordersList(
                    "$BEARER ${PrefUtils.instance.getString(Constants.Token)}",
                    "TODAY"
                )
            } else {
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
        mViewModel.errorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(context, "" + it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getOrdersClick(orderId: Int,
                               cancelClick: Boolean,
                               trackClick: Boolean,
                               reOrderClick: Boolean,
                               restaurantClick: Boolean,
                               giveReviewClick:Boolean,
                               status:String,
                               deliveryType:String,
                               restaurantName:String) {
        if (cancelClick) {
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
                        "$BEARER ${PrefUtils.instance.getString(Constants.Token)}",
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
                intent.putExtra("restaurantName",restaurantName)
                startActivity(intent)

            }
            else{
                val intent =  Intent(requireContext(), TrackPickOrderActivity::class.java)
                intent.putExtra("order_id", orderId.toString())
                intent.putExtra("isFrom","OrdersScreen")
                intent.putExtra("restaurantName",restaurantName)
                startActivity(intent)
            }
        } else if (restaurantClick) {
            val intent = Intent(requireContext(), OrderDetailsActivity::class.java)
            intent.putExtra("order_id",orderId.toString())
            intent.putExtra("isFrom","OrdersScreen")
            startActivity(intent)
        } else if (giveReviewClick) {
            val intent = Intent(requireContext(), OrderReviewActivity::class.java)
            intent.putExtra("order_id",orderId.toString())
            intent.putExtra("isFrom","OrdersScreen")
            intent.putExtra("restaurant_name",restaurantName)
            startActivity(intent)
        } else if (reOrderClick) {
            reOrder(orderId.toString(),null)


        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun reOrder(orderId: String, key:String?) {
        val map = mutableMapOf(
            "order_id" to orderId)
        if (key != null)
            map["key"] = key
        lifecycleScope.launch {
            mViewModel.forceReOrder(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token)}",
                map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                       showProgressBar(requireContext())
                    }
                    is ApiResponse.Success -> {
                        progressDialog?.dismiss()
                        val data = it.data
                        if (data != null){
                            if (data.isSuccess!!) {
                                if (data.message == "Items added to cart for re-order!!"){
                                 //   findNavController().navigate(R.id.cartScreen)
                                    val intent =Intent(requireContext(),MainActivity::class.java)
                                    intent.putExtra("isFrom","ReOrderScreen")
                                    startActivity(intent)
                                }
                                else{
                                    val dialog = requireContext().createCustomDialog(R.layout.re_order_dialog) {}
                                    dialog.show()
                                    val tvMassage = dialog.findViewById<TextView>(R.id.getMessage)
                                    val okBtn = dialog.findViewById<TextView>(R.id.btnDialogPositive)
                                    val cancelBtn = dialog.findViewById<TextView>(R.id.btnDialogNegative)
                                    okBtn.setOnClickListener {
                                        dialog.hide()
                                        if (data.message != "All items are unavailable")
                                        reOrder(orderId,"EXCLUDE_DELETED_ITEMS")

                                    }
                                    cancelBtn.setOnClickListener {
                                        dialog.hide()

                                    }
                                    tvMassage?.text = "${data.message}"

                                }
                            }
                            else {
                                Toast.makeText(context, "" + data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                    is ApiResponse.Failure -> {
                 //   progressBarHelper.dismissProgressBar()
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }

    private   fun showProgressBar(context: Context): Dialog? {
        if (progressDialog == null)
            progressDialog = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar)
        progressDialog?.setContentView(LayoutInflater.from(context).inflate(R.layout.progress_bar, FrameLayout(context), false))
        progressDialog?.show()
        return progressDialog
    }


    private fun initChatWindow() {
        val config = ChatWindowConfiguration.Builder()
            .setLicenceNumber("18238530")
          //  .setLicenceNumber("18238530")
           // .setLicenceNumber("18087297")
            .setVisitorName(PrefUtils.instance.getString(Constants.UserFirstName))
            .setVisitorEmail(PrefUtils.instance.getString(Constants.UserEmail))
            .build()
        startChatActivity(config)


    }

    private fun startChatActivity(config: ChatWindowConfiguration) {
        val intent = Intent(requireContext(), ChatWindowActivity::class.java)
        intent.putExtras(config.asBundle())
        startActivity(intent)
    }
}