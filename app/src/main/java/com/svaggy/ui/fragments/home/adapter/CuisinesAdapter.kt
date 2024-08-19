package com.svaggy.ui.fragments.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.svaggy.R
import com.svaggy.ui.fragments.home.model.GetCuisines
import com.svaggy.utils.setSafeOnClickListener

class CuisinesAdapter(
    private val context: Context,
    private var arrayList: ArrayList<GetCuisines.Data>?,
    private val getCuisineRestaurant: (Int,String) -> Unit
) : RecyclerView.Adapter<CuisinesAdapter.CompanyViewHolder>()
{
    class CompanyViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        var cuisine: ConstraintLayout = view.findViewById(R.id.cuisine)
        var cuisine_img: ImageView = view.findViewById(R.id.cuisine_img)
        var cuisine_name: TextView = view.findViewById(R.id.cuisine_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        return CompanyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_cuisines, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return arrayList?.size?:0
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, @SuppressLint("RecyclerView") position: Int)
    {
        val item = arrayList?.get(position)

        Glide.with(context).load(item?.cuisineImage).into(holder.cuisine_img)
        holder.cuisine_name.text = item?.cuisine

        holder.cuisine.setSafeOnClickListener {
            getCuisineRestaurant(item?.id?:0,item?.cuisine?:"")
        }
    }
}
