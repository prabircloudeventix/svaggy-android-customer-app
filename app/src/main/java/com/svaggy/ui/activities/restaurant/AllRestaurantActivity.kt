package com.svaggy.ui.activities.restaurant

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityAllRestaurantBinding
import com.svaggy.pagination.adapter.AllRestaurantPageAdapter
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.home.adapter.AllRestaurantAdapter
import com.svaggy.ui.fragments.home.adapter.OfferViewAllAdapter
import com.svaggy.ui.fragments.home.adapter.RestaurantsFilterAdapter
import com.svaggy.ui.fragments.home.adapter.SortByAdapter
import com.svaggy.ui.fragments.home.model.RestaurantFilter
import com.svaggy.ui.fragments.home.model.SortByFilter
import com.svaggy.ui.fragments.home.viewmodel.HomeViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.invisible
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllRestaurantActivity : BaseActivity<ActivityAllRestaurantBinding>(ActivityAllRestaurantBinding::inflate) {
    private val mViewModel by viewModels<HomeViewModel>()
    private var cuisineId: String? = null
    var dialog: BottomSheetDialog? = null
    private var sortByList: ArrayList<SortByFilter> = ArrayList()
    private var emptyFilterList:ArrayList<RestaurantFilter> = ArrayList()
    private lateinit var allRestaurantPageAdapter:AllRestaurantPageAdapter

    override fun ActivityAllRestaurantBinding.initialize(){
        sortListAdd()



        binding.backButton.setOnClickListener {
           onBackPressed()

        }

        if (intent?.getStringExtra("method_call").equals("CuisineAllRestaurants")) {
            cuisineId = intent.getStringExtra("cuisine_id")
            getCuisineRestaurant(cuisineId =cuisineId, emptyFilterList =  emptyFilterList,sortValue = null)
            val restName = intent.getStringExtra("cuisine_name")
          binding.titleTv.text = getString(R.string.All_Restaurants_By,restName)
        }  else {
            binding.titleTv.text = getString(R.string.all_special_offers_near_you)
            getOfferRestaurant(filterList = emptyFilterList,sortValue = null)

        }
        restaurantFilterFun()


    }
    private fun restaurantFilterFun() {
        val filterTextList = listOf(
            RestaurantFilter(getString(R.string.sort_by),"sort_by", ContextCompat.getDrawable(this, R.drawable.ic_arrow_down),false),
            RestaurantFilter(getString(R.string.featured),"is_featured", null,false),
            RestaurantFilter(getString(R.string.greatOffers),"is_greatoffer", null,false),
            RestaurantFilter(getString(R.string.nearest),"is_nearest", null,false),
            RestaurantFilter(getString(R.string.veg),"is_veg", ContextCompat.getDrawable(this, R.drawable.veg_icon),false),
            RestaurantFilter(getString(R.string.non_veg),"is_non_veg", ContextCompat.getDrawable(this, R.drawable.nonveg_icon), false),
            RestaurantFilter(getString(R.string.vegan_txt),"is_vegan", ContextCompat.getDrawable(this, R.drawable.vegan), false))


        binding.recyclerRestaurantFilter.adapter = RestaurantsFilterAdapter(
            context = this,
            restaurantFilter = filterTextList,
            getSelectType = { _, _,list ->
                emptyFilterList.addAll(list)
                if (cuisineId != null)
                    getCuisineRestaurant(cuisineId = cuisineId,emptyFilterList)
                else
                    getOfferRestaurant(emptyFilterList)
            },
            sortType = { showSortDialog()})
    }


    @SuppressLint("SuspiciousIndentation")
    private fun showSortDialog(){
        val viewDialog = layoutInflater.inflate(R.layout.sheet_shortby_filter, null)
        dialog = BottomSheetDialog((this))
        dialog?.setOnShowListener {
            val bottomSheetDialogFragment = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            (viewDialog.parent as View).setBackgroundColor(ContextCompat.getColor((this), R.color.transparent))
            val behavior = bottomSheetDialogFragment?.let {
                BottomSheetBehavior.from(it)
            }
            behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        }

        val recyclerSortBy=viewDialog.findViewById<RecyclerView>(R.id.recyclerSortBy)
        val cancelImage=viewDialog.findViewById<ImageView>(R.id.cancelImage)

        cancelImage.setOnClickListener {
            dialog?.dismiss()
        }

        recyclerSortBy?.adapter = SortByAdapter(
            context = this,
            sortByList = sortByList,
            getRestaurantFilter = { value->
                if (cuisineId != null)
                    getCuisineRestaurant(cuisineId = cuisineId, emptyFilterList = emptyFilterList,sortValue = value)
                else
                    getOfferRestaurant(emptyFilterList,sortValue = value)

                dialog?.dismiss() })

        dialog?.setContentView(viewDialog)
        dialog?.show()

    }

    private fun getCuisineRestaurant(
        cuisineId: String?,
        emptyFilterList: ArrayList<RestaurantFilter>,
        sortValue:String? = null) {

        val json = JsonObject()
        json.addProperty("latitude",PrefUtils.instance.getString(Constants.Latitude).toString())
        json.addProperty("longitude", PrefUtils.instance.getString(Constants.Longitude).toString())
        json.addProperty("cuisine_id",cuisineId.toString())


        emptyFilterList.forEach {
            if (it.filterBoolean){
                // map[it.paramName] = true.toString()
                json.addProperty(it.paramName,true)
            }
        }
        if (sortValue != null){
            //  map["sort_by"] = sortValue
            json.addProperty("sort_by",sortValue)
        }


        lifecycleScope.launch {
            mViewModel.getCuisinesPagination(authToken = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",json).collect { pagingData ->
                allRestaurantPageAdapter = AllRestaurantPageAdapter(context = this@AllRestaurantActivity, goMenuScreen = ::goMenuScreen)
                allRestaurantPageAdapter.submitData(lifecycle,pagingData)
                binding.recyclerAllRestaurants.adapter = allRestaurantPageAdapter
                allRestaurantPageAdapter.loadStateFlow.collectLatest { loadState ->
                    when {
                        loadState.refresh is LoadState.Loading || loadState.append is LoadState.Loading -> {
                            binding.recyclerAllRestaurants.hide()
                            binding.shimmerBar.show()
                            binding.emptyView.hide()
                            // Show progress bar while refreshing or appending
                            // binding.progressBar?.isVisible = true
                            // binding.noOrder.visibility = View.GONE // Hide "no order" message
                        }

                        loadState.refresh is LoadState.Error || loadState.append is LoadState.Error -> {
                            binding.recyclerAllRestaurants.hide()
                            binding.shimmerBar.hide()
                            binding.emptyView.show()

                        }

                        loadState.refresh is LoadState.NotLoading && loadState.append is LoadState.NotLoading -> {
                            // Check if the adapter's item count is zero
                            if (allRestaurantPageAdapter.itemCount == 0) {
                                binding.recyclerAllRestaurants.hide()
                                binding.shimmerBar.hide()
                                binding.emptyView.show()
                            } else {
                                binding.recyclerAllRestaurants.show()
                                binding.shimmerBar.hide()
                                binding.emptyView.hide()
                            }


                        }
                    }
                }
            }
        }







//        lifecycleScope.launch {
//            mViewModel.getCuisineRestaurant(token = "${Constants.BEARER} ${
//                PrefUtils.instance.getString(
//                    Constants.Token).toString()}",
//                mMap = map).collect{
//
//                when (it) {
//                    is ApiResponse.Loading -> {
//                        mProgressBar().showProgressBar(this@AllRestaurantActivity)
//                    }
//                    is ApiResponse.Success -> {
//                        mProgressBar().dialog?.dismiss()
//                        val data = it.data
//                        if (data != null){
//                            if (data.isSuccess!!) {
//                                binding.recyclerAllRestaurants.layoutManager = LinearLayoutManager(this@AllRestaurantActivity, RecyclerView.VERTICAL, false)
//                                binding.recyclerAllRestaurants.isNestedScrollingEnabled = true
//                           //     allRestaurantAdapter = AllRestaurantAdapter(this@AllRestaurantActivity,data.data, ::goMenuScreen)
//                            //    binding.recyclerAllRestaurants.adapter = allRestaurantAdapter
//                            }
//                            else {
//                                Toast.makeText(this@AllRestaurantActivity, "" + data.message, Toast.LENGTH_SHORT).show()
//                            }
//
//                        }
//                    }
//                    is ApiResponse.Failure -> {
//                        mProgressBar().dialog?.dismiss()
//                        Toast.makeText(this@AllRestaurantActivity, "${it.msg}", Toast.LENGTH_LONG).show()
//                    }
//
//                }
//
//            }
//        }

    }







    private fun getOfferRestaurant(filterList: ArrayList<RestaurantFilter>,sortValue:String? = null) {


        val json = JsonObject()
        json.addProperty("latitude",PrefUtils.instance.getString(Constants.Latitude).toString())
        json.addProperty("longitude", PrefUtils.instance.getString(Constants.Longitude).toString())


        filterList.forEach {
            if (it.filterBoolean){
                json.addProperty(it.paramName,true)
                // map[it.paramName] = true.toString()
            }
        }
        if (sortValue != null){
            json.addProperty("sort_by", sortValue)
            //  map["sort_by"] = sortValue
        }



        lifecycleScope.launch {
            mViewModel.getOfferRestaurantsPagination(authToken = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",json).collect { pagingData ->
                allRestaurantPageAdapter = AllRestaurantPageAdapter(context = this@AllRestaurantActivity, goMenuScreen = ::goMenuScreen)
                allRestaurantPageAdapter.submitData(lifecycle,pagingData)
                binding.recyclerAllRestaurants.adapter = allRestaurantPageAdapter
                allRestaurantPageAdapter.loadStateFlow.collectLatest { loadState ->

                    when {
                        loadState.refresh is LoadState.Loading || loadState.append is LoadState.Loading -> {
                            // Show progress bar while refreshing or appending
                            // binding.progressBar?.isVisible = true
                            // binding.noOrder.visibility = View.GONE // Hide "no order" message
                            binding.recyclerAllRestaurants.hide()
                            binding.shimmerBar.show()
                            binding.emptyView.hide()
                        }

                        loadState.refresh is LoadState.Error || loadState.append is LoadState.Error -> {
                            binding.recyclerAllRestaurants.hide()
                            binding.shimmerBar.hide()
                            binding.emptyView.show()

                        }

                        loadState.refresh is LoadState.NotLoading && loadState.append is LoadState.NotLoading -> {
                            if (allRestaurantPageAdapter.itemCount == 0) {
                                binding.recyclerAllRestaurants.hide()
                                binding.shimmerBar.hide()
                                binding.emptyView.show()
                            } else {
                                binding.recyclerAllRestaurants.show()
                                binding.shimmerBar.hide()
                                binding.emptyView.hide()
                            }
                          //  binding.shimmerBar.hide()

                        }
                    }
                }
            }
        }

//        lifecycleScope.launch {
//            mViewModel.getOfferRestaurant(token = "${Constants.BEARER} ${
//                PrefUtils.instance.getString(
//                    Constants.Token).toString()}",
//                mMap =map ).collect{
//                when (it) {
//                    is ApiResponse.Loading -> {
//                        mProgressBar().showProgressBar(this@AllRestaurantActivity)
//
//                    }
//                    is ApiResponse.Success -> {
//                        mProgressBar().dialog?.dismiss()
//                        val data = it.data
//                        if (data != null){
//                            if (data.isSuccess!!) {
//                                offerViewAllAdapter = OfferViewAllAdapter(this@AllRestaurantActivity,  data.data, ::goMenuScreen)
//                                binding.recyclerAllRestaurants.adapter = offerViewAllAdapter
//                                if (data.data.isNotEmpty()){
//                                    binding.recyclerAllRestaurants.show()
//                                    binding.emptyView.hide()
//                                }else{
//                                    binding.recyclerAllRestaurants.hide()
//                                    binding.emptyView.show()
//                                }
//                            }
//                            else {
//                                Toast.makeText(this@AllRestaurantActivity, "" + data.message, Toast.LENGTH_SHORT).show()
//                            }
//
//                        }
//                    }
//                    is ApiResponse.Failure -> {
//                        mProgressBar().dialog?.dismiss()
//                        Toast.makeText(this@AllRestaurantActivity, "${it.msg}", Toast.LENGTH_LONG).show()
//                    }
//
//                }
//
//            }
//
//        }

    }









    private fun goMenuScreen(id: Int, restaurantName: String, boosterArray:ArrayList<Int>,deliveryTime:String) {
      //  val bundle = Bundle()
      //  bundle.putString("item_id", id.toString())
       // bundle.putString("item_restaurantName", restaurantName)
        val intent = Intent(this@AllRestaurantActivity,RestaurantMenuActivity::class.java)
        PrefUtils.instance.setString("item_id",id.toString())
       // intent.putExtra("item_id",id.toString())
        intent.putExtra("item_restaurantName",restaurantName)
        startActivity(intent)

       // findNavController().navigate(R.id.action_allRestaurantsScreen_to_restaurantMenuScreen, bundle)

      //  if (boosterArray.isNotEmpty())
       //     setBooster(boosterArray)
    }

    private fun setBooster(boosterArray: ArrayList<Int>) {
        val json = JsonObject()
        val boosterJsonArray = JsonArray()
        for (boosterId in boosterArray) {
            boosterJsonArray.add(boosterId)
        }
        json.add("booster_ids",boosterJsonArray)
        json.addProperty("type","CLICK")

        lifecycleScope.launch {
            mViewModel.setActionBooster(token =  "${Constants.BEARER} ${
                PrefUtils.instance.getString(
                    Constants.Token).toString()}",
                jsonObject = json).collect{
                when (it) {
                    is ApiResponse.Loading -> {


                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        Toast.makeText(this@AllRestaurantActivity,data!!.message.toString(), Toast.LENGTH_LONG).show()

                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(this@AllRestaurantActivity, "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }

    }

    private fun sortListAdd(){
        sortByList.add(SortByFilter("Relevance","relevance",false))
        sortByList.add(SortByFilter("Rating (High to Low)","ratings",false))
        sortByList.add(SortByFilter("Delivery Time (Low to High)","deliveryTimeLowToHigh",false))
        sortByList.add(SortByFilter("Delivery Time (High to Low)","deliveryTimeHighToLow",false))
    }

}