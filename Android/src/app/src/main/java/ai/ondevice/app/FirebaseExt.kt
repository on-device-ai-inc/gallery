/*
 * Copyright 2025-2026 On Device AI Inc. All rights reserved.
 * Proprietary and confidential.
 */

package ai.ondevice.app

import com.google.firebase.Firebase
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.perf.performance

/**
 * Safely creates a Firebase Performance trace.
 * Returns null if Firebase is not properly configured (e.g., in debug builds).
 */
fun safePerformanceTrace(name: String): Trace? {
    return try {
        Firebase.performance.newTrace(name)
    } catch (e: IllegalArgumentException) {
        // Firebase not configured (invalid API key) - skip tracing
        null
    } catch (e: Exception) {
        // Any other Firebase error - skip tracing
        null
    }
}

/**
 * Extension to safely start a trace (no-op if null).
 */
fun Trace?.safeStart() {
    this?.start()
}

/**
 * Extension to safely stop a trace (no-op if null).
 */
fun Trace?.safeStop() {
    this?.stop()
}

/**
 * Extension to safely add a metric to a trace (no-op if null).
 */
fun Trace?.safePutMetric(name: String, value: Long) {
    this?.putMetric(name, value)
}

/**
 * Extension to safely add an attribute to a trace (no-op if null).
 */
fun Trace?.safePutAttribute(name: String, value: String) {
    this?.putAttribute(name, value)
}
