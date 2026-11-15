package com.example.projectmatchmaker.auth

import platform.Foundation.NSUserDefaults

actual object ProfileStorageBackend {
    private const val PROFILE_KEY = "matchmaker_profile_json"

    actual fun initialize() = Unit

    actual fun save(value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, forKey = PROFILE_KEY)
    }

    actual fun load(): String? = NSUserDefaults.standardUserDefaults.stringForKey(PROFILE_KEY)

    actual fun clear() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(PROFILE_KEY)
    }
}
