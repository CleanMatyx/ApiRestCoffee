package edu.matiasborra.apirestcoffee.data

import android.util.Log
import edu.matiasborra.apirestcoffee.auth.LoginRequest
import edu.matiasborra.apirestcoffee.auth.LoginResponse
import edu.matiasborra.apirestcoffee.model.comment.CommentItem
import kotlinx.coroutines.flow.flow

/**
 * Fuente de datos remota para acceder a la API de café.
 * @constructor Crea una instancia de RemoteDataSource.
 * @author Matias Borra
 */
class RemoteDataSource {
    private val api = CoffeeAPI.getRetrofit2Api()

    /**
     * Obtiene la lista de cafés.
     * @param token Token de autenticación.
     * @return Flujo de la lista de cafés.
     * @author Matias Borra
     */
    fun getCoffees(token: String) = flow {
        emit(api.getCoffees("Bearer $token"))
    }

    /**
     * Obtiene un café por su ID.
     * @param token Token de autenticación.
     * @param id ID del café.
     * @return Flujo del café.
     * @author Matias Borra
     */
    fun getCoffeesById(token: String, id: Int) = flow {
        emit(api.getCoffeeById("Bearer $token", id))
    }

    /**
     * Obtiene los comentarios de un café por su ID.
     * @param token Token de autenticación.
     * @param id ID del café.
     * @return Flujo de los comentarios del café.
     * @author Matias Borra
     */
    fun getCoffeesCommentsById(token: String, id: Int) = flow {
        val result = api.getCoffeeCommentsById("Bearer $token", id)
        emit(result)
    }

    /**
     * Inicia sesión con el nombre de usuario y contraseña proporcionados.
     * @param request Solicitud de inicio de sesión.
     * @return Respuesta de inicio de sesión.
     * @throws Exception Si hay un error en el inicio de sesión.
     * @author Matias Borra
     */
    suspend fun login(request: LoginRequest): LoginResponse {
        val response = api.login(request)
        if(response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta vacía del servidor")
        } else {
            val errorBody = response.errorBody()?.string() ?: String
            throw Exception("Error en login: ${response.message()}| $errorBody")
        }
    }

    /**
     * Añade un comentario a un café.
     * @param token Token de autenticación.
     * @param comment Comentario a añadir.
     * @author Matias Borra
     */
    suspend fun addCoffeeComment(token: String, comment: CommentItem) {
        api.addCoffeeComment("Bearer $token", comment)
    }
}