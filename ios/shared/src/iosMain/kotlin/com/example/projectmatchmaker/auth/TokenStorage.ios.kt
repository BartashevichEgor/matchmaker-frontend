
package com.example.projectmatchmaker.auth

import platform.Foundation.NSUserDefaults

/**
 * Временная iOS-реализация хранилища для запуска общего KMP-модуля.
 * Перед публикацией приложения это хранилище нужно заменить на iOS Keychain.
 */
actual object TokenStorage {
    private const val TOKEN_KEY = "jwt_access_token"
    private const val REFRESH_TOKEN_KEY = "jwt_refresh_token"

    actual fun saveToken(token: String) {
        NSUserDefaults.standardUserDefaults.setObject(token, forKey = TOKEN_KEY)
    }

    actual fun getToken(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(TOKEN_KEY)

    actual fun saveRefreshToken(token: String) {
        NSUserDefaults.standardUserDefaults.setObject(token, forKey = REFRESH_TOKEN_KEY)
    }

    actual fun getRefreshToken(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(REFRESH_TOKEN_KEY)

    actual fun clearToken() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(TOKEN_KEY)
        NSUserDefaults.standardUserDefaults.removeObjectForKey(REFRESH_TOKEN_KEY)
    }
}
