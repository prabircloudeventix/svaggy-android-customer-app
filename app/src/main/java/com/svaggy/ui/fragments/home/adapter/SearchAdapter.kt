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

class SearchAdapter(
    private var arrayList: ArrayList<Search.Data>?,
    private val getCuisineRestaurant: (Int,Int,String,String) -> Unit
) : RecyclerView.Adapter<SearchAdapter.CompanyViewHolder>()
{
    class CompanyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var search: ConstraintLayout = view.findViewById(R.id.search)
        var txtSearch: TextView = view.findViewById(R.id.txtSearch)
        var txtSearchCategory: TextView = view.findViewById(R.id.txtSearchCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        return CompanyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return arrayList?.size?:0
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = arrayList?.get(position)
        holder.txtSearch.text = item?.name
        holder.txtSearchCategory.text = item?.type

        holder.search.setOnClickListener {
            getCuisineRestaurant(
                item?.id?:0,
                item?.restaurantId?:0,
                item?.type?:"",
                item?.name?:"")
        }
    }
}
