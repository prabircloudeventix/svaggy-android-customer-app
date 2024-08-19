package com.svaggy.ui.activities.banner

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityComboScreenBinding
import com.svaggy.pagination.adapter.ComboMelPageAdapter
import com.svaggy.ui.activities.restaurant.RestaurantMenuActivity
import com.svaggy.ui.fragments.banner.adapter.ComboMealAdapter
import com.svaggy.ui.fragments.banner.viewmodel.BannerViewModel
import com.svaggy.ui.fragments.home.adapter.RestaurantItemFilter
import com.svaggy.ui.fragments.home.model.RestaurantFilter
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.invisible
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ComboScreenActivity : BaseActivity<ActivityComboScreenBinding>(ActivityComboScreenBinding::inflate) {
    private val mViewModel by viewModels<BannerViewModel>()
    private var searchText: String = ""
    private lateinit var comboMealAdapter: ComboMealAdapter
    private var restaurantFilter: ArrayList<RestaurantFilter> = ArrayList()
    private var emptyFilterList:ArrayList<RestaurantFilter>? = null
    private lateinit var comboMelPageAdapter:ComboMelPageAdapter


    override fun ActivityComboScreenBinding.initialize(){
        binding.txtSearch.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.categoryFilter.layoutManager = LinearLayoutManager(this@ComboScreenActivity, LinearLayoutManager.HORIZONTAL, false)
        addFilterData()
        /**
         * get combo
         */
        getCombo(searchText = null, filterList = null)

       binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchText = p0.toString()
                if (searchText.isNotEmpty())
                    binding.txtSearch.setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                            getCombo(searchText = searchText, filterList = emptyFilterList)
                            return@setOnEditorActionListener true
                        }
                        false
                    }

                else
                    getCombo(searchText = null, filterList = emptyFilterList)

            }

            override fun afterTextChanged(p0: Editable?) {


            }
        })
    }

    private fun getMealRestaurant(i: Int) {
        val intent = Intent(this,RestaurantMenuActivity::class.java)
        PrefUtils.instance.setString("item_id",i.toString())
    //    intent.putExtra("item_id",i.toString())
        startActivity(intent)
       // findNavController().navigate(R.id.action_comboScreen_to_restaurantMenuScreen,bundle)
    }



    //Preet
    private fun getCombo(searchText: String?, filterList: ArrayList<RestaurantFilter>?) {


        val json = JsonObject()
        json.addProperty("latitude",PrefUtils.instance.getString(Constants.Latitude).toString())
        json.addProperty("longitude", PrefUtils.instance.getString(Constants.Longitude).toString())
//        val map = mutableMapOf(
//            "latitude" to PrefUtils.instance.getString(Constants.Latitude).toString(),
//            "longitude" to PrefUtils.instance.getString(Constants.Longitude).toString(),
//        )
        filterList?.forEach {
            if (it.filterBoolean){
                json.addProperty(it.paramName,true)
            }
        }
        if (!searchText.isNullOrEmpty())
            json.addProperty("filter_text",searchText)



        lifecycleScope.launch {
            mViewModel.getComboPagination(authToken = "${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",json).collect { pagingData ->
                comboMelPageAdapter = ComboMelPageAdapter(context = this@ComboScreenActivity, getMealRestaurant =:: getMealRestaurant )
                comboMelPageAdapter.submitData(lifecycle,pagingData)
                binding.recyclerComboMeal.adapter = comboMelPageAdapter
                comboMelPageAdapter.loadStateFlow.collectLatest { loadState ->
                    when {
                        loadState.refresh is LoadState.Loading || loadState.append is LoadState.Loading -> {
                            // Show progress bar while refreshing or appending
                            // binding.progressBar?.isVisible = true
                            // binding.noOrder.visibility = View.GONE // Hide "no order" message
                        }

                        loadState.refresh is LoadState.Error || loadState.append is LoadState.Error -> {
                            binding.recyclerComboMeal.hide()
                            binding.shimmerBar.hide()
                            binding.emptyView.show()


                        }

                        loadState.refresh is LoadState.NotLoading && loadState.append is LoadState.NotLoading -> {

                            if (comboMelPageAdapter.itemCount == 0) {
                                binding.recyclerComboMeal.hide()
                                binding.shimmerBar.hide()
                                binding.emptyView.show()
                            } else {
                                binding.recyclerComboMeal.show()
                                binding.shimmerBar.hide()
                                binding.emptyView.hide()
                            }
                            //  binding.consEmptyRestaurant.hide()

                        }
                    }
                }
            }
        }


//        lifecycleScope.launch {
//            mViewModel.getCombo(
//                token ="${Constants.BEARER} ${PrefUtils.instance.getString(Constants.Token).toString()}",
//                map =map ).collect{
//                when (it) {
//                    is ApiResponse.Loading -> {
//                        binding.progressBarMenu.show()
//                    }
//                    is ApiResponse.Success -> {
//                        val data = it.data
//                        if (data != null){
//                            binding.progressBarMenu.hide()
//                            if (data.isSuccess!!) {
//                                comboMealAdapter = ComboMealAdapter(this@ComboScreenActivity, data.data,:: getMealRestaurant)
//                                binding.recyclerComboMeal.adapter = comboMealAdapter
//                            }
//                            else {
//                                Toast.makeText(this@ComboScreenActivity, "" + data.message, Toast.LENGTH_SHORT).show()
//                            }
//
//                        }
//                    }
//                    is ApiResponse.Failure -> {
//                        binding.progressBarMenu.hide()
//                        Toast.makeText(this@ComboScreenActivity, "${it.msg}", Toast.LENGTH_LONG).show()
//                    }
//
//                }
//
//
//            }
//        }
    }
    private fun addFilterData(){

        restaurantFilter.add(RestaurantFilter(getString(R.string.veg),"is_veg", ContextCompat.getDrawable(this, R.drawable.veg_icon),false))
        restaurantFilter.add(RestaurantFilter(getString(R.string.non_veg),"is_non_veg", ContextCompat.getDrawable(this, R.drawable.nonveg_icon), false))
        restaurantFilter.add(RestaurantFilter(getString(R.string.vegan_txt),"is_vegan", ContextCompat.getDrawable(this, R.drawable.vegan), false))
        restaurantFilter.add(RestaurantFilter(getString(R.string.top_rated),"top_rated",null, false))
        binding.categoryFilter.adapter = RestaurantItemFilter(this, restaurantFilter) { filterList ->
            emptyFilterList = ArrayList()
            emptyFilterList!!.addAll(filterList)
            getCombo(searchText = searchText,filterList = emptyFilterList)


        }




    }

}