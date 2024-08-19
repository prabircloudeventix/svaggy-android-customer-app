package com.svaggy.ui.fragments.home.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.svaggy.R
import com.svaggy.databinding.ItemOfferRestaurantBinding
import com.svaggy.ui.fragments.home.model.AllRestaurant
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.PrefUtils

class OfferRestaurantAdapter(
    private val context: Context,
    private var arrayList: ArrayList<AllRestaurant.Data>,
    private val goMenuScreen: (Int, String,ArrayList<Int>,String) -> Unit
) : RecyclerView.Adapter<BindingHolder<ItemOfferRestaurantBinding>>() {
    private var getCuisinesSt: String? = null
    private val boosterArray:ArrayList<Int> = ArrayList()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingHolder<ItemOfferRestaurantBinding> {
        val binding = DataBindingUtil.inflate<ItemOfferRestaurantBinding>(
            LayoutInflater.from(parent.context), R.layout.item_offer_restaurant, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (arrayList.size > 5) 5
        else arrayList.size
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemOfferRestaurantBinding>, position: Int) {
        val item = arrayList [position]

        val stringBuilder = StringBuilder()
        for (s in item.restaurantCuisines) {
            stringBuilder.append(s.cuisine).append(", ")
        }
        getCuisinesSt = stringBuilder.toString()
        if (getCuisinesSt!!.endsWith(", ")) {
            getCuisinesSt = getCuisinesSt!!.substring(0, getCuisinesSt!!.length - 2)
            holder.binding.txtCuisines.text = getCuisinesSt
        }
        holder.binding.txtRestaurantName.text = item.restaurantName
        Glide.with(context).load(item.restaurantImage).into(holder.binding.imgRestaurant)

        holder.binding.tvDeliveryTime.text = item.deliveryTime
        holder.binding.txtLocationtxt.text = item.distance
        holder.binding.txtRating.text = item.ratings.toString()

        //DELIVERY_AVAILABLE

        if (item.deliveryType.equals("DELIVERY_BY_SVAGGY") || item.deliveryType.equals("DELIVERY_BY_RESTAURANT")){
            holder.binding.getTypeImg.setImageResource(R.drawable.ic_check)
            holder.binding.getType.setTextColor(Color.parseColor("#268836"))
            holder.binding.getType.text = context.getString(R.string.delivery_Avai)
        }else{
            holder.binding.getTypeImg.setImageResource(R.drawable.ic_pick)
            holder.binding.getType.setTextColor(Color.parseColor("#CE221E"))
            holder.binding.getType.text = context.getString(R.string.pick_up)

        }

        holder.binding.restaurant.setOnClickListener {
            item.boosterId?.forEach {value ->
                if (value != null)
                boosterArray.add(value)
            }
            goMenuScreen(
                item.id ?: 0,
                item.deliveryType ?: "",
                boosterArray,
                item?.deliveryTime?:""
            )
        }
    }
}
