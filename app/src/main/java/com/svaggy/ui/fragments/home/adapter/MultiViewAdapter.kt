package com.svaggy.ui.fragments.home.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.HeaderItemBinding
import com.svaggy.databinding.ItemCategoryListBinding
import com.svaggy.ui.activities.restaurant.TopLinearSmoothScroller
import com.svaggy.ui.fragments.home.model.RestaurantFilter
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem
import com.svaggy.utils.hide
import com.svaggy.utils.invisible
import com.svaggy.utils.show

class MultiViewAdapter(
    private val context: Context,
    private val homeActivity: Activity,
    private val clearAllCart: () -> Unit,
    private val placeOrder: (itemId: Int, addOnList: ArrayList<Int>, adapterPosition: Int?,addCartBt: ConstraintLayout,clAddSub:ConstraintLayout,itemCount:TextView) -> Unit,
    private val editCart: (itemId: Int, quantity: Int, cartId: Int?, adapterPosition: Int?,addCartBt: ConstraintLayout,clAddSub:ConstraintLayout,itemCount:TextView) -> Unit,
    private val editRepeatCart: (cartId: Int, quantity: Int, menuItemId: Int, adapterPosition: Int?,addCartBt: ConstraintLayout,clAddSub:ConstraintLayout,itemCount:TextView,isRemoveCart:Boolean) -> Unit,
    private val addFilter: (filterList: ArrayList<RestaurantFilter>) -> Unit,
    private val searchList: (searchView: EditText) -> Unit,
    private val showInfo: (view: TextView,viewImg:ImageView) -> Unit,
    private val goBack: (view: ImageView) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var menuItemAdapter: MenuItemAdapter? = null
    private var restaurantFilter: ArrayList<RestaurantFilter>? = null
    private  var specificRestaurantAdapter: SpecificRestaurantAdapter? = null
    private lateinit var offerRc:RecyclerView
    private var restName:TextView? = null
    private var searchView:EditText? = null
    private var getRating:TextView? = null
    private var getReviewCount:TextView? = null
    private var getCuisine:String?=null
    private var setCuisine:TextView?=null
    private var deliverTypeNew:TextView?=null
    private var todaySchedule:TextView?=null
    private var restClose:ImageView?=null
    private var txtRestaurantClosed:TextView?=null
    private var getDeliveryTime:TextView? = null
    private var getDiscount:TextView? = null
    private var dotLine:ImageView? = null
    private var rushCard:ConstraintLayout? = null
    private var spaceView:View? = null
    private  var innerRc:RecyclerView? = null




    companion object {
        private const val VIEW_TYPE_SEARCH_BOX = 0
        private const val VIEW_TYPE_ITEM = 1
    }

      private var itemList: ArrayList<RestaurantsMenuItem.Data.Categories> = ArrayList()

    // ViewHolder for the search box
    private var filteredList: ArrayList<RestaurantsMenuItem.Data.Categories> = ArrayList()
    // other variables...

    init {
        filteredList.addAll(itemList)
    }


    private fun addRestaurantFilter(binding: HeaderItemBinding) {
        restaurantFilter = ArrayList()
        restaurantFilter?.add(RestaurantFilter(context.getString(R.string.veg),"is_veg", ContextCompat.getDrawable(context, R.drawable.veg_icon),false))
        restaurantFilter?.add(RestaurantFilter(context.getString(R.string.non_veg),"is_non_veg", ContextCompat.getDrawable(context, R.drawable.nonveg_icon), false))
        restaurantFilter?.add(RestaurantFilter(context.getString(R.string.vegan_txt),"is_vegan", ContextCompat.getDrawable(context, R.drawable.vegan), false))
        binding.recyclerRestaurantFilter.adapter = RestaurantItemFilter(context, restaurantFilter!!) { filterList ->
            addFilter(filterList)

        }

    }

    // ViewHolder for list items
    inner class ItemViewHolder(binding: ItemCategoryListBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvCategoryName = binding.tvcategoryName
        var recyclerCategoryItem  = binding.recyclerCategoryItem
        val consCategory = binding.consCategory
        val imgMoreItem = binding.imgMoreItem


        init {
           innerRc = binding.recyclerCategoryItem
        }



    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SEARCH_BOX -> {
                val view = HeaderItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                SearchBoxViewHolder(view)
            }
            VIEW_TYPE_ITEM -> {
                val view = ItemCategoryListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                ItemViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }




    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchBoxViewHolder -> {

            }
            is ItemViewHolder -> {
                val item = itemList[position-1]
                holder.tvCategoryName.text = item?.categoryName + " (${item?.menuItems?.size})"
                menuItemAdapter = MenuItemAdapter(
                        context = context,
                        homeActivity =homeActivity,
                        arrayList =item.menuItems,
                        clearAllCart = clearAllCart,
                        placeOrder =placeOrder,
                        editCart =editCart ,
                    editRepeatCart = editRepeatCart,
                        position)
                holder.recyclerCategoryItem.adapter = menuItemAdapter



                holder.consCategory.setOnClickListener {
                    if(item.isViewVisibility) {
                        holder.imgMoreItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_black_uparrow))
                        holder.recyclerCategoryItem.visibility = View.VISIBLE
                        item.isViewVisibility = false
                    } else {
                        holder.imgMoreItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.show_down))
                        holder.imgMoreItem.setColorFilter(ContextCompat.getColor(context, R.color.txt_black))
                        holder.recyclerCategoryItem.visibility = View.GONE
                        item.isViewVisibility = true
                    }

                }



            }
        }
    }

    fun changePos(position: Int) {
        menuItemAdapter?.notifyItemMoved(3,0)

    }



//    fun scrollToMatch(recyclerView: RecyclerView, adapter: MultiViewAdapter, query: String) {
//        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//        val menuItems = adapter.itemList
//
//        for (i in menuItems.indices) {
//            if (menuItems[i].menuItems.c) {
//                layoutManager.scrollToPositionWithOffset(i, 0)
//                recyclerView.smoothScrollToPosition(i)
//                break
//            }
//        }
//
//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
//                if (firstVisiblePosition != RecyclerView.NO_POSITION) {
//                    val currentItem = menuItems[firstVisiblePosition]
//                    if (!currentItem.contains(query, ignoreCase = true)) {
//                        recyclerView.stopScroll()
//                    }
//                }
//            }
//        })
//    }

    override fun getItemCount(): Int {
        return itemList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_SEARCH_BOX
        } else {
            VIEW_TYPE_ITEM
        }
    }


    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }




    @SuppressLint("UseCompatLoadingForDrawables")
    fun updateData(
        items: ArrayList<RestaurantsMenuItem.Data.Categories>,
        offer: ArrayList<RestaurantsMenuItem.Data.Categories.MenuItems.MenuAddOns.OfferModel>,
        response: RestaurantsMenuItem.Data,
        ) {
        itemList.clear()
        itemList.addAll(items)
        specificRestaurantAdapter = SpecificRestaurantAdapter(offer,context)
        if (::offerRc.isInitialized)
        offerRc.adapter = specificRestaurantAdapter
        restName?.text = response.restaurantDetails?.restaurantName
        getDeliveryTime?.text = response.restaurantDetails?.deliveryTime
        searchView?.hint = context.getString(R.string.search_in,response.restaurantDetails?.restaurantName)
        getRating?.text = response.restaurantDetails?.ratings.toString()
        getReviewCount?.text = context.getString(R.string.review,response.restaurantDetails?.reviewCount.toString())
        val stringBuilder = StringBuilder()
        for (i in response.restaurantDetails?.restaurantCuisines!!){
            stringBuilder.append(i.cuisine).append(", ")
        }

        getCuisine= stringBuilder.toString()
        if (getCuisine!!.endsWith(", ")) {
            getCuisine = getCuisine!!.substring(0, getCuisine!!.length - 2)
             setCuisine?.text=   getCuisine
        }
        if (response.restaurantDetails?.deliveryType.equals("DELIVERY_BY_SVAGGY") ||
            response.restaurantDetails?.deliveryType.equals("DELIVERY_BY_RESTAURANT")){
           // deliverTypeImg?.setImageResource(R.drawable.ic_check)
            deliverTypeNew?.setTextColor(Color.parseColor("#268836"))
            deliverTypeNew?.text = context.getString(R.string.delivery_Avai)
            val leftDrawable = ContextCompat.getDrawable(context, R.drawable.ic_check)
            deliverTypeNew?.setCompoundDrawablesWithIntrinsicBounds(leftDrawable,null,null,null)
        }
        else{
          //  deliverTypeImg?.setImageResource(R.drawable.ic_pick)
            deliverTypeNew?.setTextColor(Color.parseColor("#CE221E"))
            deliverTypeNew?.text = context.getString(R.string.pick_up)
            val leftDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pick)
            deliverTypeNew?.setCompoundDrawablesWithIntrinsicBounds(leftDrawable,null,null,null)

        }
        if (!response.restaurantDetails?.isActive!!){
            txtRestaurantClosed?.show()
            restClose?.show()
        }
        if (response.restaurantDetails?.is_rush_mode!!){
            rushCard?.show()
            spaceView?.hide()
        }
        else{
            rushCard?.hide()
            spaceView?.show()
        }
        if (!response.restaurantDetails?.discount.isNullOrEmpty()){
           // getDiscount?.text = response.restaurantDetails?.discount
            getDiscount?.text = context.getString(R.string.off_On,response.restaurantDetails?.discount.toString())
            dotLine?.show()
            getDiscount?.show()
        }else{
            dotLine?.invisible()
            getDiscount?.hide()

        }

        todaySchedule?.text = response.todaySchedule.toString()


        notifyDataSetChanged()
    }

    fun syncScrollPosition(fistPosition : Int, searchedItemPosition :  Int) {
        menuItemAdapter?.notifyItemChanged(fistPosition)
        menuItemAdapter?.notifyItemChanged(searchedItemPosition)
    }



//    fun filter(query: String) {
//        val lowerCaseQuery = query.lowercase()
//        filteredList = if (query.isEmpty()) {
//            ArrayList(itemList)
//        } else {
//            val filtered = ArrayList<RestaurantsMenuItem.Data.Categories>()
//            for (category in itemList) {
//                val filteredMenuItems = category.menuItems.filter {
//                    it.dishName?.lowercase()?.contains(lowerCaseQuery) == true || it.description?.lowercase()?.contains(lowerCaseQuery) == true
//                }
//                if (filteredMenuItems.isNotEmpty() || category.categoryName?.lowercase()?.contains(lowerCaseQuery) == true) {
//                    val filteredCategory = category.copy(menuItems = ArrayList(filteredMenuItems))
//                    filtered.add(filteredCategory)
//                }
//            }
//            filtered
//        }
//        notifyDataSetChanged()
//    }

    @SuppressLint("SuspiciousIndentation")
    inner class SearchBoxViewHolder(binding:HeaderItemBinding) : RecyclerView.ViewHolder(binding.root) {


        init {
            offerRc = binding.offerRc
            restName = binding.txtRestaurantName
            deliverTypeNew = binding.getDeliveryType
           // deliverTypeImg = binding.getTypeImg
            getRating = binding.txtRestaurantRating
            getReviewCount = binding.txtTotalReview
            getDeliveryTime = binding.getDeliveryTime
            getDiscount = binding.getDiscount
            dotLine = binding.line
            rushCard = binding.rushCard
            spaceView = binding.spaceView
            setCuisine = binding.txtCuisines
            searchView = binding.txtSearch
            restClose = binding.restCloseImg
            txtRestaurantClosed = binding.txtRestaurantClosed
            todaySchedule = binding.getResTiming
            addRestaurantFilter(binding)
              searchList(binding.txtSearch)
            showInfo(binding.getMoreInfo,binding.imageView50)
            goBack(binding.btnBack)
//            searchView?.addTextChangedListener(object : TextWatcher {
//                override fun beforeTextChanged(
//                    s: CharSequence?,
//                    start: Int,
//                    count: Int,
//                    after: Int
//                ) {
//                }
//
//                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    filter(s.toString())
//                }
//
//                override fun afterTextChanged(s: Editable?) {}
//            })

        }
    }


}

class TopLinearSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    override fun getVerticalSnapPreference(): Int {
        return SNAP_TO_START
    }
}
