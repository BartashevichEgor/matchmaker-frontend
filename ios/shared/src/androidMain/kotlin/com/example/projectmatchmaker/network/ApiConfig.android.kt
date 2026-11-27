package com.example.projectmatchmaker.network

/**
 * Порт backend пробрасывается в loopback эмулятора командой adb reverse.
 */
actual fun platformApiBaseUrl(): String = "http://127.0.0.1:8000"

