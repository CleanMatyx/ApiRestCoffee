package edu.matiasborra.apirestcoffee.model.comment


import com.google.gson.annotations.SerializedName

data class CommentItem(
    @SerializedName("comment")
    val comment: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("idCoffee")
    val idCoffee: Int,
    @SerializedName("user")
    val user: String
)