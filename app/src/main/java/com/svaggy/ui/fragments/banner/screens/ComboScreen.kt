package com.svaggy.ui.fragments.banner.screens

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.FragmentComboScreenBinding
import com.svaggy.ui.fragments.banner.adapter.ComboMealAdapter
import com.svaggy.ui.fragments.banner.viewmodel.BannerViewModel
import com.svaggy.ui.fragments.home.adapter.RestaurantItemFilter
import com.svaggy.ui.fragments.home.model.RestaurantFilter
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.BEARER
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ComboScreen : Fragment() {
    private var _binding: FragmentComboScreenBinding? = null
    private val binding get() = _binding!!
    private val mViewModel by viewModels<BannerViewModel>()
    private var searchText: String? = null
    private lateinit var comboMealAdapter: ComboMealAdapter
    private var restaurantFilter: ArrayList<RestaurantFilter> = ArrayList()
    private var emptyFilterList:ArrayList<RestaurantFilter>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComboScreenBinding.inflate(inflater, container, false)
      //  PrefUtils.instance.setString(Constants.FragmentBackName, "HomeFragment")
      //  PrefUtils.instance.setString(Constants.CurrentDestinationId,(activity as MainActivity).navController?.currentDestination?.id.toString())
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
      //  (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility=View.GONE
        (activity as MainActivity).binding.toolbar.titleTv.text = context?.getString(R.string.select_combo)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addFilterData()
        /**
         * get combo
         */
        getCombo(searchText = null, filterList = null)
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })

        binding.txtSearch.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())
        binding.categoryFilter.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchText = p0.toString()
                if (searchText!!.isNotEmpty())
                    binding.txtSearch.setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
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
        val bundle = Bundle()
        bundle.putString("item_id", i.toString())
        findNavController().navigate(R.id.action_comboScreen_to_restaurantMenuScreen,bundle)
    }



    //Preet
    private fun getCombo(searchText: String?, filterList: ArrayList<RestaurantFilter>?) {
        val map = mutableMapOf(
            "latitude" to PrefUtils.instance.getString(Constants.Latitude).toString(),
            "longitude" to PrefUtils.instance.getString(Constants.Longitude).toString(),
        )
        filterList?.forEach {
            if (it.filterBoolean){
                map[it.paramName] = true.toString()
            }
        }
        if (!searchText.isNullOrEmpty())
            map["filter_text"] = searchText


        lifecycleScope.launch {
            mViewModel.getCombo(
                token ="$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map =map ).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                    binding.progressBarMenu.show()
                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        if (data != null){
                          binding.progressBarMenu.hide()
                            if (data.isSuccess!!) {
                                comboMealAdapter = ComboMealAdapter(requireContext(), data.data,:: getMealRestaurant)
                                binding.recyclerComboMeal.adapter = comboMealAdapter
                            }
                            else {
                                Toast.makeText(context, "" + data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                    is ApiResponse.Failure -> {
                        binding.progressBarMenu.hide()
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }


            }
        }
    }
    private fun addFilterData(){

        restaurantFilter.add(RestaurantFilter(getString(R.string.veg),"is_veg", ContextCompat.getDrawable(requireContext(), R.drawable.veg_icon),false))
        restaurantFilter.add(RestaurantFilter(getString(R.string.non_veg),"is_non_veg", ContextCompat.getDrawable(requireContext(), R.drawable.nonveg_icon), false))
        restaurantFilter.add(RestaurantFilter(getString(R.string.vegan_txt),"is_vegan", ContextCompat.getDrawable(requireContext(), R.drawable.vegan), false))
        restaurantFilter.add(RestaurantFilter(getString(R.string.top_rated),"top_rated",null, false))
        binding.categoryFilter.adapter = RestaurantItemFilter(requireContext(), restaurantFilter) { filterList ->
            emptyFilterList = ArrayList()
            emptyFilterList!!.addAll(filterList)
            getCombo(searchText = searchText,filterList = emptyFilterList)


        }




    }

}