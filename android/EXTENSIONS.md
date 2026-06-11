# Feature Extensions - CameraX, Contextual AI, Social Sharing

## 🎥 CameraX Integration

### Overview
Voice-controlled camera system for automated selfie capture and video recording with direct social media sharing.

### Features
- **Voice Commands**:
  - "Selfie" - Capture selfie immediately
  - "Selfie with timer" - 3-second countdown before capture
  - "Front camera" - Switch to front camera
  - "Back camera" - Switch to back camera
  - "Toggle camera" - Switch between front/back
  - "Record video" - Start video recording
  - "Stop recording" - Stop and save video

### Implementation
```kotlin
// CameraXManager.kt
- Initialize CameraX with lifecycle binding
- Support front/back camera switching
- 3-second timer for selfie capture
- Callback-based image/video delivery
- Error handling and logging
```

### Usage
```kotlin
val cameraManager = CameraXManager(context, lifecycleOwner)
cameraManager.initialize()

// Take selfie with timer
cameraManager.captureSelfieWithTimer { imagePath ->
    Timber.d("Selfie saved at: $imagePath")
}

// Listen to events
cameraManager.setOnImageCapturedListener { path ->
    // Handle captured image
}
```

### Files Created
- `CameraXManager.kt` - Main camera management
- `VoiceCommandParser.kt` - Command parsing with fuzzy matching

---

## 💬 Gemini Contextual Conversation (Ghupshup Mode)

### Overview
Enhanced Gemini API integration with casual, friendly persona maintaining Urdu/English mix conversational mode.

### System Prompt Features
```
Personality:
- Casual and friendly tone
- Mix Urdu and English naturally
- Use friendly Urdu phrases:
  * "Haan bhai" - Hey man
  * "Wah" - Wow
  * "Bilkul" - Sure/Absolutely
  * "Kya chal raha hai?" - What's going on?
  * "Samjhae?" - Understand?
  * "Badiya" - Cool
  
- Include gossipy, conversational vibe
- Ask follow-up questions
- Show personality and humor
- Use emojis occasionally
```

### API Configuration
- **Temperature**: 0.8 (increased for casual tone)
- **Top P**: 0.95
- **Model**: gemini-2.5-flash
- **System Instruction**: Custom ghupshup persona

### Implementation
```kotlin
// EnhancedGeminiApiService.kt
- Contextual conversation with history
- Casual ghupshup mode responses
- Multi-turn conversation support
- Streaming response capability
- Greeting validation
```

### Voice Commands Integration
```kotlin
"Send message" → Parsed for camera/share commands
"Unknown command" → Casual conversation in ghupshup mode
```

### Usage
```kotlin
val geminiService = EnhancedGeminiApiService(apiKey)

// Casual query
val response = geminiService.sendCasualQuery("What's up Myra?")
// Response: "Haan bhai! Sab kuch theek-thaak chal raha hai? (Everything going good?)"

// Contextual conversation
val response = geminiService.sendContextualQuery(
    "Tell me a joke",
    conversationContext
)
// Response: "Bilkul! Ek joke sunau... 😄"
```

### Conversation Examples
- User: "Selfie lao" → Action: Capture selfie → Myra: "📸 Acha photo aya!"
- User: "Kya news hai?" → Casual response in mixed Urdu/English
- User: "Share to Instagram" → Action: Share to Instagram → Myra: "Gram par aa gaya!"

---

## 📱 Social Intent Sharing

### Overview
System intents for automatic media forwarding to Facebook, Instagram, and TikTok native applications.

### Supported Platforms
- **Facebook** & Facebook Lite
- **Instagram** & Instagram Reels
- **TikTok** (Global & Regional versions)

### Implementation
```kotlin
// SocialSharingManager.kt
- App installation detection
- Native intent-based sharing
- Multiple file support (gallery)
- Fallback to Play Store
- Platform availability checking
```

### Voice Commands
- "Share to Facebook" - Share current media to Facebook
- "Share to Instagram" - Share to Instagram
- "Share to TikTok" - Share video to TikTok

### Features
```kotlin
shareToFacebook(imagePath, caption)
shareToInstagram(imagePath, caption)
shareToTikTok(videoPath, caption)
shareMultipleMedia(paths, platform, caption)
openPlatformCamera(platform) // Open native camera
getAvailablePlatforms() // List installed platforms
```

### Deep Link Intents
```kotlin
// Facebook
Intent.ACTION_SEND
Type: image/*
Package: com.facebook.katana / com.facebook.lite

// Instagram
Intent.ACTION_SEND
Extra: "com.instagram.share.ADD_TO_FEED" = true
Type: image/*
Package: com.instagram.android / com.instagram.lite

// TikTok
Intent.ACTION_SEND
Type: video/*
Package: com.zhiliaoapp.musically / com.ss.android.ugc.trill
```

### Usage
```kotlin
val socialManager = SocialSharingManager(context)

// Share to Facebook
socialManager.shareToFacebook(
    "/path/to/image.jpg",
    "Check this out! 📸"
)

// Share to Instagram
socialManager.shareToInstagram(
    "/path/to/image.jpg",
    "Amazing shot! 📸"
)

// Share to TikTok
socialManager.shareToTikTok(
    "/path/to/video.mp4",
    "Viral content! 🎬"
)

// Multiple files
socialManager.shareMultipleMedia(
    listOf("/path/to/img1.jpg", "/path/to/img2.jpg"),
    "facebook",
    "Photo album! 📸"
)

// Check available platforms
val platforms = socialManager.getAvailablePlatforms()
// Returns: [facebook, instagram, tiktok]
```

### Error Handling
- App not installed → Redirect to Play Store
- Share denied → Callback with error message
- No media captured → User notification
- Permission denied → Request permission

---

## Voice Integration in ViewModel

### EnhancedChatViewModel.kt
Complete integration of all features:

```kotlin
// Command parsing flow
User Input
    ↓
VoiceCommandParser (fuzzy matching)
    ↓
Action Type (Camera/Social/Chat)
    ↓
Handle Action
    ↓
Casual Response (Ghupshup Mode)
    ↓
Media Output / Chat Response
```

### Conversation Examples

**Camera Commands**:
- User: "Selfie" 
- Myra: "📸 Selfie captured! Acha photo aya!"
- Action: Capture selfie

**Video Recording**:
- User: "Record video"
- Myra: "🎬 Recording started! Bilkul!"
- Action: Start recording

**Social Sharing**:
- User: "Share to Instagram"
- Myra: "✅ Shared to Instagram! Gram par aa gaya!"
- Action: Share last media to Instagram

**Casual Chat**:
- User: "Kya chal raha hai?"
- Myra: "Haan bhai! Bilkul sab theek-thaak! Tum batao, kya news hai? 😊"
- Action: Casual conversation

---

## Permissions Required

```xml
<!-- Camera -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- Storage for media -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />

<!-- Internet for sharing -->
<uses-permission android:name="android.permission.INTERNET" />
```

### Queries for Social Apps
```xml
<queries>
    <package android:name="com.facebook.katana" />
    <package android:name="com.facebook.lite" />
    <package android:name="com.instagram.android" />
    <package android:name="com.instagram.lite" />
    <package android:name="com.zhiliaoapp.musically" />
    <package android:name="com.ss.android.ugc.trill" />
</queries>
```

---

## Dependencies (Add to build.gradle)

```gradle
// CameraX
implementation 'androidx.camera:camera-core:1.3.0'
implementation 'androidx.camera:camera-camera2:1.3.0'
implementation 'androidx.camera:camera-lifecycle:1.3.0'
implementation 'androidx.camera:camera-view:1.3.0'
implementation 'androidx.camera:camera-extensions:1.3.0'

// All other dependencies already included
```

---

## Testing Commands

### Camera Tests
```
"Take selfie"
"Selfie with timer"
"Front camera"
"Back camera"
"Toggle camera"
"Record video"
"Stop recording"
```

### Social Tests
```
"Share to Facebook"
"Share to Instagram"
"Share to TikTok"
```

### Chat Tests
```
"Kya chal raha hai?"
"Haan bhai, story suno"
"Wah! That's amazing"
```

---

## Error Recovery

| Issue | Solution |
|-------|----------|
| Camera not initializing | Check CAMERA permission granted |
| Social share fails | Verify app is installed, check permissions |
| Command not recognized | Use fuzzy matching, check spelling |
| No media to share | Capture/record before sharing |
| App crash on share | Add try-catch, verify intent safety |

---

## Future Enhancements

- [ ] Multi-language support (Punjabi, Arabic, etc.)
- [ ] Advanced video effects and filters
- [ ] Real-time translation in ghupshup mode
- [ ] Direct streaming to multiple platforms
- [ ] Media editing before sharing
- [ ] Custom captions with templates
- [ ] Scheduled posting
- [ ] Analytics on shared content

