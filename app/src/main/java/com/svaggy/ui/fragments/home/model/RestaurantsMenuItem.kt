package com.svaggy.ui.fragments.home.model

import com.google.gson.annotations.SerializedName



data class RestaurantsMenuItem(
    @SerializedName("status_code") var statusCode: Int? = null,
    @SerializedName("is_success") var isSuccess: Boolean? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: Data? = Data()
) {

    data class Data(
        @SerializedName("categories"         ) var categories        : ArrayList<Categories> = arrayListOf(),
        @SerializedName("food_types"         ) var foodTypes         : ArrayList<String>     = arrayListOf(),
        @SerializedName("restaurant_details" ) var restaurantDetails : RestaurantDetails?    = RestaurantDetails(),
        @SerializedName("popular_dishes"     ) var popularDishes     : ArrayList<String>     = arrayListOf(),
        @SerializedName("offers"             ) val offer             : ArrayList<Categories.MenuItems.MenuAddOns.OfferModel>     = arrayListOf(),
        @SerializedName("complete_schedule"  ) val completeSchedule  : ArrayList<Categories.MenuItems.MenuAddOns.TodaySchedule>     = arrayListOf(),
        @SerializedName("todays_schedule"    ) val todaySchedule     :String? = null
    ) {
        data class RestaurantDetails (
            @SerializedName("id"                  ) var id                 : Int?                          = null,
            @SerializedName("restaurant_name"     ) var restaurantName     : String?                       = null,
            @SerializedName("address") var address     : String?                       = null,
            @SerializedName("is_active"           ) var isActive           : Boolean?                      = null,
            @SerializedName("restaurant_cuisines" ) var restaurantCuisines : ArrayList<RestaurantCuisines> = arrayListOf(),
            @SerializedName("restaurant_image"    ) var restaurantImage    : String?                       = null,
            @SerializedName("distance"            ) var distance           : String?                       = null,
            @SerializedName("discount"            ) var discount           : String?                       = null,
            @SerializedName("ratings"             ) var ratings            : Double?                       = null,
            @SerializedName("review_count"        ) val reviewCount        : Int?                         = null,
            @SerializedName("booster_ids"         ) val boosterId           : ArrayList<Int>?              = null,
            @SerializedName("delivery_time"       ) var deliveryTime       : String?                       =null ,
            @SerializedName("delivery_type"       ) var deliveryType       : String?                       = null,
            @SerializedName("is_rush_mode"       ) var is_rush_mode       : Boolean?                       = null
        )
        {
            data class RestaurantCuisines (
                @SerializedName("id"      ) var id      : Int?    = null,
                @SerializedName("cuisine" ) var cuisine : String? = null
            )
        }
        data class Categories(

            @SerializedName("id"            ) var id           : Int?                 = null,
            @SerializedName("category_name" ) var categoryName : String?              = null,
            @SerializedName("menu_items"    ) var menuItems    : ArrayList<MenuItems> = arrayListOf(),
            var isViewVisibility: Boolean = false
        ) {
            data class MenuItems(
                @SerializedName("id"              ) var id            : Int?                  = null,
                @SerializedName("dish_name"       ) var dishName      : String?               = null,
                @SerializedName("dish_type"       ) var dishType      : String?               = null,
                @SerializedName("price"           ) var price         : Double?               = null,
                @SerializedName("description"     ) var description   : String?               = null,
                @SerializedName("is_active"       ) var isActive      : Boolean?              = null,
                @SerializedName("menu_add_ons"    ) var menuAddOns    : ArrayList<MenuAddOns> = arrayListOf(),
                @SerializedName("menu_item_image" ) var menuItemImage : String?               = null,
                @SerializedName("quantity"        ) var quantity      : Int?                  = null,
                @SerializedName("menu_total_quantity") var menu_total_quantity: Int?           = null,
                @SerializedName("actual_price"    ) var actualPrice   : Double?                  = null,
                @SerializedName("max_quantity"    ) var maxQuantity   : Int?                  = null,
                @SerializedName("menu_item_id"    ) var menuItemId    : Int?                  = null
            ) {
                data class MenuAddOns(

                    @SerializedName("id") var id: Int? = null,
                    @SerializedName("add_ons_id") var addOnsId: Int? = null,
                    @SerializedName("add_ons_name") var addOnsName: String? = null,
                    @SerializedName("is_required") var isRequired: Boolean? = null,
                    @SerializedName("choose_upto") var chooseUpto: Int? = null,
                    @SerializedName("add_ons_relations") var addOnsRelations: ArrayList<AddOnsRelations> = arrayListOf()

                ) {
                    data class AddOnsRelations(

                        @SerializedName("id"          ) var id         : Int?     = null,
                        @SerializedName("item_name"   ) var itemName   : String?  = null,
                        @SerializedName("dish_type"   ) var dishType   : String?  = null,
                        @SerializedName("price"       ) var price      : Double?     = null,
                        @SerializedName("add_ons_name") var addOnsName: String? = null,
                        @SerializedName("actual_price") var actualPrice: Double? = null,
                        @SerializedName("is_selected" ) var isSelected : Boolean? = null

                    )

                    data class OfferModel(
                        val coupon_code:String,
                        val description:String
                    )

                    data class TodaySchedule(
                        val day:String,
                        val timings:String
                    )
                }
            }
        }
    }
}