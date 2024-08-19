package com.svaggy.ui.activities.track

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityOrderDetailsBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.cart.adapter.BillDetailAdapter
import com.svaggy.ui.fragments.order.adapter.OrderItemDetailsAdapter
import com.svaggy.ui.fragments.order.screens.OrderDetailsAdapter
import com.svaggy.ui.fragments.order.viewmodel.OrdersViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.invisible
import com.svaggy.utils.show
import com.svaggy.utils.updateStatusBarColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class OrderDetailsActivity :BaseActivity<ActivityOrderDetailsBinding>(ActivityOrderDetailsBinding::inflate){
    private val mViewModel by viewModels<OrdersViewModel>()
    private var orderID: String? = ""
    private lateinit var ordersAdapter: OrderItemDetailsAdapter
    private lateinit var isFrom:String


    override fun ActivityOrderDetailsBinding.initialize(){
        updateStatusBarColor("#F6F6F6",true)
        orderID = intent.getStringExtra("order_id")
        isFrom = intent.getStringExtra("isFrom").toString()
        if (!orderID.isNullOrEmpty()) {
            orderDetailsObserver()
        }
       binding.backButton.setOnClickListener {
          onBackPressed()
        }
        if (isFrom == "TodayOrder"){
            binding.btTrackOrder.text = getString(R.string.trackorder)

        }else{
            binding.btTrackOrder.text = getString(R.string.go_to_home)

        }

        binding.btTrackOrder.setOnClickListener {
            if ( binding.btTrackOrder.text.equals("Track Order")){
                val intent = Intent(this@OrderDetailsActivity,TrackOrderActivity::class.java)
                intent.putExtra("order_id",orderID)
                startActivity(intent)
                finish()

            }else{
                val intent = Intent(this@OrderDetailsActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                finish()


            }
            //findNavController().popBackStack(R.id.trackOrderScreen,false)
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

                        ordersAdapter = OrderItemDetailsAdapter(this@OrderDetailsActivity,
                            it.data?.data?.cart_data?.cart_items,
                            it.data?.data?.currency_key ?: "",)
                        binding.recyclerProductItem.adapter = ordersAdapter
                        binding.itemAddCt.visibility = View.VISIBLE
                        binding.tvOrder.visibility = View.VISIBLE

                        val billDetailsAdapter = BillDetailAdapter(this@OrderDetailsActivity,it.data?.data!!.bill_details,:: walletAvailable)
                        binding.recyclerBillDetails.adapter = billDetailsAdapter
                        binding.billDetailsLayout.visibility = View.VISIBLE
                        binding.tvBill.visibility = View.VISIBLE

                        val orderDetailsAdapter = OrderDetailsAdapter(this@OrderDetailsActivity,it.data.data.order_details)
                        binding.recyclerOrderDetails.adapter = orderDetailsAdapter
                        binding.recyclerOrderDetails.visibility = View.VISIBLE
                        binding.tvOrderDetails.visibility = View.VISIBLE

                        binding.txtTotalAmount.text = it.data.data.cart_data.total_amount.toString()

                        binding.restaurantAddress.text = it.data.data.cart_data.restaurant_details.address?:""

                        binding.txtRestaurantName.text=it.data.data.cart_data.restaurant_details.restaurant_name
                        binding.btTrackOrder.visibility = View.VISIBLE
                        binding.progressBarMenu.hide()
                        if (it.data.data.order_status == "ORDER_DELIVERED" ||
                            it.data.data.order_status == "ORDER_CANCELLED"){
                            binding.btTrackOrder.invisible()
                        }


                    }
                    is ApiResponse.Failure -> {
                        binding.progressBarMenu.hide()
                        Toast.makeText(this@OrderDetailsActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }













        mViewModel.getOrderDetailsDataMutable.observe(this) {
            if (it.is_success) {
                binding.progressBarMenu.hide()




            } else {
                binding.progressBarMenu.hide()
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun walletAvailable(b: Boolean) {

    }

}