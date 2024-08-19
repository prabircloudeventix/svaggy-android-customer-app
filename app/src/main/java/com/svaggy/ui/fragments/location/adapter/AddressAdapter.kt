package com.svaggy.ui.fragments.location.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ItemAddressBinding
import com.svaggy.ui.fragments.location.model.GetAddress
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.show


class AddressAdapter(
    var context: Context,
    private var isFrom: String,
    private var arrayList: ArrayList<GetAddress.Data>,
    private val addItemViewCart: (position:Int, addressId:Int, address: String, isClicked: String) -> Unit,
    private val clickEditAdd: (googlePin:String,isForSelf:String,addressType:String,phoneNum:String,recipientName:String,floor:String,companyName:String,streetName:String,lat:String,long:String,addressId:String) -> Unit,
    private val updateAddress: (googlePin:String,isForSelf:String,addressType:String,phoneNum:String,recipientName:String,floor:String,companyName:String,streetName:String,lat:String,long:String,addressId:String,isDefault:String,adapterPos:Int) -> Unit,
) :
    RecyclerView.Adapter<BindingHolder<ItemAddressBinding>>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemAddressBinding>
    {
        val binding = DataBindingUtil.inflate<ItemAddressBinding>(
            LayoutInflater.from(parent.context), R.layout.item_address, parent, false)
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    fun removeItem(position: Int){
        arrayList.removeAt(position)
        notifyItemRemoved(position)

    }

    override fun onBindViewHolder(holder: BindingHolder<ItemAddressBinding>, position: Int) {
        val item = arrayList[position]
        when (isFrom) {
            "AddressScreen" -> {
                holder.binding.btnSelect.visibility = View.GONE
                holder.binding.btnEditAddress.visibility = View.VISIBLE
                holder.binding.clSelectAddress.isEnabled = false


            }
            "CartMoreItemScreen" -> {
                holder.binding.btnSelect.visibility = View.GONE
                holder.binding.btnEditAddress.visibility = View.GONE
                // holder.binding.btnSetDefault.visibility = View.GONE
                //   holder.binding.btnDefault.visibility = View.GONE
                holder.binding.imgCancel.visibility = View.GONE
                holder.binding.clSelectAddress.setBackgroundResource(R.color.white)
                holder.binding.clSelectAddress.elevation = 0f
                holder.binding.txtAddress.textSize=12f
                holder.binding.txtAddressType.textSize=16f
            }
            else -> {
                holder.binding.btnSelect.visibility = View.VISIBLE
                holder.binding.btnEditAddress.visibility = View.GONE
                // holder.binding.btnSetDefault.visibility = View.GONE
                //  holder.binding.btnDefault.visibility = View.GONE
                holder.binding.clSelectAddress.isEnabled = false
            }
        }

        when (item.addressType) {
            "Home" -> {
                holder.binding.imgAddressType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.home_icon))
            }
            "Work" -> {
                holder.binding.imgAddressType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_work))
            }
            else -> {
                holder.binding.imgAddressType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_map))
            }
        }
        if (item.isDefault == true){
            holder.binding.alreadyDefault.show()
            holder.binding.setAsDefault.hide()
        }
        else{
            holder.binding.alreadyDefault.hide()
            holder.binding.setAsDefault.show()
        }
        holder.binding.setAsDefault.setOnClickListener {

            arrayList.forEachIndexed { index, item ->
                item.isDefault = index == position
            }


          //  item.isDefault = true
            updateAddress(
                item.googlePin?: "",
                item.isForSelf.toString(),
                item.addressType?: "",
                item.phoneNumber?: "",
                item.recipientName?: "",
                item.floor?: "",
                item.companyName?: "",
                item.streetNumber?: "",
                item.latitude?: "",
                item.longitude?: "",
                item.id.toString(),
                "true",
                position)

        }
        if (isFrom == "HomeLocation"){
            holder.binding.setAsDefault.hide()
            holder.binding.alreadyDefault.hide()
        }


        holder.binding.txtAddressType.text = item.addressType
        holder.binding.txtAddress.text = item.completeAddress

        holder.binding.btnSelect.setOnClickListener {
            PrefUtils.instance.setString(Constants.Latitude,item.latitude)
            PrefUtils.instance.setString(Constants.Longitude,item.longitude)
            addItemViewCart(position, item.id ?: 0, item.completeAddress ?: "","ForSelect")
        }

        holder.binding.clSelectAddress.setOnClickListener {
            addItemViewCart(position, item.id!!, item.completeAddress!!,"ForSelectAddress")
        }

        holder.binding.imgCancel.setOnClickListener {
            addItemViewCart(position, item.id!!, item.completeAddress!!,"ForDelete")
        }

        holder.binding.btnEditAddress.setOnClickListener {


            clickEditAdd(
                item.googlePin?: "",
                item.isForSelf.toString(),
                item.addressType?: "",
                item.phoneNumber?: "",
                item.recipientName?: "",
                item.floor?: "",
                item.companyName?: "",
                item.streetNumber?: "",
                item.latitude?: "",
                item.longitude?: "",
                item.id.toString()
            )
        }
    }
}