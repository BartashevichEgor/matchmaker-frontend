package com.example.projectmatchmaker.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ChatCandidateSummaryDto(
    @SerialName("user_id") val userId: Int,
    @SerialName("display_name") val displayName: String,
    val headline: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
data class ChatProjectSummaryDto(
    val id: Int,
    val title: String,
    @SerialName("owner_id") val ownerId: Int,
)

@Serializable
data class ChatMessageDto(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_user_id") val senderUserId: Int,
    @SerialName("client_message_id") val clientMessageId: String,
    val body: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class ChatConversationDto(
    val id: String,
    @SerialName("match_id") val matchId: Int,
    @SerialName("created_at") val createdAt: String = "",
    val project: ChatProjectSummaryDto,
    val candidate: ChatCandidateSummaryDto,
    @SerialName("last_message") val lastMessage: ChatMessageDto? = null,
    @SerialName("unread_count") val unreadCount: Int = 0,
)

@Serializable
data class ChatMessagesPageDto(
    @SerialName("conversation_id") val conversationId: String,
    val messages: List<ChatMessageDto>,
    @SerialName("next_cursor") val nextCursor: String? = null,
    @SerialName("unread_count") val unreadCount: Int = 0,
)

@Serializable
private data class ChatSendRequestDto(
    @SerialName("client_message_id") val clientMessageId: String,
    val body: String,
)

@Serializable
private data class ChatSendResponseDto(val message: ChatMessageDto)

@Serializable
private data class ChatSocketAuthDto(
    val type: String = "authenticate",
    @SerialName("access_token") val accessToken: String,
    @SerialName("match_id") val matchId: Int,
)

/** REST and authenticated WebSocket client for the separate backend chat service. */
class ChatApi(
    apiBaseUrl: String = platformApiBaseUrl(),
    private val accessToken: () -> String? = { null },
) {
    private val baseUrl = apiBaseUrl.trimEnd('/').replace(Regex(":8000$"), ":3000")
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }
    private val client = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
        install(WebSockets)
    }

    suspend fun conversations(): List<ChatConversationDto> =
        client.get("$baseUrl/chat/conversations") { authorize() }.checked().body()

    suspend fun messages(conversationId: String): ChatMessagesPageDto =
        client.get("$baseUrl/chat/conversations/${conversationId.encodePathSegment()}/messages") {
            authorize()
            parameter("limit", 100)
        }.checked().body()

    suspend fun sendMessage(conversationId: String, clientMessageId: String, body: String): ChatMessageDto =
        client.post("$baseUrl/chat/conversations/${conversationId.encodePathSegment()}/messages") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(ChatSendRequestDto(clientMessageId, body))
        }.checked().body<ChatSendResponseDto>().message

    suspend fun markRead(conversationId: String) {
        client.post("$baseUrl/chat/conversations/${conversationId.encodePathSegment()}/read") {
            authorize()
        }.checked()
    }

    suspend fun listen(matchId: Int, token: String, onMessage: suspend (ChatMessageDto) -> Unit) {
        val socketUrl = baseUrl.replaceFirst("https://", "wss://").replaceFirst("http://", "ws://") + "/chat/ws"
        val socket = client.webSocketSession(urlString = socketUrl)
        try {
            socket.send(Frame.Text(json.encodeToString(ChatSocketAuthDto(accessToken = token, matchId = matchId))))
            for (frame in socket.incoming) {
                if (frame is Frame.Text) {
                    val event = json.parseToJsonElement(frame.data.decodeToString()).jsonObject
                    if (event["type"]?.jsonPrimitive?.content == "message") {
                        event["message"]?.let { onMessage(json.decodeFromJsonElement(it)) }
                    }
                    if (event["type"]?.jsonPrimitive?.content == "error") {
                        throw IllegalStateException("Чат отключён: ${event["code"]?.jsonPrimitive?.content.orEmpty()}")
                    }
                }
            }
        } finally {
            socket.close()
        }
    }

    fun close() = client.close()

    private fun io.ktor.client.request.HttpRequestBuilder.authorize() {
        accessToken()?.takeIf(String::isNotBlank)?.let(::bearerAuth)
    }

    private suspend fun io.ktor.client.statement.HttpResponse.checked(): io.ktor.client.statement.HttpResponse {
        if (status.isSuccess()) return this
        throw ApiException("Чат-сервис ответил с ошибкой (${status.value}): ${bodyAsText().take(160)}", status.value)
    }

    private fun String.encodePathSegment(): String = replace("%", "%25").replace("/", "%2F")
}
