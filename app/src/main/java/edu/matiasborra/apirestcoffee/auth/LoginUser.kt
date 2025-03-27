package edu.matiasborra.apirestcoffee.auth

import com.google.gson.annotations.SerializedName

sealed class LoginState {
    object Idle: LoginState()
    object Loading: LoginState()
    data class Success(val response: LoginResponse): LoginState()
    data class Error(val message: String): LoginState()
}

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
    val message: String?,
    val username: String
)