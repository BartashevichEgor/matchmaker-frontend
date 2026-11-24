package com.example.projectmatchmaker.network

/**
 * Android-эмулятор видит loopback компьютера через адрес 10.0.2.2.
 */
// Android Emulator routes 10.0.2.2 to the host machine's loopback interface.
actual fun platformApiBaseUrl(): String = "http://10.0.2.2:8000"

