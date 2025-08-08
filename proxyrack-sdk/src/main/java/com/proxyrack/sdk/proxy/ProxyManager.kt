package com.proxyrack.sdk.proxy

/**
 * Interface to the underlying proxy implementation
 * Abstracts the native golang proxy library
 */
internal interface ProxyManager {
    fun connect(host: String, port: Long, deviceID: String, username: String, sharingBandwidth: Boolean)
    fun disconnect()
    fun registerOnLogEntryCallback(callback: (String) -> Unit)
    fun registerOnConnectCallback(callback: () -> Unit)
    fun registerOnDisconnectCallback(callback: () -> Unit)
    fun unregisterOnDisconnectCallback()
}