package com.example.projectmatchmaker.auth

actual object ProfileStorageBackend {
    private var profileJson: String? = null

    actual fun initialize() = Unit
    actual fun save(value: String) { profileJson = value }
    actual fun load(): String? = profileJson
    actual fun clear() { profileJson = null }
}
