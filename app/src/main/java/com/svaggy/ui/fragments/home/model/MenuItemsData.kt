package com.svaggy.ui.fragments.home.model

sealed class MenuItemsData



data class RestaurantDetails(
    var id: Int? = null,
    var restaurantName: String? = null,
    var address: String? = null,
    var isActive: Boolean? = null,
    var restaurantImage: String? = null,
    var distance: String? = null,
    var ratings: Double? = null,
    var restaurantCuisines : String? = null,
    var deliveryTime: String? = null
) : MenuItemsData()

data class OfferType(
    var id: Int? = null
) :MenuItemsData()

data class FiltersItems(
    var id: Int? = null
) : MenuItemsData()

data class MenuItemsTitle(
    var titleName : String? = null
) : MenuItemsData()


data class RestaurantCuisines(
    var id: Int? = null,
    var cuisine: String? = null
) : MenuItemsData()


data class Categories(

    var id: Int? = null,
    var categoryName: String? = null,
    var isViewVisibility: Boolean = false
) : MenuItemsData()


data class MenuItems(
    var id: Int? = null,
    var dishName: String? = null,
    var dishType: String? = null,
    var price: Double? = null,
    var description: String? = null,
    var isActive: Boolean? = null,
    var menuItemImage: String? = null,
    var quantity: Int? = null,
    var actualPrice: Double? = null,
    var maxQuantity: Int? = null,
    var menuItemId: Int? = null
) : MenuItemsData()


data class MenuAddOns(

    var id: Int? = null,
    var addOnsId: Int? = null,
    var addOnsName: String? = null,
    var isRequired: Boolean? = null,
    var chooseUpto: Int? = null,
    var addOnsRelations: ArrayList<AddOnsRelations> = arrayListOf()

) : MenuItemsData()


data class AddOnsRelations(

    var id: Int? = null,
    var itemName: String? = null,
    var dishType: String? = null,
    var price: Double? = null,
    var addOnsName: String? = null,
    var isSelected: Boolean? = null

)



