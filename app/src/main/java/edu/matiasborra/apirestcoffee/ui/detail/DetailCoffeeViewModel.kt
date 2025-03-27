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

    // En DetailCoffeeViewModel.kt
    suspend fun addComment(token: String, comment: CommentItem) {
        repository.addCoffeeComment(token, comment)
    }

    suspend fun refreshComments(token: String) {
        val commentsResult = repository.fetchCoffeesCommentsById(token, idCoffee).first()
        comments.value = commentsResult
    }
}

@Suppress("UNCHECKED_CAST")
class DetailCoffeeViewModelFactory(
    private val repository: Repository,
    private val idCoffee: Int = -1
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetailCoffeeViewModel(repository, idCoffee) as T
    }
}