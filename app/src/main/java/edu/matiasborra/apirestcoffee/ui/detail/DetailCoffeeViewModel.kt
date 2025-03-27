package edu.matiasborra.apirestcoffee.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.matiasborra.apirestcoffee.data.Repository
import edu.matiasborra.apirestcoffee.model.coffee.CoffeeItem
import edu.matiasborra.apirestcoffee.model.comment.CommentItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel para manejar la lógica de la pantalla de detalles del café.
 * @param repository Repositorio para acceder a los datos.
 * @param idCoffee ID del café.
 * @author Matias Borra
 */
class DetailCoffeeViewModel(
    private val repository: Repository,
    private val idCoffee: Int = -1
) : ViewModel() {
    private val _coffee = MutableStateFlow<CoffeeItem?>(null)
    val coffee: StateFlow<CoffeeItem?> = _coffee.asStateFlow()
    val comments = MutableStateFlow<List<CommentItem>>(emptyList())

    init {
        viewModelScope.launch {
            repository.getSessionFlow().collect { sessionData ->
                sessionData.first?.let { token ->
                    repository.fetchCoffeesById(token, idCoffee).collect {
                        _coffee.value = it
                    }
                    repository.fetchCoffeesCommentsById(token, idCoffee).collect {
                        comments.value = it
                    }
                }
            }
        }
    }

    /**
     * Añade un comentario a un café.
     * @param token Token de autenticación.
     * @param comment Comentario a añadir.
     * @author Matias Borra
     */
    suspend fun addComment(token: String, comment: CommentItem) {
        repository.addCoffeeComment(token, comment)
    }

    /**
     * Refresca los comentarios de un café.
     * @param token Token de autenticación.
     * @author Matias Borra
     */
    suspend fun refreshComments(token: String) {
        val commentsResult = repository.fetchCoffeesCommentsById(token, idCoffee).first()
        comments.value = commentsResult
    }
}

/**
 * Factory para crear instancias de DetailCoffeeViewModel.
 * @param repository Repositorio para acceder a los datos.
 * @param idCoffee ID del café.
 * @author Matias Borra
 */
@Suppress("UNCHECKED_CAST")
class DetailCoffeeViewModelFactory(
    private val repository: Repository,
    private val idCoffee: Int = -1
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetailCoffeeViewModel(repository, idCoffee) as T
    }
}