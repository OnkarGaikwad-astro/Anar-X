package com.farmlens.anarai.ml

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

class OnDeviceLLMService(private val context: Context) {
    private var llmInference: LlmInference? = null
    private val modelName = "gemma.bin"
    
    val isModelAvailable: Boolean
        get() = File(context.getExternalFilesDir(null), modelName).exists()

    suspend fun initialize() {
        if (llmInference != null) return
        
        withContext(Dispatchers.IO) {
            val modelFile = File(context.getExternalFilesDir(null), modelName)
            if (!modelFile.exists()) {
                throw Exception("Model file not found. Please download it first.")
            }
            
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(512)
                .setTemperature(0.7f)
                .build()
                
            llmInference = LlmInference.createFromOptions(context, options)
        }
    }

    fun generateResponse(prompt: String): Flow<String> = flow {
        val inference = llmInference ?: throw Exception("LLM not initialized")
        
        // Gemma requires specific prompt formatting
        val formattedPrompt = "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"
        
        try {
            val response = inference.generateResponse(formattedPrompt)
            // MediaPipe currently returns the full response at once in this version,
            // but we wrap it in a Flow for future streaming compatibility
            emit(response)
        } catch (e: Exception) {
            emit("Error generating response: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    fun close() {
        llmInference?.close()
        llmInference = null
    }
}
