/*
 * Copyright 2025-2026 On Device AI Inc. All rights reserved.
 * Proprietary and confidential.
 */

package ai.ondevice.app.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.security.MessageDigest

/**
 * Runtime integrity checks for anti-piracy and tamper detection.
 * All checks are log-only by default (no hard blocks).
 *
 * Call [runAll] from Application.onCreate() or MainActivity.onCreate().
 */
object IntegrityChecker {
    private const val TAG = "IntegrityChecker"

    /**
     * SHA-256 of the release signing certificate.
     * To compute: keytool -list -v -keystore ondevice-ai-release.keystore | grep SHA256
     * Then format as uppercase hex without colons.
     *
     * TODO: Replace with actual release cert hash before shipping.
     */
    private const val EXPECTED_CERT_HASH = "PLACEHOLDER_REPLACE_WITH_ACTUAL_CERT_HASH"

    fun runAll(context: Context) {
        checkSignature(context)
        checkInstallerSource(context)
        checkDebuggable(context)
        checkDebuggerAttached()
        checkEmulator()
        checkRoot()
        checkFridaXposed()
    }

    /**
     * Verify that the APK was signed with our release certificate.
     * Detects repackaged APKs signed with a different key.
     */
    private fun checkSignature(context: Context) {
        try {
            val signingInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                ).signingInfo
            } else {
                null
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && signingInfo != null) {
                if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo.signingCertificateHistory
                }
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                ).signatures
            }

            if (signatures.isNullOrEmpty()) {
                Log.e(TAG, "INTEGRITY: No signatures found on APK")
                return
            }

            val certBytes = signatures[0].toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(certBytes)
            val hexHash = digest.joinToString("") { "%02X".format(it) }

            if (EXPECTED_CERT_HASH != "PLACEHOLDER_REPLACE_WITH_ACTUAL_CERT_HASH" &&
                hexHash != EXPECTED_CERT_HASH
            ) {
                Log.e(TAG, "INTEGRITY: Signature mismatch. Expected=$EXPECTED_CERT_HASH Got=$hexHash")
            }
        } catch (e: Exception) {
            Log.e(TAG, "INTEGRITY: Signature check failed", e)
        }
    }

    /**
     * Log the installer source (Play Store, sideload, etc).
     */
    private fun checkInstallerSource(context: Context) {
        try {
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }
            Log.i(TAG, "INTEGRITY: Installer source: ${installer ?: "sideload/unknown"}")
        } catch (e: Exception) {
            Log.i(TAG, "INTEGRITY: Could not determine installer source")
        }
    }

    /**
     * Check if the APK is marked as debuggable.
     * Release builds should never have this flag.
     */
    private fun checkDebuggable(context: Context) {
        val isDebuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            Log.e(TAG, "INTEGRITY: App is debuggable (should not be in release)")
        }
    }

    /**
     * Check if a debugger is currently attached.
     */
    private fun checkDebuggerAttached() {
        if (android.os.Debug.isDebuggerConnected()) {
            Log.e(TAG, "INTEGRITY: Debugger is connected")
        }
    }

    /**
     * Detect common emulator signals.
     */
    private fun checkEmulator() {
        val suspiciousProps = listOf(
            Build.FINGERPRINT.contains("generic", ignoreCase = true),
            Build.MODEL.contains("Emulator", ignoreCase = true),
            Build.MODEL.contains("Android SDK", ignoreCase = true),
            Build.MANUFACTURER.contains("Genymotion", ignoreCase = true),
            Build.HARDWARE.contains("goldfish", ignoreCase = true),
            Build.HARDWARE.contains("ranchu", ignoreCase = true),
            Build.PRODUCT.contains("sdk", ignoreCase = true),
            Build.PRODUCT.contains("vbox", ignoreCase = true),
        )
        if (suspiciousProps.any { it }) {
            Log.w(TAG, "INTEGRITY: Emulator detected (fingerprint=${Build.FINGERPRINT})")
        }
    }

    /**
     * Detect rooted devices by checking for su binary and root management apps.
     */
    private fun checkRoot() {
        val suPaths = arrayOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/data/local/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/su/bin/su", "/su/bin",
        )
        val rootPackages = arrayOf(
            "com.topjohnwu.magisk",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
        )

        val hasSu = suPaths.any { java.io.File(it).exists() }
        val hasTestKeys = Build.TAGS?.contains("test-keys") == true
        val hasRootApps = rootPackages.any { pkg ->
            try {
                Runtime.getRuntime().exec(arrayOf("pm", "path", pkg)).inputStream.bufferedReader().readLine() != null
            } catch (_: Exception) { false }
        }

        if (hasSu || hasTestKeys || hasRootApps) {
            Log.w(TAG, "INTEGRITY: Root detected (su=$hasSu, testKeys=$hasTestKeys, rootApps=$hasRootApps)")
        }
    }

    /**
     * Detect Frida and Xposed hooking frameworks.
     * Checks: Frida default port, /proc/self/maps for frida gadget,
     * Xposed installer package, and Xposed bridge in loaded libraries.
     */
    private fun checkFridaXposed() {
        // Check Frida default port (27042)
        val fridaPort = try {
            java.net.Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress("127.0.0.1", 27042), 100)
                true
            }
        } catch (_: Exception) { false }

        // Check /proc/self/maps for frida-agent
        val hasFridaInMaps = try {
            java.io.File("/proc/self/maps").readLines().any {
                it.contains("frida", ignoreCase = true) || it.contains("gadget", ignoreCase = true)
            }
        } catch (_: Exception) { false }

        // Check for Xposed installer
        val hasXposed = try {
            Runtime.getRuntime().exec(arrayOf("pm", "path", "de.robv.android.xposed.installer")).inputStream.bufferedReader().readLine() != null
        } catch (_: Exception) { false }

        // Check for Xposed bridge in loaded native libraries
        val hasXposedBridge = try {
            java.io.File("/proc/self/maps").readLines().any {
                it.contains("XposedBridge", ignoreCase = true)
            }
        } catch (_: Exception) { false }

        if (fridaPort || hasFridaInMaps || hasXposed || hasXposedBridge) {
            Log.e(TAG, "INTEGRITY: Hooking framework detected (frida=$fridaPort, fridaMaps=$hasFridaInMaps, xposed=$hasXposed, xposedBridge=$hasXposedBridge)")
        }
    }
}
