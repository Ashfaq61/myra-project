package com.myra.ai.network.api

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Production-ready Gemini 2.5 Flash API integration.
 * Handles user queries and complete response extraction.
 */
class GeminiApiService(private val apiKey: String) {

    companion object {
        private const val MODEL_NAME = "gemini-2.5-flash"
        private const val TEMPERATURE = 0.7f
        private const val TOP_P = 0.95f
        private const val TOP_K = 64
        private const val MAX_OUTPUT_TOKENS = 2048
    }

    private val generativeModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = TEMPERATURE
                topP = TOP_P
                topK = TOP_K
                maxOutputTokens = MAX_OUTPUT_TOKENS
            }
        )
    }

    /**
     * Send user query to Gemini and get response
     * @param userQuery User's natural language query
     * @return GeminiResponse containing complete response data
     */
    suspend fun sendQuery(userQuery: String): GeminiResponse = withContext(Dispatchers.IO) {
        try {
            Timber.d("Sending query to Gemini: $userQuery")

            val response = generativeModel.generateContent(
                content {
                    text(userQuery)
                }
            )

            Timber.d("Received response from Gemini")
            return@withContext extractResponseData(response)
        } catch (e: Exception) {
            Timber.e(e, "Error sending query to Gemini")
            return@withContext GeminiResponse(
                success = false,
                text = null,
                error = e.message ?: "Unknown error occurred"
            )
        }
    }

    /**
     * Send multi-turn conversation to Gemini
     * @param messages List of ConversationMessage objects
     * @return GeminiResponse with response data
     */
    suspend fun sendConversation(messages: List<ConversationMessage>): GeminiResponse =
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Sending conversation with ${messages.size} messages")

                val contentList = messages.map { message ->
                    content {
                        text(message.text)
                    }
                }

                val response = generativeModel.generateContent(
                    contentList
                )

                return@withContext extractResponseData(response)
            } catch (e: Exception) {
                Timber.e(e, "Error in conversation")
                return@withContext GeminiResponse(
                    success = false,
                    text = null,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }

    /**
     * Extract complete response data from Gemini response
     */
    private fun extractResponseData(
        response: com.google.ai.client.generativeai.type.GenerateContentResponse
    ): GeminiResponse {
        return try {
            val text = response.text

            if (text.isNullOrEmpty()) {
                Timber.w("Empty response from Gemini")
                return GeminiResponse(
                    success = false,
                    text = null,
                    error = "Empty response received"
                )
            }

            Timber.d("Successfully extracted response: ${text.take(100)}...")

            GeminiResponse(
                success = true,
                text = text,
                error = null
            )
        } catch (e: Exception) {
            Timber.e(e, "Error extracting response data")
            GeminiResponse(
                success = false,
                text = null,
                error = e.message ?: "Failed to extract response"
            )
        }
    }

    /**
     * Stream response from Gemini for real-time output
     * @param userQuery User's query
     * @param onChunk Callback for each chunk of response
     * @param onError Callback for errors
     * @param onComplete Callback when streaming is complete
     */
    suspend fun streamQuery(
        userQuery: String,
        onChunk: (String) -> Unit,
        onError: (Exception) -> Unit,
        onComplete: () -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            Timber.d("Starting stream query: $userQuery")

            generativeModel.generateContentStream(
                content {
                    text(userQuery)
                }
            ).collect { chunk ->
                chunk.text?.let { text ->
                    Timber.d("Received chunk: ${text.take(50)}...")
                    onChunk(text)
                }
            }

            onComplete()
            Timber.d("Stream query completed")
        } catch (e: Exception) {
            Timber.e(e, "Error in stream query")
            onError(e)
        }
    }

    /**
     * Validate API key connectivity
     */
    suspend fun validateConnection(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = generativeModel.generateContent("ping")
            response.text?.isNotEmpty() == true
        } catch (e: Exception) {
            Timber.e(e, "Connection validation failed")
            false
        }
    }
}

/**
 * Data class for Gemini response
 */
data class GeminiResponse(
    val success: Boolean,
    val text: String?,
    val error: String?
)

/**
 * Data class for conversation messages
 */
data class ConversationMessage(
    val role: String, // "user" or "assistant"
    val text: String
)
