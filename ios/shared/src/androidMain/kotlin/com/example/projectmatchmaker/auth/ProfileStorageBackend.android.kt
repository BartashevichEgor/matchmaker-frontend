package com.example.projectmatchmaker.auth

import android.content.Context
import android.content.SharedPreferences

actual object ProfileStorageBackend {
    private const val FILE_NAME = "profile_cache"
    private const val PROFILE_KEY = "profile_json"
    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    actual fun initialize() = Unit

    actual fun save(value: String) {
        preferences?.edit()?.putString(PROFILE_KEY, value)?.apply()
    }

    actual fun load(): String? = preferences?.getString(PROFILE_KEY, null)

    actual fun clear() {
        preferences?.edit()?.remove(PROFILE_KEY)?.apply()
    }
}
