package com.svaggy.ui.activities.savePref

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

class PreferencesAdapter(
    private val context: Context,
    private var arrayList: ArrayList<UserPreference.Data>
) : RecyclerView.Adapter<PreferencesAdapter.CompanyViewHolder>()
{
    private var rowIndex = -1
    class CompanyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var preference = view.findViewById<ConstraintLayout>(R.id.preference)
        var preferenceImg = view.findViewById<ImageView>(R.id.preference_img)
        var preferenceName = view.findViewById<TextView>(R.id.preference_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        return CompanyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_preferences, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, @SuppressLint("RecyclerView") position: Int)
    {
        val item = arrayList[position]

//        val requestBuilder = GlideToVectorYou.init().with(context).requestBuilder
//        requestBuilder.load(item.foodPreferenceImage).transition(DrawableTransitionOptions.withCrossFade())
//            .apply(RequestOptions().centerCrop()).into(holder.preferenceImg)

        Glide.with(context).load(item.foodPreferenceImage).into(holder.preferenceImg)
        holder.preferenceName.text = item.preferenceName

        holder.preference.setOnClickListener {
            item.isSelected = !item.isSelected

            rowIndex=position
            notifyDataSetChanged()
        }
        if(item.isSelected){
            holder.preference.setBackgroundResource(R.drawable.bg_preference_fil)
            holder.preference
        }
        else
        {
            holder.preference.setBackgroundResource(R.drawable.radius_black)
        }
    }

    fun getSelected(): ArrayList<UserPreference.Data> {
        val selected: ArrayList<UserPreference.Data> = ArrayList()
        for (i in arrayList.indices) {
            if (arrayList[i].isSelected) {
                selected.add(arrayList[i])
            }
        }
        return selected
    }
}
