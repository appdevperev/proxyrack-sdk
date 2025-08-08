package com.proxyrack.sdk

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.proxyrack.sdk.proxy.ProxyManager
import com.proxyrack.sdk.proxy.ProxyManagerProviderImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Background service that manages the proxy connection
 * Runs as foreground service to ensure Android doesn't kill it
 */
class ProxySDKService : Service() {

    companion object {
        // Service actions
        const val ACTION_START = "com.proxyrack.sdk.START"
        const val ACTION_STOP = "com.proxyrack.sdk.STOP"

        // Intent extras
        const val EXTRA_CLIENT_KEY = "clientKey"

        // Service state
        var isServiceRunning = false
            private set

        var currentStatus = ProxyStatus.DISCONNECTED
            private set

        // Callbacks (called on main thread)
        var statusCallback: ((ProxyStatus) -> Unit)? = null
        var logCallback: ((String) -> Unit)? = null

        // Notification
        private const val CHANNEL_ID = "ProxySDKServiceChannel"
        private const val NOTIFICATION_ID = 1001
    }

    private val TAG = "ProxySDKService"
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var proxyManager: ProxyManager? = null
    private val proxyManagerProvider = ProxyManagerProviderImpl()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupProxyManager()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_START -> {
                val clientKey = intent.getStringExtra(EXTRA_CLIENT_KEY) ?: ""
                startProxy(clientKey)
            }
            ACTION_STOP -> {
                stopProxy()
            }
        }

        return START_NOT_STICKY
    }

    private fun startProxy(clientKey: String) {
        if (isServiceRunning) {
            Log.d(TAG, "Proxy already running")
            return
        }

        Log.d(TAG, "Starting proxy with client key: ${clientKey.take(6)}...")

        // Start foreground service
        val notification = createNotification("Starting proxy...")
        startForeground(NOTIFICATION_ID, notification)

        isServiceRunning = true
        updateStatus(ProxyStatus.CONNECTING)

        // Update configuration with new client key
        SDKConfig.initialize(clientKey, this)

        registerCallbacks()
        connect()
    }

    private fun stopProxy() {
        Log.d(TAG, "Stopping proxy service")

        proxyManager?.unregisterOnDisconnectCallback()
        proxyManager?.disconnect()

        isServiceRunning = false
        updateStatus(ProxyStatus.DISCONNECTED)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun setupProxyManager() {
        try {
            proxyManager = proxyManagerProvider.new()
            Log.d(TAG, "ProxyManager created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create ProxyManager", e)
            updateStatus(ProxyStatus.ERROR)
        }
    }

    private fun registerCallbacks() {
        proxyManager?.registerOnLogEntryCallback { msg ->
            Log.d(TAG, "Proxy: $msg")
            mainHandler.post {
                logCallback?.invoke(msg)
            }
        }

        proxyManager?.registerOnConnectCallback {
            Log.d(TAG, "Proxy connected successfully")
            updateStatus(ProxyStatus.CONNECTED)
            updateNotification("Connected - Earning money!")
        }

        proxyManager?.registerOnDisconnectCallback {
            Log.d(TAG, "Proxy disconnected unexpectedly")

            // Only attempt reconnect if service is still supposed to be running
            if (isServiceRunning && currentStatus != ProxyStatus.DISCONNECTED) {
                updateStatus(ProxyStatus.ERROR)
                attemptReconnect()
            }
        }
    }

    private fun connect() {
        serviceScope.launch {
            try {
                Log.d(TAG, "Connecting to ${SDKConfig.SERVER_IP}:${SDKConfig.SERVER_PORT}")
                Log.d(TAG, "Device ID: ${SDKConfig.deviceID.take(8)}...")

                proxyManager?.connect(
                    SDKConfig.SERVER_IP,
                    SDKConfig.SERVER_PORT,
                    SDKConfig.deviceID,
                    SDKConfig.username,
                    true // sharingBandwidth = true
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect to proxy backend", e)
                updateStatus(ProxyStatus.ERROR)
                if (isServiceRunning) {
                    attemptReconnect()
                }
            }
        }
    }

    private fun attemptReconnect() {
        serviceScope.launch {
            Log.d(TAG, "Will attempt reconnect in 5 seconds...")
            mainHandler.post {
                logCallback?.invoke("Connection lost. Reconnecting in 5 seconds...")
            }

            delay(5000L)

            // Only reconnect if service is still running and in error state
            if (isServiceRunning && currentStatus == ProxyStatus.ERROR) {
                Log.d(TAG, "Attempting to reconnect...")
                updateStatus(ProxyStatus.CONNECTING)
                connect()
            }
        }
    }

    private fun updateStatus(status: ProxyStatus) {
        currentStatus = status
        Log.d(TAG, "Status updated to: $status")

        mainHandler.post {
            statusCallback?.invoke(status)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Proxy SDK Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mobile proxy service running in background"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mobile Proxy")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")

        proxyManager?.unregisterOnDisconnectCallback()
        proxyManager?.disconnect()

        isServiceRunning = false
        updateStatus(ProxyStatus.DISCONNECTED)
    }
}