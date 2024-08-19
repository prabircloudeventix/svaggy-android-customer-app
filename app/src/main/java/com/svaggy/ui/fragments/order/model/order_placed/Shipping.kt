package com.svaggy.ui.fragments.order.model.order_placed

data class Shipping(
    val address: Address?,
    val carrier: Any?,
    val name: String?,
    val phone: Any?,
    val tracking_number: Any?
)