package com.svaggy.ui.fragments.location.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.UserAddressItemBinding
import com.svaggy.ui.fragments.location.model.GetAddress
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.HOME
import com.svaggy.utils.Constants.WORK
import com.svaggy.utils.PrefUtils

class UserAddressAdapter(private val context: Context,
                         private val list: ArrayList<GetAddress.Data>,
                         private val selectAddress:(addressId:String)-> Unit) :RecyclerView.Adapter<UserAddressAdapter.UserAddressHolder>() {





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAddressHolder {
        val view = UserAddressItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UserAddressHolder(view)


    }

    override fun getItemCount(): Int {
       return list.size


    }

    override fun onBindViewHolder(holder: UserAddressHolder, position: Int) {
        val item = list[position]

        holder.addressTypeText.text = item.addressType ?: ""
        when (item.addressType) {
            HOME -> {
                holder.locationType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.home_icon))
            }
            WORK -> {
                holder.locationType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_work))

            }
            else -> {
                holder.locationType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_map))
            }
        }
        holder.address.text = item.completeAddress





        holder.selectAddress.setOnClickListener {
            val addressId = item.id.toString()
            val latitude = item.latitude
            val longitude = item.longitude
            PrefUtils.instance.setString(Constants.AddressId,addressId)
            PrefUtils.instance.setString(Constants.Latitude,latitude)
            PrefUtils.instance.setString(Constants.Longitude,longitude)
            selectAddress(addressId)


        }
    }

    inner class UserAddressHolder (binding:UserAddressItemBinding): RecyclerView.ViewHolder(binding.root){
        val selectAddress = binding.clSelectAddress
        val locationType = binding.imgAddressType
        val addressTypeText = binding.txtAddressType
        val address = binding.txtAddress

    }
}