# Voice Response & Notification Announcer Guide

## 🎤 Hands-Free Continuous Voice Listener

### Overview
After the wake word "Myra" is detected, the app automatically continues listening for the user's actual command without requiring any screen tap.

### How It Works

**State Machine:**
```
1. LISTENING MODE (Waiting for Wake Word)
   └─ SpeechRecognizer continuously listens
   └─ Auto-restarts on timeout
   └─ No user interaction needed

2. WAKE WORD DETECTED ("Myra")
   └─ Brief pause
   └─ Triggers "Ready for command" notification
   └─ Enters COMMAND MODE

3. COMMAND MODE (Listening for Actual Request)
   └─ SpeechRecognizer captures user command
   └─ 10-second timeout
   └─ 3-second silence = end of speech
   └─ Processes command

4. PROCESSING & RESPONSE
   └─ Executes voice command or sends to AI
   └─ Returns to LISTENING MODE
```

### Files Added
```
ContinuousVoiceListener.kt
├── initialize()                          # Setup SpeechRecognizer
├── startListening()                      # Begin continuous loop
├── listeningLoop()                       # Main recognition loop
├── startSpeechRecognition()             # No user tap required
├── enterCommandListeningMode()          # Auto-capture after wake word
├── ContinuousRecognitionListener        # Wake word detection
└── CommandRecognitionListener           # Command capture
```

### Voice Commands
```
Wake Word: "Myra"
↓
Automatic Listening for Command:
- "Take selfie"
- "Record video"
- "Share to Instagram"
- "Kya chal raha hai?" (What's up?)
- Any other voice command
```

### Usage
```kotlin
// Initialize and start listening
val voiceListener = ContinuousVoiceListener(context, onCommand, onStateChange)
voiceListener.initialize()
voiceListener.startListening()

// No manual start needed!
// System automatically listens continuously
// User just says "Myra" then their command
```

### Key Features
- ✅ **No Tap Required** - Continuous background listening
- ✅ **Auto-Resume** - Restarts after timeout
- ✅ **Wake Word Detection** - "Myra" triggers command mode
- ✅ **Auto Command Capture** - Listens for 10 seconds after wake word
- ✅ **Silence Detection** - 3 seconds of silence ends speech
- ✅ **Error Recovery** - Restarts on recognition errors
- ✅ **State Callbacks** - Listen to state changes

---

## 📲 Notification Listener Service

### Overview
Intercepts incoming notifications from WhatsApp, Facebook, Instagram, Telegram, and Messenger. Extracts app name, sender, and message content.

### Supported Apps
- ✅ WhatsApp & WhatsApp Business
- ✅ Facebook & Facebook Lite
- ✅ Instagram & Instagram Lite
- ✅ Telegram
- ✅ Facebook Messenger

### How It Works

**Flow:**
```
Incoming Notification
        ↓
NotificationListenerService.onNotificationPosted()
        ↓
Extract Package Name
        ↓
Check if App Supported
        ↓
Extract Data:
├─ App Name (WhatsApp, Instagram, etc.)
├─ Sender Name (Contact/Group)
└─ Message Content (Text)
        ↓
Trigger Callback
        ↓
Announce via TextToSpeech
```

### Files Added
```
NotificationInterceptorService.kt
├── onNotificationPosted()           # Intercept new notifications
├── isSupportedApp()                 # Check if app is supported
├── extractSenderName()              # Get contact/group name
├── extractMessage()                 # Get message text
└── setOnNotificationReceivedListener() # Register callback
```

### Setup Instructions

**Step 1: Enable in Manifest**
```xml
<uses-permission android:name="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" />

<service
    android:name=".service.NotificationInterceptorService"
    android:enabled="true"
    android:exported="true"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>
```

**Step 2: Grant Notification Access**
1. Go to **Settings → Apps & Notifications → Notifications**
2. Select **Advanced Settings** (or scroll down)
3. Tap **Notification Access**
4. Find **Myra AI Assistant**
5. Toggle **ON**

Or automated:
```kotlin
val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
startActivity(intent)
```

### Usage
```kotlin
// Set callback for incoming notifications
NotificationInterceptorService.setOnNotificationReceivedListener { notification ->
    val appName = notification.appName        // "WhatsApp"
    val sender = notification.sender          // "John Doe"
    val message = notification.message        // "Hey! What's up?"
    val timestamp = notification.timestamp    // System time
    
    // Process notification
    Timber.d("$appName from $sender: $message")
}
```

### Notification Data Class
```kotlin
data class NotificationData(
    val appName: String,        // "WhatsApp", "Instagram", etc.
    val packageName: String,    // "com.whatsapp"
    val sender: String,         // "Mom", "Work Group", etc.
    val message: String,        // Actual message content
    val timestamp: Long         // When received
)
```

---

## 🔊 Text-to-Speech Announcer

### Overview
Converts intercepted notification content to speech and announces it aloud with context.

### How It Works

**Announcement Format:**
```
"[App] message from [Sender]: [Message]"

Examples:
- "WhatsApp message from John: Hey, what's up?"
- "Instagram message from Sarah: Check out my new post!"
- "Facebook message from Mom: Call me when free"
- "Telegram from Team: Meeting at 3 PM"
```

### Files Added
```
NotificationAnnouncer.kt
├── initialize()                    # Initialize TextToSpeech
├── announceNotification()          # Main announcement method
├── buildAnnouncementText()         # Format text for speech
├── configureTextToSpeech()         # Set speech parameters
├── stop()                          # Stop speaking
└── Callbacks: onSpeechStart/End/Error
```

### Features
- ✅ **Multi-App Support** - Works with all major messaging apps
- ✅ **Natural Announcement** - "WhatsApp from John: Hello"
- ✅ **Message Truncation** - Limits to 300 chars for reasonable speech time
- ✅ **Queue Management** - Handles multiple notifications
- ✅ **Speed Control** - 0.9x speech rate for clarity
- ✅ **Language Support** - Uses device default language
- ✅ **Error Handling** - Graceful fallback on TTS errors

### Usage
```kotlin
val announcer = NotificationAnnouncer(context)
announcer.initialize { isSuccess ->
    if (isSuccess) {
        // Ready to announce
    }
}

// Announce a notification
announcer.announceNotification(
    appName = "WhatsApp",
    sender = "John",
    message = "Hey, how are you?"
)
// Spoken: "WhatsApp message from John: Hey, how are you?"
```

### Configuration
```kotlin
// Set speech rate (0.5 - 2.0, where 1.0 is normal)
tts.setSpeechRate(0.9f)  // 10% slower for clarity

// Set pitch (1.0 is normal)
tts.setPitch(1.0f)

// Set language
tts.setLanguage(Locale.getDefault())
```

### Callbacks
```kotlin
announcer.setOnSpeechStartListener {
    Timber.d("Started announcing")
    updateUI()
}

announcer.setOnSpeechEndListener {
    Timber.d("Finished announcing")
    clearUI()
}

announcer.setOnErrorListener { error ->
    Timber.e("TTS Error: $error")
}
```

---

## 🔐 Permissions Required

```xml
<!-- Continuous Voice -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

<!-- Notification Access -->
<uses-permission android:name="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" />

<!-- Text-to-Speech -->
<uses-permission android:name="android.permission.INTERNET" />
```

### Runtime Permission Requests
```kotlin
val permissions = arrayOf(
    android.Manifest.permission.RECORD_AUDIO,
    android.Manifest.permission.MODIFY_AUDIO_SETTINGS
)
requestPermissions(permissions, REQUEST_CODE)
```

### User Setup Checklist
- [ ] Grant microphone permission
- [ ] Enable Notification Access (Settings)
- [ ] Grant audio modification permission
- [ ] Check volume is not muted
- [ ] Ensure TextToSpeech language installed

---

## 🧪 Testing

### Manual Tests

**Voice Listening:**
```
1. Start app
2. Tap microphone button
3. Say "Myra"
4. After beep, say a command: "Take selfie"
5. Command should execute
```

**Notification Announcing:**
```
1. Enable Notification Access for Myra
2. Send WhatsApp message to your phone
3. Listen for TTS announcement: "WhatsApp from John: Hello"
```

### Automated Tests
```bash
# Test voice recognition
adb shell am start -n com.myra.ai.assistant/.ui.EnhancedMainActivity

# Test notification listener
adb shell am startservice com.myra.ai.assistant/.service.NotificationInterceptorService

# Send test notification
adb shell service call notification 1 s16 "com.myra.ai.assistant"
```

---

## ⚙️ Advanced Configuration

### Modify Wake Word
In `ContinuousVoiceListener.kt`:
```kotlin
private val WAKE_WORD = "myra"  // Change to your word
```

### Adjust Listening Timeouts
```kotlin
private val COMMAND_TIMEOUT = 10000L      // 10 seconds
private val SILENCE_TIMEOUT = 3000L       // 3 seconds of silence
```

### Filter Notifications
In `NotificationInterceptorService.kt`:
```kotlin
private fun isSupportedApp(packageName: String): Boolean {
    // Add or remove apps here
    return packageName in listOf(
        WHATSAPP_PACKAGE,
        INSTAGRAM_PACKAGE,
        // Add more...
    )
}
```

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| Wake word not detected | Check microphone permission, speak clearly |
| No notification sounds | Enable Notification Access in Settings |
| TTS not working | Ensure TextToSpeech language is installed |
| Continuous listening stops | Check microphone permission, restart app |
| Wrong sender extracted | Verify notification format from app |

---

## 🚀 Features Overview

| Feature | Status | Details |
|---------|--------|---------|
| Continuous Listening | ✅ | Auto-restarts, no tap needed |
| Wake Word Detection | ✅ | "Myra" triggers command mode |
| Command Capture | ✅ | 10-second listen after wake word |
| Notification Interception | ✅ | WhatsApp, FB, Instagram, Telegram |
| Text Extraction | ✅ | App, sender, message |
| Text-to-Speech | ✅ | Announces notifications aloud |
| Message Queueing | ✅ | Handles multiple notifications |
| Error Recovery | ✅ | Auto-restart on failures |

---

## 📚 API Reference

### ContinuousVoiceListener
```kotlin
initialize()                                              // Setup
startListening()                                         // Begin
stopListening()                                          // Stop
setOnWakeWordDetectedListener(callback)                 // Wake word
setOnVoiceDataListener(callback)                        // Command
shutdown()                                              // Cleanup
```

### NotificationInterceptorService
```kotlin
onNotificationPosted(sbn)                               // Intercept
setOnNotificationReceivedListener(callback)             // Register
```

### NotificationAnnouncer
```kotlin
initialize(onComplete)                                 // Setup TTS
announceNotification(app, sender, message)            // Announce
stop()                                                 // Stop TTS
setOnSpeechStartListener(callback)                    // Events
setOnSpeechEndListener(callback)
setOnErrorListener(callback)
shutdown()                                            // Cleanup
```

---

**Myra AI now listens continuously without requiring taps, automatically announces incoming messages, and provides a truly hands-free experience!** 🎤📢

