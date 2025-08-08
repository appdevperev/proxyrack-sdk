package com.proxyrack.sdk

import android.content.Context
import android.provider.Settings
import java.util.UUID

/**
 * Internal configuration management for the SDK
 * NOT part of public API - for internal use only
 */
internal object SDKConfig {
    // Proxyrack backend configuration
    const val SERVER_IP = "mobile-socket.culturegps.com"
    const val SERVER_PORT = 443L

    // User configuration
    var clientKey: String = ""
        private set

    var deviceID: String = ""
        private set

    var username: String = ""
        private set

    /**
     * Initialize configuration with client key and context
     */
    fun initialize(clientKey: String, context: Context) {
        this.clientKey = clientKey
        this.deviceID = generateDeviceID(context)
        this.username = clientKey // Use clientKey as username for simplicity
    }

    /**
     * Generate unique device ID for this device
     */
    private fun generateDeviceID(context: Context): String {
        return try {
            // Try to get Android ID first (persists across app installs)
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            // Check for invalid Android ID
            if (androidId != null && androidId != "9774d56d682e549c") {
                androidId
            } else {
                // Fallback to random UUID (will change on reinstall)
                UUID.randomUUID().toString()
            }
        } catch (e: Exception) {
            // Final fallback
            UUID.randomUUID().toString()
        }
    }
}