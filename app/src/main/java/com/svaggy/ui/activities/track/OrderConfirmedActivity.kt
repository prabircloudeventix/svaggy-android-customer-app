package com.svaggy.ui.activities.track

import android.content.Intent
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityOrderConfimerdBinding
import com.svaggy.ui.fragments.payment.viewmodel.PaymentViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class OrderConfirmedActivity : BaseActivity<ActivityOrderConfimerdBinding>(ActivityOrderConfimerdBinding::inflate) {

    private var orderId: String? = null
    private var orderType: Boolean? = null
    private var deliveryType: String? = null
    private var restaurantId: String? = null
    private var boosterList: ArrayList<Int>? = null
    private val mViewModelOrder by viewModels<PaymentViewModel>()

    override fun ActivityOrderConfimerdBinding.initialize(){
        orderId = intent.getStringExtra("orderId")
        orderType = intent.getBooleanExtra("checkOrderType",false)
        restaurantId = intent.getStringExtra("restaurantId")
        deliveryType = intent.getStringExtra("deliveryType")
        boosterList =  intent?.getSerializableExtra("boosterArray") as? ArrayList<Int>
        binding.orderConfirmGif.let { imageView ->
            Glide.with(this@OrderConfirmedActivity)
                .asGif()
                .load(R.drawable.order_confirm)
                .listener(object : RequestListener<GifDrawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<GifDrawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: GifDrawable,
                        model: Any,
                        target: Target<GifDrawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource.setLoopCount(2)
                        resource.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                            override fun onAnimationEnd(drawable: Drawable?) {
                                if (deliveryType.equals("PICKUP_ONLY")){
                                        val intent = Intent(this@OrderConfirmedActivity,TrackPickOrderActivity::class.java)
                                        intent.putExtra("order_id",orderId.toString())
                                        intent.putExtra("isFrom","OrderConfirmed")
                                        startActivity(intent)
                                        finish()

                                   }else{
                                       if (orderType!!){
                                           val intent = Intent(this@OrderConfirmedActivity,TrackOrderActivity::class.java)
                                           intent.putExtra("order_id",orderId.toString())
                                           intent.putExtra("isFrom","OrderConfirmed")
                                           startActivity(intent)
                                           finish()

                                       }else{
                                           val intent = Intent(this@OrderConfirmedActivity,TrackPickOrderActivity::class.java)
                                           intent.putExtra("order_id",orderId.toString())
                                           intent.putExtra("isFrom","OrderConfirmed")
                                           startActivity(intent)
                                           finish()

                                       }

                                    }

//                                if (orderType != null && orderType!!)
//                                    findNavController().navigate(OrderConfirmedScreenDirections.actionOrderConfirmedScreenToTrackOrderScreen(orderId = orderId.toString()))
//                                else
//                                    findNavController().navigate(OrderConfirmedScreenDirections.actionOrderConfirmedScreenToTrackPickOrder(orderId = orderId.toString()))


                            }
                        })

                        return false
                    }
                })
                .into(imageView)
        }
        PrefUtils.instance.clearCartItemsFromSharedPreferences()
        PrefUtils.instance.setString("IS_PROMO", "")
        if (boosterList != null && orderId != null && restaurantId != null){
            val boosterArray = ArrayList<Int>()
            boosterList?.forEach {
                boosterArray.add(it)
            }
            setBoosterAction(boosterArray = boosterArray, orderId = orderId!!.toInt())
        }

    }


    private fun setBoosterAction(boosterArray: ArrayList<Int>, orderId: Int?) {
        val json = JsonObject()
        val boosterJsonArray = JsonArray()
        for (boosterId in boosterArray) {
            boosterJsonArray.add(boosterId)
        }
        json.add("booster_ids",boosterJsonArray)
        json.addProperty("type","CONVERSION")
        json.addProperty("restaurant_id", restaurantId!!.toInt())
        json.addProperty("order_id",orderId)

        lifecycleScope.launch {
            mViewModelOrder.setBoosterConversion(token =  "${Constants.BEARER} ${
                PrefUtils.instance.getString(
                    Constants.Token).toString()}",
                jsonObject = json).collect{

                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        if (data != null){
                            if (data.is_success!!) {
                                //Remove Shared Pref Booster List
                                PrefUtils.instance.removeBoosterList("Booster")

                            }
                            else {
                                Toast.makeText(this@OrderConfirmedActivity, "" + data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(this@OrderConfirmedActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }

    }

}