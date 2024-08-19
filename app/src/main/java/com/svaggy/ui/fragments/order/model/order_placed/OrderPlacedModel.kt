package com.svaggy.ui.fragments.order.model.order_placed

data class OrderPlacedModel(
    val `data`: Data?,
    val is_success: Boolean?,
    val message: String?,
    val status_code: Int?
)