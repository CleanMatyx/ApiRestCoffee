package edu.matiasborra.apirestcoffee.data

import edu.matiasborra.apirestcoffee.auth.LoginRequest
import edu.matiasborra.apirestcoffee.auth.LoginResponse
import edu.matiasborra.apirestcoffee.auth.SessionManager
import edu.matiasborra.apirestcoffee.model.coffee.Coffees
import edu.matiasborra.apirestcoffee.model.coffee.CoffeeItem
import edu.matiasborra.apirestcoffee.model.comment.CommentItem
import edu.matiasborra.apirestcoffee.model.comment.Comments
import kotlinx.coroutines.flow.Flow

class Repository(private val sessionManager: SessionManager, val ds: RemoteDataSource) {
    fun fetchCoffees(token:String): Flow<Coffees> {
        return ds.getCoffees(token)
    }

    fun fetchCoffeesById(token: String, id: Int): Flow<CoffeeItem> {
        return ds.getCoffeesById(token, id)
    }

    fun fetchCoffeesCommentsById(token: String, id: Int): Flow<Comments> {
        return ds.getCoffeesCommentsById(token, id)
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        val response = ds.login(request)
        sessionManager.saveSession(response.token!!, response.username)
        return response
    }

    suspend fun addCoffeeComment(token: String, comment: CommentItem) {
        ds.addCoffeeComment(token, comment)
    }

    suspend fun saveSession(token: String, username: String) {
        sessionManager.saveSession(token, username)
    }

    fun getSessionFlow(): Flow<Pair<String?, String?>> = sessionManager.sessionFlow

    suspend fun logout() {
        sessionManager.clearSession()
    }
}