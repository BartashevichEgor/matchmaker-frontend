package com.example.projectmatchmaker.network

/**
 * Для локальной разработки используем ADB reverse (adb reverse tcp:8000 tcp:8000),
 * чтобы эмулятор обращался к backend на компьютере через loopback.
 */
actual fun platformApiBaseUrl(): String = "http://127.0.0.1:8000"

