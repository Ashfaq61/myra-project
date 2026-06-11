package com.myra.ai.assistant.service

import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

/**
 * Enhanced continuous voice listener with hands-free recognition.
 * Automatically continues listening after wake word detection.
 */
class ContinuousVoiceListener(
    private val context: android.content.Context,
    private val onCommandDetected: (String) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit
) {

    private lateinit var speechRecognizer: SpeechRecognizer
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var isListening = false
    private var isProcessingCommand = false
    
    private val WAKE_WORD = "myra"
    private val COMMAND_TIMEOUT = 10000L // 10 seconds to capture command
    private val SILENCE_TIMEOUT = 3000L  // 3 seconds of silence = end of speech

    private var onWakeWordCallback: (() -> Unit)? = null
    private var onVoiceDataCallback: ((String) -> Unit)? = null

    /**
     * Initialize speech recognizer
     */
    fun initialize() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Timber.w("Speech recognition not available")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer.setRecognitionListener(ContinuousRecognitionListener())
        
        Timber.d("ContinuousVoiceListener initialized")
    }

    /**
     * Start continuous listening loop
     */
    fun startListening() {
        if (!::speechRecognizer.isInitialized) {
            initialize()
        }

        scope.launch {
            Timber.d("Starting continuous voice listening loop")
            listeningLoop()
        }
    }

    /**
     * Main listening loop - continuous without user intervention
     */
    private suspend fun listeningLoop() {
        while (true) {
            try {
                if (!isListening && !isProcessingCommand) {
                    Timber.d("Restarting listening...")
                    startSpeechRecognition()
                    onListeningStateChanged(true)
                }
                delay(500)
            } catch (e: Exception) {
                Timber.e(e, "Error in listening loop")
                delay(1000)
            }
        }
    }

    /**
     * Start speech recognition without user tap
     */
    private fun startSpeechRecognition() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, SILENCE_TIMEOUT)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
            }

            speechRecognizer.startListening(intent)
            isListening = true
            Timber.d("Speech recognition started")
        } catch (e: Exception) {
            Timber.e(e, "Error starting speech recognition")
            isListening = false
        }
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        try {
            if (::speechRecognizer.isInitialized) {
                speechRecognizer.stopListening()
                speechRecognizer.cancel()
            }
            isListening = false
            onListeningStateChanged(false)
            Timber.d("Listening stopped")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping listening")
        }
    }

    /**
     * Set wake word callback
     */
    fun setOnWakeWordDetectedListener(callback: () -> Unit) {
        onWakeWordCallback = callback
    }

    /**
     * Set voice data callback
     */
    fun setOnVoiceDataListener(callback: (String) -> Unit) {
        onVoiceDataCallback = callback
    }

    /**
     * Recognition listener for continuous listening
     */
    inner class ContinuousRecognitionListener : RecognitionListener {

        override fun onReadyForSpeech(params: android.os.Bundle?) {
            Timber.d("Ready for speech input")
        }

        override fun onBeginningOfSpeech() {
            Timber.d("User started speaking")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Audio level changes for visual feedback
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            Timber.d("Audio buffer received")
        }

        override fun onEndOfSpeech() {
            Timber.d("End of speech detected")
        }

        override fun onError(error: Int) {
            Timber.w("Recognition error: $error")
            isListening = false
            onListeningStateChanged(false)

            // Restart listening after error
            scope.launch {
                delay(500)
                startSpeechRecognition()
            }
        }

        override fun onResults(results: android.os.Bundle?) {
            Timber.d("Recognition results received")
            isListening = false

            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.let { recognizedTexts ->
                Timber.d("Recognized ${recognizedTexts.size} options")

                for (text in recognizedTexts) {
                    Timber.d("Recognized: $text")

                    // Check if wake word detected
                    if (text.lowercase(Locale.ROOT).contains(WAKE_WORD)) {
                        Timber.d("Wake word detected!")
                        onWakeWordCallback?.invoke()

                        // Enter command listening mode
                        enterCommandListeningMode()
                        return@let
                    }
                }

                // No wake word, restart listening
                scope.launch {
                    delay(500)
                    startSpeechRecognition()
                }
            }
        }

        override fun onPartialResults(partialResults: android.os.Bundle?) {
            val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            partialMatches?.firstOrNull()?.let { text ->
                if (text.lowercase(Locale.ROOT).contains(WAKE_WORD)) {
                    Timber.d("Partial wake word match: $text")
                }
            }
        }

        override fun onEvent(eventType: Int, params: android.os.Bundle?) {
            Timber.d("Recognition event: $eventType")
        }
    }

    /**
     * Enter command listening mode after wake word
     */
    private fun enterCommandListeningMode() {
        isProcessingCommand = true

        scope.launch {
            try {
                Timber.d("Entering command listening mode")
                
                // Start listening for actual command
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, SILENCE_TIMEOUT)
                }

                // Create command listener
                val commandListener = CommandRecognitionListener()
                speechRecognizer.setRecognitionListener(commandListener)
                speechRecognizer.startListening(intent)

                // Set timeout for command
                delay(COMMAND_TIMEOUT)
                
                if (isProcessingCommand) {
                    Timber.w("Command timeout - no input received")
                    isProcessingCommand = false
                    
                    // Resume normal listening
                    delay(500)
                    speechRecognizer.setRecognitionListener(ContinuousRecognitionListener())
                    startSpeechRecognition()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in command listening mode")
                isProcessingCommand = false
            }
        }
    }

    /**
     * Recognition listener for command input
     */
    inner class CommandRecognitionListener : RecognitionListener {

        override fun onReadyForSpeech(params: android.os.Bundle?) {
            Timber.d("Ready for command input")
        }

        override fun onBeginningOfSpeech() {
            Timber.d("Command input started")
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            Timber.d("End of command input")
        }

        override fun onError(error: Int) {
            Timber.w("Command recognition error: $error")
            isProcessingCommand = false

            scope.launch {
                delay(500)
                speechRecognizer.setRecognitionListener(ContinuousRecognitionListener())
                startSpeechRecognition()
            }
        }

        override fun onResults(results: android.os.Bundle?) {
            Timber.d("Command results received")

            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val command = matches?.firstOrNull() ?: ""

            if (command.isNotEmpty()) {
                Timber.d("Command received: $command")
                onVoiceDataCallback?.invoke(command)
                onCommandDetected(command)
            }

            isProcessingCommand = false

            // Resume continuous listening
            scope.launch {
                delay(500)
                speechRecognizer.setRecognitionListener(ContinuousRecognitionListener())
                startSpeechRecognition()
            }
        }

        override fun onPartialResults(partialResults: android.os.Bundle?) {
            val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            Timber.d("Partial command: ${partialMatches?.firstOrNull()}")
        }

        override fun onEvent(eventType: Int, params: android.os.Bundle?) {
            Timber.d("Command event: $eventType")
        }
    }

    /**
     * Cleanup resources
     */
    fun shutdown() {
        try {
            if (::speechRecognizer.isInitialized) {
                speechRecognizer.destroy()
            }
            scope.cancel()
            Timber.d("ContinuousVoiceListener shutdown")
        } catch (e: Exception) {
            Timber.e(e, "Error shutting down voice listener")
        }
    }
}

private fun Intent(action: String): Intent = android.content.Intent(action)
