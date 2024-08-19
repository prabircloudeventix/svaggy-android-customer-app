package com.svaggy.ui.fragments.order.model.orderwithtracking

data class OrderWithTracking(
    val `data`: Data?,
    val is_success: Boolean?,
    val message: String?,
    val status_code: Int?
)