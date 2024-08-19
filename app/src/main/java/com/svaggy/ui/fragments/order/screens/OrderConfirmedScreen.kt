package com.svaggy.ui.fragments.order.screens

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.svaggy.databinding.FragmentOrderConfirmedScreenBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.payment.viewmodel.PaymentViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.onBackPressedDispatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderConfirmedScreen : Fragment() {
    lateinit var binding: FragmentOrderConfirmedScreenBinding
    private var orderId: String? = null
    private var orderType: Boolean? = null
    private var restaurantId: String? = null
    private var boosterList: IntArray? = null
    private val mViewModelOrder by viewModels<PaymentViewModel>()

    // Inside your activity or fragment


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderConfirmedScreenBinding.inflate(inflater, container, false)
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.GONE
       // (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility = View.GONE
        orderId = arguments?.getString("orderId")
        orderType = arguments?.getBoolean("checkOrderType")
        restaurantId = arguments?.getString("restaurantId")
       boosterList =  arguments?.getIntArray("boosterArray")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onBackPressedDispatcher {
            findNavController().popBackStack(R.id.homeFragment, false)

        }
        binding.orderConfirmGif.let { imageView ->
            Glide.with(this)
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
                                if (orderType != null && orderType!!)
                                findNavController().navigate(OrderConfirmedScreenDirections.actionOrderConfirmedScreenToTrackOrderScreen(orderId = orderId.toString()))
                                else
                                    findNavController().navigate(OrderConfirmedScreenDirections.actionOrderConfirmedScreenToTrackPickOrder(orderId = orderId.toString()))


                            }
                        })

                        return false
                    }
                })
                .into(imageView)
        }
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
            mViewModelOrder.setBoosterConversion(token =  "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
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
                                Toast.makeText(context, "" + data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }

    }
}

