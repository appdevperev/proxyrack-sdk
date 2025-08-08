# 📱 Proxyrack Android SDK

Transform any Android app into a mobile proxy earning platform with just a few lines of code.

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

## 🚀 Features

- **🔌 Simple Integration**: 3-line setup - initialize, start, stop
- **📱 Real-time Callbacks**: Get status updates and log messages
- **🔄 Automatic Reconnection**: Handles network interruptions gracefully
- **⚡ Foreground Service**: Runs reliably in background
- **🎨 Clean Architecture**: No UI dependencies or conflicts
- **🏭 Production Ready**: Built from proven, working proxy technology
- **💰 Monetization**: Turn your app users into earning opportunities

## 📦 Installation

### Method 1: AAR File (Recommended)

1. **Download** the latest `proxyrack-sdk-release.aar` from releases
2. **Copy** the AAR file to your app's `libs/` folder
3. **Add** to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation files('libs/proxyrack-sdk-release.aar')
}
```

### Method 2: Maven Central (Coming Soon)

```kotlin
dependencies {
    implementation 'com.proxyrack:android-sdk:1.0.0'
}
```

### Method 3: JitPack

```kotlin
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.proxyrack:android-sdk:1.0.0'
}
```

## 🔧 Setup

### 1. Add Required Permissions

Add these permissions to your app's `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### 2. Basic Integration

```kotlin
import com.proxyrack.sdk.ProxySDK
import com.proxyrack.sdk.ProxyStatus

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize SDK with your client key
        ProxySDK.initialize(this, "your-client-key-from-proxyrack")
        
        // 2. Set up status callback (optional)
        ProxySDK.setStatusCallback { status ->
            when (status) {
                ProxyStatus.CONNECTED -> {
                    showToast("✅ Connected - Earning money!")
                }
                ProxyStatus.DISCONNECTED -> {
                    showToast("❌ Disconnected")
                }
                ProxyStatus.CONNECTING -> {
                    showToast("🔄 Connecting...")
                }
                ProxyStatus.ERROR -> {
                    showToast("⚠️ Error - Will auto-retry")
                }
            }
        }
        
        // 3. Start proxy
        findViewById<Button>(R.id.startButton).setOnClickListener {
            if (ProxySDK.start()) {
                Log.d("Proxy", "Proxy start requested")
            } else {
                Log.e("Proxy", "Failed to start proxy")
            }
        }
        
        // 4. Stop proxy
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            ProxySDK.stop()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        ProxySDK.clearCallbacks() // Prevent memory leaks
    }
}
```

## 📖 API Reference

### Core Methods

#### `ProxySDK.initialize(context, clientKey)`
Initializes the SDK with your Proxyrack client key.

**Parameters:**
- `context: Context` - Application context (automatically uses applicationContext)
- `clientKey: String` - Your unique client key from Proxyrack dashboard

**Example:**
```kotlin
ProxySDK.initialize(this, "pk_live_abcd1234efgh5678")
```

#### `ProxySDK.start(): Boolean`
Starts the proxy service. Returns `true` if started successfully.

**Returns:**
- `Boolean` - `true` if service started, `false` if failed or not initialized

**Example:**
```kotlin
val success = ProxySDK.start()
if (success) {
    println("Proxy started successfully")
} else {
    println("Failed to start proxy")
}
```

#### `ProxySDK.stop()`
Stops the proxy service immediately.

**Example:**
```kotlin
ProxySDK.stop()
```

#### `ProxySDK.isRunning(): Boolean`
Checks if the proxy service is currently running.

**Returns:**
- `Boolean` - `true` if proxy is active and earning

**Example:**
```kotlin
if (ProxySDK.isRunning()) {
    statusText.text = "Proxy is active"
}
```

#### `ProxySDK.getStatus(): ProxyStatus`
Gets the current connection status.

**Returns:**
- `ProxyStatus` - Current status enum

**Example:**
```kotlin
when (ProxySDK.getStatus()) {
    ProxyStatus.CONNECTED -> showGreenIndicator()
    ProxyStatus.CONNECTING -> showYellowIndicator()
    ProxyStatus.DISCONNECTED -> showRedIndicator()
    ProxyStatus.ERROR -> showErrorIndicator()
}
```

### Callback Methods

#### `ProxySDK.setStatusCallback(callback)`
Sets a callback for real-time status updates.

**Parameters:**
- `callback: (ProxyStatus) -> Unit` - Function called when status changes

**Example:**
```kotlin
ProxySDK.setStatusCallback { status ->
    runOnUiThread {
        updateUIStatus(status)
    }
}
```

#### `ProxySDK.setLogCallback(callback)`
Sets a callback for debug log messages.

**Parameters:**
- `callback: (String) -> Unit` - Function called for each log message

**Example:**
```kotlin
ProxySDK.setLogCallback { message ->
    Log.d("ProxySDK", "Debug: $message")
    logTextView.append("$message\n")
}
```

#### `ProxySDK.clearCallbacks()`
Clears all callbacks to prevent memory leaks.

**Example:**
```kotlin
override fun onDestroy() {
    super.onDestroy()
    ProxySDK.clearCallbacks()
}
```

### Utility Methods

#### `ProxySDK.getDeviceId(): String`
Gets the unique device ID used for this proxy.

**Returns:**
- `String` - Unique device identifier

**Example:**
```kotlin
val deviceId = ProxySDK.getDeviceId()
deviceIdText.text = "Device: ${deviceId.take(12)}..."
```

## 🔄 ProxyStatus Enum

| Status | Description |
|--------|-------------|
| `DISCONNECTED` | Proxy is not connected, not earning money |
| `CONNECTING` | Attempting to connect to backend |
| `CONNECTED` | Connected and actively earning money |
| `ERROR` | Error occurred, auto-retry in progress |

## 🎨 Jetpack Compose Integration

```kotlin
@Composable
fun ProxyControlScreen() {
    var status by remember { mutableStateOf(ProxySDK.getStatus()) }
    var logs by remember { mutableStateOf(listOf<String>()) }
    
    // Set up callbacks
    LaunchedEffect(Unit) {
        ProxySDK.setStatusCallback { newStatus ->
            status = newStatus
        }
        
        ProxySDK.setLogCallback { message ->
            logs = logs + message
        }
    }
    
    Column(modifier = Modifier.padding(16.dp)) {
        // Status display
        Text(
            text = "Status: ${status.name}",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // Control buttons
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { ProxySDK.start() },
                enabled = status == ProxyStatus.DISCONNECTED
            ) {
                Text("Start Proxy")
            }
            
            Button(
                onClick = { ProxySDK.stop() },
                enabled = status != ProxyStatus.DISCONNECTED
            ) {
                Text("Stop Proxy")
            }
        }
        
        // Logs
        LazyColumn {
            items(logs) { log ->
                Text(
                    text = log,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}
```

## 🛡️ Background Execution

The SDK automatically handles:

- **Foreground Service**: Persistent notification ensures Android doesn't kill the service
- **Auto-Reconnection**: Handles network drops and reconnects automatically
- **Battery Optimization**: Works with Android's battery optimization features
- **Permissions**: Requests necessary permissions automatically

The service shows a notification like:
```
📱 Mobile Proxy
🔗 Connected - Earning money!
```

## 🔧 Advanced Configuration

### Custom Error Handling

```kotlin
ProxySDK.setStatusCallback { status ->
    when (status) {
        ProxyStatus.ERROR -> {
            // Log error details
            analytics.logEvent("proxy_error")
            
            // Show user-friendly message
            showSnackbar("Connection issue - retrying automatically")
            
            // Optional: Implement custom retry logic
            Handler(Looper.getMainLooper()).postDelayed({
                if (ProxySDK.getStatus() == ProxyStatus.ERROR) {
                    ProxySDK.stop()
                    ProxySDK.start()
                }
            }, 10000) // Retry after 10 seconds
        }
    }
}
```

### Integration with Analytics

```kotlin
ProxySDK.setStatusCallback { status ->
    // Track proxy usage
    Firebase.analytics.logEvent("proxy_status_changed") {
        param("status", status.name)
        param("device_id", ProxySDK.getDeviceId())
    }
}

ProxySDK.setLogCallback { message ->
    // Log important events
    if (message.contains("connected", ignoreCase = true)) {
        Firebase.analytics.logEvent("proxy_connected")
    }
}
```

### User Permission Flow

```kotlin
private fun startProxyWithPermissions() {
    // Check notification permission (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                this, 
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermission()
            return
        }
    }
    
    // Start proxy
    if (ProxySDK.start()) {
        showToast("Proxy started - You're earning money!")
    } else {
        showToast("Failed to start proxy")
    }
}
```

## 📱 Sample Apps

### Basic Integration
See `/sample-app/` for a complete working example with:
- Material Design 3 UI
- Real-time status updates
- Log display
- Start/stop functionality

### Advanced Features
See `/sample-advanced/` for examples including:
- User onboarding flow
- Earnings tracking
- Settings management
- Analytics integration

## 🚫 Limitations & Requirements

### Requirements
- **Android API 21+** (Android 5.0 Lollipop)
- **Internet permission** - Required for proxy functionality
- **Foreground service permission** - Required for background operation
- **Valid client key** - Obtain from [Proxyrack Dashboard](https://dashboard.proxyrack.com)

### Limitations
- **Mobile networks only** - Works best on cellular data (3G/4G/5G)
- **No IP rotation** - Uses device's current mobile IP address
- **Foreground notification** - Required by Android for background services
- **Data usage** - Users should be aware of potential data consumption

### Device Compatibility
- ✅ **Smartphones & Tablets** - Full support
- ✅ **Android TV** - Limited support (no mobile network)
- ❌ **Wear OS** - Not supported
- ❌ **Android Auto** - Not supported

## 🐛 Troubleshooting

### Common Issues

#### "Failed to start proxy service"
**Possible causes:**
- No internet connection
- Invalid client key
- Missing permissions

**Solutions:**
```kotlin
// Check internet connectivity
private fun isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}

// Verify client key format
private fun isValidClientKey(key: String): Boolean {
    return key.isNotEmpty() && key.startsWith("pk_")
}
```

#### "Service keeps disconnecting"
**Possible causes:**
- Weak mobile signal
- Battery optimization enabled
- Background app restrictions

**Solutions:**
```kotlin
// Request battery optimization exemption
@SuppressLint("BatteryLife")
private fun requestBatteryOptimizationExemption() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }
}
```

#### "No log messages appearing"
**Possible causes:**
- Log callback not set before starting
- Callback not called on main thread

**Solution:**
```kotlin
// Ensure callback is set before starting
ProxySDK.setLogCallback { message ->
    runOnUiThread {
        // Update UI safely
        logTextView.append("$message\n")
    }
}
ProxySDK.start()
```

### Debug Mode

Enable detailed logging for debugging:

```kotlin
// Add to your Application class or MainActivity
if (BuildConfig.DEBUG) {
    ProxySDK.setLogCallback { message ->
        Log.d("ProxySDK_Debug", message)
    }
}
```

### Testing Checklist

- [ ] SDK initializes without errors
- [ ] Start/stop buttons work correctly
- [ ] Status updates appear in real-time
- [ ] Service survives app backgrounding
- [ ] Notification appears when proxy is active
- [ ] Device ID remains consistent across restarts
- [ ] No memory leaks (check with LeakCanary)

## 💰 Business Model

### How It Works
1. **Users download your app** with integrated Proxyrack SDK
2. **Users opt-in to earn money** by sharing their mobile internet
3. **SDK connects to Proxyrack network** seamlessly
4. **Internet traffic flows through user devices**
5. **Users earn money, you earn revenue share**

### Revenue Sharing
- **Users earn**: $0.20 - $1.00 per GB shared (varies by location)
- **Developers earn**: Revenue share based on usage volume
- **Payments**: Monthly payments via PayPal, bank transfer, or crypto

### Best Practices
- **Transparent consent**: Clearly explain to users how the proxy works
- **Battery awareness**: Inform users about potential battery usage
- **Data disclosure**: Explain data usage implications
- **Easy opt-out**: Provide simple way to disable proxy

## 🔒 Privacy & Security

### Data Handling
- **No personal data collected** by the SDK
- **Device ID is anonymized** and only used for network management
- **No access to user files** or personal information
- **HTTPS traffic remains encrypted** end-to-end

### Security Features
- **Secure backend communication** with TLS encryption
- **Authenticated connections** using client keys
- **Rate limiting** to prevent abuse
- **Network isolation** from app data

### Compliance
- **GDPR compliant** - No personal data processing
- **CCPA compliant** - No personal information sale
- **Mobile privacy standards** - Follows Android privacy guidelines
---

<div align="center">

**Ready to start earning?** 🚀

[Get Your Client Key](https://dashboard.proxyrack.com) • [View Sample App](./sample-app/) • [Join Discord](https://discord.gg/proxyrack)

Made with ❤️ by the Proxyrack team

</div>