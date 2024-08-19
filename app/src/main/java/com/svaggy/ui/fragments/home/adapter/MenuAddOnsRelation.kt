package com.svaggy.ui.fragments.home.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.paging.LOGGER
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ItemAddOnsRelationBinding
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.Constants
import com.svaggy.utils.PrefUtils

class MenuAddOnsRelation(
    var context: Context,
    var menuAddOnsRelation: ArrayList<RestaurantsMenuItem.Data.Categories.MenuItems.MenuAddOns.AddOnsRelations>,
    var addOnsName:String,
    var itemRequired: Boolean,
    var itemchoose: Int,
    private val addOnCheckArray: (Int) -> Unit,
    private val addOnRadioArray: (Int) -> Unit
) : RecyclerView.Adapter<BindingHolder<ItemAddOnsRelationBinding>>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION
    var addOnsSelectArray: ArrayList<Int>? = ArrayList()
    var trueCount: Int?=0
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingHolder<ItemAddOnsRelationBinding> {
        val binding = DataBindingUtil.inflate<ItemAddOnsRelationBinding>(
            LayoutInflater.from(parent.context), R.layout.item_add_ons_relation, parent, false
        )
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return menuAddOnsRelation.size
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemAddOnsRelationBinding>, position: Int) {
        val item = menuAddOnsRelation[position]
        menuAddOnsRelation[position].addOnsName=addOnsName
        //var abc =menuAddOnsRelation
        val trueItems = menuAddOnsRelation.filter { it.isSelected!! }

        // Extract the ids of the true items

        holder.binding.txtItemNameRelations.text =
            menuAddOnsRelation[position].itemName + ", CZK ${PrefUtils.instance.formatDouble(menuAddOnsRelation[position].price!!)}"

        if (itemRequired && itemchoose == 1) {
            holder.binding.radioSelectRelation.visibility = View.VISIBLE
            holder.binding.checkSelectRelation.visibility = View.INVISIBLE
        }
        else {
            holder.binding.radioSelectRelation.visibility = View.INVISIBLE
            holder.binding.checkSelectRelation.visibility = View.VISIBLE
        }

        if (PrefUtils.instance.getString(Constants.FragmentBackName).equals("RestaurantMenuScreen")){
            holder.binding.radioSelectRelation.isChecked = position == selectedPosition
        }
        else if (PrefUtils.instance.getString(Constants.FragmentBackName).equals("CartScreen")){
            holder.binding.checkSelectRelation.isChecked = item.isSelected?:false
            holder.binding.radioSelectRelation.isChecked = item.isSelected?:false
        }
        else if (PrefUtils.instance.getString(Constants.FragmentBackName).equals("CartMoreItemScreen")){
            holder.binding.checkSelectRelation.isChecked = item.isSelected?:false
            holder.binding.radioSelectRelation.isChecked = item.isSelected?:false
        }
        else{
            holder.binding.radioSelectRelation.isChecked = position == selectedPosition
        }
        /*holder.binding.checkSelectRelation.isChecked = item.isSelected?:false
        holder.binding.radioSelectRelation.isChecked = item.isSelected?:false*/
        holder.binding.radioSelectRelation.setOnClickListener {
            selectedPosition = holder.adapterPosition

            menuAddOnsRelation.forEachIndexed { index, _ ->
                menuAddOnsRelation[index].isSelected = false
            }

            val allFalse = menuAddOnsRelation.all { it.isSelected == false }

            if (allFalse) {
                notifyDataSetChanged()
                item.isSelected = true
                arrayAddRadioFun(item.id)
            }
        }

        holder.binding.checkSelectRelation.setOnClickListener {

            trueCount = menuAddOnsRelation.count {
                it.isSelected == true
            }
            if (!holder.binding.checkSelectRelation.isChecked){
                holder.binding.checkSelectRelation.isClickable=true
                holder.binding.checkSelectRelation.isChecked=false
                item.isSelected=false
                addOnsSelectArray!!.removeAll { it == item.id }
                arrayAddFun(item.id)
                PrefUtils.instance.setArray(Constants.AddOnsArray, addOnsSelectArray!!)
            }
            else{
                trueCount = menuAddOnsRelation.count { it.isSelected == true }
                if (trueCount!! < itemchoose){
                    holder.binding.checkSelectRelation.isClickable=true
                    if ((addOnsSelectArray?.size ?: 0) <= itemchoose){
                        item.isSelected=true
                        holder.binding.checkSelectRelation.isChecked=true
                        if (addOnsSelectArray!!.all { it != item.id }) {
                            //addOnsSelectArray!!.add(item.id!!)
                            //addOnsSelectArray!!.add(item.id?:0)
                            arrayAddFun(item.id)
                            //PrefUtils.instance.setArray(Constants.AddOnsArray, addOnsSelectArray!!)
                        }
                        else {
                            println("Match found")
                            addOnsSelectArray!!.removeAll {
                                it == item.id }

                            println(addOnsSelectArray)
                            //PrefUtils.instance.setArray(Constants.AddOnsArray, addOnsSelectArray!!)
                        }
                    }
                }
                else{
                    holder.binding.checkSelectRelation.isClickable=false
                    holder.binding.checkSelectRelation.isChecked=false
                    Toast.makeText(context, "You can choose max $itemchoose items", Toast.LENGTH_SHORT).show()
                }
            }
            notifyDataSetChanged()
        }
    }

    private fun arrayAddRadioFun(id: Int?) {
        addOnRadioArray(id!!)
        val abc =menuAddOnsRelation
        val trueItems = abc.filter { it.isSelected!! }

        // Extract the ids of the true items
        val trueItemIds = trueItems.map { it.id }
    }

    private fun arrayAddFun(id: Int?) {
        val abc =menuAddOnsRelation
        val trueItems = abc.filter { it.isSelected!! }

        // Extract the ids of the true items
        val trueItemIds = trueItems.map {
            it.id }
        addOnsSelectArray!!.add(id!!)
        addOnCheckArray(id)
    }

}