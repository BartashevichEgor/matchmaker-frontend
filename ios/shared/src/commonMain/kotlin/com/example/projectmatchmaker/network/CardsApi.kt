package com.example.projectmatchmaker.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Typed client for the currently implemented backend card/matching routes.
 * The UI can keep its local demo data when this client is not wired in.
 */
class CardsApi(
    private val baseUrl: String = platformApiBaseUrl(),
    private val accessToken: () -> String? = { null },
) {
    private val apiUrl = "${baseUrl.trimEnd('/')}/api/v1"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val client = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
    }

    suspend fun myProfile(): ProfileReadDto =
        client.get("$apiUrl/profiles/me") { authorize() }.checked().body()

    suspend fun projects(
        offset: Int = 0,
        limit: Int = 20,
        skillId: Int? = null,
        roleId: Int? = null,
        mine: Boolean = false,
    ): List<ProjectDto> = client.get("$apiUrl/projects") {
        authorize()
        parameter("offset", offset)
        parameter("limit", limit)
        parameter("mine", mine)
        skillId?.let { parameter("skill_id", it) }
        roleId?.let { parameter("role_id", it) }
    }.checked().body()

    suspend fun recommendedProjects(limit: Int = 20, offset: Int = 0): List<ProjectScoreDto> =
        client.get("$apiUrl/recommendations/projects") {
            authorize()
            parameter("limit", limit)
            parameter("offset", offset)
        }.checked().body()

    suspend fun recommendedCandidates(projectId: Int, limit: Int = 20, offset: Int = 0): List<CandidateScoreDto> =
        client.get("$apiUrl/projects/$projectId/recommendations/candidates") {
            authorize()
            parameter("limit", limit)
            parameter("offset", offset)
        }.checked().body()

    suspend fun swipeProject(projectId: Int, decision: String): SwipeResponseDto =
        client.put("$apiUrl/projects/$projectId/my-swipe") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(SwipeRequestDto(decision))
        }.checked().body()

    suspend fun swipeCandidate(projectId: Int, candidateId: Int, decision: String): SwipeResponseDto =
        client.put("$apiUrl/projects/$projectId/candidates/$candidateId/swipe") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(SwipeRequestDto(decision))
        }.checked().body()

    suspend fun matches(offset: Int = 0, limit: Int = 20): List<MatchDto> =
        client.get("$apiUrl/matches") {
            authorize()
            parameter("offset", offset)
            parameter("limit", limit)
        }.checked().body()

    fun close() = client.close()

    private fun HttpRequestBuilder.authorize() {
        accessToken()?.takeIf { it.isNotBlank() }?.let(::bearerAuth)
    }

    private suspend fun HttpResponse.checked(): HttpResponse {
        if (status.isSuccess()) return this
        val detail = runCatching {
            json.parseToJsonElement(bodyAsText()).jsonObject["detail"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()
        throw ApiException(detail ?: "Ошибка сервера (${status.value})")
    }
}
