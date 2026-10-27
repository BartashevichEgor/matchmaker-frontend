
package com.example.projectmatchmaker.auth

import platform.Foundation.NSUserDefaults

/**
 * Временная iOS-реализация хранилища для запуска общего KMP-модуля.
 * Перед публикацией приложения это хранилище нужно заменить на iOS Keychain.
 */
actual object TokenStorage {
    private const val TOKEN_KEY = "jwt_access_token"

    actual fun saveToken(token: String) {
        NSUserDefaults.standardUserDefaults.setObject(token, forKey = TOKEN_KEY)
    }

    actual fun getToken(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(TOKEN_KEY)

    actual fun clearToken() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(TOKEN_KEY)
    }
}
