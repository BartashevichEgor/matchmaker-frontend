package com.example.projectmatchmaker.auth

import com.example.projectmatchmaker.ProfileData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Persists non-secret profile fields on the device; JWTs remain in TokenStorage. */
object ProfileStorage {
    private val json = Json { ignoreUnknownKeys = true }

    fun save(profile: ProfileData) {
        runCatching { json.encodeToString(profile) }
            .onSuccess(ProfileStorageBackend::save)
    }

    fun load(): ProfileData? = ProfileStorageBackend.load()?.let { encoded ->
        runCatching { json.decodeFromString<ProfileData>(encoded) }.getOrNull()
    }

    fun clear() = ProfileStorageBackend.clear()
}

expect object ProfileStorageBackend {
    fun initialize()
    fun save(value: String)
    fun load(): String?
    fun clear()
}
