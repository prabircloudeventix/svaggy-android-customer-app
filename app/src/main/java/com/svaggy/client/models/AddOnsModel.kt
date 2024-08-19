package com.svaggy.client.models

data class AddOnsModel(
    val menu_item: MenuItem,
)

data class MenuItem(
    val quantity: Int,
    val cart_id: Int,
    val add_ons: List<Int>
)
