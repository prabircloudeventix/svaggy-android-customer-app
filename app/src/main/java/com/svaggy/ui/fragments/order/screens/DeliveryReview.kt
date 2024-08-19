package com.svaggy.ui.fragments.order.screens

import android.content.res.ColorStateList
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
import com.svaggy.R
import com.svaggy.databinding.FragmentDeliveryReviewBinding
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
class DeliveryReview : Fragment() {

    private var _binding: FragmentDeliveryReviewBinding? = null
    private val binding get() = _binding!!
    private val mViewModel by viewModels<PaymentViewModel>()
    private var getRating = 0
    private var orderID: String = ""





    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDeliveryReviewBinding.inflate(inflater, container, false)
        orderID = arguments?.getString("order_id").toString()
        onBackPressedDispatcher {
            findNavController().popBackStack(R.id.homeFragment,false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBinding()


        binding.btnBack.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)

        }

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
            binding.animation.setAnimation(R.raw.delivery_guy_new)
            proceedToReview.setOnClickListener {
                sentReview()
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    private fun sentReview(){
        val jsonObject = JsonObject()
        jsonObject.addProperty("order_id", orderID)
        jsonObject.addProperty("reviews", binding.edtReview.text.toString())
        jsonObject.addProperty("ratings", getRating)
        jsonObject.addProperty("review_type", "DRIVER_REVIEW")
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