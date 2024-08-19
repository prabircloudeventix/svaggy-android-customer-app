package com.svaggy.ui.fragments.home.model

data class SharedPrefModel(
    val dishName: String?,
    val dishType: String?,
    val price: Double?,
    val isActive: Boolean?,
    val menuItemId: Int?,
    val actualPrice: Double?,
    val quantity: Int?,
    val cartId: Int?,
)