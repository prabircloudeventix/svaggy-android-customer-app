package com.svaggy.ui.fragments.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ItemDeliveryOffersBinding
import com.svaggy.databinding.ItemMenuBinding
import com.svaggy.databinding.ItemMenuFilterLayoutBinding
import com.svaggy.databinding.ItemMenuTitleBinding
import com.svaggy.databinding.ItemRestaurantBinding
import com.svaggy.ui.fragments.home.model.FiltersItems
import com.svaggy.ui.fragments.home.model.MenuItems
import com.svaggy.ui.fragments.home.model.MenuItemsData
import com.svaggy.ui.fragments.home.model.MenuItemsTitle
import com.svaggy.ui.fragments.home.model.OfferType
import com.svaggy.ui.fragments.home.model.RestaurantDetails
import com.svaggy.utils.showRectImag


class RestaurantsMenusItemsAdapters(
    val context: Context,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<MenuItemsData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {


            VIEW_TYPE_RESTAURANTS_DETAILS -> {
                val binding =
                    ItemRestaurantBinding.inflate(inflater, parent, false)
                viewHolder = ViewHolderRestaurants(binding)
            }

            VIEW_TYPE_OFFERS -> {
                val binding =
                    ItemDeliveryOffersBinding.inflate(inflater, parent, false)
                viewHolder = ViewHolderOffer(binding)
            }

            VIEW_TYPE_FILTER -> {
                val binding =
                    ItemMenuFilterLayoutBinding.inflate(inflater, parent, false)
                viewHolder = ViewHolderFilters(binding)
            }

            VIEW_TYPE_MENU_TITLE -> {
                val binding =
                    ItemMenuTitleBinding.inflate(inflater, parent, false)
                viewHolder = ViewHolderMenuTitle(binding)
            }

            VIEW_TYPE_MENUS_ITEMS -> {
                val binding =
                    ItemMenuBinding.inflate(inflater, parent, false)
                viewHolder = ViewHolderMenuItems(binding)
            }

        }
        return viewHolder!!

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            VIEW_TYPE_RESTAURANTS_DETAILS -> {
                val result = data[position] as RestaurantDetails
                val itemViewHolder: ViewHolderRestaurants =
                    holder as ViewHolderRestaurants
                result.let { itemViewHolder.bind(it) }
            }

            VIEW_TYPE_OFFERS -> {
                val result = data[position] as OfferType
                val itemViewHolder: ViewHolderOffer =
                    holder as ViewHolderOffer
                result.let { itemViewHolder.bind(it) }
            }

            VIEW_TYPE_FILTER -> {
                val result = data[position] as FiltersItems
                val itemViewHolder: ViewHolderFilters =
                    holder as ViewHolderFilters
                result.let { itemViewHolder.bind(it) }
            }

            VIEW_TYPE_MENU_TITLE -> {
                val result = data[position] as MenuItemsTitle
                val itemViewHolder: ViewHolderMenuTitle =
                    holder as ViewHolderMenuTitle
                result.let { itemViewHolder.bind(it) }
            }
            VIEW_TYPE_MENUS_ITEMS -> {
                val result = data[position] as MenuItems
                val itemViewHolder: ViewHolderMenuItems =
                    holder as ViewHolderMenuItems
                result.let { itemViewHolder.bind(it) }
            }



        }
    }




    inner class ViewHolderRestaurants(val binding: ItemRestaurantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RestaurantDetails) {
            binding.txtRestaurantName.text = item.restaurantName
            binding.txtCuisines.text = item.restaurantCuisines

        }
    }

    inner class ViewHolderOffer(val binding: ItemDeliveryOffersBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OfferType) {


        }
    }

    inner class ViewHolderFilters(val binding: ItemMenuFilterLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FiltersItems) {
           //Binds Views

        }
    }


    inner class ViewHolderMenuTitle(val binding: ItemMenuTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MenuItemsTitle) {
            //Binds Views
            binding.tvcategoryName.text = item.titleName
        }
    }


    inner class ViewHolderMenuItems(val binding: ItemMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MenuItems) {
            //Binds View

            binding.txtDishName.text = item.dishName
            binding.txtDishPrice.text = item.price.toString()


            item.menuItemImage?.let { showRectImag(imageUrl = it, photoView = binding.imgDish) }


            when (item.dishType) {
                "Vegan" -> {
                    binding.imgDishType.setImageResource(R.drawable.vegan)
                }

                "Veg" -> {
                    binding.imgDishType.setImageResource(R.drawable.veg_icon)
                }

                "Non-Veg" -> {
                    binding.imgDishType.setImageResource(R.drawable.nonveg_icon)
                }
            }

        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is RestaurantDetails -> VIEW_TYPE_RESTAURANTS_DETAILS
            is OfferType -> VIEW_TYPE_OFFERS
            is FiltersItems -> VIEW_TYPE_FILTER
            is MenuItemsTitle -> VIEW_TYPE_MENU_TITLE
            is MenuItems -> VIEW_TYPE_MENUS_ITEMS
            else -> -0
        }
    }


    override fun getItemCount(): Int {
        return data.size
    }


    @SuppressLint("NotifyDataSetChanged")
    fun setData(newData: ArrayList<MenuItemsData>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong() - 1
    }


    companion object {
        private const val VIEW_TYPE_RESTAURANTS_DETAILS = 0
        private const val VIEW_TYPE_OFFERS = 1
        private const val VIEW_TYPE_FILTER = 2
        private const val VIEW_TYPE_MENU_TITLE = 3
        private const val VIEW_TYPE_MENUS_ITEMS = 4
    }


}