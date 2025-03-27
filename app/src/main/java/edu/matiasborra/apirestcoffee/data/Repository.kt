package edu.matiasborra.apirestcoffee.data

import edu.matiasborra.apirestcoffee.auth.LoginRequest
import edu.matiasborra.apirestcoffee.auth.LoginResponse
import edu.matiasborra.apirestcoffee.auth.SessionManager
import edu.matiasborra.apirestcoffee.model.coffee.Coffees
import edu.matiasborra.apirestcoffee.model.coffee.CoffeeItem
import edu.matiasborra.apirestcoffee.model.comment.CommentItem
import edu.matiasborra.apirestcoffee.model.comment.Comments
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para manejar las operaciones de datos de la aplicación.
 * @property sessionManager Administrador de sesión para manejar la autenticación.
 * @property ds Fuente de datos remota para acceder a la API.
 * @constructor Crea una instancia de Repository.
 * @author Matias Borra
 */
class Repository(private val sessionManager: SessionManager, val ds: RemoteDataSource) {

    /**
     * Obtiene la lista de cafés.
     * @param token Token de autenticación.
     * @return Flujo de la lista de cafés.
     * @author Matias Borra
     */
    fun fetchCoffees(token: String): Flow<Coffees> {
        return ds.getCoffees(token)
    }

    /**
     * Obtiene un café por su ID.
     * @param token Token de autenticación.
     * @param id ID del café.
     * @return Flujo del café.
     * @author Matias Borra
     */
    fun fetchCoffeesById(token: String, id: Int): Flow<CoffeeItem> {
        return ds.getCoffeesById(token, id)
    }

    /**
     * Obtiene los comentarios de un café por su ID.
     * @param token Token de autenticación.
     * @param id ID del café.
     * @return Flujo de los comentarios del café.
     * @author Matias Borra
     */
    fun fetchCoffeesCommentsById(token: String, id: Int): Flow<Comments> {
        return ds.getCoffeesCommentsById(token, id)
    }

    /**
     * Inicia sesión con el nombre de usuario y contraseña proporcionados.
     * @param request Solicitud de inicio de sesión.
     * @return Respuesta de inicio de sesión.
     * @throws Exception Si hay un error en el inicio de sesión.
     * @author Matias Borra
     */
    suspend fun login(request: LoginRequest): LoginResponse {
        val response = ds.login(request)
        sessionManager.saveSession(response.token!!, response.username)
        return response
    }

    /**
     * Añade un comentario a un café.
     * @param token Token de autenticación.
     * @param comment Comentario a añadir.
     * @author Matias Borra
     */
    suspend fun addCoffeeComment(token: String, comment: CommentItem) {
        ds.addCoffeeComment(token, comment)
    }

    /**
     * Guarda la sesión actual.
     * @param token Token de autenticación.
     * @param username Nombre de usuario.
     * @author Matias Borra
     */
    suspend fun saveSession(token: String, username: String) {
        sessionManager.saveSession(token, username)
    }

    /**
     * Obtiene el flujo de la sesión actual.
     * @return Flujo de la sesión.
     * @author Matias Borra
     */
    fun getSessionFlow(): Flow<Pair<String?, String?>> = sessionManager.sessionFlow

    /**
     * Cierra la sesión actual.
     * @author Matias Borra
     */
    suspend fun logout() {
        sessionManager.clearSession()
    }
}