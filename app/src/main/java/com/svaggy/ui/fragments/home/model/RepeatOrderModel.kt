package com.svaggy.ui.fragments.home.model

import com.svaggy.ui.activities.restaurant.model.AddOnRelationModel

data class RepeatOrderModel(
    val dishName: String?,
    val dishType: String?,
    val price: Double?,
    val itemId: Int?,
    val menuItemId: Int?,
    val actualPrice: Double?,
    val quantity: Int?,
    val addOns: ArrayList<AddOnRelationModel>,

)