package com.svaggy.ui.fragments.payment.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.svaggy.R
import com.svaggy.databinding.ItemCardDateailsBinding
import com.svaggy.ui.fragments.payment.model.GetCard
import com.svaggy.utils.BindingHolder

class CardListAdapter(
    var context: Context,
    private var arrayList: ArrayList<GetCard.Data>,
    private val clickCard: ( stripeId:String, cardNumber:String, cardExpiry:String, cardName:String,  ) -> Unit,

    ) : RecyclerView.Adapter<BindingHolder<ItemCardDateailsBinding>>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ItemCardDateailsBinding>
    {
        val binding = DataBindingUtil.inflate<ItemCardDateailsBinding>(
            LayoutInflater.from(parent.context), R.layout.item_card_dateails, parent, false)
        return BindingHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: BindingHolder<ItemCardDateailsBinding>, position: Int) {
        val item = arrayList[position]
            when (item.cardBrand) {
            "Visa" -> {
                Glide.with(context).load(R.drawable.logos_visa).into(holder.binding.cardImage)
            }
            "MasterCard" -> {
                Glide.with(context).load(R.drawable.logos_mastercard).into(holder.binding.cardImage)
            }
            "American Express" -> {
                Glide.with(context).load(R.drawable.logos_amex).into(holder.binding.cardImage)
            }
            else -> {
                Glide.with(context).load(R.drawable.logos_visa).into(holder.binding.cardImage)
            }
        }

        val expiry = "${item.expMonth}/${item.expYear}"
        holder.binding.cardNameTv.text = item.cardNickname
        holder.binding.cardNumber.text = "**** **** **** "+item.lastFourDigit
        holder.binding.txtManage.setOnClickListener {
            clickCard( item.stripeCardId!!, holder.binding.cardNumber.text.toString(), expiry, item.cardNickname!!)
        }
    }
}