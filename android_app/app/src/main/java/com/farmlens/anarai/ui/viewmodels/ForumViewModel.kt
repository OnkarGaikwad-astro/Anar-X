package com.farmlens.anarai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmlens.anarai.data.remote.ForumPost
import com.farmlens.anarai.data.remote.ForumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class ForumUiState {
    object Loading : ForumUiState()
    data class Success(val posts: List<ForumPost>) : ForumUiState()
    data class Error(val message: String) : ForumUiState()
}

class ForumViewModel : ViewModel() {
    private val repository = ForumRepository()

    private val _uiState = MutableStateFlow<ForumUiState>(ForumUiState.Loading)
    val uiState: StateFlow<ForumUiState> = _uiState.asStateFlow()

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _uiState.value = ForumUiState.Loading
            repository.getAllPosts()
                .catch { e ->
                    _uiState.value = ForumUiState.Error(e.message ?: "An unknown error occurred")
                }
                .collect { posts ->
                    // Sort posts by date descending, or let Supabase do it if we add .order()
                    _uiState.value = ForumUiState.Success(posts)
                }
        }
    }

    fun addPost(content: String) {
        viewModelScope.launch {
            try {
                val newPost = ForumPost(
                    authorName = "You", // In a real app, this comes from auth
                    content = content
                )
                repository.insertPost(newPost)
                fetchPosts() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = ForumUiState.Error(e.message ?: "Failed to add post")
            }
        }
    }
}
