package edu.matiasborra.apirestcoffee.model.coffee

import com.google.gson.annotations.SerializedName

/**
 * Clase que representa un elemento de café.
 * @property coffeeName Nombre del café.
 * @property coffeeDesc Descripción del café.
 * @property comments Número de comentarios del café.
 * @property id ID del café.
 * @constructor Crea una instancia de CoffeeItem.
 * @author Matias Borra
 */
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