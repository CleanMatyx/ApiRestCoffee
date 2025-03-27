package edu.matiasborra.apirestcoffee.model.coffee


import com.google.gson.annotations.SerializedName

data class CoffeeItem(
    @SerializedName("coffee_name")
    val coffeeName: String,
    @SerializedName("coffee_desc")
    val coffeeDesc: String,
    @SerializedName("comments")
    val comments: Int,
    @SerializedName("id")
    val id: Int
)