package edu.matiasborra.apirestcoffee.data

import edu.matiasborra.apirestcoffee.auth.LoginRequest
import edu.matiasborra.apirestcoffee.auth.LoginResponse
import edu.matiasborra.apirestcoffee.model.coffee.Coffees
import edu.matiasborra.apirestcoffee.model.coffee.CoffeeItem
import edu.matiasborra.apirestcoffee.model.comment.CommentItem
import edu.matiasborra.apirestcoffee.model.comment.Comments
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Clase que proporciona la configuración de Retrofit para acceder a la API de café.
 * @author Matias Borra
 */
class CoffeeAPI {
    companion object {
        const val BASE_URL = "https://www.javiercarrasco.es/api/coffee/"

        /**
         * Obtiene una instancia de CoffeeAPIInterface configurada con Retrofit.
         * @return CoffeeAPIInterface Instancia de la interfaz de la API.
         * @author Matias Borra
         */
        fun getRetrofit2Api(): CoffeeAPIInterface {
            return Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(CoffeeAPIInterface::class.java)
        }
    }
}

/**
 * Interfaz que define los métodos para acceder a la API de café.
 * @author Matias Borra
 */
interface CoffeeAPIInterface {
    /**
     * Obtiene la lista de cafés.
     * @param token Token de autenticación.
     * @return Lista de cafés.
     * @author Matias Borra
     */
    @GET("coffee")
    suspend fun getCoffees(@Header("Authorization") token: String): Coffees

    /**
     * Obtiene un café por su ID.
     * @param token Token de autenticación.
     * @param id ID del café.
     * @return Detalles del café.
     * @author Matias Borra
     */
    @GET("coffee/{id}")
    suspend fun getCoffeeById(@Header("Authorization") token: String, @Path("id") id: Int): CoffeeItem

    /**
     * Añade un comentario a un café.
     * @param token Token de autenticación.
     * @param comment Comentario a añadir.
     * @return Respuesta del servidor con el comentario añadido.
     * @author Matias Borra
     */
    @POST("comments")
    suspend fun addCoffeeComment(@Header("Authorization") token: String, @Body comment: CommentItem): Response<CommentItem>

    /**
     * Obtiene los comentarios de un café por su ID.
     * @param token Token de autenticación.
     * @param id ID del café.
     * @return Lista de comentarios del café.
     * @author Matias Borra
     */
    @GET("comments/{id}")
    suspend fun getCoffeeCommentsById(@Header("Authorization") token: String, @Path("id") id: Int): Comments

    /**
     * Inicia sesión con el nombre de usuario y contraseña proporcionados.
     * @param request Solicitud de inicio de sesión.
     * @return Respuesta de inicio de sesión.
     * @author Matias Borra
     */
    @POST("login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}