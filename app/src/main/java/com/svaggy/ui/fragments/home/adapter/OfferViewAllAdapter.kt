package com.svaggy.ui.fragments.home.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.svaggy.R
import com.svaggy.databinding.ItemAllRestaurantsBinding
import com.svaggy.ui.fragments.home.model.AllRestaurant
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.PrefUtils
class OfferViewAllAdapter(
    private val context: Context,
    private var arrayList: ArrayList<AllRestaurant.Data>?,
    private val goMenuScreen: (Int,String,ArrayList<Int>,String) -> Unit
): RecyclerView.Adapter<BindingHolder<ItemAllRestaurantsBinding>>() {

    private var getCuisinesSt:String?=null
    private val boosterArray:ArrayList<Int> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemAllRestaurantsBinding> {
        val binding = DataBindingUtil.inflate<ItemAllRestaurantsBinding>(
            LayoutInflater.from(parent.context), R.layout.item_all_restaurants, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList?.size?:0
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemAllRestaurantsBinding>, position: Int) {
        val item = arrayList?.get(position)

        Glide.with(context).load(item?.restaurantImage).into(holder.binding.imgRestaurant)
        holder.binding.txtRestaurantName.text = item?.restaurantName

        //holder.bind(holder.binding.recyclerCuisinesCountry,item?.restaurant_cuisines)
        val stringBuilder = StringBuilder()
        for (s in item?.restaurantCuisines!!) {
            stringBuilder.append(s.cuisine).append(", ")
        }
        getCuisinesSt= stringBuilder.toString()
        if (getCuisinesSt!!.endsWith(", ")) {
            getCuisinesSt = getCuisinesSt!!.substring(0, getCuisinesSt!!.length - 2)
            holder.binding.txtCuisines.text=getCuisinesSt
        }
        //holder.binding.txtCuisines.text = stringBuilder.toString()
        val childMembersAdapter = OfferCusinesCountryAdapter(item.restaurantCuisines)
//        holder.binding.recyclerCuisinesCountry.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
//        holder.binding.recyclerCuisinesCountry.adapter = childMembersAdapter

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
        holder.binding.tvDistance.text = item.distance
        holder.binding.tvDeliveryTime.text = item.deliveryTime
        //DELIVERY_AVAILABLE

        if (item.deliveryType.equals("DELIVERY_BY_SVAGGY") || item.deliveryType.equals("DELIVERY_BY_RESTAURANT")){
            holder.binding.imageView25.setImageResource(R.drawable.ic_check)
            holder.binding.getDeliveryType.setTextColor(Color.parseColor("#268836"))
            holder.binding.getDeliveryType.text = context.getString(R.string.delivery_Avai)
        }else{
            holder.binding.imageView25.setImageResource(R.drawable.ic_pick)
            holder.binding.getDeliveryType.setTextColor(Color.parseColor("#CE221E"))
            holder.binding.getDeliveryType.text = context.getString(R.string.pick_up)

        }
    }
}
