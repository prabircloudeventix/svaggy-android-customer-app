package com.svaggy.ui.fragments.home.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.databinding.FragmentAllRestaurantsScreenBinding
import com.svaggy.ui.fragments.home.adapter.AllRestaurantAdapter
import com.svaggy.ui.fragments.home.adapter.OfferViewAllAdapter
import com.svaggy.ui.fragments.home.adapter.RestaurantsFilterAdapter
import com.svaggy.ui.fragments.home.adapter.SortByAdapter
import com.svaggy.ui.fragments.home.model.RestaurantFilter
import com.svaggy.ui.fragments.home.model.SortByFilter
import com.svaggy.ui.fragments.home.viewmodel.HomeViewModel
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.BEARER
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.onBackPressedDispatcher
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllRestaurantsScreen : Fragment() {



    private var _binding: FragmentAllRestaurantsScreenBinding? = null
    private val binding get() = _binding!!





    private var offerViewAllAdapter: OfferViewAllAdapter? = null
    private var allRestaurantAdapter: AllRestaurantAdapter? = null
    private val mViewModel by viewModels<HomeViewModel>()
    private var cuisineId: String? = null
    var dialog: BottomSheetDialog? = null
    private var sortByList: ArrayList<SortByFilter> = ArrayList()
    private var emptyFilterList:ArrayList<RestaurantFilter> = ArrayList()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAllRestaurantsScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
     //   (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility = View.GONE
        sortListAdd()

        onBackPressedDispatcher{
            findNavController().popBackStack()
        }

        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        if (arguments?.getString("method_call").equals("CuisineAllRestaurants")) {
            cuisineId = arguments?.getString("cuisine_id")
            getCuisineRestaurant(cuisineId =cuisineId, emptyFilterList =  emptyFilterList,sortValue = null)
            val restName = arguments?.getString("cuisine_name")
            (activity as MainActivity).binding.toolbar.titleTv.text = requireContext().getString(R.string.All_Restaurants_By,restName)
        }  else {
            (activity as MainActivity).binding.toolbar.titleTv.text = getString(R.string.all_special_offers_near_you)
            getOfferRestaurant(filterList = emptyFilterList,sortValue = null)

        }
        restaurantFilterFun()

    }

    private fun restaurantFilterFun() {
        val filterTextList = listOf(
            RestaurantFilter(getString(R.string.sort_by),"sort_by", ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down),false),
            RestaurantFilter(getString(R.string.featured),"is_featured", null,false),
            RestaurantFilter(getString(R.string.greatOffers),"is_greatoffer", null,false),
            RestaurantFilter(getString(R.string.nearest),"is_nearest", null,false),
            RestaurantFilter(getString(R.string.veg),"is_veg", ContextCompat.getDrawable(requireContext(), R.drawable.veg_icon),false),
            RestaurantFilter(getString(R.string.non_veg),"is_non_veg", ContextCompat.getDrawable(requireContext(), R.drawable.nonveg_icon), false),
            RestaurantFilter(getString(R.string.vegan_txt),"is_vegan", ContextCompat.getDrawable(requireContext(), R.drawable.vegan), false))


        binding.recyclerRestaurantFilter.adapter = RestaurantsFilterAdapter(
            context = requireContext(),
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
        dialog = BottomSheetDialog(requireContext())
        dialog?.setOnShowListener {
            val bottomSheetDialogFragment = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            (viewDialog.parent as View).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
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
            context = requireContext(),
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

        val map = mutableMapOf(
            "cuisine_id" to cuisineId.toString(),
            "latitude" to PrefUtils.instance.getString(Constants.Latitude).toString(),
            "longitude" to PrefUtils.instance.getString(Constants.Longitude).toString(),
        )


        emptyFilterList.forEach {
            if (it.filterBoolean){
                map[it.paramName] = true.toString()
            }
        }
        if (sortValue != null){
            map["sort_by"] = sortValue
        }


        lifecycleScope.launch {
            mViewModel.getCuisineRestaurant(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                mMap = map).collect{

                when (it) {
                    is ApiResponse.Loading -> {
                      //  progressBarHelper.showProgressBar()
                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        if (data != null){
                       //  progressBarHelper.dismissProgressBar()
                            if (data.isSuccess!!) {
                                binding.recyclerAllRestaurants.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                                binding.recyclerAllRestaurants.isNestedScrollingEnabled = true
                           //     allRestaurantAdapter = AllRestaurantAdapter(requireContext(),data.data, ::goMenuScreen)
                                binding.recyclerAllRestaurants.adapter = allRestaurantAdapter
                            }
                            else {
                                Toast.makeText(context, "" + data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                    is ApiResponse.Failure -> {
                     //   progressBarHelper.dismissProgressBar()
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }
        }

    }

    private fun getOfferRestaurant(filterList: ArrayList<RestaurantFilter>,sortValue:String? = null) {
        val map = mutableMapOf(
            "latitude" to PrefUtils.instance.getString(Constants.Latitude).toString(),
            "longitude" to PrefUtils.instance.getString(Constants.Longitude).toString())

        filterList.forEach {
            if (it.filterBoolean){
                map[it.paramName] = true.toString()
            }
        }
        if (sortValue != null){
            map["sort_by"] = sortValue
        }




        lifecycleScope.launch {
            mViewModel.getOfferRestaurant(token = "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                mMap =map ).collect{
                when (it) {
                    is ApiResponse.Loading -> {
                     //   progressBarHelper.showProgressBar()

                    }
                    is ApiResponse.Success -> {
                  //      progressBarHelper.dismissProgressBar()
                        val data = it.data
                        if (data != null){
                            if (data.isSuccess!!) {
                              //  offerViewAllAdapter = OfferViewAllAdapter(requireContext(),  data.data, ::goMenuScreen)
                                    binding.recyclerAllRestaurants.adapter = offerViewAllAdapter
                                if (data.data.isNotEmpty()){
                                    binding.recyclerAllRestaurants.show()
                                    binding.emptyView.hide()
                                }else{
                                    binding.recyclerAllRestaurants.hide()
                                    binding.emptyView.show()
                                }
                            }
                            else {
                                Toast.makeText(context, "" + data.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                    is ApiResponse.Failure -> {
                    //    progressBarHelper.dismissProgressBar()
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
                    }

                }

            }

        }

    }
    private fun goMenuScreen(id: Int, restaurantName: String, boosterArray:ArrayList<Int>) {
        val bundle = Bundle()
        bundle.putString("item_id", id.toString())
        bundle.putString("item_restaurantName", restaurantName)

        findNavController().navigate(R.id.action_allRestaurantsScreen_to_restaurantMenuScreen, bundle)
        if (boosterArray.isNotEmpty())
            setBooster(boosterArray)
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
            mViewModel.setActionBooster(token =  "$BEARER ${PrefUtils.instance.getString(Constants.Token).toString()}",
                jsonObject = json).collect{
                when (it) {
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Success -> {
                        val data = it.data

                    }
                    is ApiResponse.Failure -> {
                        Toast.makeText(requireContext(), "${it.msg}", Toast.LENGTH_LONG).show()
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
