package com.svaggy.ui.fragments.cart.screens

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
import com.svaggy.databinding.FragmentCartBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.checkout.CheckOutActivity
import com.svaggy.ui.activities.restaurant.RestaurantMenuActivity
import com.svaggy.ui.fragments.cart.adapter.CartItemAdapter
import com.svaggy.ui.fragments.cart.viewmodel.CartViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.BEARER
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.onBackPressedDispatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartScreen : Fragment() {
    private  var _binding: FragmentCartBinding? = null
    val binding get() = _binding!!
    private val mViewModel by viewModels<CartViewModel>()
    private var cartItemAdapter: CartItemAdapter? = null
    private  var restaurantId:String = ""
    private  var getDeliveryType:String = ""


    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        PrefUtils.instance.setString(Constants.FragmentBackName, "CartScreen")
        PrefUtils.instance.setString(Constants.DeliveryDate, "")
        PrefUtils.instance.setString(Constants.DeliveryTime, "")
      //  (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.GONE
      //  (activity as MainActivity).showBottomNavigationHome()
      //  itemCount = (activity as MainActivity).findViewById(R.id.tv_count_text)
     //   countLayout = (activity as MainActivity).findViewById<ConstraintLayout>(R.id.rl_cart_count_num)
        /**
         * get cart item
         */

         //  getCartItem()
        setCartData()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBackPressedDispatcher {
            findNavController().popBackStack(R.id.homeFragment,false)
        }



        binding.txtViewMenu.setOnClickListener {
            val intent = Intent(requireContext(),RestaurantMenuActivity::class.java)
                //   intent.putExtra("item_id",restaurantId)
            PrefUtils.instance.setString("item_id",restaurantId)
            startActivity(intent)

        }
        binding.cartContinueBt.setOnClickListener {
            val intent = Intent(requireContext(),CheckOutActivity::class.java)
            intent.putExtra("item_id",restaurantId)
            startActivityForResult(intent,101)


        }
        binding.txtViewRestaurant.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment,false)
            (activity as MainActivity).binding.bottomNavigationView.selectedItemId = R.id.homeFragment
        }
    }

    override fun onResume() {
        super.onResume()
        binding.deleteAllCart.setOnClickListener {
            deleteDialog()
        }
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
            mViewModel.deleteAllCartItem(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}").collect{
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

    private fun setCartData(){
        if ((activity as MainActivity).cartDataList.isNotEmpty()){

            binding.txtRestaurantName.visibility = View.VISIBLE
            binding.txtViewMenu.visibility = View.VISIBLE
            binding.cartRecyclerView.visibility = View.VISIBLE
            binding.totalAmountCl.visibility = View.VISIBLE
            binding.consEmptyCart.visibility = View.GONE

            cartItemAdapter = CartItemAdapter(
                context =  requireContext(),
                activity =  activity as MainActivity,
                arrayList = (activity as MainActivity).cartDataList,
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
            binding.txtRestaurantName.text = (activity as MainActivity).cartItemRestaurantName
            binding.txtTotalPrice.text = (activity as MainActivity).cartItemTotalPrices

        }else{
            (activity as MainActivity).badgeDrawable.isVisible = false
            binding.txtRestaurantName.visibility = View.GONE
            binding.txtViewMenu.visibility = View.GONE
            binding.totalAmountCl.visibility = View.GONE
            binding.cartRecyclerView.visibility = View.GONE
            binding.consEmptyCart.visibility = View.VISIBLE

        }
    }

    @SuppressLint("SetTextI18n")
    private fun getCartItem(isLoaderShow:Boolean = true) {
        lifecycleScope.launch {
            mViewModel.getCartItem(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}").collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        if (isLoaderShow)
                            SvaggyApplication.progressBarLoader.start(requireContext())
                    }
                    is ApiResponse.Success -> {
                        SvaggyApplication.progressBarLoader.stop()
                        val response = it.data
                        if (response != null){
                            if (response.isSuccess == true) {
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
                                        activity =  activity as MainActivity,
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
                                    binding.txtTotalPrice.text = "CZK " +PrefUtils.instance.formatDouble(response.data?.totalAmount!!)
                                    (activity as MainActivity).badgeDrawable.isVisible = true
                                    (activity as MainActivity).badgeDrawable.number = response.data?.totalCartQuantity ?: 0
                                } else {
                                    (activity as MainActivity).badgeDrawable.isVisible = false
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


    private fun cartUpdate(menuItemId:Int?,cartId: Int, quantity: Int,addOns: ArrayList<Int>) {
        lifecycleScope.launch {
            mViewModel.updateCartItem(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                cartId = cartId,
                quantity = quantity).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response!!.isSuccess!!){
                            if (quantity == 0){
                                PrefUtils.instance.removeMenuItemsList("$menuItemId")
                                PrefUtils.instance.removeBoosterList("Booster")
                               ( activity as MainActivity).getCartItem()
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
                token ="$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                addOnModel =addOnsModel).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {

                        val response = it.data
                        if (response != null){
                            if (response.isSuccess!!) {
                                if (quantity == 0){
                                    PrefUtils.instance.removeMenuItemsList("$menuItemId")
                                    PrefUtils.instance.removeBoosterList("Booster")
                                    ( activity as MainActivity).getCartItem()
                                }
                                getCartItem(isLoaderShow = false)
                            }
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getCartItem()

    }




}