package com.svaggy.ui.fragments.home.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.internal.ViewUtils
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.svaggy.R
import com.svaggy.databinding.FragmentRestaurantMenuScreensBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.activities.guestuser.GuestHomeActivity
import com.svaggy.ui.fragments.cart.viewmodel.CartViewModel
import com.svaggy.ui.fragments.home.adapter.AboutRestaurantAdapter
import com.svaggy.ui.fragments.home.adapter.CategoryNameAdapter
import com.svaggy.ui.fragments.home.adapter.MultiViewAdapter
import com.svaggy.ui.fragments.home.model.RestaurantFilter
import com.svaggy.ui.fragments.home.model.SharedPrefModel
import com.svaggy.ui.fragments.home.viewmodel.HomeViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.BEARER
import com.svaggy.utils.Constants.BROADCAST_ID
import com.svaggy.utils.Constants.ITEM_ID
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.updateStatusBarColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem.Data.Categories
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem.Data.Categories.MenuItems.MenuAddOns.OfferModel
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem.Data.RestaurantDetails.RestaurantCuisines
import com.svaggy.utils.Constants.DELIVERY_TYPE
import com.svaggy.utils.onBackPressedDispatcher


@AndroidEntryPoint
class RestaurantMenuScreen : Fragment() {
   private var _binding: FragmentRestaurantMenuScreensBinding? = null
  private  val binding get() = _binding!!

    private val mViewModel by viewModels<HomeViewModel>()
    private val mCartViewModel by viewModels<CartViewModel>()
    private var aboutRestaurantAdapter: AboutRestaurantAdapter? = null
    private var categoryMenuName: CategoryNameAdapter? = null
    private var categoryArrayList: ArrayList<Categories> = ArrayList()
    private var specificOfferList: ArrayList<OfferModel> = ArrayList()
    private var restaurantId: String? = null
    private var broadCastId: String? = null
    private var filterPopup: PopupWindow? = null
    private var recyclerView: RecyclerView?=null
    var dialog: BottomSheetDialog?=null
    private var getCuisine:String?=null
    private var filterText:String? = null
    private var popularDishesString:String?=""
    private val selectFilterList:ArrayList<RestaurantFilter> = ArrayList()
    var restaurantCuisines: ArrayList<RestaurantCuisines>? = ArrayList()
    private var adapterPosition:Int? = null
    private var addCartBt:ConstraintLayout? = null
    private var clAddSub:ConstraintLayout? = null
    private var itemCountText:TextView? = null
    private lateinit var multiViewAdapter: MultiViewAdapter
    private var restName:String? = null
    private var deliveryType:String? = null
    private var setBoosterOnTime = true






    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRestaurantMenuScreensBinding.inflate(inflater, container, false)
        requireActivity().updateStatusBarColor("#F6F6F6",true)
        return binding.root
    }
    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBackPressedDispatcher {
            if (arguments?.getString("pop_back")=="HomeFragment"){
                findNavController().popBackStack(R.id.homeFragment,false)
            }
            else{
                findNavController().popBackStack()
            }

        }





        binding.menuButton.setOnClickListener {
            dismissPopup()
            filterPopup = showAlertFilter()
        }

        binding.consViewCart.setOnClickListener {
            findNavController().navigate(R.id.action_restaurantMenuScreen2_to_guestCartFragment)
        }
        getMenuObserver()
        if (arguments?.getString("isFrom")=="CompareFood"){
                    filterText = arguments?.getString("search_text").toString()
                   // getMenuItem(filterText =filterText, filterList = selectFilterList)
                }
                else{
                 //   getMenuItem(filterText = filterText, filterList = selectFilterList)
                }
                getCartItem(isUpdateAdapter = false)


    }

    private fun showInfoDialog() {
        val viewDialog = layoutInflater.inflate(R.layout.sheet_about_restaurant, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setOnShowListener {
            val bottomSheetDialogFragment = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            (viewDialog.parent as View).setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.transparent
                )
            )
            val behavior = bottomSheetDialogFragment?.let {
                BottomSheetBehavior.from(it)
            }
            behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        }
        val restaurantName  =viewDialog.findViewById<TextView>(R.id.restaurantName)
        val tvPopularDishes =viewDialog.findViewById<TextView>(R.id.tvPopularDishes)
        val ivCancelDialog   =viewDialog.findViewById<ImageView>(R.id.ivCancleDilog)
        val recyclerCountryDishes =viewDialog.findViewById<RecyclerView>(R.id.recyclerCountryDishes)

        aboutRestaurantAdapter = AboutRestaurantAdapter(requireContext(),(activity as MainActivity), restaurantCuisines?: ArrayList())

        recyclerCountryDishes.layoutManager =
            GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)
        recyclerCountryDishes?.adapter = aboutRestaurantAdapter


        restaurantName.text= requireContext().getString(R.string.about,restName)
        tvPopularDishes.text=popularDishesString
        ivCancelDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(viewDialog)
        dialog.show()

    }


    private fun dismissPopup() {
        filterPopup?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            filterPopup = null
        }

    }



    private fun getMenuItem(filterText: String? = null, filterList: ArrayList<RestaurantFilter>? = null) {
        val map = mutableMapOf(
            "restaurant_id" to restaurantId!!,
        )
        if (!filterText.isNullOrEmpty()){
            map["filter_text"] = filterText
        }
        filterList?.forEach {
            if (it.filterBoolean){
                map[it.paramName] = true.toString()
            }
        }



        lifecycleScope.launch{
            mViewModel.getMeMenuItem(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}", map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {
               //     progressBarHelper.showProgressBar()

                    }
                    is ApiResponse.Success -> {
                 //     progressBarHelper.dismissProgressBar()
                        if (it.data != null){
                            if (it.data.isSuccess!!) {
                                val response = it.data.data
                                restaurantCuisines=  response?.restaurantDetails?.restaurantCuisines
                                restName = response?.restaurantDetails?.restaurantName
                                val stringBuilder = StringBuilder()
                                for (i in response?.restaurantDetails?.restaurantCuisines!!){
                                    stringBuilder.append(i.cuisine).append(", ")
                                }

                                getCuisine= stringBuilder.toString()
                                if (getCuisine!!.endsWith(", ")) {
                                    getCuisine = getCuisine!!.substring(0, getCuisine!!.length - 2)

                                }

                                val popularDishesBuilder = StringBuilder()
                                for (i in response.popularDishes){
                                    popularDishesBuilder.append(i).append(", ")
                                }

                                popularDishesString= popularDishesBuilder.toString()
                                if (popularDishesString!!.endsWith(", ")) {
                                    popularDishesString = popularDishesString!!.substring(0, popularDishesString!!.length - 2)
                                }
                                PrefUtils.instance.setString(Constants.MenutRestaurantId,response.restaurantDetails?.id.toString())
                                val getType = response.restaurantDetails!!.deliveryType.toString()
                                categoryArrayList.addAll(response.categories)
                                categoryArrayList = response.categories
                                specificOfferList.clear()
                                specificOfferList.addAll(response.offer)
                                multiViewAdapter.updateData(
                                    categoryArrayList,
                                    specificOfferList,
                                    response,
                                    )
                                if (!response.restaurantDetails!!.boosterId.isNullOrEmpty()){
                                    if (setBoosterOnTime){
                                        setBooster(boosterArray = response.restaurantDetails!!.boosterId!!, restaurantId = response.restaurantDetails!!.id!!)
                                        setBoosterOnTime = false

                                    }
                                }

                            }
                        }
                    }
                    is ApiResponse.Failure -> {
                  //    progressBarHelper.dismissProgressBar()

                    }


                }

            }
        }

    }


    private fun editCart(itemId: Int, quantity: Int) {
        val cartList = PrefUtils.instance.getMenuItemsList("$itemId")
        var cartId = 0
        cartList?.forEach {
            cartId = it.cartId?: 0 }
        lifecycleScope.launch {
            mCartViewModel.editCart(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                itemId = cartId,
                quantity = quantity).collect{
                when (it) {
                    is ApiResponse.Loading -> {
               //      progressBarHelper.showProgressBar()
                    }
                    is ApiResponse.Success -> {
                  //     progressBarHelper.dismissProgressBar()
                        if (it.data != null){
                            if (it.data.isSuccess!!) {
                                if (quantity == 0){
                                    PrefUtils.instance.removeMenuItemsList("$itemId")
                                    PrefUtils.instance.removeBoosterList("Booster")
//                                    if (addCartBt != null && clAddSub != null ){
//                                        addCartBt!!.show()
//                                        clAddSub!!.invisible()
//                                    }
                                }
                                getCartItem(true)

                            }
                        }
                    }
                    is ApiResponse.Failure -> {
                  //    progressBarHelper.dismissProgressBar()


                    }


                }

            }
        }

    }



//    private fun getPosition(pos: Int) {
//        Toast.makeText(requireContext(),i.toString(),Toast.LENGTH_LONG).show()
//        binding.recyclerMenuItem.smoothScrollToPosition(i)
//    }

    private fun showAlertFilter(): PopupWindow {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.sheet_category_listname, null)

        filterPopup = PopupWindow(view, 700, 500, true)
        filterPopup?.showAtLocation(view, Gravity.BOTTOM, 0, 40)
        filterPopup?.isTouchable = true
        filterPopup?.isFocusable = true
        filterPopup?.isOutsideTouchable = true
        filterPopup?.isFocusable = true

        recyclerView = view.findViewById(R.id.recyclerCategoryName)
        recyclerView?.addItemDecoration(
            DividerItemDecoration(
                recyclerView?.context,
                DividerItemDecoration.VERTICAL
            )
        )
       // categoryMenuName =  CategoryNameAdapter(categoryArrayList,::getPosition)
        recyclerView?.adapter = categoryMenuName
        return PopupWindow(view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }



    private fun getMenuObserver() {
        mCartViewModel.response.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
            //   progressBarHelper.dismissProgressBar()
//                if (this.addCartBt != null && this.clAddSub != null && this.itemCountText != null){
//                    addCartBt!!.invisible()
//                    clAddSub!!.show()
//                    itemCountText!!.text = "1"
//
//                }
                getCartItem(isUpdateAdapter = true)
            } else {
           //    progressBarHelper.dismissProgressBar()
                binding.consViewCart.visibility = View.GONE
                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }





    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun getCartItem(isUpdateAdapter:Boolean?) {
        lifecycleScope.launch {
            mCartViewModel.getCartItem(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}").collect{
                when (it) {
                    is ApiResponse.Loading -> {}
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response != null){
                            if (response.isSuccess!!) {
                                if ((response.data?.cartItems?.size ?: 0) > 0) {
                                    response.data?.cartItems?.forEach {item ->
                                        val listOf = listOf(
                                            SharedPrefModel(item.dishName!!,
                                                item.dishType!!,
                                                item.price!!,
                                                item.isActive!!,
                                                item.menuItemId!!,
                                                item.actualPrice!!,
                                                item.quantity!!,
                                                item.id!!))
                                        PrefUtils.instance.saveMenuItemsList("${item.menuItemId}",listOf)
                                    }

                                    binding.txtItems.text = "${response.data?.cartItems?.size} items, CZK ${PrefUtils.instance.formatDouble(response.data?.totalAmount!!)}"
                                    (activity as GuestHomeActivity).badgeDrawable.number = response.data?.totalCartQuantity ?: 0
                                } else {
                                    (activity as GuestHomeActivity).badgeDrawable.isVisible = false

                                }
                                if (isUpdateAdapter != null && isUpdateAdapter){
                                    if (adapterPosition != null)
                                   multiViewAdapter.notifyItemChanged(adapterPosition!!)
                                    else
                                       multiViewAdapter.notifyDataSetChanged()
                                }


                                PrefUtils.instance.setString(Constants.CartRestaurantId,response.data?.restaurantDetails?.id.toString())

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

    private fun deleteAllCartItem(){
        lifecycleScope.launch {
            mCartViewModel.deleteAllCartItem(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}").collect{
                when (it) {
                    is ApiResponse.Loading -> {}
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response != null){
                            if (response.isSuccess!!) {
                                getCartItem(isUpdateAdapter = false)
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

    //add restaurant id
    private fun setBroadCast(broadCastId:String){
        val json = JsonObject()
        json.addProperty("broadcast_id",broadCastId)
        json.addProperty("type","CLICK")
        lifecycleScope.launch {
            mViewModel.setBroadCast(token = "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                jsonObject =json ).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        Toast.makeText(requireContext(),data!!.message.toString(),Toast.LENGTH_LONG).show()

                    }
                    is ApiResponse.Failure -> {

                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }
            }

        }
    }

    //
    private fun setBooster(boosterArray: ArrayList<Int>, restaurantId: Int) {
        val json = JsonObject()
        val boosterJsonArray = JsonArray()
        for (boosterId in boosterArray) {
            boosterJsonArray.add(boosterId)
        }
        json.add("booster_ids",boosterJsonArray)
        json.addProperty("type","CLICK")
        json.addProperty("restaurant_id", restaurantId)

        lifecycleScope.launch {
            mViewModel.setActionBooster(token =  "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                jsonObject = json).collect{

                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        if (data != null){

                            if (data.is_success!!) {

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