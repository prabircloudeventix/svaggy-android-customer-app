package com.svaggy.ui.fragments.order.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.CancelOrderItemBinding
import com.svaggy.databinding.OrderTrackItemBinding
import com.svaggy.ui.fragments.order.model.orderwithtracking.OrderTracking
import com.svaggy.utils.hide
import com.svaggy.utils.invisible
import com.svaggy.utils.show
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class TrackIconAdapter : RecyclerView.Adapter<TrackIconAdapter.TrackHolder>() {

    private val trackList = ArrayList<OrderTracking>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val view = OrderTrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackHolder(view)

    }

    override fun getItemCount(): Int {
        return trackList.size
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: TrackHolder, position: Int) {
        val data = trackList[position]
        holder.getOrderText.text = data.description
        if (data.timestamp == null) {
            if (position == trackList.size - 1) {
                holder.tvIcon.setImageResource(R.drawable.ic_current_location_bg)
                holder.getOrderTime.text = data.description
                holder.getOrderText.text = ContextCompat.getString(holder.tvIcon.context, R.string.home)
            } else {
                holder.tvIcon.setImageResource(R.drawable.ic_track_order_pending)
            }
            holder.dotLine.invisible()
        } else {
            holder.tvIcon.setImageResource(R.drawable.ic_track_order_done)
            if (data.description == "Order delivered") {
                holder.dotLine.hide()
            } else {
                holder.dotLine.show()
            }
            //set date
            val orderDate = data.timestamp
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val currentDate = orderDate.let { sdf.parse(it) }
            currentDate?.let {
                val calendar = Calendar.getInstance()
                calendar.time = currentDate
                val formattedDate = SimpleDateFormat("dd MMM yyyy").format(calendar.time)
                val formattedTime = SimpleDateFormat("HH:mm").format(calendar.time)
                holder.getOrderTime.text = "$formattedDate, $formattedTime"
            }


        }


    }

    fun setTrackList(newList: List<OrderTracking>) {
        val diffResult = DiffUtil.calculateDiff(TrackDiffCallback(trackList, newList))
        trackList.clear()
        trackList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class TrackHolder(binding: OrderTrackItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val getOrderText = binding.getOrderText
        val getOrderTime = binding.getOrderTime
        val tvIcon = binding.tvIcon
        val dotLine = binding.dotLine

    }
}

class TrackDiffCallback(
    private val oldList: List<OrderTracking>,
    private val newList: List<OrderTracking>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
