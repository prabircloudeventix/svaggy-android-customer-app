package com.svaggy.ui.fragments.cart.model

data class FilterModel(
    val menuAddOnId:Int,
    var relation:Int)

data class FilterModelPos(
    val menuAddOnId:Int,
    var relation:Int,
    val adapterPos:Int)

