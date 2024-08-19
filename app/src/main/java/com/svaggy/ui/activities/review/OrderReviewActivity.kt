package com.svaggy.ui.activities.review

import android.content.Intent
import android.content.res.ColorStateList
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonObject
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityDeliveryReviewBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.payment.viewmodel.PaymentViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderReviewActivity : BaseActivity<ActivityDeliveryReviewBinding>(ActivityDeliveryReviewBinding::inflate) {
    private val mViewModel by viewModels<PaymentViewModel>()
    private var getRating = 0
    private var orderID: String = ""
    private var isFrom: String = ""
    private var restaurantName: String = ""


    override fun ActivityDeliveryReviewBinding.initialize(){

        orderID = intent?.getStringExtra("order_id").toString()
        isFrom = intent?.getStringExtra("isFrom").toString()
        restaurantName = intent?.getStringExtra("restaurant_name").toString()
        binding.txtRateOrder.text = getString(R.string.how_was_your_food_from_anchor_james,restaurantName)

        binding.btnBack.setOnClickListener {
            if (isFrom == "OrdersScreen"){
                super.onBackPressed()}
            else{
                startActivity(Intent(this@OrderReviewActivity,MainActivity::class.java))}
        }
        initBinding()

    }
    private fun initBinding() {
        binding.apply {
            ratingBar.numStars = 5
            ratingBar.rating = 0.0f
            ratingBar.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(this@OrderReviewActivity, R.color.yellow))
            ratingBar.setOnRatingBarChangeListener {_, rating, _ ->
                when(rating){
                    0.0F ->{getRating = 0}
                    0.5F, 1.0F ->{getRating = 1}
                    1.5F, 2.0F ->{getRating = 2}
                    2.5F, 3.0F ->{getRating = 3}
                    3.5F, 4.0F ->{getRating = 4}
                    4.5F, 5.0F ->{getRating = 5}

                }

            }
            animation.setAnimation(R.raw.review)
            proceedToReview.setOnClickListener {
                if (orderID != "null" && getRating != 0){
                sentReview()
                }
            }
        }

    }

    private fun sentReview(){
        val jsonObject = JsonObject()
        jsonObject.addProperty("order_id", orderID.toInt())
        jsonObject.addProperty("reviews", binding.edtReview.text.toString())
        jsonObject.addProperty("ratings", getRating)
        jsonObject.addProperty("review_type", "ORDER_REVIEW")
        lifecycleScope.launch {
            mViewModel.sentReview(token = "${Constants.BEARER} ${
                PrefUtils.instance.getString(
                    Constants.Token)}",
                jsonObject =jsonObject).collect {

                when (it) {
                    is ApiResponse.Loading -> {
                        binding.progressBarMenu.show()
                    }

                    is ApiResponse.Success -> {
                        binding.progressBarMenu.hide()
                        val isSuccess = it.data?.is_success
                        if (isSuccess != null && isSuccess) {
                           startActivity(Intent(this@OrderReviewActivity,MainActivity::class.java))
                        }
                    }
                    is ApiResponse.Failure -> {
                        binding.progressBarMenu.hide()
                        Toast.makeText(this@OrderReviewActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }


                }
            }
        }
    }

    override fun onBackPressed() {
        if (isFrom == "OrdersScreen")
            super.onBackPressed()
        else
           startActivity(Intent(this,MainActivity::class.java))


    }
}