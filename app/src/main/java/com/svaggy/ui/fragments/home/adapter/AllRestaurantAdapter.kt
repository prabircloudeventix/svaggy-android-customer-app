package com.svaggy.ui.fragments.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.svaggy.R
import com.svaggy.databinding.ItemAllRestaurantsBinding
import com.svaggy.ui.fragments.home.model.AllRestaurant
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.show

class AllRestaurantAdapter(
    private val context: Context,
    private var arrayList: ArrayList<AllRestaurant.Data>?,
    private val goMenuScreen: (Int,String,ArrayList<Int>,String) -> Unit
): RecyclerView.Adapter<BindingHolder<ItemAllRestaurantsBinding>>() {
    var getCuisinesst:String?=null
    private val boosterArray:ArrayList<Int> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemAllRestaurantsBinding>
    {
        val binding = DataBindingUtil.inflate<ItemAllRestaurantsBinding>(
            LayoutInflater.from(parent.context), R.layout.item_all_restaurants, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList?.size?:0
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: BindingHolder<ItemAllRestaurantsBinding>, position: Int)
    {
        val item = arrayList?.get(position)

        if (item?.isActive == true) {
            Glide.with(context).load(item.restaurantImage).into(holder.binding.imgRestaurant)
        }
        else
        {
            Glide.with(context).load(item?.restaurantImage).into(holder.binding.imgRestaurant)
            val matrix = ColorMatrix()
            matrix.setSaturation(0F)
            val filter = ColorMatrixColorFilter(matrix)
            holder.binding.imgRestaurant.colorFilter = filter
        }
        holder.binding.txtRestaurantName.text = item?.restaurantName
        holder.binding.tvDistance.text = item?.distance
        holder.binding.tvDeliveryTime.text = item?.deliveryTime
        holder.binding.tvRating.text = item?.ratings?.toString()
        if (item?.isFeatured!!){
            holder.binding.isFeatured.show()
        }
        if (item.is_rush_mode!!){
            holder.binding.isRushMode.show()

        }

        if (item.deliveryType.equals("DELIVERY_BY_SVAGGY") || item.deliveryType.equals("DELIVERY_BY_RESTAURANT")){
            holder.binding.imageView25.setImageResource(R.drawable.ic_check)
            holder.binding.getDeliveryType.setTextColor(Color.parseColor("#268836"))
            holder.binding.getDeliveryType.text = context.getString(R.string.delivery_Avai)
        }else{
            holder.binding.imageView25.setImageResource(R.drawable.ic_pick)
            holder.binding.getDeliveryType.setTextColor(Color.parseColor("#CE221E"))
            holder.binding.getDeliveryType.text = context.getString(R.string.pick_up)

        }







        // Convert the array to a single string with each element on a new line
        // Convert the array to a single string with each element on a new line
        var stringBuilder = StringBuilder()
        for (s in item.restaurantCuisines) {
            stringBuilder.append(s.cuisine).append(", ")
        }
        getCuisinesst= stringBuilder.toString()
        if (getCuisinesst!!.endsWith(", ")) {
            getCuisinesst = getCuisinesst!!.substring(0, getCuisinesst!!.length - 2)
            holder.binding.txtCuisines.text=getCuisinesst
        }

        //holder.binding.txtCuisines.text = stringBuilder.toString()
//        holder.bind(holder.binding.recyclerCuisinesCountry,item?.restaurantCuisines)

        holder.binding.restaurant.setOnClickListener {
            item.boosterId?.forEach {value ->
                if (value != null)
                boosterArray.add(value)
            }
            goMenuScreen(
                item.id?:0,
                item.deliveryType?:"",
                boosterArray,
                item.deliveryTime?:"")
        }
    }
}