package com.svaggy.ui.activities.restaurant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityCouponBinding
import com.svaggy.databinding.FragmentCouponCodeScreenBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.cart.adapter.CouponsAdapter
import com.svaggy.ui.fragments.cart.viewmodel.CartViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import com.svaggy.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CouponActivity : BaseActivity<ActivityCouponBinding>(ActivityCouponBinding::inflate) {
    private val mViewModel by viewModels<CartViewModel>()
    private var couponsAdapter: CouponsAdapter? = null
    private  var restaurantId :String? = null
    private var checkOutId :String? = null

   override fun ActivityCouponBinding.initialize(){


       restaurantId = intent.getStringExtra("restaurantId")
       checkOutId = intent.getStringExtra("checkOutId")


      binding.backButton.setOnClickListener {
          onBackPressed()
       }
       getCoupon(restaurantId)

    }

    private fun getCoupon(restaurantId:String?) {
        lifecycleScope.launch {
            mViewModel.getCoupon(
                authToken = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                restaurantId = restaurantId ?: "").collect {

                when (it) {
                    is ApiResponse.Loading -> {


                    }

                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response != null ){
                            couponsAdapter = CouponsAdapter(response.data) { couponId,  ->
                                lifecycleScope.launch {
                                    mViewModel.setCoupon(authToken = "${Constants.BEARER} ${
                                        PrefUtils.instance.getString(
                                            Constants.Token).toString()}",
                                        checkoutId = checkOutId!!,
                                        couponId = couponId,
                                        isCouponAdded = true).collect{res ->
                                        when (res) {
                                            is ApiResponse.Loading -> {


                                            }
                                            is ApiResponse.Success -> {
                                                if (res.data != null && res.data.isSuccess == true){
                                                    onBackPressed()
                                                }else{
                                                    binding.root.showSnackBar(res.data!!.message.toString())

                                                }
                                            }
                                            is ApiResponse.Failure -> {
                                                Toast.makeText(this@CouponActivity,res.msg, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            }
                            binding.recyclerCoupons.show()
                            binding.noPromoView.hide()
                            binding.recyclerCoupons.adapter = couponsAdapter
                            if (response.data.isEmpty()){
                                binding.recyclerCoupons.hide()
                                binding.noPromoView.show()
                            }

                        }



                    }

                    is ApiResponse.Failure -> {
                        Toast.makeText(this@CouponActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }


            }

        }
    }


}