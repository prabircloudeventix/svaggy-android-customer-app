package com.svaggy.ui.fragments.banner.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.svaggy.R
import com.svaggy.databinding.FragmentCompareFoodPricesBinding
import com.svaggy.ui.activities.MainActivity
import com.svaggy.ui.fragments.banner.adapter.CompareFoodAdapter
import com.svaggy.ui.fragments.banner.viewmodel.BannerViewModel
import com.svaggy.ui.fragments.home.adapter.SortByAdapter
import com.svaggy.ui.fragments.home.model.SortByFilter
import com.svaggy.utils.ApiResponse
import com.svaggy.utils.Constants
import com.svaggy.utils.NoLeadingSpaceFilter
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CompareFoodPrices : Fragment() {

    private var _binding: FragmentCompareFoodPricesBinding? = null
    private val binding get() = _binding!!
    private val sortByList:ArrayList<SortByFilter> = ArrayList()




    private val mViewModel by viewModels<BannerViewModel>()
    private var searchText: String? = null
    private lateinit var compareFoodAdapter: CompareFoodAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCompareFoodPricesBinding.inflate(inflater, container, false)
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = ContextCompat.getString(requireContext(), R.string.compare_food_prices)
        addSortList()
        return binding.root
    }

    private fun addSortList() {
        sortByList.add(SortByFilter("Popular","POPULAR",false))
        sortByList.add(SortByFilter("Near Me","NEAR_ME",false))
        sortByList.add(SortByFilter("Cost (High to Low)","COST_HIGH_TO_LOW",false))
        sortByList.add(SortByFilter("Cost (Low to High)","COST_LOW_TO_HIGH",false))
        sortByList.add(SortByFilter("Rating (High to Low)","RATINGS_HIGH_TO_LOW",false))

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })

        binding.txtSearch.filters = arrayOf<InputFilter>(NoLeadingSpaceFilter())

        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                searchText = p0.toString().trim()
                if (!searchText.isNullOrEmpty()){
                    binding.txtSearch.setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            getFoodCompare(filterText = searchText!!,sortBy = null)

                            return@setOnEditorActionListener true
                        }
                        false
                    }

                }else
                    getFoodCompare(filterText = "",sortBy = null)



            }
        })

        binding.sortBy.setOnClickListener {
            showSortDialog()

        }
    }

    private fun showSortDialog(){

        val viewDialog = layoutInflater.inflate(R.layout.sheet_shortby_filter, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setOnShowListener {
            val bottomSheetDialogFragment = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            (viewDialog.parent as View).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
            val behavior = bottomSheetDialogFragment?.let {
                BottomSheetBehavior.from(it)
            }
            behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        }

        val recyclerSortBy=viewDialog.findViewById<RecyclerView>(R.id.recyclerSortBy)
        val cancelImage=viewDialog.findViewById<ImageView>(R.id.cancelImage)

        cancelImage.setOnClickListener {
            dialog.dismiss()
        }

        recyclerSortBy?.adapter = SortByAdapter(
            context = requireContext(),
            sortByList = sortByList,
            getRestaurantFilter = { value->
               getFoodCompare(filterText =searchText,sortBy = value )
                dialog.dismiss() })

        dialog.setContentView(viewDialog)
        dialog.show()

    }



    @SuppressLint("SetTextI18n")
    private  fun getFoodCompare(filterText:String?, sortBy:String?){

            val map = mutableMapOf("filter_text" to filterText!!)

        if (sortBy != null)
            map["sort_by"] = sortBy



        lifecycleScope.launch {
            mViewModel.getCompareFood(token ="Bearer ${PrefUtils.instance.getString(Constants.Token).toString()}",
                map = map).collect{
                    when (it) {
                    is ApiResponse.Loading -> {
                      binding.progressBarMenu.show()
                    }
                    is ApiResponse.Success -> {
                        val data = it.data
                        if (data != null){
                            if (data.isSuccess!!) {
                                if (!searchText.isNullOrEmpty()) {
                                    binding.txtShowingResult.text = "${ContextCompat.getString(requireContext(), R.string.showing_results_for)} \""
                                    binding.txtSearchTxt.text = searchText+"\""
                                    binding.txtShowingResult.show()
                                    binding.txtSearchTxt.show()
                                    binding.sortBy.show()
                                } else {
                                    binding.txtShowingResult.hide()
                                    binding.txtShowingResult.text = ""
                                    binding.txtSearchTxt.hide()
                                    binding.sortBy.hide()
                                }
                                binding.recyclerCompare.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                                compareFoodAdapter = CompareFoodAdapter(requireContext(), data.data, ::getCompareFoodRestaurant)
                                binding.recyclerCompare.adapter = compareFoodAdapter
                            }
                          binding.progressBarMenu.hide()

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

    private fun getCompareFoodRestaurant(restaurantId: Int, menuId: Int, productName: TextView) {
        val bundle = Bundle()
        bundle.putString("item_id", restaurantId.toString())
        bundle.putString("search_text", productName.text.toString())
        bundle.putInt("menu_id", menuId)
        bundle.putString("isFrom", "CompareFood")
        findNavController().navigate(R.id.action_compareFoodPrices_to_restaurantMenuScreen, bundle)
    }
}