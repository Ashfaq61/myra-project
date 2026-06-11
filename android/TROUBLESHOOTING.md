## Troubleshooting Guide

### Common Issues & Solutions

#### 1. Build Failures

**Error**: `gradlew: command not found`
```bash
# Solution: Make scripts executable
chmod +x gradlew
chmod +x gradlew.bat
```

**Error**: `Java version mismatch`
```bash
# Solution: Ensure Java 11+
java -version

# Set JAVA_HOME if needed
export JAVA_HOME=/path/to/java11
```

**Error**: `SDK not found`
```bash
# Solution: Set ANDROID_HOME
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

#### 2. Voice Recognition Issues

**Problem**: Voice activation not working
```
Solution Checklist:
✓ Verify RECORD_AUDIO permission is granted
✓ Check device has speech recognition service
✓ Ensure microphone is functional
✓ Check logs: adb logcat | grep "Myra"
✓ Verify internet connection (for cloud speech)
```

**Problem**: Wake word not detected
```
Solutions:
1. Check Timber logs for recognition errors
2. Verify wake word is "myra" (lowercase)
3. Try speaking louder/clearer
4. Increase microphone sensitivity
5. Check device language settings
```

#### 3. Call Management Issues

**Problem**: Call management service not working
```
Requirements:
✓ Android 10+ (API 29+)
✓ ANSWER_PHONE_CALLS permission
✓ Device supports Telecom framework
✓ Telecom framework is enabled

Debug:
- Check: adb logcat | grep "CallManagement"
- Verify app is NOT default dialer
- Check if other apps intercept calls
```

**Problem**: Auto-accept not working
```
Check:
1. Enable "Auto-accept calls" in app preferences
2. Verify permission is granted
3. Check incoming call triggers event
4. Review Timber logs for errors
```

#### 4. WhatsApp Automation Issues

**Problem**: Accessibility service not enabled
```
Critical: MUST enable manually
Settings → Accessibility → Myra AI Assistant → ON

After enabling:
1. Grant all requested permissions
2. Restart app
3. Check logs for accessibility events
```

**Problem**: Messages not sending
```
Check:
1. WhatsApp is installed and updated
2. Accessibility service is enabled
3. Phone number format includes country code
4. WhatsApp chat is open before sending
5. Check Timber logs for xpath errors
```

**Problem**: WhatsApp deep links not working
```
Solutions:
1. Verify phone number format: +country_code + number
2. Ensure WhatsApp is installed
3. Check if deep links are blocked by system
4. Try with web.whatsapp.com instead (if applicable)

Test link format:
Voice: https://wa.me/1234567890
Video: vnd.android.cursor.item/vnd.com.whatsapp.voip.call/1234567890
```

#### 5. Device Control Issues

**Problem**: Flashlight not working
```
Check:
1. Device has camera with flash
2. CAMERA and FLASHLIGHT permissions granted
3. Camera not in use by other apps
4. Device not in power save mode
```

**Problem**: WiFi/Bluetooth control not working
```
Check:
1. Permissions are granted (CHANGE_WIFI_STATE, BLUETOOTH_ADMIN)
2. Device is not in airplane mode
3. No other apps blocking control
4. Device supports the operation
```

#### 6. Gemini API Issues

**Problem**: "Invalid API Key" error
```
Solution:
1. Verify API key from Google Cloud Console
2. Ensure key is not expired
3. Check key has proper permissions
4. Add key via SecurePreferencesManager:
   val prefs = SecurePreferencesManager(context)
   prefs.setGeminiApiKey("YOUR_KEY")
```

**Problem**: API calls timeout
```
Solutions:
1. Check internet connection
2. Increase timeout in HttpClientFactory:
   connectTimeout(60, TimeUnit.SECONDS)
3. Check Gemini API status
4. Verify proxy settings
```

**Problem**: Empty responses from API
```
Check:
1. Query is not empty
2. API quota not exceeded
3. Model name is correct (gemini-2.5-flash)
4. Request format is valid
5. Review GeminiApiService.extractResponseData()
```

#### 7. Permission Issues

**Problem**: "Permission denied" at runtime
```
Solution:
1. Request permissions in MainActivity
2. Handle permission denial gracefully
3. Show permission rationale
4. Test on Android 10+ (runtime permissions)

Test:
adb shell pm grant com.myra.ai.assistant android.permission.RECORD_AUDIO
```

**Problem**: Accessibility service permission error
```
Solution:
1. Enable in Settings → Accessibility
2. Grant all requested permissions
3. Restart app
4. Verify service is bound
```

#### 8. Performance Issues

**Problem**: App crashes on older devices
```
Solutions:
1. Check min SDK (24 = Android 7.0)
2. Reduce app size with ProGuard
3. Test on emulator with Android 7.0
4. Profile with Android Studio Profiler
```

**Problem**: High battery drain
```
Optimization:
1. Reduce voice listening frequency
2. Release resources in onDestroy()
3. Use PowerManager.WakeLock carefully
4. Profile with Battery Historian
```

**Problem**: Memory leaks
```
Debug:
1. Use Android Studio Profiler
2. Check for circular references
3. Clear listeners in onDestroy()
4. Test with LeakCanary
```

#### 9. Network Issues

**Problem**: SSL/Certificate errors
```
Solution:
1. Ensure HTTPS is used
2. Check system date/time
3. Update security certificates
4. Test with: curl -v https://api.gemini.ai
```

**Problem**: Proxy/Firewall blocking
```
Check:
1. Corporate proxy settings
2. VPN compatibility
3. Firewall rules
4. Network timeout settings
```

#### 10. Testing Issues

**Problem**: Tests not running
```
Solutions:
1. Ensure device/emulator connected:
   adb devices
2. Grant test permissions:
   adb shell pm grant com.myra.ai.assistant ...
3. Run specific test:
   ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.myra.ai.assistant.MyTest
```

### Debug Scripts

Use provided debug tools:
```bash
# Interactive debug menu
chmod +x debug.sh
./debug.sh

# Testing suite
chmod +x test.sh
./test.sh

# Quick start menu
chmod +x quickstart.sh
./quickstart.sh
```

### Logging

**View Myra logs only**:
```bash
adb logcat | grep "Myra"
```

**View specific component**:
```bash
adb logcat | grep "CallManagement"
adb logcat | grep "VoiceActivation"
adb logcat | grep "GeminiApi"
```

**Export logs**:
```bash
adb logcat -d > myra_logs.txt
```

**Set log level**:
```bash
adb shell setprop log.tag.Myra VERBOSE
```

### Android Studio Debugging

1. **Breakpoints**: Click line number to add breakpoint
2. **Debugger**: Run → Debug 'app'
3. **Logcat**: View → Tool Windows → Logcat
4. **Device Explorer**: View → Tool Windows → Device File Explorer
5. **Profiler**: View → Tool Windows → Profiler

### Emergency Recovery

**Reset app data**:
```bash
adb shell pm clear com.myra.ai.assistant
```

**Force stop**:
```bash
adb shell am force-stop com.myra.ai.assistant
```

**Reinstall**:
```bash
adb uninstall com.myra.ai.assistant
./gradlew installDebug
```

### Getting Help

1. **Check logs**: Most issues visible in logcat
2. **Review docs**: README.md, FEATURES.md, ARCHITECTURE.md
3. **GitHub Issues**: Create detailed issue with logs
4. **Stack Overflow**: Tag with `android`, `kotlin`, `gemini`

### Performance Profiling

```bash
# CPU profiling
./gradlew assembleDebug --profile

# Memory profiling
adb shell dumpsys meminfo com.myra.ai.assistant

# Battery profiling
adb shell dumpsys batterystats
```

