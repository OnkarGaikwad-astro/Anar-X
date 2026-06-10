package com.farmlens.anarai.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.farmlens.anarai.ml.OnDeviceLLMService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AskAurexViewModel(application: Application) : AndroidViewModel(application) {

    private val llmService = OnDeviceLLMService(application)

    private val _isModelDownloaded = MutableStateFlow(llmService.isModelAvailable)
    val isModelDownloaded: StateFlow<Boolean> = _isModelDownloaded.asStateFlow()

    private val _isModelInitializing = MutableStateFlow(false)
    val isModelInitializing: StateFlow<Boolean> = _isModelInitializing.asStateFlow()

    private val _isModelReady = MutableStateFlow(false)
    val isModelReady: StateFlow<Boolean> = _isModelReady.asStateFlow()

    private val _messages = MutableStateFlow(
        listOf(ChatMessage("Hello! I am Aurex, your completely offline agricultural AI. How can I help you today?", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun updateModelDownloadedStatus() {
        _isModelDownloaded.value = llmService.isModelAvailable
    }

    fun initializeModel() {
        if (!_isModelReady.value && !_isModelInitializing.value) {
            viewModelScope.launch {
                _isModelInitializing.value = true
                try {
                    llmService.initialize()
                    _isModelReady.value = true
                } catch (e: Exception) {
                    // Handle initialization error
                } finally {
                    _isModelInitializing.value = false
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // Add user message
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(ChatMessage(text, isUser = true))
        
        // Add AI typing placeholder
        val aiMessageIndex = currentMessages.size
        currentMessages.add(ChatMessage("Thinking...", isUser = false, isTyping = true))
        _messages.value = currentMessages

        viewModelScope.launch {
            try {
                llmService.generateResponse(text).collectLatest { response ->
                    val updatedMessages = _messages.value.toMutableList()
                    updatedMessages[aiMessageIndex] = ChatMessage(response, isUser = false)
                    _messages.value = updatedMessages
                }
            } catch (e: Exception) {
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages[aiMessageIndex] = ChatMessage("Sorry, an error occurred: ${e.message}", isUser = false)
                _messages.value = updatedMessages
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        llmService.close()
    }
}
