package com.svaggy.ui.activities.restaurant

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.internal.ViewUtils
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.app.SvaggyApplication
import com.svaggy.client.models.AddOnsModel
import com.svaggy.client.models.MenuItem
import com.svaggy.databinding.ActivityRestaurantMenuBinding
import com.svaggy.ui.activities.checkout.CheckOutActivity
import com.svaggy.ui.activities.guestuser.GuestHomeActivity
import com.svaggy.ui.activities.restaurant.adapter.RestaurantTimingAdapter
import com.svaggy.ui.activities.restaurant.model.AddOnRelationModel
import com.svaggy.ui.fragments.cart.viewmodel.CartViewModel
import com.svaggy.ui.fragments.home.adapter.AboutRestaurantAdapter
import com.svaggy.ui.fragments.home.adapter.CategoryNameAdapter
import com.svaggy.ui.fragments.home.adapter.MultiViewAdapter
import com.svaggy.ui.fragments.home.model.RepeatOrderModel
import com.svaggy.ui.fragments.home.model.RestaurantFilter
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem
import com.svaggy.ui.fragments.home.model.SharedPrefModel
import com.svaggy.ui.fragments.home.viewmodel.HomeViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.invisible
import com.svaggy.utils.setSafeOnClickListener
import com.svaggy.utils.show
import com.svaggy.utils.updateStatusBarColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class RestaurantMenuActivity : BaseActivity<ActivityRestaurantMenuBinding>(ActivityRestaurantMenuBinding::inflate) {
    private val mViewModel by viewModels<HomeViewModel>()
    private val mCartViewModel by viewModels<CartViewModel>()
    private var aboutRestaurantAdapter: AboutRestaurantAdapter? = null
    private var categoryMenuName: CategoryNameAdapter? = null
    private var categoryArrayList: ArrayList<RestaurantsMenuItem.Data.Categories> = ArrayList()
    private var specificOfferList: ArrayList<RestaurantsMenuItem.Data.Categories.MenuItems.MenuAddOns.OfferModel> = ArrayList()
    private var restaurantId: String = "0"
    private var broadCastId: String? = null
    private var filterPopup: PopupWindow? = null
    private var recyclerView: RecyclerView?=null
    var dialog: BottomSheetDialog?=null
    private var getCuisine:String?=null
    private var filterText:String? = null
    private var popularDishesString:String=""
    private val selectFilterList:ArrayList<RestaurantFilter> = ArrayList()
    var restaurantCuisines: ArrayList<RestaurantsMenuItem.Data.RestaurantDetails.RestaurantCuisines>? = ArrayList()
    private var adapterPosition:Int? = null
    private var addCartBt: ConstraintLayout? = null
    private var clAddSub: ConstraintLayout? = null
    private var itemCountText: TextView? = null
    private lateinit var multiViewAdapter: MultiViewAdapter
    private var restName:String? = null
    private var deliveryType:String? = null
    private var setBoosterOnTime = true
    private lateinit var isFrom:String
    private var searchItemId:Int? = null
    private lateinit var repeatOrderList:ArrayList<RepeatOrderModel>
    private  var restaurantTimingAdapter: RestaurantTimingAdapter? = null












    override fun ActivityRestaurantMenuBinding.initialize(){
        updateStatusBarColor("#F6F6F6",true)
        restaurantId = PrefUtils.instance.getString("item_id").toString()
        deliveryType = PrefUtils.instance.getString("deliveryType")
        broadCastId = intent.getStringExtra(Constants.BROADCAST_ID)
        isFrom = intent.getStringExtra("isFrom").toString()
        searchItemId = intent.getIntExtra("itemId", 0)

        if (broadCastId != null && !broadCastId.equals("null"))
            setBroadCast(broadCastId!!)


        binding.menuButton.setOnClickListener {
            dismissPopup()
            filterPopup = showAlertFilter()
        }
//        binding.menuButton.setOnClickListener {
//            dismissPopup()
//            filterPopup = showAlertFilter() }

        binding.consViewCart.setSafeOnClickListener {
            if (isFrom == "CheckOut"){
                finish()
            }
            else{
                when(PrefUtils.instance.getString(Constants.IsGuestUser)){

                    "false" ->{
                        PrefUtils.instance.setString("restaurantId",restaurantId)
                        val intent = Intent(this@RestaurantMenuActivity,CheckOutActivity::class.java)
                        intent.putExtra("pop_back","RestaurantMenuScreen")
                        intent.putExtra(Constants.DELIVERY_TYPE,deliveryType)
                        intent.putExtra("restaurantId",restaurantId)
                        startActivity(intent)
                    }
                    "true" ->{
                        startActivity(Intent(this@RestaurantMenuActivity,GuestHomeActivity::class.java)
                            .putExtra("isFrom","CartScreen"))
                    }
                    else ->{
                        PrefUtils.instance.setString("restaurantId",restaurantId)
                        val intent = Intent(this@RestaurantMenuActivity,CheckOutActivity::class.java)
                        intent.putExtra("pop_back","RestaurantMenuScreen")
                        intent.putExtra(Constants.DELIVERY_TYPE,deliveryType)
                        intent.putExtra("restaurantId",restaurantId)
                        startActivity(intent)

                    }
                }

            }
        }


        multiViewAdapter =MultiViewAdapter(context = this@RestaurantMenuActivity,
            homeActivity =  this@RestaurantMenuActivity,
            clearAllCart ={deleteAllCartItem()},
            placeOrder = {itemId, addOnsItemList,_,addCartBtn,cllAdd,itemCountText ->
                this@RestaurantMenuActivity.addCartBt = addCartBtn
                this@RestaurantMenuActivity.clAddSub = cllAdd
                this@RestaurantMenuActivity.itemCountText = itemCountText
                mCartViewModel.placeOrder(
                    "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                    restaurantId.toInt(), itemId, 1,addOnsItemList)
                         },
            editCart = {itemId,quantity,_,_,addCartBtn,cllAdd,itemCountText ->
                this@RestaurantMenuActivity.addCartBt = addCartBtn
                this@RestaurantMenuActivity.clAddSub = cllAdd
                this@RestaurantMenuActivity.itemCountText = itemCountText
                // adapterPosition = getAdapterPosition
                editCart2(itemId,quantity)
                       },
            editRepeatCart = {cartId,quantity,menuItemId,_,addCartBtn,cllAdd,itemCountText,isRemoveCart ->
                this@RestaurantMenuActivity.addCartBt = addCartBtn
                this@RestaurantMenuActivity.clAddSub = cllAdd
                this@RestaurantMenuActivity.itemCountText = itemCountText
                editRepeatCart(cartId,quantity,menuItemId,isRemoveCart)

            },
            addFilter = {filterList ->
                selectFilterList.addAll(filterList)
                getMenuItem(filterText = filterText,filterList = selectFilterList)
            },
            searchList = {searchBox ->
                searchBox.addTextChangedListener(object : TextWatcher {

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    @SuppressLint("RestrictedApi")
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        val query = s.toString()
                        searchBox.setOnEditorActionListener { _, actionId, _ ->
                            if (actionId == EditorInfo.IME_ACTION_DONE ||actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO
                                || actionId == EditorInfo.IME_ACTION_SEARCH) {
                                query.let { userQuery ->
                                    if (userQuery.isEmpty()) {
                                        ViewUtils.hideKeyboard(searchBox)
                                        filterText = userQuery
                                        getMenuItem(filterText =null,filterList =selectFilterList)

                                    } else {
                                        filterText = userQuery
                                        getMenuItem(filterText = filterText,filterList =selectFilterList)
                                    }

                                }


                                return@setOnEditorActionListener true
                            }
                            false
                        }




                    }

                    override fun afterTextChanged(s: Editable?) {

                    }
                })
            },
            showInfo = {showInfo,clickImg ->
                showInfo.setSafeOnClickListener {
                    showInfoDialog()

                }
                clickImg.setSafeOnClickListener {
                    showInfoDialog()

                }
            },
            goBack = {backBtn ->
                backBtn.setOnClickListener {
//                    if (broadCastId != null && !broadCastId.equals("null"))
//                        findNavController().navigate(R.id.homeFragment)
//                    else
//                        findNavController().popBackStack()
                    if (isFrom == "CheckOut")
                        finish()
                        else
                     onBackPressed()

                }
            })
        binding.recyclerMenuItem.adapter = multiViewAdapter



    }




    override fun onResume() {
        super.onResume()
        getMenuObserver()

        getCartItem(isUpdateAdapter = false)
        if (intent.getStringExtra("isFrom")=="CompareFood"){
            filterText = intent.getStringExtra("search_text").toString()
            getMenuItem(filterText =filterText, filterList = selectFilterList)
        }
        else{
            getMenuItem(filterText = filterText, filterList = selectFilterList)
        }
    }
    private fun showInfoDialog() {
        val viewDialog = layoutInflater.inflate(R.layout.sheet_about_restaurant, null)
        val dialog = BottomSheetDialog(this)
        dialog.setOnShowListener {
            val bottomSheetDialogFragment = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            (viewDialog.parent as View).setBackgroundColor(
                ContextCompat.getColor(
                   this,
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
        val timeRc =viewDialog.findViewById<RecyclerView>(R.id.restaurantTimeRc)

        aboutRestaurantAdapter = AboutRestaurantAdapter(this,this, restaurantCuisines?: ArrayList())
        if (restaurantTimingAdapter != null){
        timeRc.adapter = restaurantTimingAdapter
        }

        recyclerCountryDishes?.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        recyclerCountryDishes?.adapter = aboutRestaurantAdapter


        restaurantName.text= getString(R.string.about,restName)
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
            "restaurant_id" to restaurantId,
            "latitude" to PrefUtils.instance.getString(Constants.Latitude).toString(),
            "longitude" to PrefUtils.instance.getString(Constants.Longitude).toString())
        if (!filterText.isNullOrEmpty()){
            map["filter_text"] = filterText
        }
        filterList?.forEach {
            if (it.filterBoolean){
                map[it.paramName] = true.toString()
            }
        }



        lifecycleScope.launch{
            mViewModel.getMeMenuItem(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}", map = map).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        SvaggyApplication.progressBarLoader.start(this@RestaurantMenuActivity)


                    }
                    is ApiResponse.Success -> {
                        SvaggyApplication.progressBarLoader.stop()

                        if (it.data != null){
                            if (it.data.isSuccess!!) {
                                val response = it.data.data
                                if (response?.completeSchedule != null){
                                restaurantTimingAdapter = RestaurantTimingAdapter(response.completeSchedule)
                                }
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
                                if (popularDishesString.endsWith(", ")) {
                                    popularDishesString = popularDishesString.substring(0, popularDishesString.length - 2)
                                }
                                PrefUtils.instance.setString(Constants.MenutRestaurantId,response.restaurantDetails?.id.toString())
                                val getType = response.restaurantDetails!!.deliveryType.toString()
                             //   categoryArrayList.addAll(response.categories)
                                categoryArrayList = response.categories
                                specificOfferList.clear()
                                specificOfferList.addAll(response.offer)
                                multiViewAdapter.updateData(categoryArrayList,specificOfferList,response)
                                if (!response.restaurantDetails!!.boosterId.isNullOrEmpty()){
                                    if (setBoosterOnTime){
                                        setBooster(boosterArray = response.restaurantDetails!!.boosterId!!, restaurantId = response.restaurantDetails!!.id!!)
                                        setBoosterOnTime = false

                                    }
                                }

                            }
                        }
                        if (isFrom == "SearchActivity"){
                            isFrom = ""
                            delay(500)
                            scrollToDish(dishId = searchItemId ?: 0,categoryArrayList)

                        }
                    }
                    is ApiResponse.Failure -> {
                        SvaggyApplication.progressBarLoader.stop()

                    }


                }

            }
        }

    }





   //  Function to filter local data
   // Function to filter local data

//    private fun scrollToDish(dishId: Int, category: List<RestaurantsMenuItem.Data.Categories>) {
//
//        category.forEachIndexed {index,items ->
//            items.menuItems.forEachIndexed { innerIndex, item ->
//                if (dishId == item.id) {
//                    val smoothScroller = TopLinearSmoothScroller(this)
//                    smoothScroller.targetPosition = index + 1
//                    //binding.recyclerMenuItem.smoothScrollBy(0,100)
//                   // binding.recyclerMenuItem.scrollY = 3
//                    // Convert inches to pixels
//                   // binding.recyclerMenuItem.nestedScrollBy()
//                    binding.recyclerMenuItem.layoutManager?.startSmoothScroll(smoothScroller)
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        delay(3000)
//                        nextScroll(innerIndex)
//                    }
//
//                    return
//                }
//            }
//
//            }
//
//    }


    private fun scrollToDish(dishId: Int, category: List<RestaurantsMenuItem.Data.Categories>) {

        category.forEachIndexed { index, items ->
            items.menuItems.forEachIndexed { innerIndex, item ->
                if (dishId == item.id) {
                    val smoothScroller = TopLinearSmoothScroller(this)
                    smoothScroller.targetPosition = index + 1
                    binding.recyclerMenuItem.layoutManager?.startSmoothScroll(smoothScroller)

                    Log.d("DishID", "item name is " + item.dishName + " Position $innerIndex" + " This item is in ${items.categoryName} this in position $index")

                    val get0IndexValue = items.menuItems[0]
                    val getSearchedItemValue = items.menuItems[innerIndex]

                    items.menuItems[0] = getSearchedItemValue
                    items.menuItems[innerIndex] = get0IndexValue

                    multiViewAdapter.syncScrollPosition(0, innerIndex)

                    return
                }
            }
        }

    }

    private fun nextScroll(innerIndex: Int) {
        val calculateValue = innerIndex * 200
        // Calculate the scroll distance in pixels based on inches (innerIndex)
        val scrollInches = calculateValue.toFloat() // Inner index represents inches
        val scrollPixels = scrollInches * resources.displayMetrics.density // Convert inches to pixels

        // Scroll the RecyclerView by the calculated distance
        binding.recyclerMenuItem.smoothScrollBy(0, scrollPixels.toInt())
    }


        private fun editCart2(itemId: Int, quantity: Int) {
        val cartList = PrefUtils.instance.getMenuItemsList("$itemId")
        var cartId = 0
        cartList?.forEach {
            cartId = it.cartId?: 0
        }
        lifecycleScope.launch {
            mCartViewModel.editCart(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                itemId = cartId,
                quantity = quantity).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        SvaggyApplication.progressBarLoader.start(this@RestaurantMenuActivity)
                    }
                    is ApiResponse.Success -> {
                        SvaggyApplication.progressBarLoader.stop()
                        if (it.data != null){
                            if (it.data.isSuccess!!) {
                                if (quantity == 0){
                                    PrefUtils.instance.removeMenuItemsList("$itemId")
                                    PrefUtils.instance.removeBoosterList("Booster")

//                                    if (addCartBt != null && clAddSub != null ){
//                                        addCartBt!!.show()
//                                        clAddSub!!.invisible()
//                                    }
                                    this@RestaurantMenuActivity.addCartBt?.show()
                                    this@RestaurantMenuActivity.clAddSub?.invisible()
                                    this@RestaurantMenuActivity.itemCountText?.text = ContextCompat.getString(this@RestaurantMenuActivity, R.string._0)
                                }else{
                                    this@RestaurantMenuActivity.itemCountText?.text = quantity.toString()
                                }
                                getCartItem(true,itemId)

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

    private fun editCart(itemId: Int,quantity: Int,addOns: ArrayList<Int>){
        val addOnsToSend = if (addOns.isEmpty()) arrayListOf() else addOns
        val cartList = PrefUtils.instance.getMenuItemsList("$itemId")
        var cartId = 0
        cartList?.forEach {
            cartId = it.cartId?: 0
        }
        val addOnsModel = AddOnsModel(
            menu_item = MenuItem(
                quantity = quantity,
                cart_id = cartId,
                add_ons = addOnsToSend
            )
        )


        lifecycleScope.launch {
            mCartViewModel.updateAddOn2(
                token ="${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                addOnModel =addOnsModel).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {

                        val response = it.data
                        if (response != null){
                            if (response.isSuccess!!) {
                                if (quantity == 0){
                                    PrefUtils.instance.removeMenuItemsList("$itemId")
                                    PrefUtils.instance.removeBoosterList("Booster")

//                                    if (addCartBt != null && clAddSub != null ){
//                                        addCartBt!!.show()
//                                        clAddSub!!.invisible()
//                                    }
                                    this@RestaurantMenuActivity.addCartBt?.show()
                                    this@RestaurantMenuActivity.clAddSub?.invisible()
                                    this@RestaurantMenuActivity.itemCountText?.text = ContextCompat.getString(this@RestaurantMenuActivity, R.string._0)
                                }else{
                                    this@RestaurantMenuActivity.itemCountText?.text = quantity.toString()
                                }
                                getCartItem(true,itemId)

                            }
                        }
                    }
                    is ApiResponse.Failure -> {

                        Toast.makeText(this@RestaurantMenuActivity, "${it.msg}", Toast.LENGTH_LONG).show()
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









    private fun editRepeatCart(cartId: Int, quantity: Int, menuItemId: Int, isRemoveCart: Boolean) {

        lifecycleScope.launch {
            mCartViewModel.editCart(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
                itemId = cartId,
                quantity = quantity).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        SvaggyApplication.progressBarLoader.start(this@RestaurantMenuActivity)
                    }
                    is ApiResponse.Success -> {
                        SvaggyApplication.progressBarLoader.stop()
                        if (it.data != null){
                            if (it.data.isSuccess!!) {
                                if (quantity == 0 && isRemoveCart){
                                    PrefUtils.instance.removeMenuItemsList("$cartId")
                                    PrefUtils.instance.removeBoosterList("Booster")
                                    PrefUtils.instance.removeRepeatOrderList("repeatList")

//                                    if (addCartBt != null && clAddSub != null ){
//                                        addCartBt!!.show()
//                                        clAddSub!!.invisible()
//                                    }
                                    this@RestaurantMenuActivity.addCartBt?.show()
                                    this@RestaurantMenuActivity.clAddSub?.invisible()
                                  //  this@RestaurantMenuActivity.itemCountText?.text = ContextCompat.getString(this@RestaurantMenuActivity, R.string._0)
                                }
                                getCartItem(true,menuItemId)

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



    private fun getPosition(pos: Int,categoryName:String) {
    //    Log.d("preet565",pos.toString())
       // binding.recyclerMenuItem.smoothScrollToPosition(pos)
     // val posNew =  multiViewAdapter.smoothScroll(categoryName)
      //  Log.d("preet567",posNew.toString())
      //  binding.recyclerMenuItem.smoothScrollToPosition(posNew)

        val smoothScroller = TopLinearSmoothScroller(this)
        smoothScroller.targetPosition = pos+1
        Log.d("preet",smoothScroller.toString())
        binding.recyclerMenuItem.layoutManager?.startSmoothScroll(smoothScroller)
    }


    private fun showAlertFilter(): PopupWindow {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
        categoryMenuName =  CategoryNameAdapter(categoryArrayList,::getPosition)
        recyclerView?.adapter = categoryMenuName
        return PopupWindow(view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }



    private fun getMenuObserver() {
        mCartViewModel.response.observe(this) {
            if (it.isSuccess == true) {
                this@RestaurantMenuActivity.addCartBt?.invisible()
                this@RestaurantMenuActivity.clAddSub?.show()
              //  this@RestaurantMenuActivity.itemCountText?.text = ContextCompat.getString(this@RestaurantMenuActivity, R.string._1)
                val menuId = it.data?.menuItemId
                getCartItem(isUpdateAdapter = true,itemId = menuId,isLoaderShow = false,isLoaderHide = true)
            } else {
                binding.consViewCart.visibility = View.GONE
                Toast.makeText(this, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }





    @SuppressLint("SetTextI18n", "NotifyDataSetChanged", "SuspiciousIndentation")
    private fun getCartItem(isUpdateAdapter: Boolean?, itemId: Int? = null,isLoaderShow:Boolean = false,isLoaderHide:Boolean = false) {
        lifecycleScope.launch {
            mCartViewModel.getCartItem(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}").collect{
                when (it) {
                    is ApiResponse.Loading -> {
                        if (isLoaderShow)
                        SvaggyApplication.progressBarLoader.start(this@RestaurantMenuActivity)
                    }
                    is ApiResponse.Success -> {
                        if (isLoaderHide)
                            SvaggyApplication.progressBarLoader.stop()
                        val response = it.data
                        if (response != null){
                            if (response.isSuccess == true) {
                                if ((response.data?.cartItems?.size ?: 0) > 0) {
                                    repeatOrderList = ArrayList()
                                    response.data?.cartItems?.forEach {item ->
                                        val addList = ArrayList<AddOnRelationModel>()
                                        if (itemCountText != null ){
                                            if (itemId != null && itemId == item.menuItemId)
                                            itemCountText!!.text = item.menu_total_quantity.toString()

                                        }
                                        val listOf = listOf(
                                            SharedPrefModel(item.dishName?: "",
                                                item.dishType?: "",
                                                item.price?: 0.0,
                                                item.isActive?: false,
                                                item.menuItemId?: 0,
                                                item.actualPrice?: 0.0,
                                                item.menu_total_quantity?: 0,
                                                item.id?: 0))
                                        PrefUtils.instance.saveMenuItemsList("${item.menuItemId}",listOf)


                                            item.menuAddOns.forEach { menuAddOn ->
                                                menuAddOn.addOnsRelations.forEach {addOnRelation ->
                                                    if (addOnRelation.isSelected == true){
                                                        addList.add(AddOnRelationModel(addOnName =addOnRelation.itemName?:"", addOnId =addOnRelation.id ?: 0))
                                                    }
                                                }
                                            }
                                        repeatOrderList.add(RepeatOrderModel(
                                            dishName = item.dishName ?: "",
                                            dishType = item.dishType?: "",
                                            price = item.price?: 0.0,
                                            itemId = item.id?: 0,
                                            menuItemId = item.menuItemId ?:0,
                                            actualPrice = item.actualPrice?: 0.0,
                                            quantity = item.quantity?: 0,
                                            addOns = addList))
                                      //  PrefUtils.instance.saveRepeatOrder("${item.menuItemId}",repeatOrderList)
                                      //  val list = PrefUtils.instance.getRepeatItemsList("${item.menuItemId}")
                                       // Log.d("preet514",list?.size.toString())
                                    }

                                    PrefUtils.instance.saveRepeatOrder("repeatList",repeatOrderList)
                                    binding.txtItems.text = "${response.data?.totalCartQuantity} items, CZK ${PrefUtils.instance.formatDouble(response.data?.totalAmount!!)}"
                                    binding.consViewCart.visibility = View.VISIBLE




                                } else {
                                    binding.consViewCart.visibility = View.GONE
                                }
                                PrefUtils.instance.setString(Constants.CartRestaurantId,response.data?.restaurantDetails?.id.toString())
                            }
                        }
                    }
                    is ApiResponse.Failure -> {
                        SvaggyApplication.progressBarLoader.stop()
                        Toast.makeText(this@RestaurantMenuActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }
            }


        }

    }

    private fun deleteAllCartItem(){
        lifecycleScope.launch {
            mCartViewModel.deleteAllCartItem(token = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}").collect{
                when (it) {
                    is ApiResponse.Loading -> {}
                    is ApiResponse.Success -> {
                        val response = it.data
                        if (response != null){
                            if (response.isSuccess == true) {
                                getCartItem(isUpdateAdapter = false)
                                PrefUtils.instance.clearCartItemsFromSharedPreferences()
                            }
                        }
                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(this@RestaurantMenuActivity, "${it.msg}", Toast.LENGTH_LONG).show()
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
                        Toast.makeText(this@RestaurantMenuActivity,data!!.message.toString(), Toast.LENGTH_LONG).show()

                    }
                    is ApiResponse.Failure -> {

                        Toast.makeText(this@RestaurantMenuActivity, "${it.msg}", Toast.LENGTH_LONG).show()
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
            mViewModel.setActionBooster(token =  "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
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
                                Toast.makeText(this@RestaurantMenuActivity, "" + data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                    is ApiResponse.Failure -> {

                        Toast.makeText(this@RestaurantMenuActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }

    }


    override fun onBackPressed() {
        if (isFrom == "CheckOut")
            finish()
        else
            super.onBackPressed()
    }

}

class TopLinearSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    override fun getVerticalSnapPreference(): Int {
        return SNAP_TO_START
    }
}

