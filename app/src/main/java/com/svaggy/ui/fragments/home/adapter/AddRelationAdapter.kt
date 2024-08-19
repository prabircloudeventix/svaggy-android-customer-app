package com.svaggy.ui.fragments.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.svaggy.databinding.ItemAddOnsRelationBinding
import com.svaggy.ui.fragments.home.model.RestaurantsMenuItem
import com.svaggy.utils.Constants
import com.svaggy.utils.Constants.requiredCounterValue
import com.svaggy.utils.Constants.requiredWithChoseUp
import com.svaggy.utils.PrefUtils
import com.svaggy.utils.hide
import com.svaggy.utils.invisible
import com.svaggy.utils.show

class AddRelationAdapter(
   private val menuAddOne: Int?,
    private val addOnsRelations: ArrayList<RestaurantsMenuItem.Data.Categories.MenuItems.MenuAddOns.AddOnsRelations>,
    private val required: Boolean,
    private val chooseUpto: Int,
    private val context: Context,
    private val updateCart: (itemPrices: Double?, itemId: Int?, isAdd: Boolean?,menuId:Int?) -> Unit,
    ) : RecyclerView.Adapter<AddRelationAdapter.RelationHolder>() {
    private var trueValue = 1
    private var selectValue = 0










    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationHolder {
        val view = ItemAddOnsRelationBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return RelationHolder(view)

    }

    override fun getItemCount(): Int {
       return addOnsRelations.size

    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RelationHolder, position: Int) {



        val item = addOnsRelations[position]
        holder.itemName.text = item.itemName + ", CZK ${PrefUtils.instance.formatDouble(item.price!!)}"
        holder.radioCheckBox.isChecked = item.isSelected ?: false
        holder.checkBox.isChecked = item.isSelected ?: false



        if (required && chooseUpto == 1) {
            holder.radioCheckBox.show()
            holder.clickRadio.show()
            holder.checkBox.invisible()
            holder.clickCheckBox.hide()
        }
        else {
            holder.radioCheckBox.invisible()
            holder.clickRadio.hide()
            holder.checkBox.show()
            holder.clickCheckBox.show()
        }
        holder.clickCheckBox.setOnClickListener {

            if (chooseUpto != 0){
                if (required){
                    if (!item.isSelected!!){
                        selectValue++
                        if (selectValue <= chooseUpto){
                            requiredWithChoseUp--
                        }
                        requiredCounterValue--
                        updateCart(item.price!!,item.id!!,true,menuAddOne)
                        addOnsRelations[position].isSelected = true
                        notifyDataSetChanged()


                    }else{
                        if (selectValue <= chooseUpto){
                            requiredWithChoseUp++
                        }
                        updateCart(item.price!!,item.id!!,false,menuAddOne)
                        addOnsRelations[position].isSelected = false
                        selectValue--
                        notifyDataSetChanged()

                    }

                }else{
                    if (!item.isSelected!!){
                        addOnsRelations.forEach {
                            if (it.isSelected!!){
                                trueValue++

                            }
                        }
                        if (trueValue > chooseUpto){
                            Toast.makeText(context,"please remove selected item",Toast.LENGTH_SHORT).show()

                        }
                        else{
                            updateCart(item.price!!,item.id!!,true,menuAddOne)
                            addOnsRelations[position].isSelected = true
                            notifyDataSetChanged()
                        }

                    }
                    else{
                        updateCart(item.price!!,item.id!!,false,menuAddOne)
                        addOnsRelations[position].isSelected = false
                        trueValue = 1
                        notifyDataSetChanged()
                    }

                }


            }
        }


//        holder.clickCheckBox.setOnClickListener {
//
//            if (chooseUpto != 0){
//                if (required){
//                    addOnsRelations.forEach {
//                        if (it.isSelected!!) {
//                            selectValue++
//
//                        }
//                    }
//                    if (!item.isSelected!!){
//                        if (selectValue <= chooseUpto)
//                        requiredWithChoseUp--
//                        requiredCounterValue--
//                        updateCart(item.price!!,item.id!!,true,menuAddOne)
//                            addOnsRelations[position].isSelected = true
//                            notifyDataSetChanged()
//
//
//                    }else{
//                        if (selectValue < chooseUpto)
//                        requiredWithChoseUp++
//                      //  requiredCounterValue--
//                        updateCart(item.price!!,item.id!!,false,menuAddOne)
//                        addOnsRelations[position].isSelected = false
//                        notifyDataSetChanged()
//
//                    }
//
//                }else{
//                    if (!item.isSelected!!){
//                        addOnsRelations.forEach {
//                            if (it.isSelected!!){
//                                trueValue++
//
//                            }
//                        }
//                        if (trueValue > chooseUpto){
//                            Toast.makeText(context,"please remove selected item",Toast.LENGTH_SHORT).show()
//
//                        }
//                        else{
//                            updateCart(item.price!!,item.id!!,true,menuAddOne)
//                            addOnsRelations[position].isSelected = true
//                            notifyDataSetChanged()
//                        }
//
//                    }
//                    else{
//                        updateCart(item.price!!,item.id!!,false,menuAddOne)
//                        addOnsRelations[position].isSelected = false
//                        trueValue = 1
//                        notifyDataSetChanged()
//                    }
//
//                }
//
//
//            }
// }
        holder.clickRadio.setOnClickListener {
            addOnsRelations.forEach {
                if (it.isSelected!!){
                    requiredCounterValue++
                    updateCart(it.price!!,null,false,menuAddOne)
                    it.isSelected = false
                }
            }
            requiredCounterValue--
            updateCart(item.price!!,item.id!!,true,menuAddOne)
            addOnsRelations[position].isSelected =  true
            notifyDataSetChanged()
        }

    }

    inner class RelationHolder(binding: ItemAddOnsRelationBinding):RecyclerView.ViewHolder(binding.root){
        val itemName = binding.txtItemNameRelations
        val checkBox = binding.checkSelectRelation
        val radioCheckBox = binding.radioSelectRelation
        val clickCheckBox = binding.clickCheckBox
        val clickRadio = binding.clickRadio


    }
}