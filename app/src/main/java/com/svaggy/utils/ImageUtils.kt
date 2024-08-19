package com.svaggy.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.svaggy.R

fun showRectImag(
    imageUrl: String,
    photoView: ImageView
) {
    val options = RequestOptions().frame(1000).format(DecodeFormat.PREFER_RGB_565)
    Glide.with(photoView)
        .asBitmap()
        .load(imageUrl)
        .apply(options)
        .format(DecodeFormat.PREFER_RGB_565)
        .transform(CenterCrop(), RoundedCorners(10))
        .placeholder(R.drawable.placeholder_cuisine)
        .error(R.drawable.placeholder_cuisine)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(photoView)
}