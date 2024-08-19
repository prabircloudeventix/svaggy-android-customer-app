package com.svaggy.ui.fragments.order.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.svaggy.ui.activities.MainActivity
import com.svaggy.R
import com.svaggy.app.SvaggyApplication
import com.svaggy.databinding.FragmentOrdersScreenBinding
import com.svaggy.ui.fragments.order.adapter.OrderTabItemAdapter
import com.svaggy.ui.fragments.order.ordertabs.CancelledFragment
import com.svaggy.ui.fragments.order.ordertabs.DeliveredFragment
import com.svaggy.ui.fragments.order.ordertabs.TodayFragment
import com.svaggy.ui.fragments.order.ordertabs.UpcomingFragment
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.onBackPressedDispatcher
import com.svaggy.utils.updateStatusBarColor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrdersScreen : Fragment() {
    lateinit var binding: FragmentOrdersScreenBinding
    private var selectedIndex = 0
    private var tabTextItemList: List<String>? = null
    private lateinit var tabItemAdapter: OrderTabItemAdapter
    private lateinit var pagerAdapter: PagerAdapter
    var type = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        requireActivity().updateStatusBarColor("#F6F6F6",true)
        binding = FragmentOrdersScreenBinding.inflate(inflater, container, false)

        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = context?.getString(R.string.order_txt)
      //  (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility = View.GONE
         type = arguments?.getString("type").toString()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBackPressedDispatcher {
            findNavController().popBackStack()
        }

        requireActivity().updateStatusBarColor("#F6F6F6",true)
        (activity as MainActivity).binding.toolbar.toolbarId.visibility = View.VISIBLE
        (activity as MainActivity).binding.toolbar.titleTv.text = context?.getString(R.string.order_txt)
     //   (activity as MainActivity).binding.bottomNavigationView.bottomNavigationMain.visibility = View.GONE
        type = arguments?.getString("type").toString()


        tabTextItemList = listOf(
            requireContext().getString(R.string.today_txt),
            requireContext().getString(R.string.upcoming_txt),
            requireContext().getString(R.string.delivered_txt),
            requireContext().getString(R.string.unfulfilled_txt))

        pagerAdapter = PagerAdapter(childFragmentManager, lifecycle)
        binding.pager.adapter = pagerAdapter
        binding.pager.isUserInputEnabled = false

            tabItemAdapter = OrderTabItemAdapter(selectedIndex, requireContext(), tabTextItemList!!) { tabPos ->
                binding.pager.setCurrentItem(tabPos, true)
                selectedIndex = tabPos
            }




        binding.pager.setCurrentItem(selectedIndex, false)
        binding.tabItemRc.adapter = tabItemAdapter

        if (type.isNotEmpty() && type == "ACCEPTED_ORDER"){
            binding.pager.setCurrentItem(1,true)
            selectedIndex = 1
            tabItemAdapter.getSwitchPos(1)

        }


        (activity as MainActivity).binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()

        }
    }
}

class PagerAdapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm,lifecycle) {
    override fun getItemCount(): Int {
        return  4
    }
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TodayFragment()
            1 -> UpcomingFragment()
            2 -> DeliveredFragment()
            3 -> CancelledFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}