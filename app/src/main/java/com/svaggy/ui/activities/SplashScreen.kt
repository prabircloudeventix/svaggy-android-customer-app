package com.svaggy.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivitySplashScreenNewBinding
import com.svaggy.ui.activities.auth.PhoneLogInActivity
import com.svaggy.ui.activities.guestuser.GuestHomeActivity
import com.svaggy.ui.activities.intro.FirstIntroActivity
import com.svaggy.ui.activities.track.TrackOrderActivity
import com.svaggy.ui.activities.review.OrderReviewActivity
import com.svaggy.ui.fragments.home.adapter.SliderAdapter
import com.svaggy.ui.fragments.home.viewmodel.HomeViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.updateStatusBarColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreen :  BaseActivity<ActivitySplashScreenNewBinding>(ActivitySplashScreenNewBinding::inflate)  {
    private var type = ""
    private var orderId = ""
    private var reviewType = ""
    private var restaurantName = ""
    private  var restaurantId:String =""
    private  var broadCastId:String = ""
    private val mViewModel by viewModels<HomeViewModel>()




    override fun ActivitySplashScreenNewBinding.initialize() {
        this@SplashScreen.updateStatusBarColor("#DB5E5B",false)

        getCartItem()
        if (intent.extras != null ) {
            if ( intent.extras!!.containsKey("type")){
                type = intent.extras!!.getString("type").toString()
                restaurantId = intent.getStringExtra("restaurant_id").toString()
                broadCastId = intent.getStringExtra("broadcast_id").toString()
            }
            orderId = intent.getStringExtra("order_id") ?: ""
            reviewType = intent.getStringExtra("review_type") ?: ""
            restaurantName = intent.getStringExtra("restaurant_name") ?: ""

//            orderId = intent.extras!!.getString(" order_id").toString()
//            reviewType = intent.extras!!.getString(" review_type").toString()

        }

    }

    override fun onResume() {
        super.onResume()
        if (PrefUtils.instance.hasInternetConnection(this)) {
            lifecycleScope.launch {
                delay(3000)
                if (PrefUtils.instance.getString(Constants.IsLogin) == "true") {
                    if (!orderId.isNullOrEmpty()){
                        if (reviewType == "ORDER_REVIEW"){
                            val intent = Intent(this@SplashScreen,OrderReviewActivity::class.java)
                            intent.putExtra("order_id",orderId)
                            intent.putExtra("restaurant_name",restaurantName)
                            intent.putExtra("isFrom","Splash")
                            startActivity(intent)
                        }else{
                            Log.d("preet",orderId)
                            val intent = Intent(this@SplashScreen,TrackOrderActivity::class.java)
                            intent.putExtra("order_id",orderId)
                            intent.putExtra("restaurant_name",restaurantName)
                            intent.putExtra("isFrom","Splash")
                            startActivity(intent)

                        }


                    }
                    else{
                        val intent = Intent(this@SplashScreen,MainActivity::class.java)
                        intent.putExtra("type",type)
                        intent.putExtra("restaurant_id",restaurantId)
                        intent.putExtra("broadcast_id",broadCastId)
                        startActivity(intent)
                        finish()


                    }

                } else {
                    if (!PrefUtils.instance.getBoolean(Constants.IsNotFirstOpen)) {
                        Constants.imageUrl.clear()
                        Constants.imageUrl.add(R.drawable.offer1)
                        Constants.imageUrl.add(R.drawable.offer2)
                        Constants.imageUrl.add(R.drawable.offer3)
                        startActivity(Intent(this@SplashScreen,FirstIntroActivity::class.java))
                        finish()
                    } else {
                        if (PrefUtils.instance.getString(Constants.IsGuestUser) == "true"){
                            val intent = Intent(this@SplashScreen,GuestHomeActivity::class.java)
                            intent.putExtra("type",type)
                            intent.putExtra("restaurant_id",restaurantId)
                            intent.putExtra("broadcast_id",broadCastId)
                            startActivity(intent)
                            finish() }
                        else{

                            startActivity(Intent(this@SplashScreen,PhoneLogInActivity::class.java))
                            finish()

                        }


                    }
                }
            }
        } else {
            PrefUtils.instance.alertDialog(this,
                getString(R.string.app_name),
                "Unable to connect to the internet. Please check your network connection and try again.",
                okAlert = {
                    it.dismiss()
                  //  activity?.finish()
                }, cancelAlert = {
                    it.dismiss()
                },
                false
            )
        }
    }


    private fun getCartItem() {
        lifecycleScope.launch {
            mViewModel.getCartItem(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}").collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response!!.isSuccess!!) {
                            Constants.imageUrl.clear()
                            if (response.data?.isShowPromoBanner == true && response.data?.isComboAvailable == true){
                                PrefUtils.instance.setBoolean("IS_SHOW_PROMO",true)
                                PrefUtils.instance.setBoolean("IS_COMBO_AVAILABLE",true)
                                    Constants.imageUrl.add(R.drawable.offer1)
                                    Constants.imageUrl.add(R.drawable.offer2)
                                    Constants.imageUrl.add(R.drawable.offer3)

                            }
                            else{
                                PrefUtils.instance.setBoolean("IS_SHOW_PROMO",false)
                                PrefUtils.instance.setBoolean("IS_COMBO_AVAILABLE",false)
                                if (response.data?.isShowPromoBanner == true){
                                    PrefUtils.instance.setBoolean("IS_SHOW_PROMO",true)
                                    Constants.imageUrl.add(R.drawable.offer1)
                                    Constants.imageUrl.add(R.drawable.offer2)
                                  //  Constants.imageUrl.add(R.drawable.offer3)
                                }
                                if (response.data?.isComboAvailable == true){
                                    PrefUtils.instance.setBoolean("IS_COMBO_AVAILABLE",true)
                                    //  Constants.imageUrl.add(R.drawable.offer1)
                                    Constants.imageUrl.add(R.drawable.offer2)
                                    Constants.imageUrl.add(R.drawable.offer3)
                                }
                                if (response.data?.isShowPromoBanner == false && response.data?.isComboAvailable == false){
                                    Constants.imageUrl.add(R.drawable.offer2)

                                }



                            }

                        }
                    }
                    is ApiResponse.Failure -> {
                        Constants.imageUrl.clear()
                        Constants.imageUrl.add(R.drawable.offer1)
                        Constants.imageUrl.add(R.drawable.offer2)
                        Constants.imageUrl.add(R.drawable.offer3)
                    }

                }
            }


        }

    }

}