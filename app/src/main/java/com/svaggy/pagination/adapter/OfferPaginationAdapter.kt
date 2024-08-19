package com.svaggy.pagination.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.svaggy.R
import com.svaggy.databinding.ItemOfferRestaurantBinding
import com.svaggy.ui.fragments.home.model.AllRestaurant
import com.svaggy.utils.hide
import com.svaggy.utils.show

class OfferPaginationAdapter(
    private val context: Context,
    private val goMenuScreen: (Int, String, ArrayList<Int>, String) -> Unit
) : PagingDataAdapter<AllRestaurant.Data, RecyclerView.ViewHolder>(OfferRestaurantDiffCallback()) {

    private var getCuisinesst: String? = null
    private val boosterArray: ArrayList<Int> = ArrayList()
    companion object {
        private const val ITEM_VIEW_TYPE = 0
        private const val SHIMMER_VIEW_TYPE = 1
        private const val MAX_ITEMS = 5
    }


    override fun getItemCount(): Int {
        // Limit the number of items to 5 if the total item count exceeds 5
        return if (super.getItemCount() > MAX_ITEMS) MAX_ITEMS else super.getItemCount()
    }





    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is OfferPaginationAdapter.ShimmerViewHolder -> {

            }
            is OfferPaginationAdapter.RestaurantOfferItemHolder -> {
                val item = getItem(position)
                if (item != null){
                    val stringBuilder = StringBuilder()
                    for (s in item.restaurantCuisines) {
                        stringBuilder.append(s.cuisine).append(", ")
                    }
                    getCuisinesst = stringBuilder.toString()
                    if (getCuisinesst!!.endsWith(", ")) {
                        getCuisinesst = getCuisinesst!!.substring(0, getCuisinesst!!.length - 2)
                        holder.txtCuisines.text = getCuisinesst
                    }
                    holder.txtRestaurantName.text = item.restaurantName
                    Glide.with(context).load(item.restaurantImage).into(holder.imgRestaurant)

                    holder.tvDeliveryTime.text = item.deliveryTime
                    holder.txtLocationtxt.text = item.distance
                    holder.txtRating.text = item.ratings.toString()

                    //DELIVERY_AVAILABLE

                    if (item.deliveryType.equals("DELIVERY_BY_SVAGGY") || item.deliveryType.equals("DELIVERY_BY_RESTAURANT")){
                        holder.getTypeImg.setImageResource(R.drawable.ic_check)
                        holder.getType.setTextColor(Color.parseColor("#268836"))
                        holder.getType.text = context.getString(R.string.delivery_Avai)
                    }else{
                        holder.getTypeImg.setImageResource(R.drawable.ic_pick)
                        holder.getType.setTextColor(Color.parseColor("#CE221E"))
                        holder.getType.text = context.getString(R.string.pick_up)

                    }

                    holder.restaurant.setOnClickListener {
                        item.boosterId?.forEach {value ->
                            if (value != null)
                                boosterArray.add(value)
                        }
                        goMenuScreen(
                            item.id ?: 0,
                            item.deliveryType ?: "",
                            boosterArray,
                            item.deliveryTime?:""
                        )
                    }
                    if (item.discount.isNullOrEmpty()){
                        holder.txtPercentOff.hide()
                        holder.txtFlatDeal.hide()
                        holder.txtSelectedDishes.hide()
                    }
                    else{
                        holder.txtPercentOff.show()
                        holder.txtFlatDeal.show()
                        holder.txtSelectedDishes.show()
                        holder.txtPercentOff.text = item.discount.toString()
                    }
                    if (item.isActive == false){
                        holder.currentlyUnavailable.show()
                       holder.txtPercentOff.alpha = 0.5f
                        holder.txtFlatDeal.alpha  = 0.5f
                       holder.txtSelectedDishes.alpha = 0.5f


                        val matrix = ColorMatrix()

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
                }




            }
        }

    }

    inner class RestaurantOfferItemHolder(binding: ItemOfferRestaurantBinding) : RecyclerView.ViewHolder(binding.root) {
        val imgRestaurant = binding.imgRestaurant
        val txtRestaurantName = binding.txtRestaurantName
        val tvDeliveryTime = binding.tvDeliveryTime
        val imageView25 = binding.imageView25
        val txtCuisines = binding.txtCuisines
        val restaurant = binding.restaurant
        val getTypeImg = binding.getTypeImg
        val getType = binding.getType
        val txtLocationtxt = binding.txtLocationtxt
        val txtRating = binding.txtRating
        val txtPercentOff = binding.txtPercentOff
        val txtFlatDeal = binding.txtFlatDeal
        val txtSelectedDishes = binding.txtSelectedDishes
        val currentlyUnavailable = binding.currentlyUnavailable


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SHIMMER_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.shimmer_layout_restaurant, parent, false)
                ShimmerViewHolder(view)
            }
            ITEM_VIEW_TYPE -> {
                val view = ItemOfferRestaurantBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                RestaurantOfferItemHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }


    }

    inner class ShimmerViewHolder(view: View) : RecyclerView.ViewHolder(view)

}

class OfferRestaurantDiffCallback : DiffUtil.ItemCallback<AllRestaurant.Data>() {
    override fun areItemsTheSame(oldItem: AllRestaurant.Data, newItem: AllRestaurant.Data): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: AllRestaurant.Data, newItem: AllRestaurant.Data): Boolean {
        return oldItem == newItem
    }
}

