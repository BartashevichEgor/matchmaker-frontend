package com.example.projectmatchmaker.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AuthApi(
    private val baseUrl: String = platformApiBaseUrl(),
) {
    private val apiUrl = "${baseUrl.trimEnd('/')}/api/v1"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun login(email: String, password: String): TokenResponse {
        val response = client.post("$apiUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = email.trim(), password = password))
        }

        if (!response.status.isSuccess()) {
            throw ApiException(readError(response.bodyAsText(), response.status.value))
        }

        return response.body()
    }

    suspend fun register(username: String, email: String, password: String): MessageResponse {
        val response = client.post("$apiUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterRequest(
                    username = username.trim(),
                    email = email.trim(),
                    password = password,
                )
            )
        }

        if (!response.status.isSuccess()) {
            throw ApiException(readError(response.bodyAsText(), response.status.value))
        }

        return response.body()
    }

    suspend fun verifyEmail(email: String, code: String): MessageResponse {
        val response = client.post("$apiUrl/auth/verify-email") {
            contentType(ContentType.Application.Json)
            setBody(VerifyEmailRequest(email = email.trim(), code = code.trim()))
        }
        if (!response.status.isSuccess()) {
            throw ApiException(readError(response.bodyAsText(), response.status.value))
        }
        return response.body()
    }

    suspend fun resendVerification(email: String): MessageResponse {
        val response = client.post("$apiUrl/auth/resend-verification") {
            contentType(ContentType.Application.Json)
            setBody(EmailRequest(email = email.trim()))
        }
        if (!response.status.isSuccess()) {
            throw ApiException(readError(response.bodyAsText(), response.status.value))
        }
        return response.body()
    }

    suspend fun refresh(refreshToken: String): TokenResponse {
        val response = client.post("$apiUrl/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(refreshToken))
        }
        if (!response.status.isSuccess()) {
            throw ApiException(readError(response.bodyAsText(), response.status.value))
        }
        return response.body()
    }

    fun close() {
        client.close()
    }

    private fun readError(body: String, statusCode: Int): String {
        val serverMessage = runCatching {
            json.parseToJsonElement(body).jsonObject["detail"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()

        return serverMessage ?: if (statusCode == 422) {
            "Проверьте правильность заполнения полей"
        } else {
            "Ошибка сервера ($statusCode)"
        }
    }
}

class ApiException(message: String) : Exception(message)

