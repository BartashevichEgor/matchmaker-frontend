package com.example.projectmatchmaker.network

/**
 * Локальный backend пробрасывается командой `adb reverse tcp:8000 tcp:8000`.
 * Такой адрес одинаково работает во встроенном эмуляторе Android Studio и на
 * подключённом по USB Android-устройстве.
 */
actual fun platformApiBaseUrl(): String = "http://127.0.0.1:8000"

