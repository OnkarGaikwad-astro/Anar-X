package com.farmlens.anarai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmlens.anarai.data.remote.ForumPost
import com.farmlens.anarai.data.remote.ForumReply
import com.farmlens.anarai.data.remote.ForumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class PostDetailUiState {
    object Loading : PostDetailUiState()
    data class Success(val post: ForumPost, val replies: List<ForumReply>) : PostDetailUiState()
    data class Error(val message: String) : PostDetailUiState()
}

class PostDetailViewModel : ViewModel() {
    private val repository = ForumRepository()

    private val _uiState = MutableStateFlow<PostDetailUiState>(PostDetailUiState.Loading)
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private var currentPostId: String? = null

    fun fetchPostDetails(postId: String) {
        currentPostId = postId
        viewModelScope.launch {
            _uiState.value = PostDetailUiState.Loading
            try {
                repository.getPost(postId).collect { post ->
                    if (post != null) {
                        repository.getReplies(postId).collect { replies ->
                            _uiState.value = PostDetailUiState.Success(post, replies)
                        }
                    } else {
                        _uiState.value = PostDetailUiState.Error("Post not found")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PostDetailUiState.Error(e.message ?: "Failed to load post details")
            }
        }
    }

    fun addReply(content: String) {
        val postId = currentPostId ?: return
        viewModelScope.launch {
            try {
                val newReply = ForumReply(
                    postId = postId,
                    authorName = "You", // In a real app, this comes from auth
                    content = content
                )
                repository.insertReply(newReply)
                fetchPostDetails(postId) // Refresh
            } catch (e: Exception) {
                // Should show a Toast or something in UI, but simple error for now
                _uiState.value = PostDetailUiState.Error(e.message ?: "Failed to add reply")
            }
        }
    }
}
