package com.svaggy.ui.fragments.cart.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import io.opencensus.stats.View

class TimeSlotAdapter(context: Context, timeSlots: List<String>) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, timeSlots) {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(
        position: Int,
        convertView: android.view.View?,
        parent: ViewGroup
    ): android.view.View {
        val view = convertView ?: inflater.inflate(android.R.layout.simple_spinner_item, parent, false)

        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = getItem(position)

        return view
    }

    override fun getDropDownView(
        position: Int,
        convertView: android.view.View?,
        parent: ViewGroup
    ): android.view.View? {
        val view = convertView ?: inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)

        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = getItem(position)

        return view
    }
}