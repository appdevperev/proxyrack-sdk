package com.proxyrack.sdk.proxy

/**
 * Factory interface for creating ProxyManager instances
 */
internal interface ProxyManagerProvider {
    fun new(): ProxyManager
}