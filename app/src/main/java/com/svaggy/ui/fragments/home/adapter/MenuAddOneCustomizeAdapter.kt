package com.svaggy.ui.fragments.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.R
import com.svaggy.databinding.ItemCustomizeBinding
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem
import com.svaggy.utils.BindingHolder
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.requiredCounterValue

class MenuAddOneCustomizeAdapter(
    private var context: Context,
    private var menuAddOns: ArrayList<RestaurantsMenuItem.Data.Categories.MenuItems.MenuAddOns>,
    private val updateCart: (itemPrices: Double?, itemId: Int?, isAdd: Boolean?,menuId:Int?) -> Unit,
) : RecyclerView.Adapter<BindingHolder<ItemCustomizeBinding>>() {
    private lateinit var addRelationCustomizeAdapter:AddRelationCustomizeAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemCustomizeBinding> {
        val binding = DataBindingUtil.inflate<ItemCustomizeBinding>(LayoutInflater.from(parent.context), R.layout.item_customize, parent, false)
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return menuAddOns.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BindingHolder<ItemCustomizeBinding>, position: Int) {
        val item = menuAddOns[position]


        holder.binding.txtAddOnsName.text = menuAddOns[position].addOnsName

        if (item.isRequired == true) {
            holder.binding.txtRequired.visibility = View.VISIBLE
            holder.binding.txtChooseUpto.text = "Select any " + menuAddOns[position].chooseUpto

        } else {
            holder.binding.txtRequired.visibility = View.GONE
            holder.binding.txtChooseUpto.text = "Choose up to " + menuAddOns[position].chooseUpto + " Items"

        }

        addRelationCustomizeAdapter = AddRelationCustomizeAdapter(
                item.id,
                addOnsRelations = item.addOnsRelations,
                required = item.isRequired!!,
                chooseUpto = item.chooseUpto!!,
                context =  context,
                updateCart = updateCart)

        holder.binding.recyclerAddOneRelation.adapter = addRelationCustomizeAdapter

    }


}
