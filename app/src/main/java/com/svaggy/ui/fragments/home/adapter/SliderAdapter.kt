package com.svaggy.ui.fragments.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.svaggy.R
import com.svaggy.imageslider.ImageModel
import com.svaggy.imageslider.SliderViewAdapter

class SliderAdapter(
    imageUrl: ArrayList<ImageModel>,
    private val getSliderItem: (sliderItem: String) -> Unit,
) : SliderViewAdapter<SliderAdapter.SliderViewHolder>()
{
    private var sliderList: ArrayList<ImageModel> = imageUrl

    override fun getCount(): Int {
        return sliderList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?): SliderViewHolder {
        val inflate: View =
            LayoutInflater.from(parent?.context).inflate(R.layout.combo_layout, null)

        return SliderViewHolder(inflate)
    }

    override fun onBindViewHolder(viewHolder: SliderViewHolder?, position: Int) {

        if (viewHolder != null) {
            Glide.with(viewHolder.itemView).load(sliderList[position].imageIndex).fitCenter()
                .into(viewHolder.imageView)
        }

        viewHolder?.imageView?.setOnClickListener {
            getSliderItem(sliderList[position].type)
        }
    }
    class SliderViewHolder(itemView: View) : ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.offerImage)
    }

}