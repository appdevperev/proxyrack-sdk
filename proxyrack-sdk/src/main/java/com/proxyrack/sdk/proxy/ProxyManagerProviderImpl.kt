package com.proxyrack.sdk.proxy

import android.os.Build
import android.os.Process
import com.proxyrack.proxylib.android.Android.newManager

/**
 * Creates ProxyManager instances that wrap the native golang library
 */
internal class ProxyManagerProviderImpl: ProxyManagerProvider {

    private val pid = Process.myPid().toLong()
    private val androidApiVersion = Build.VERSION.SDK_INT.toString()
    private val cpuArch = getCPUArchitecture()
    private val version = "55"

    override fun new(): ProxyManager {
        val nativeManager = newManager(pid, version, androidApiVersion, cpuArch)
        return ProxyManagerAdapter(nativeManager)
    }

    private fun getCPUArchitecture(): String {
        val supportedABIs = Build.SUPPORTED_ABIS

        if (supportedABIs.isNotEmpty()) {
            val abi = supportedABIs[0]
            return when {
                abi.startsWith("arm64") -> "arm64"
                abi.startsWith("armeabi") -> "armeabi"
                abi.startsWith("x86_64") -> "x86_64"
                abi.startsWith("x86") -> "x86"
                else -> "unknown"
            }
        }
        return "unknown"
    }
}