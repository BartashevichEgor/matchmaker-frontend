package com.example.projectmatchmaker.network

/**
 * Android-эмулятор видит backend на компьютере через специальный адрес
 * 10.0.2.2. Для физического телефона нужен адрес компьютера в локальной сети.
 */
actual fun platformApiBaseUrl(): String = "http://10.0.2.2:8000"

