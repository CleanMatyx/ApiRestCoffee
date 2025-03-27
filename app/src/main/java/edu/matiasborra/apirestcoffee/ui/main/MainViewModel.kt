package edu.matiasborra.apirestcoffee.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.matiasborra.apirestcoffee.auth.LoginRequest
import edu.matiasborra.apirestcoffee.auth.LoginState
import edu.matiasborra.apirestcoffee.data.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository): ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: Flow<LoginState>
        get() = _loginState

    fun fetchCoffees(token: String) = repository.fetchCoffees(token)

    fun fetchCoffeesById(token: String, id: Int) = repository.fetchCoffeesById(token, id)

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = repository.login(LoginRequest(username, password))
                response.token?.let { token ->
                    repository.saveSession(token, username)
                }
                Log.d("MainViewModel", "Login response: $response")
                _loginState.value = LoginState.Success(response)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message?: "Error desconocido")
            }
        }
    }

    fun logout() {
        viewModelScope.launch() {
            repository.logout()
            _loginState.value = LoginState.Idle
        }
    }

    fun getSessionFlow() = repository.getSessionFlow()
}

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(private val repository: Repository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}