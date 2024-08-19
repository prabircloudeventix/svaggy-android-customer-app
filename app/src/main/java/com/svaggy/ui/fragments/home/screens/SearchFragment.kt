package com.svaggy.ui.fragments.home.screens

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.FragmentSearchBinding
import com.svaggy.ui.fragments.home.adapter.CuisinesAdapter
import com.svaggy.ui.fragments.home.adapter.RecentSearchAdapter
import com.svaggy.ui.fragments.home.adapter.SearchAdapter
import com.svaggy.ui.fragments.home.model.GetCuisines
import com.svaggy.ui.fragments.home.model.Search
import com.svaggy.ui.fragments.home.viewmodel.HomeViewModel
import com.svaggy.utils.Constants
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.onBackPressedDispatcher
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Type

@AndroidEntryPoint
class SearchFragment : Fragment() {
    lateinit var binding: FragmentSearchBinding

    private var cuisinesAdapter: CuisinesAdapter? = null
    private var cuisinesLists = ArrayList<GetCuisines.Data>()

    private lateinit var searchText: String
    private var recentSearchLists = ArrayList<Search.Data>()
    private var recentSearchListsDs:List<Search.Data>?=null
    private var recentSearchAdapter: RecentSearchAdapter? = null
    private var searchAdapter: SearchAdapter? = null
    private val mViewModel by viewModels<HomeViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        initObserver()
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = requireContext().getString(R.string.search)
   //     (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility=View.GONE
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.edtSearch.requestFocus()
        PrefUtils.instance.showKeyboard(requireActivity())
        mViewModel.getPopularCuisines("Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBackPressedDispatcher{
            findNavController().navigate(R.id.homeFragment)

        }
        val json = PrefUtils.instance.getString("courses")
        if (!json.isNullOrEmpty()) {
            val type: Type = object : TypeToken<ArrayList<Search.Data>>() {}.type
            recentSearchLists =  Gson().fromJson(json, type)
        }


        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
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
        mViewModel.getCuisinesMutable.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
              binding.progressBarMenu.hide()
                binding.recyclerCuisines.visibility = View.VISIBLE
                binding.recyclerSearch.visibility = View.GONE
                binding.recyclerCuisines.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                binding.recyclerCuisines.isNestedScrollingEnabled = true
                cuisinesAdapter = context?.let { it1 -> CuisinesAdapter(it1, it.data,:: getCuisineRestaurant) }
                binding.recyclerCuisines.adapter = cuisinesAdapter
                cuisinesLists = it.data

                if (recentSearchLists.size > 0)
                {
                    binding.txtRecentSearch.visibility = View.VISIBLE
                    binding.recyclerRecentSearch.visibility = View.VISIBLE

                    binding.recyclerRecentSearch.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    binding.recyclerRecentSearch.isNestedScrollingEnabled = false
                    recentSearchAdapter = context?.let { it1 -> RecentSearchAdapter(it1, recentSearchLists,:: getSearchData) }
                    binding.recyclerRecentSearch.adapter = recentSearchAdapter
                }
                else {
                    binding.txtRecentSearch.visibility = View.GONE
                    binding.recyclerRecentSearch.visibility = View.GONE
                }
            } else {
              binding.progressBarMenu.hide()
//                Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show()
            }
        }
        mViewModel.getSearchDataMutable.observe(viewLifecycleOwner) {
            if (it.isSuccess!!) {
               binding.progressBarMenu.hide()
                if (it.data.size > 0)
                {
                    binding.recyclerCuisines.visibility = View.GONE
                    binding.recyclerSearch.visibility = View.VISIBLE
                    if (searchText.isNotEmpty()) {
                        binding.txtPopularCuisines.visibility = View.VISIBLE
                        binding.txtPopularCuisines.text = requireContext().getString(R.string.showing_result,searchText)
                    }
                    else
                    {
                        binding.txtPopularCuisines.hide()
                        binding.txtPopularCuisines.text = ""
                    }
                    binding.recyclerSearch.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
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

    private fun getCuisineRestaurant(cuisineId: Int, cuisineName: String) {
        val bundle = Bundle()
        bundle.putString("cuisine_id", cuisineId.toString())
        bundle.putString("cuisine_name", cuisineName)
        bundle.putString("method_call", "CuisineAllRestaurants")
        findNavController().navigate(R.id.action_searchFragment_to_allRestaurantsScreen,bundle)
    }

    private fun getSearchData(searchId: Int, restaurantId:Int, searchType:String, name:String)
    {
//        if (recentSearchLists.isEmpty()){
//            recentSearchLists.add(Search.Data(searchId,restaurantId,searchType,name))
//        }
        val originalList:ArrayList<String> = ArrayList()
        if (!recentSearchLists.isEmpty())
        {
            if (recentSearchLists.size > 3)
            {
                for (i in 0 until recentSearchLists.size){
                    originalList.add(recentSearchLists[i].name?:"")
                    if (originalList.size==recentSearchLists.size){
                        if (originalList.contains(name)) {
                            originalList.remove(name)
                            recentSearchListsDs=recentSearchLists.distinct()
                        } else {
                                val lastElement = recentSearchLists.removeAt(recentSearchLists.size - 1)
                            recentSearchLists.add(0, Search.Data(searchId, restaurantId, searchType, name))
                        }
                    }
                }
                recentSearchLists.add(Search.Data(searchId, restaurantId, searchType, name))
                recentSearchListsDs=recentSearchLists.distinct()
            }
            else
            {
                recentSearchLists.add(0,Search.Data(searchId,restaurantId,searchType,name))
                recentSearchListsDs=recentSearchLists.distinct()
            }

        }
        else {
            recentSearchLists.add(Search.Data(searchId,restaurantId,searchType,name))
            recentSearchListsDs=recentSearchLists.distinct()
        }
        PrefUtils.instance.saveSearchList("courses",recentSearchLists)

        val restId:Int
        if (searchType == "Restaurant") {
            restId = searchId
        }
        else {
            restId = restaurantId
        }
        val bundle = Bundle()
        bundle.putString("cuisine_id", searchId.toString())
        bundle.putString("item_id", restId.toString())
        bundle.putString("cuisine_name", name)
        bundle.putString("method_call", "CuisineAllRestaurants")
        if (restId > 0) {
            findNavController().navigate(R.id.action_searchFragment_to_restaurantMenuScreen,bundle)
        }
        else
        {
            findNavController().navigate(R.id.action_searchFragment_to_allRestaurantsScreen,bundle)
        }
    }
}