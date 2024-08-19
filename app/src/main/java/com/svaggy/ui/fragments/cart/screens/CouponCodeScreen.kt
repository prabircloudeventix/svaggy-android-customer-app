package com.svaggy.ui.fragments.cart.screens


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.FragmentCouponCodeScreenBinding
import com.svaggy.ui.fragments.cart.adapter.CouponsAdapter
import com.svaggy.ui.fragments.cart.viewmodel.CartViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.BEARER
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.onBackPressedDispatcher
import com.svaggy.utils.show
import com.svaggy.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CouponCodeScreen : Fragment() {
    lateinit var binding: FragmentCouponCodeScreenBinding
    private val mViewModel by viewModels<CartViewModel>()
    private var couponsAdapter: CouponsAdapter? = null
    private  var restaurantId :String? = null
    private var checkOutId :String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCouponCodeScreenBinding.inflate(inflater, container, false)

//        PrefUtils.instance.setString(
//            Constants.CurrentDestinationId,
//            (activity as MainActivity).navController.currentDestination?.id.toString()
//        )
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = ContextCompat.getString(requireContext(), R.string.coupons)
       // (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility = View.GONE
        restaurantId = arguments?.getString("restaurantId")
        checkOutId = arguments?.getString("checkOutId")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onBackPressedDispatcher {
            findNavController().popBackStack(R.id.cartMoreItemScreen,false)

        }
        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack(R.id.cartMoreItemScreen,false)
        }
        getCoupon(restaurantId)


    }

    private fun getCoupon(restaurantId:String?) {
        lifecycleScope.launch {
            mViewModel.getCoupon(
                authToken = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                restaurantId = restaurantId ?: "").collect {

                when (it) {
                    is ApiResponse.Loading -> {


                    }

                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response != null){
                            couponsAdapter = CouponsAdapter(response.data) { couponId,  ->
                                lifecycleScope.launch {
                                    mViewModel.setCoupon(authToken = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                                        checkoutId = checkOutId!!,
                                        couponId = couponId,
                                        isCouponAdded = true).collect{res ->
                                        when (res) {
                                            is ApiResponse.Loading -> {


                                            }
                                            is ApiResponse.Success -> {
                                                if (res.data != null && res.data.isSuccess == true){
                                                    findNavController().popBackStack(R.id.cartMoreItemScreen,false)
                                                }else{
                                                    binding.root.showSnackBar(res.data!!.message.toString())


                                                }

                                            }
                                            is ApiResponse.Failure -> {
                                                Toast.makeText(requireContext(),res.msg,Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            }
                            binding.recyclerCoupons.adapter = couponsAdapter

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