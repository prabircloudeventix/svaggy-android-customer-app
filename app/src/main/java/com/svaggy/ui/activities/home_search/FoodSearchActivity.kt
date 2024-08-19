package com.svaggy.ui.activities.home_search

import android.content.Intent
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityFoodSearchBinding
import com.svaggy.ui.activities.restaurant.AllRestaurantActivity
import com.svaggy.ui.activities.restaurant.RestaurantMenuActivity
import com.svaggy.ui.fragments.home.adapter.CuisinesAdapter
import com.svaggy.ui.fragments.home.adapter.RecentSearchAdapter
import com.svaggy.ui.fragments.home.adapter.SearchAdapter
import com.svaggy.ui.fragments.home.model.GetCuisines
import com.svaggy.ui.fragments.home.model.Search
import com.svaggy.ui.fragments.home.viewmodel.HomeViewModel
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.ITEM_ID
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Type

@AndroidEntryPoint
class FoodSearchActivity : BaseActivity<ActivityFoodSearchBinding>(ActivityFoodSearchBinding::inflate) {


    private var cuisinesAdapter: CuisinesAdapter? = null
    private var cuisinesLists = ArrayList<GetCuisines.Data>()

    private lateinit var searchText: String
    private var recentSearchLists = ArrayList<Search.Data>()
    private var recentSearchAdapter: RecentSearchAdapter? = null
    private var searchAdapter: SearchAdapter? = null
    private val mViewModel by viewModels<HomeViewModel>()


    override fun ActivityFoodSearchBinding.initialize(){
        binding.edtSearch.requestFocus()
       // PrefUtils.instance.showKeyboard(this@FoodSearchActivity)
        initObserver()
        mViewModel.getPopularCuisines("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}")

        binding.edtSearch.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                searchText = p0.toString()
                mViewModel.searchData(
                    "Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                    p0.toString(),"",""
                )
            }
        })

    }
    private fun initObserver() {
        mViewModel.getCuisinesMutable.observe(this) {
            if (it.isSuccess!!) {
                binding.progressBarMenu.hide()
                binding.recyclerCuisines.visibility = View.VISIBLE
                binding.recyclerSearch.visibility = View.GONE
                binding.recyclerCuisines.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
                binding.recyclerCuisines.isNestedScrollingEnabled = true
                cuisinesAdapter =  CuisinesAdapter(this, it.data,:: getCuisineRestaurant)
                binding.recyclerCuisines.adapter = cuisinesAdapter
                cuisinesLists = it.data

//                if (recentSearchLists.size > 0) {
//                    binding.txtRecentSearch.visibility = View.VISIBLE
//                    binding.recyclerRecentSearch.visibility = View.VISIBLE
//
//
//                }
//                else {
//                    binding.txtRecentSearch.visibility = View.GONE
//                    binding.recyclerRecentSearch.visibility = View.GONE
//                }
            } else {
                binding.progressBarMenu.hide()
//                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
        mViewModel.getSearchDataMutable.observe(this) {
            if (it.isSuccess!!) {
                binding.progressBarMenu.hide()
                if (it.data.size > 0) {
                    binding.recyclerCuisines.visibility = View.GONE
                    binding.recyclerSearch.visibility = View.VISIBLE
                    if (searchText.isNotEmpty()) {
                        binding.txtPopularCuisines.visibility = View.VISIBLE
                        binding.txtPopularCuisines.text = getString(R.string.showing_result,searchText)
                    }
                    else
                    {
                        binding.txtPopularCuisines.hide()
                        binding.txtPopularCuisines.text = ""
                    }
                    binding.recyclerSearch.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                    binding.recyclerSearch.isNestedScrollingEnabled = true
                    searchAdapter = SearchAdapter( it.data,:: getSearchData)
                    binding.recyclerSearch.adapter = searchAdapter
                }
                else
                {
                    binding.txtPopularCuisines.hide()
                    binding.recyclerCuisines.visibility = View.VISIBLE
                    binding.recyclerSearch.visibility = View.GONE
                }

            } else {
                binding.progressBarMenu.hide()
//                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val json = PrefUtils.instance.getString("courses")
        if (!json.isNullOrEmpty()) {
            val type: Type = object : TypeToken<ArrayList<Search.Data>>() {}.type
            recentSearchLists =  Gson().fromJson(json, type)
        }
        binding.recyclerRecentSearch.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recentSearchAdapter =  RecentSearchAdapter(this, recentSearchLists,:: getSearchData)
        binding.recyclerRecentSearch.adapter = recentSearchAdapter
        if (recentSearchAdapter?.itemCount == 0){
            binding.txtRecentSearch.hide()

        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getCuisineRestaurant(cuisineId: Int, cuisineName: String) {
        val  intent = Intent(this,AllRestaurantActivity::class.java)
        intent.putExtra("cuisine_id",cuisineId.toString())
        intent.putExtra("cuisine_name",cuisineName)
        intent.putExtra("method_call","CuisineAllRestaurants")
        startActivity(intent)
    }

    private fun getSearchData(searchId: Int, restaurantId:Int, searchType:String, name:String) {

//        val originalList:ArrayList<String> = ArrayList()
//        if (!recentSearchLists.isEmpty()) {
//            if (recentSearchLists.size > 3) {
//                for (i in 0 until recentSearchLists.size){
//                    originalList.add(recentSearchLists[i].name?:"")
//                    if (originalList.size==recentSearchLists.size){
//                        if (originalList.contains(name)) {
//                            originalList.remove(name)
//                            recentSearchListsDs=recentSearchLists.distinct()
//                        }
//                        else {
//                            recentSearchLists.add(0, Search.Data(searchId, restaurantId, searchType, name))
//                        }
//                    }
//                }
//                recentSearchLists.add(Search.Data(searchId, restaurantId, searchType, name))
//                recentSearchListsDs=recentSearchLists.distinct()
//            }
//            else {
//                recentSearchLists.add(0,Search.Data(searchId,restaurantId,searchType,name))
//                recentSearchListsDs=recentSearchLists.distinct()
//            }
//
//        }
//        else {
//            recentSearchLists.add(Search.Data(searchId,restaurantId,searchType,name))
//            recentSearchListsDs=recentSearchLists.distinct()
//        }
     //   PrefUtils.instance.saveSearchList("courses",recentSearchLists)
        PrefUtils.instance.saveSearchListNew("courses",Search.Data(searchId,restaurantId,searchType,name))

        val restId:Int
        if (searchType == "Restaurant") {
            restId = searchId
        }
        else {
            restId = restaurantId
        }
//        val bundle = Bundle()
//        bundle.putString("cuisine_id", searchId.toString())
//        bundle.putString("item_id", restId.toString())
//        bundle.putString("cuisine_name", name)
//        bundle.putString("method_call", "CuisineAllRestaurants")
        if (restId > 0) {
           val intent =Intent(this,RestaurantMenuActivity::class.java)
            PrefUtils.instance.setString("item_id",restId.toString())
            intent.putExtra("itemId", searchId)
           // intent.putExtra(ITEM_ID,restId.toString())
            intent.putExtra("cuisine_name",name)
            intent.putExtra("isFrom","SearchActivity")
            startActivity(intent)
         //   findNavController().navigate(R.id.action_searchFragment_to_restaurantMenuScreen,bundle)
        }
        else {
           val intent = Intent(this,AllRestaurantActivity::class.java)
            intent.putExtra("cuisine_id", searchId.toString())
            intent.putExtra("item_id",restId.toString())
            intent.putExtra("cuisine_name",name)
            intent.putExtra("method_call","CuisineAllRestaurants")
            startActivity(intent)
         //   findNavController().navigate(R.id.action_searchFragment_to_allRestaurantsScreen,bundle)
        }
    }

}