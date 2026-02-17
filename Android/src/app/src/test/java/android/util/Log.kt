/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.util

/**
 * Mock implementation of android.util.Log for unit tests.
 *
 * This allows compression classes to use Log.d(), Log.e(), etc. in unit tests
 * without requiring the Android framework or Robolectric.
 *
 * All log calls are redirected to println() for test output visibility.
 */
@Suppress("unused")
object Log {
    @JvmStatic
    fun d(tag: String, msg: String): Int {
        println("DEBUG: $tag: $msg")
        return 0
    }

    @JvmStatic
    fun i(tag: String, msg: String): Int {
        println("INFO: $tag: $msg")
        return 0
    }

    @JvmStatic
    fun w(tag: String, msg: String): Int {
        println("WARN: $tag: $msg")
        return 0
    }

    @JvmStatic
    fun e(tag: String, msg: String): Int {
        println("ERROR: $tag: $msg")
        return 0
    }

    @JvmStatic
    fun e(tag: String, msg: String, tr: Throwable): Int {
        println("ERROR: $tag: $msg")
        tr.printStackTrace()
        return 0
    }

    @JvmStatic
    fun v(tag: String, msg: String): Int {
        println("VERBOSE: $tag: $msg")
        return 0
    }

    @JvmStatic
    fun wtf(tag: String, msg: String): Int {
        println("WTF: $tag: $msg")
        return 0
    }
}
