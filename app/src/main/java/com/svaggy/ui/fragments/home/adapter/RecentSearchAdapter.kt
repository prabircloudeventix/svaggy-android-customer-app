package com.svaggy.ui.fragments.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.ui.fragments.home.model.Search

class RecentSearchAdapter(
    private val context: Context,
    private var arrayList: ArrayList<Search.Data>?,
    private val getCuisineRestaurant: (Int,Int,String,String) -> Unit
) : RecyclerView.Adapter<RecentSearchAdapter.CompanyViewHolder>()
{
    class CompanyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var recentSearch = view.findViewById<ConstraintLayout>(R.id.itemRecentSearch)
        var txtSearchName = view.findViewById<TextView>(R.id.txtSearchName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        return CompanyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_recent_search, parent, false)
        )
    }

    override fun getItemCount(): Int {
        if (arrayList!!.size > 5){
            return 5
        }else{
            return arrayList?.size ?: 0

        }

    }

    override fun onBindViewHolder(holder: CompanyViewHolder, @SuppressLint("RecyclerView") position: Int)
    {
        val item = arrayList?.get(position)

        holder.txtSearchName.text = item?.name

        holder.recentSearch.setOnClickListener {
            getCuisineRestaurant(
                item?.id?:0,
                item?.restaurantId?:0,
                item?.type?:"",
                item?.name?:"")
        }
    }
}
