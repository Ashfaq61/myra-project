package com.myra.ai.assistant.data.repository

import com.myra.ai.assistant.data.model.ChatMessage
import com.myra.ai.assistant.network.api.GeminiApiService
import timber.log.Timber

/**
 * Repository for managing chat data and API calls
 */
class ChatRepository(private val geminiService: GeminiApiService) {

    /**
     * Send message and get response from Gemini
     */
    suspend fun sendMessage(message: String): ChatMessage? {
        return try {
            Timber.d("Repository: sending message")
            val response = geminiService.sendQuery(message)
            
            if (response.success && response.text != null) {
                ChatMessage(
                    text = response.text,
                    isUserMessage = false
                )
            } else {
                Timber.e("Repository: error - ${response.error}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Repository: exception sending message")
            null
        }
    }
}
