package com.proxyrack.sdk

/**
 * Represents the current status of the proxy connection
 */
enum class ProxyStatus {
    /**
     * Proxy is disconnected and not earning money
     */
    DISCONNECTED,

    /**
     * Proxy is attempting to connect to backend
     */
    CONNECTING,

    /**
     * Proxy is connected and earning money
     */
    CONNECTED,

    /**
     * An error occurred. Auto-reconnect will be attempted
     */
    ERROR
}