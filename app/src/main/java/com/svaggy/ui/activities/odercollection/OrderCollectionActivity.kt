package com.svaggy.ui.activities.odercollection

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.svaggy.R
import com.svaggy.app.BaseActivity
import com.svaggy.databinding.ActivityOrderCollectionBinding
import com.svaggy.ui.fragments.order.adapter.OrderTabItemAdapter
import com.svaggy.ui.fragments.order.ordertabs.CancelledFragment
import com.svaggy.ui.fragments.order.ordertabs.DeliveredFragment
import com.svaggy.ui.fragments.order.ordertabs.TodayFragment
import com.svaggy.ui.fragments.order.ordertabs.UpcomingFragment
import com.svaggy.utils.updateStatusBarColor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderCollectionActivity :BaseActivity<ActivityOrderCollectionBinding>(ActivityOrderCollectionBinding::inflate) {
    private var selectedIndex = 0
    private var tabTextItemList: List<String>? = null
    private lateinit var tabItemAdapter: OrderTabItemAdapter
    private lateinit var pagerAdapter: PagerAdapter
    var type = ""

    override fun ActivityOrderCollectionBinding.initialize(){
      updateStatusBarColor("#F6F6F6",true)
        type = intent.getStringExtra("type").toString()


        tabTextItemList = listOf(
           getString(R.string.today_txt),
            getString(R.string.upcoming_txt),
            getString(R.string.delivered_txt),
            getString(R.string.unfulfilled_txt))

        pagerAdapter = PagerAdapter(supportFragmentManager, lifecycle)
        binding.pager.adapter = pagerAdapter
        binding.pager.isUserInputEnabled = false

        tabItemAdapter = OrderTabItemAdapter(selectedIndex, this@OrderCollectionActivity, tabTextItemList!!) { tabPos ->
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


       binding.backButton.setOnClickListener {
           onBackPressed()
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


}


