package com.example.projectmatchmaker.network

/**
 * AuthApi and CardsApi append `/api/v1` themselves. Keeping the platform
 * value at the server root avoids the old `/api/v1/api/v1` URL on iOS.
 */
actual fun platformApiBaseUrl(): String = "http://127.0.0.1:8000"

