package edu.matiasborra.apirestcoffee.model.comment

import com.google.gson.annotations.SerializedName

/**
 * Clase que representa un comentario de un café.
 * @property comment Texto del comentario.
 * @property id ID del comentario.
 * @property idCoffee ID del café al que pertenece el comentario.
 * @property user Nombre del usuario que hizo el comentario.
 * @constructor Crea una instancia de CommentItem.
 * @author Matias Borra
 */
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