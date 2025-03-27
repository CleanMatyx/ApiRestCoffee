package edu.matiasborra.apirestcoffee.auth

import com.google.gson.annotations.SerializedName

/**
 * Estado de la autenticación del usuario.
 * @author Matias Borra
 */
sealed class LoginState {
    /**
     * Estado inactivo de la autenticación.
     * @author Matias Borra
     */
    object Idle: LoginState()

    /**
     * Estado de carga de la autenticación.
     * @author Matias Borra
     */
    object Loading: LoginState()

    /**
     * Estado de éxito de la autenticación.
     * @property response Respuesta de la autenticación.
     * @author Matias Borra
     */
    data class Success(val response: LoginResponse): LoginState()

    /**
     * Estado de error de la autenticación.
     * @property message Mensaje de error.
     * @author Matias Borra
     */
    data class Error(val message: String): LoginState()
}

/**
 * Solicitud de inicio de sesión.
 * @property user Nombre de usuario.
 * @property password Contraseña del usuario.
 * @constructor Crea una instancia de LoginRequest.
 * @author Matias Borra
 */
data class LoginRequest(
    @SerializedName("usuario")
    val user: String,
    @SerializedName("password")
    val password: String
)

/**
 * Respuesta de inicio de sesión.
 * @property ok Indica si la autenticación fue exitosa.
 * @property token Token de autenticación.
 * @property message Mensaje de la respuesta.
 * @property username Nombre de usuario.
 * @constructor Crea una instancia de LoginResponse.
 * @author Matias Borra
 */
data class LoginResponse(
    @SerializedName("ok")
    val ok: Boolean,
    @SerializedName("token")
    val token: String?,
    @SerializedName("message")
    val message: String?,
    val username: String
)