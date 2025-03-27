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

class CoffeeAPI {
    companion object {
        const val BASE_URL = "https://www.javiercarrasco.es/api/coffee/"
        fun getRetrofit2Api(): CoffeeAPIInterface {
            return Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(CoffeeAPIInterface::class.java)
        }
    }
}

interface CoffeeAPIInterface {
    @GET("coffee")
    suspend fun getCoffees(@Header("Authorization") token: String): Coffees
    @GET("coffee/{id}")
    suspend fun getCoffeeById(@Header("Authorization") token: String, @Path("id") id: Int): CoffeeItem
    @POST("comments")
    suspend fun addCoffeeComment(@Header("Authorization") token: String, @Body comment: CommentItem
    ): Response<CommentItem>
    @GET("comments/{id}")
    suspend fun getCoffeeCommentsById(@Header("Authorization") token: String, @Path("id") id: Int): Comments
    @POST("login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}