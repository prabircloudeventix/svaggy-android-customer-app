package com.svaggy.pagination.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.svaggy.R
import com.svaggy.databinding.ItemAllRestaurantsBinding
import com.svaggy.ui.fragments.home.model.AllRestaurant
import com.svaggy.utils.hide
import com.svaggy.utils.show

class AllRestaurantPageAdapter(
    private val context: Context,
    private val goMenuScreen: (Int, String, ArrayList<Int>, String) -> Unit
) : PagingDataAdapter<AllRestaurant.Data, RecyclerView.ViewHolder>(RestaurantDiffCallback()) {

    private var getCuisinesst: String? = null
    private val boosterArray: ArrayList<Int> = ArrayList()
    companion object {
        private const val ITEM_VIEW_TYPE = 0
        private const val SHIMMER_VIEW_TYPE = 1
    }







    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AllRestaurantPageAdapter.ShimmerViewHolder -> {

            }
            is AllRestaurantPageAdapter.RestaurantItemHolder -> {
                val item = getItem(position)

                item?.let {
                    if (it.isActive == true) {
                        Glide.with(context).load(it.restaurantImage).into(holder.imgRestaurant)
                        holder.currentlyUnavailable.hide()
                    }
                    else {
//                        holder.currentlyUnavailable.show()
//                        Glide.with(context).load(it.restaurantImage).into(holder.imgRestaurant)
//                        val matrix = ColorMatrix()
//                        matrix.setSaturation(0F)
//                        val filter = ColorMatrixColorFilter(matrix)
//                        holder.imgRestaurant.colorFilter = filter
                        holder.currentlyUnavailable.show()
                        Glide.with(context).load(it.restaurantImage).into(holder.imgRestaurant)

                        val matrix = ColorMatrix()
                        // Desaturate (black and white)
                        matrix.setSaturation(0F)

                        // Darken
                        val scale = 0.5f // Adjust this value (0 to 1) to get the desired darkness
                        val darkeningMatrix = ColorMatrix(
                            floatArrayOf(
                                scale, 0f, 0f, 0f, 0f,
                                0f, scale, 0f, 0f, 0f,
                                0f, 0f, scale, 0f, 0f,
                                0f, 0f, 0f, 1f, 0f
                            )
                        )

                        // Concatenate the matrices
                        matrix.postConcat(darkeningMatrix)
                        val filter = ColorMatrixColorFilter(matrix)
                        holder.imgRestaurant.colorFilter = filter
                    }


                    holder.txtRestaurantName.text = it.restaurantName
                    holder.tvDistance.text = it.distance
                    holder.tvDeliveryTime.text = it.deliveryTime
                    holder.tvRating.text = it.ratings?.toString()

                    if (it.isFeatured == true) {
                        holder.isFeatured.show()
                    }

                    if (it.is_rush_mode == true) {
                        holder.isRushMode.show()
                    }

                    if (it.deliveryType == "DELIVERY_BY_SVAGGY" || it.deliveryType == "DELIVERY_BY_RESTAURANT") {
                        holder.imageView25.setImageResource(R.drawable.ic_check)
                        holder.getDeliveryType.setTextColor(Color.parseColor("#268836"))
                        holder.getDeliveryType.text = context.getString(R.string.delivery_Avai)
                    } else {
                        holder.imageView25.setImageResource(R.drawable.ic_pick)
                        holder.getDeliveryType.setTextColor(Color.parseColor("#CE221E"))
                        holder.getDeliveryType.text = context.getString(R.string.pick_up)
                    }

                    val stringBuilder = StringBuilder()
                    for (s in it.restaurantCuisines) {
                        stringBuilder.append(s.cuisine).append(", ")
                    }

                    getCuisinesst = stringBuilder.toString()
                    if (getCuisinesst!!.endsWith(", ")) {
                        getCuisinesst = getCuisinesst!!.substring(0, getCuisinesst!!.length - 2)
                        holder.txtCuisines.text = getCuisinesst
                    }

                    holder.restaurant.setOnClickListener { _ ->
                        it.boosterId?.forEach { value ->
                            value?.let { boosterArray.add(it) }
                        }
                        goMenuScreen(
                            it.id ?: 0,
                            it.deliveryType ?: "",
                            boosterArray,
                            it.deliveryTime ?: ""
                        )
                    }

                    if (!item.discount.isNullOrEmpty()){
                        holder.txtFlatDeal.show()
                        holder.txtPercentOff.show()
                        holder.txtSelectedDishes.show()
                        holder.txtPercentOff.text = item.discount ?: ""

                    }else{
                        holder.txtFlatDeal.hide()
                        holder.txtPercentOff.hide()
                        holder.txtSelectedDishes.hide()

                    }
                }



            }
        }

    }

    inner class RestaurantItemHolder(binding: ItemAllRestaurantsBinding) : RecyclerView.ViewHolder(binding.root) {
        val imgRestaurant = binding.imgRestaurant
        val txtRestaurantName = binding.txtRestaurantName
        val tvDistance = binding.tvDistance
        val tvDeliveryTime = binding.tvDeliveryTime
        val tvRating = binding.tvRating
        val isFeatured = binding.isFeatured
        val isRushMode = binding.isRushMode
        val imageView25 = binding.imageView25
        val getDeliveryType = binding.getDeliveryType
        val txtCuisines = binding.txtCuisines
        val restaurant = binding.restaurant
        val currentlyUnavailable = binding.currentlyUnavailable
        val txtFlatDeal = binding.txtFlatDeal
        val txtPercentOff = binding.txtPercentOff
        val txtSelectedDishes = binding.txtSelectedDishes


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
                SHIMMER_VIEW_TYPE -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.shimmer_layout_restaurant, parent, false)
                    ShimmerViewHolder(view)
                }
                ITEM_VIEW_TYPE -> {
                    val view = ItemAllRestaurantsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                    RestaurantItemHolder(view)
                }
                else -> throw IllegalArgumentException("Invalid view type")
            }


        }

    inner class ShimmerViewHolder(view: View) : RecyclerView.ViewHolder(view)

}

class RestaurantDiffCallback : DiffUtil.ItemCallback<AllRestaurant.Data>() {
    override fun areItemsTheSame(@NonNull oldItem: AllRestaurant.Data, @NonNull newItem: AllRestaurant.Data): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(@NonNull oldItem: AllRestaurant.Data, @NonNull newItem: AllRestaurant.Data): Boolean {
        return oldItem == newItem
    }
}























