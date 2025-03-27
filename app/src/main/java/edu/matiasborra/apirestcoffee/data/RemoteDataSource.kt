package edu.matiasborra.apirestcoffee.data

import android.util.Log
import edu.matiasborra.apirestcoffee.auth.LoginRequest
import edu.matiasborra.apirestcoffee.auth.LoginResponse
import edu.matiasborra.apirestcoffee.model.comment.CommentItem
import kotlinx.coroutines.flow.flow

class RemoteDataSource {
    private val api = CoffeeAPI.getRetrofit2Api()
    fun getCoffees(token: String) = flow {
        emit(api.getCoffees("Bearer $token"))
    }
    fun getCoffeesById(token: String, id: Int) = flow {
        emit(api.getCoffeeById("Bearer $token", id))
    }
    fun getCoffeesCommentsById(token: String, id: Int) = flow {
        val result = api.getCoffeeCommentsById("Bearer $token", id)
        // Convertir result de Coffees a Comments si es necesario
        emit(result)
    }
    suspend fun login(request: LoginRequest): LoginResponse {
        val response = api.login(request)
        if(response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta vacía del servidor")
        } else {
            val errorBody = response.errorBody()?.string() ?: String
            Log.e("ApiRestCoffee", "Error: ${response.message()} | $errorBody")
            throw Exception("Error en login: ${response.message()}")
        }
    }

    suspend fun addCoffeeComment(token: String, comment: CommentItem) {
        api.addCoffeeComment("Bearer $token", comment)
    }
}