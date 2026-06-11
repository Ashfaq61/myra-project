package com.myra.ai.assistant.ui

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewmodels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.myra.ai.assistant.R
import com.myra.ai.assistant.data.model.ChatMessage
import com.myra.ai.assistant.databinding.ActivityMainBinding
import com.myra.ai.assistant.service.ContinuousVoiceListener
import com.myra.ai.assistant.service.NotificationInterceptorService
import com.myra.ai.assistant.service.VoiceActivationService
import com.myra.ai.assistant.tts.NotificationAnnouncer
import com.myra.ai.assistant.ui.adapter.ChatMessageAdapter
import com.myra.ai.assistant.ui.viewmodel.ChatViewModel
import com.myra.ai.assistant.util.PermissionUtils
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

/**
 * Enhanced MainActivity with continuous voice listening and notification announcements.
 * Displays chat interface with dark theme and fluid UI.
 */
class EnhancedMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatMessageAdapter
    
    private lateinit var continuousVoiceListener: ContinuousVoiceListener
    private lateinit var notificationAnnouncer: NotificationAnnouncer
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var isVoiceActiveMode = false

    // Permission launchers
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionsResult(permissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Timber.d("EnhancedMainActivity created")

        setupUI()
        setupRecyclerView()
        setupListeners()
        requestPermissions()
        observeViewModel()
        
        // Initialize voice components
        initializeVoiceListener()
        initializeNotificationSystem()
        
        startVoiceActivationService()
    }

    /**
     * Setup UI components
     */
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Myra AI Assistant - Voice Enabled"
    }

    /**
     * Setup RecyclerView for chat messages
     */
    private fun setupRecyclerView() {
        chatAdapter = ChatMessageAdapter()
        binding.chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@EnhancedMainActivity).apply {
                stackFromEnd = true
            }
        }
    }

    /**
     * Setup view listeners
     */
    private fun setupListeners() {
        // Send button
        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        // Microphone button for manual voice input
        binding.microphoneButton.setOnClickListener {
            if (isVoiceActiveMode) {
                stopContinuousListening()
                isVoiceActiveMode = false
                binding.microphoneButton.setIconTint(getColor(R.color.microphone_idle))
                Snackbar.make(binding.root, "Voice mode disabled", Snackbar.LENGTH_SHORT).show()
            } else {
                startContinuousListening()
                isVoiceActiveMode = true
                binding.microphoneButton.setIconTint(getColor(R.color.microphone_active))
                Snackbar.make(binding.root, "Voice mode enabled - Say 'Myra' to activate", Snackbar.LENGTH_LONG).show()
            }
        }

        // Enter key to send
        binding.messageInput.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.action == android.view.KeyEvent.ACTION_DOWN) {
                sendMessage()
                return@setOnKeyListener true
            }
            false
        }
    }

    /**
     * Initialize continuous voice listener
     */
    private fun initializeVoiceListener() {
        continuousVoiceListener = ContinuousVoiceListener(
            context = this,
            onCommandDetected = { command ->
                Timber.d("Command detected: $command")
                binding.messageInput.setText(command)
                sendMessage()
            },
            onListeningStateChanged = { isListening ->
                updateListeningUI(isListening)
            }
        )

        continuousVoiceListener.initialize()

        // Set callbacks
        continuousVoiceListener.setOnWakeWordDetectedListener {
            Timber.d("Wake word detected!")
            val msg = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "🎤 Listening for your command...",
                isUserMessage = false
            )
            addMessageToChat(msg)
        }

        continuousVoiceListener.setOnVoiceDataListener { voiceText ->
            Timber.d("Voice data: $voiceText")
        }
    }

    /**
     * Initialize notification system
     */
    private fun initializeNotificationSystem() {
        notificationAnnouncer = NotificationAnnouncer(this)
        notificationAnnouncer.initialize { isSuccess ->
            if (isSuccess) {
                Timber.d("TextToSpeech initialized")
            } else {
                Timber.w("Failed to initialize TextToSpeech")
            }
        }

        // Set callbacks
        notificationAnnouncer.setOnSpeechStartListener {
            Timber.d("TTS speech started")
        }

        notificationAnnouncer.setOnSpeechEndListener {
            Timber.d("TTS speech ended")
        }

        notificationAnnouncer.setOnErrorListener { error ->
            Timber.e("TTS error: $error")
        }

        // Set notification interceptor callback
        NotificationInterceptorService.setOnNotificationReceivedListener { notificationData ->
            Timber.d("Notification received: ${notificationData.appName} from ${notificationData.sender}")

            // Announce notification
            notificationAnnouncer.announceNotification(
                notificationData.appName,
                notificationData.sender,
                notificationData.message
            )

            // Add to chat as info message
            val infoMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "📱 ${notificationData.appName} from ${notificationData.sender}: ${notificationData.message}",
                isUserMessage = false
            )
            addMessageToChat(infoMsg)
        }
    }

    /**
     * Start continuous listening
     */
    private fun startContinuousListening() {
        if (PermissionUtils.isPermissionGranted(this, Manifest.permission.RECORD_AUDIO)) {
            continuousVoiceListener.startListening()
            addMessageToChat(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "🎤 Listening mode activated! Say 'Myra' to start.",
                    isUserMessage = false
                )
            )
        } else {
            Snackbar.make(binding.root, "Microphone permission required", Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Stop continuous listening
     */
    private fun stopContinuousListening() {
        continuousVoiceListener.stopListening()
    }

    /**
     * Update listening UI state
     */
    private fun updateListeningUI(isListening: Boolean) {
        if (isListening) {
            binding.microphoneButton.setIconTint(getColor(R.color.microphone_listening))
        } else {
            binding.microphoneButton.setIconTint(getColor(R.color.microphone_idle))
        }
    }

    /**
     * Send message to Myra AI
     */
    private fun sendMessage() {
        val message = binding.messageInput.text.toString().trim()

        if (message.isEmpty()) {
            Snackbar.make(binding.root, "Please enter a message", Snackbar.LENGTH_SHORT).show()
            return
        }

        binding.messageInput.setText("")
        hideKeyboard()

        // Add user message to UI
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = message,
            isUserMessage = true
        )
        addMessageToChat(userMessage)
        binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)

        // Send to ViewModel
        lifecycleScope.launch {
            viewModel.sendMessage(message)
        }
    }

    /**
     * Observe ViewModel changes
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.chatMessages.collect { messages ->
                val assistantMessages = messages.filter { !it.isUserMessage }
                if (assistantMessages.isNotEmpty()) {
                    val lastMessage = assistantMessages.last()
                    if (!chatAdapter.toString().contains(lastMessage.id)) {
                        addMessageToChat(lastMessage)
                        binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                if (error != null) {
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Add message to chat
     */
    private fun addMessageToChat(message: ChatMessage) {
        chatAdapter.addMessage(message)
        binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
    }

    /**
     * Request required permissions
     */
    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.POST_NOTIFICATIONS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        permissionLauncher.launch(permissions.toTypedArray())
    }

    /**
     * Handle permissions result
     */
    private fun handlePermissionsResult(permissions: Map<String, Boolean>) {
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Timber.w("Some permissions were denied")
            Snackbar.make(binding.root, "Some permissions denied - features may be limited", Snackbar.LENGTH_LONG).show()
        }
    }

    /**
     * Start voice activation service
     */
    private fun startVoiceActivationService() {
        val intent = Intent(this, VoiceActivationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    /**
     * Hide keyboard
     */
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.messageInput.windowToken, 0)
    }

    override fun onDestroy() {
        continuousVoiceListener.shutdown()
        notificationAnnouncer.shutdown()
        speechRecognizer?.destroy()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == "com.myra.ai.WAKE_WORD_DETECTED") {
            Timber.d("Wake word detected from service")
            startContinuousListening()
        }
    }
}
