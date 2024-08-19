package com.svaggy.ui.fragments.order.screens

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.JsonObject
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.FragmentOrderReviewBinding
import com.svaggy.ui.fragments.payment.viewmodel.PaymentViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.onBackPressedDispatcher
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderReview : Fragment() {
   private lateinit var binding: FragmentOrderReviewBinding
    private val mViewModel by viewModels<PaymentViewModel>()
    private var getRating = 0
    private var orderID: String = ""
    private var isFrom: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOrderReviewBinding.inflate(inflater, container, false)
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = context?.getString(R.string.rate_order)
      //  (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility = View.GONE
        //PrefUtils.instance.setString(Constants.CurrentDestinationId,(activity as MainActivity).navController?.currentDestination?.id.toString())
        orderID = arguments?.getString("order_id").toString()
        isFrom = arguments?.getString("isFrom").toString()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBackPressedDispatcher {
            if (isFrom == "OrdersScreen")
                findNavController().popBackStack()
            else
                findNavController().popBackStack(R.id.homeFragment,false)




        }
        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            if (isFrom == "OrdersScreen")
                findNavController().popBackStack()
            else
                findNavController().popBackStack(R.id.homeFragment,false)
        }
        initBinding()



    }

    private fun initBinding() {
        binding.apply {
            ratingBar.numStars = 5
           ratingBar.rating = 0.0f
            ratingBar.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.yellow))
            ratingBar.setOnRatingBarChangeListener {_, rating, _ ->
                when(rating){
                    0.5F, 1.0F ->{getRating = 1}
                    1.5F, 2.0F ->{getRating = 2}
                    2.5F, 3.0F ->{getRating = 3}
                    3.5F, 4.0F ->{getRating = 4}
                    4.5F, 5.0F ->{getRating = 5}

                }

            }
           animation.setAnimation(R.raw.review)
            proceedToReview.setOnClickListener {
                sentReview()
            }
        }

    }

    private fun sentReview(){
        val jsonObject = JsonObject()
        jsonObject.addProperty("order_id", orderID)
        jsonObject.addProperty("reviews", binding.edtReview.text.toString())
        jsonObject.addProperty("ratings", getRating)
        lifecycleScope.launch {
            mViewModel.sentReview(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token)}",
                jsonObject =jsonObject).collect {

                when (it) {
                    is ApiResponse.Loading -> {
                       binding.progressBarMenu.show()
                    }

                    is ApiResponse.Success -> {
                      binding.progressBarMenu.hide()
                        val isSuccess = it.data?.is_success
                        if (isSuccess != null && isSuccess) {
                            findNavController().popBackStack(R.id.homeFragment,false)
                        }
                    }
                    is ApiResponse.Failure -> {
                        binding.progressBarMenu.hide()
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }


                }
            }
        }
    }
}