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

/**
 * ViewModel para manejar la lógica de la pantalla principal.
 * @param repository Repositorio para acceder a los datos.
 * @author Matias Borra
 */
class MainViewModel(private val repository: Repository): ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: Flow<LoginState>
        get() = _loginState

    /**
     * Obtiene la lista de cafés.
     * @param token Token de autenticación.
     * @author Matias Borra
     */
    fun fetchCoffees(token: String) = repository.fetchCoffees(token)

    /**
     * Obtiene un café por su ID.
     * @param token Token de autenticación.
     * @param id ID del café.
     * @author Matias Borra
     */
    fun fetchCoffeesById(token: String, id: Int) = repository.fetchCoffeesById(token, id)

    /**
     * Inicia sesión con el nombre de usuario y contraseña proporcionados.
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @author Matias Borra
     */
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

    /**
     * Cierra la sesión actual.
     * @author Matias Borra
     */
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _loginState.value = LoginState.Idle
        }
    }

    /**
     * Obtiene el flujo de la sesión actual.
     * @return Flujo de la sesión.
     * @author Matias Borra
     */
    fun getSessionFlow() = repository.getSessionFlow()
}

/**
 * Factory para crear instancias de MainViewModel.
 * @param repository Repositorio para acceder a los datos.
 * @author Matias Borra
 */
@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(private val repository: Repository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}