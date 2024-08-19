package com.svaggy.utils

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearSmoothScroller

class HalfSmoothScroller(context: Context) : LinearSmoothScroller(context) {

    override fun calculateDtToFit(viewStart: Int, viewEnd: Int, boxStart: Int, boxEnd: Int, snapPreference: Int): Int {
        return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
    }

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
    }

    companion object {
        private const val MILLISECONDS_PER_INCH = 150f // Adjust the scrolling speed here
    }
}