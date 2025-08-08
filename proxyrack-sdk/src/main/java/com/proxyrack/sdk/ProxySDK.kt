package com.proxyrack.sdk

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Main SDK interface for Proxyrack Mobile Proxy
 *
 * Usage:
 * 1. ProxySDK.initialize(context, "your-client-key")
 * 2. ProxySDK.start()
 * 3. ProxySDK.stop()
 */
object ProxySDK {

    private var context: Context? = null
    private var clientKey: String = ""
    private var isInitialized = false

    private const val TAG = "ProxySDK"

    /**
     * Initialize the SDK with context and client key
     * Must be called before using any other SDK methods
     *
     * @param context Application context (will be stored as applicationContext)
     * @param clientKey Your Proxyrack client key provided by Proxyrack
     */
    fun initialize(context: Context, clientKey: String) {
        this.context = context.applicationContext
        this.clientKey = clientKey
        this.isInitialized = true

        SDKConfig.initialize(clientKey, context.applicationContext)
        Log.d(TAG, "SDK initialized successfully")
    }

    /**
     * Start the proxy service
     * Device will start earning money by sharing internet connection
     *
     * @return true if service started successfully, false if failed or not initialized
     */
    fun start(): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "SDK not initialized. Call initialize() first")
            return false
        }

        if (clientKey.isEmpty()) {
            Log.e(TAG, "Client key is empty. Provide valid client key in initialize()")
            return false
        }

        return try {
            val intent = Intent(context, ProxySDKService::class.java).apply {
                action = ProxySDKService.ACTION_START
                putExtra(ProxySDKService.EXTRA_CLIENT_KEY, clientKey)
            }
            ContextCompat.startForegroundService(context!!, intent)
            Log.d(TAG, "Proxy service start requested")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start proxy service", e)
            false
        }
    }

    /**
     * Stop the proxy service
     * Device will stop earning money
     */
    fun stop() {
        if (!isInitialized) {
            Log.e(TAG, "SDK not initialized")
            return
        }

        try {
            val intent = Intent(context, ProxySDKService::class.java).apply {
                action = ProxySDKService.ACTION_STOP
            }
            context?.startService(intent)
            Log.d(TAG, "Proxy service stop requested")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop proxy service", e)
        }
    }

    /**
     * Check if proxy service is currently running
     *
     * @return true if proxy is running and earning money, false otherwise
     */
    fun isRunning(): Boolean {
        return ProxySDKService.isServiceRunning
    }

    /**
     * Get current connection status
     *
     * @return Current ProxyStatus (DISCONNECTED, CONNECTING, CONNECTED, ERROR)
     */
    fun getStatus(): ProxyStatus {
        return ProxySDKService.currentStatus
    }

    /**
     * Set callback to receive connection status updates
     * Callback will be called on main thread
     *
     * @param callback Function called when status changes
     */
    fun setStatusCallback(callback: (ProxyStatus) -> Unit) {
        ProxySDKService.statusCallback = callback
    }

    /**
     * Set callback to receive log messages from proxy
     * Useful for debugging. Callback will be called on main thread
     *
     * @param callback Function called when log message received
     */
    fun setLogCallback(callback: (String) -> Unit) {
        ProxySDKService.logCallback = callback
    }

    /**
     * Clear all callbacks
     * Call this to prevent memory leaks when no longer needed
     */
    fun clearCallbacks() {
        ProxySDKService.statusCallback = null
        ProxySDKService.logCallback = null
    }

    /**
     * Get the device ID being used for this proxy
     *
     * @return Device ID string or empty if not initialized
     */
    fun getDeviceId(): String {
        return if (isInitialized) SDKConfig.deviceID else ""
    }
}