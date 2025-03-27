package edu.matiasborra.apirestcoffee.data

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("usuario")
    val user: String,
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("ok")
    val ok: Boolean,
    @SerializedName("token")
    val token: String?,
    @SerializedName("message")
    val message: String?
)