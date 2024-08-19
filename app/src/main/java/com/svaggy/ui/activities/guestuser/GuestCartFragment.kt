package com.svaggy.ui.activities.guestuser

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.svaggy.R
import com.svaggy.app.SvaggyApplication
import com.svaggy.client.models.AddOnsModel
import com.svaggy.client.models.MenuItem
import com.svaggy.databinding.FragmentGuestCartBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.auth.PhoneLogInActivity
import com.svaggy.ui.activities.restaurant.RestaurantMenuActivity
import com.svaggy.ui.fragments.cart.adapter.CartItemAdapter
import com.svaggy.ui.fragments.cart.viewmodel.CartViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.ProgressBarLoader
import com.svaggy.utils.onBackPressedDispatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GuestCartFragment : Fragment() {

    private  var _binding: FragmentGuestCartBinding? = null
    val binding get() = _binding!!
    private val mViewModel by viewModels<CartViewModel>()
    private var cartItemAdapter: CartItemAdapter? = null
    private  var restaurantId:String = ""
    private  var getDeliveryType:String = ""
    private val progressBarLoader by lazy {
        ProgressBarLoader()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGuestCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBackPressedDispatcher {
            findNavController().popBackStack(R.id.guestHomeFragment,false)
        }



        binding.txtViewMenu.setOnClickListener {
            // findNavController().navigate(R.id.action_cartScreen_to_restaurantMenuScreen, bundle)
            val intent = Intent(requireContext(), RestaurantMenuActivity::class.java)
            //intent.putExtra("item_id",restaurantId)
            PrefUtils.instance.setString("item_id", restaurantId)
            startActivity(intent)

        }
        binding.cartContinueBt.setOnClickListener {
            startActivity(Intent(requireContext(), PhoneLogInActivity::class.java).putExtra("isFrom","GuestCart"))


        }
        binding.txtViewRestaurant.setOnClickListener {
            findNavController().popBackStack(R.id.guestHomeFragment,false)
            (activity as GuestHomeActivity).binding.bottomNavigationView.selectedItemId = R.id.guestHomeFragment
        }
        binding.deleteAllCart.setOnClickListener {
            deleteDialog()

        }
        /**
         * get cart item
         */

        getCartItem()
    }
    private fun deleteDialog(){
        PrefUtils.instance.showCustomDialog(
            context = requireContext(),
            message =requireContext().getString(R.string.text_cart,binding.txtRestaurantName.text),
            txtNegative =requireContext().getString(R.string.no),
            txtPositive = requireContext().getString(R.string.yes),
            note = requireContext().getString(R.string.clear_cart),
            isNoteShow = false,
            isNegativeShow = true,
            negativeAlert = {
                it.dismiss()
            },
            positiveAlert = {
                deleteAllCartItem()
                it.dismiss()
                getCartItem()
            }
        )
    }

    private fun deleteAllCartItem(){
        lifecycleScope.launch {
            mViewModel.deleteAllCartItem(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}").collect{
                when (it) {
                    is ApiResponse.Loading -> {}
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response != null){
                            if (response.isSuccess!!) {
                                PrefUtils.instance.clearCartItemsFromSharedPreferences()
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
    @SuppressLint("SetTextI18n")
    private fun getCartItem(isLoaderShow:Boolean = true) {
        lifecycleScope.launch {
            mViewModel.getCartItem(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}").collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        if (isLoaderShow)
                            SvaggyApplication.progressBarLoader.start(requireContext())
                    }
                    is ApiResponse.Success -> {
                        SvaggyApplication.progressBarLoader.stop()
                        val response = it.data
                        if (response != null){
                            if (response.isSuccess!!) {
                                restaurantId = response.data?.restaurantDetails?.id.toString()
                                getDeliveryType = response.data?.restaurantDetails?.deliveryType.toString()
                                if (response.data?.cartItems?.size != 0) {
                                    binding.txtRestaurantName.visibility = View.VISIBLE
                                    binding.txtViewMenu.visibility = View.VISIBLE
                                    binding.cartRecyclerView.visibility = View.VISIBLE
                                    binding.totalAmountCl.visibility = View.VISIBLE
                                    binding.consEmptyCart.visibility = View.GONE

                                    cartItemAdapter = CartItemAdapter(
                                        context =  requireContext(),
                                        activity =  activity as GuestHomeActivity,
                                        arrayList =  response.data?.cartItems,
                                        updateQuantity = {menuItemId, cartId, quantity, addOns ->
                                            updateAddOn(menuItemId = menuItemId,cartId = cartId, quantity = quantity, addOns =addOns)
                                        },
                                        updateAddOn = {cartId, quantity, addOns ->
                                            updateAddOn(menuItemId = null,cartId = cartId, quantity = quantity, addOns =addOns)
                                        },
                                        dataReset = {
                                            getCartItem()

                                        }
                                    )
                                    binding.cartRecyclerView.adapter = cartItemAdapter
                                    binding.txtRestaurantName.text = response.data?.restaurantDetails?.restaurantName
                                    PrefUtils.instance.setString(Constants.RestaurantName,response.data?.restaurantDetails?.restaurantName)
                                    binding.txtTotalPrice.text = "CZK " +PrefUtils.instance.formatDouble(response.data?.totalAmount ?: 0.0)
                                    (activity as GuestHomeActivity).badgeDrawable.isVisible = true
                                    (activity as GuestHomeActivity).badgeDrawable.number = response.data?.totalCartQuantity ?: 0
                                } else {
                                    (activity as GuestHomeActivity).badgeDrawable.isVisible = false
                                    binding.txtRestaurantName.visibility = View.GONE
                                    binding.txtViewMenu.visibility = View.GONE
                                    binding.totalAmountCl.visibility = View.GONE
                                    binding.cartRecyclerView.visibility = View.GONE
                                    binding.consEmptyCart.visibility = View.VISIBLE
                                }
                            }
                        }




                    }
                    is ApiResponse.Failure -> {
                        SvaggyApplication.progressBarLoader.stop()

                    }

                }
            }


        }

    }


    private fun cartUpdate(menuItemId:Int?,cartId: Int, quantity: Int, addOns: ArrayList<Int>) {
        lifecycleScope.launch {
            mViewModel.updateCartItem(token = "${Constants.BEARER} ${
                PrefUtils.instance.getString(
                    Constants.Token).toString()}",
                cartId = cartId,
                quantity = quantity).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response?.isSuccess == true){
                            if (quantity == 0){
                                PrefUtils.instance.removeMenuItemsList("$menuItemId")
                                PrefUtils.instance.removeBoosterList("Booster")
                                ( activity as GuestHomeActivity).getCartItem()
                            }
                            getCartItem()

                        }

                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }
    }

    private fun updateAddOn(menuItemId:Int?,cartId: Int, quantity: Int,addOns: ArrayList<Int>){
        val addOnsToSend = if (addOns.isEmpty()) arrayListOf() else addOns



        val addOnsModel = AddOnsModel(
            menu_item = MenuItem(
                quantity = quantity,
                cart_id = cartId,
                add_ons = addOnsToSend
            )
        )



        lifecycleScope.launch {
            mViewModel.updateAddOn2(
                token ="${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                addOnModel =addOnsModel).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {

                        val response = it.data
                        if (response?.isSuccess == true) {
                                if (quantity == 0){
                                    PrefUtils.instance.removeMenuItemsList("$menuItemId")
                                    PrefUtils.instance.removeBoosterList("Booster")
                                    ( activity as GuestHomeActivity).getCartItem()
                                }
                                getCartItem(isLoaderShow = false)
                            }

                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
//            mViewModel.updateAddOn(
//                token ="$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
//                cartId = cartId,
//                quantity = quantity,
//                addOns = arrayListOf()
//            ).collect{
//                when (it) {
//                    is ApiResponse.Loading -> {
//
//                    }
//                    is ApiResponse.Success -> {
//
//                        val response = it.data
//                        if (response != null){
//                            if (response.isSuccess!!) {
//                                getCartItem()
//                            }
//                        }
//                    }
//                    is ApiResponse.Failure -> {
//
//                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
//                    }
//
//                }
//
//            }
        }
    }






}