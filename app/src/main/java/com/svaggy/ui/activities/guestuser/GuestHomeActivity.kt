package com.svaggy.ui.activities.guestuser

import android.content.Intent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.badge.BadgeDrawable
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityGuestHomeBinding
import com.svaggy.imageslider.ImageModel
import com.svaggy.ui.activities.auth.PhoneLogInActivity
import com.svaggy.ui.fragments.cart.viewmodel.CartViewModel
import com.svaggy.ui.fragments.home.adapter.SliderAdapter
import com.svaggy.ui.fragments.home.model.SharedPrefModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class GuestHomeActivity : BaseActivity<ActivityGuestHomeBinding>(inflateMethod = ActivityGuestHomeBinding::inflate ) {
    private lateinit var navController: NavController
    val mViewModel by viewModels<CartViewModel>()
    lateinit var badgeDrawable: BadgeDrawable
    private lateinit var isFrom:String
    private var lastPosition = 0
    val imageUrl:ArrayList<ImageModel> = ArrayList()


    override fun ActivityGuestHomeBinding.initialize() {
        navController = this@GuestHomeActivity.findNavController(R.id.containerView)
        lastPosition = R.id.guestHomeFragment
        badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.guestCartFragment)
        badgeDrawable.badgeTextColor = getColor(R.color.white)
        badgeDrawable.backgroundColor = getColor(R.color.primaryColor)
        badgeDrawable.isVisible = false
        isFrom = intent.getStringExtra("isFrom").toString()

        if (isFrom == "CartScreen"){
            navController.navigate(R.id.guestCartFragment)
            bottomNavigationView.selectedItemId = R.id.guestCartFragment
        }
        showBottomNavigationHome()


    }

    override fun onResume() {
        super.onResume()
        getCartItem()
    }



    private fun showBottomNavigationHome() {
        binding.apply {
            bottomNavigationView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.guestHomeFragment -> {
                        lastPosition = R.id.guestHomeFragment
                        if (navController.currentDestination?.id != R.id.guestHomeFragment) {
                            navController.navigate(R.id.guestHomeFragment)
                        }
                        true
                    }

                    R.id.guestCartFragment -> {
                        lastPosition = R.id.guestCartFragment
                        if (navController.currentDestination?.id != R.id.guestCartFragment) {
                            navController.navigate(R.id.guestCartFragment)
                        }
                        true
                    }

                    R.id.profileScreen -> {
                    val intent = Intent(this@GuestHomeActivity, PhoneLogInActivity::class.java).putExtra("isFrom","GuestHome")
                        startActivityForResult(intent,101)
                        true
                    }

                    else -> false
                }
            }

        }

    }

    fun getCartItem() {
        lifecycleScope.launch {
            mViewModel.getCartItem(token = "${Constants.BEARER} ${
                PrefUtils.instance.getString(
                    Constants.Token).toString()}").collect{
                when (it) {
                    is ApiResponse.Loading -> {
                    }
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response?.isSuccess == true) {
                            PrefUtils.instance.clearCartItemsFromSharedPreferences()
                            if ((response.data?.cartItems?.size ?: 0) > 0) {
                                response.data?.cartItems?.forEach { item ->
                                    val listOf = listOf(
                                        SharedPrefModel(
                                            item.dishName ?: "",
                                            item.dishType ?: "",
                                            item.price ?: 0.0,
                                            item.isActive ?: false,
                                            item.menuItemId ?: 0,
                                            item.actualPrice ?: 0.0,
                                            item.quantity ?: 0,
                                            item.id ?: 0
                                        )
                                    )
                                    PrefUtils.instance.saveMenuItemsList("${item.menuItemId}", listOf)
                                  badgeDrawable.isVisible = true
                                    badgeDrawable.number = response.data?.totalCartQuantity ?: 0
                                }

                            } else {
                               badgeDrawable.isVisible = false
                            }
                            PrefUtils.instance.setString(Constants.CartRestaurantId, response.data?.restaurantDetails?.id.toString())

                        }
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(this@GuestHomeActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }
            }


        }

    }







    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.bottomNavigationView.selectedItemId = lastPosition
    }

}
