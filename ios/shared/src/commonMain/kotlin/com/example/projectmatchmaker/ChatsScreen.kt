package com.example.projectmatchmaker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectmatchmaker.network.ChatApi
import com.example.projectmatchmaker.network.ChatCandidateSummaryDto
import com.example.projectmatchmaker.network.ChatConversationDto
import com.example.projectmatchmaker.network.ChatMessageDto
import com.example.projectmatchmaker.network.ChatProjectSummaryDto
import com.example.projectmatchmaker.network.ApiException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val DEMO_CONVERSATION_ID = "demo:codegarden"
private const val DEMO_CANDIDATE_ID = 9201
private const val DEMO_OWNER_ID = 9200

private val demoConversation = ChatConversationDto(
    id = DEMO_CONVERSATION_ID,
    matchId = -1,
    createdAt = "2026-11-25T09:30:00Z",
    project = ChatProjectSummaryDto(id = -1, title = "CodeGarden", ownerId = DEMO_OWNER_ID),
    candidate = ChatCandidateSummaryDto(userId = DEMO_CANDIDATE_ID, displayName = "Мария Волкова", headline = "UI/UX-дизайнер"),
    unreadCount = 1,
)

private val demoChatSeed = listOf(
    ChatMessageDto("demo-message-1", DEMO_CONVERSATION_ID, DEMO_CANDIDATE_ID, "demo-seed-1", "Привет! Увидела ваш проект в каталоге. Ищете дизайнера?", "2026-11-25T09:30:00Z"),
    ChatMessageDto("demo-message-2", DEMO_CONVERSATION_ID, DEMO_OWNER_ID, "demo-seed-2", "Да, будем рады познакомиться и рассказать подробнее.", "2026-11-25T09:32:00Z"),
)

@Composable
fun ChatsScreen(
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onProfile: () -> Unit,
    api: ChatApi? = null,
    accessToken: String? = null,
    currentUserId: Int? = null,
    initialDemoChatRequest: Int = 0,
    demoMessages: List<ChatMessageDto> = emptyList(),
    onDemoMessagesChange: (List<ChatMessageDto>) -> Unit = {},
    onSessionUnauthorized: () -> Unit = {},
) {
    var conversations by remember { mutableStateOf(emptyList<ChatConversationDto>()) }
    var messages by remember { mutableStateOf(emptyList<ChatMessageDto>()) }
    var selected by remember { mutableStateOf<ChatConversationDto?>(null) }
    var draft by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var connection by remember { mutableStateOf("Подключаемся…") }
    var reload by remember { mutableStateOf(0) }
    val sampleMessages = demoMessages.ifEmpty { demoChatSeed }
    val scope = rememberCoroutineScope()
    val messageListState = rememberLazyListState()

    fun openDemoChat() {
        selected = demoConversation.copy(lastMessage = sampleMessages.lastOrNull())
        messages = sampleMessages
        connection = "Пример чата"
        error = null
        draft = ""
    }

    LaunchedEffect(initialDemoChatRequest) {
        if (initialDemoChatRequest > 0) openDemoChat()
    }

    LaunchedEffect(api, accessToken, reload) {
        if (api == null || accessToken.isNullOrBlank()) {
            conversations = if (api == null) emptyList() else conversations
            error = if (api == null) "Чат-сервис не подключён." else "Войдите в аккаунт, чтобы открыть чаты."
            return@LaunchedEffect
        }
        loading = true
        error = null
        runCatching { api.conversations() }
            .onSuccess { conversations = it.sortedByDescending { chat -> chat.lastMessage?.createdAt ?: chat.createdAt } }
            .onFailure {
                error = it.toChatMessage("Не удалось загрузить список чатов")
                if ((it as? ApiException)?.statusCode == 401) onSessionUnauthorized()
            }
        loading = false
    }

    LaunchedEffect(api, accessToken, selected?.id, reload) {
        val conversation = selected ?: return@LaunchedEffect
        if (conversation.id == DEMO_CONVERSATION_ID) {
            messages = sampleMessages
            loading = false
            error = null
            connection = "Пример чата"
            return@LaunchedEffect
        }
        val chatApi = api ?: return@LaunchedEffect
        if (accessToken.isNullOrBlank()) return@LaunchedEffect
        messages = emptyList()
        loading = true
        runCatching { chatApi.messages(conversation.id) }
            .onSuccess { page ->
                messages = page.messages
                runCatching { chatApi.markRead(conversation.id) }
                conversations = conversations.map { if (it.id == conversation.id) it.copy(unreadCount = 0) else it }
            }
            .onFailure {
                error = it.toChatMessage("Не удалось загрузить переписку")
                if ((it as? ApiException)?.statusCode == 401) onSessionUnauthorized()
            }
        loading = false
    }

    LaunchedEffect(api, accessToken, selected?.matchId) {
        val conversation = selected ?: return@LaunchedEffect
        if (conversation.id == DEMO_CONVERSATION_ID) return@LaunchedEffect
        val chatApi = api ?: return@LaunchedEffect
        val token = accessToken?.takeIf(String::isNotBlank) ?: return@LaunchedEffect
        var retryDelay = 1_000L
        while (currentCoroutineContext().isActive) {
            try {
                connection = "Подключаемся…"
                chatApi.listen(conversation.matchId, token) { incoming ->
                    messages = mergeMessages(messages, incoming)
                    conversations = conversations.map {
                        if (it.id == conversation.id) it.copy(lastMessage = incoming) else it
                    }
                    connection = "В сети"
                }
                connection = "Соединение закрыто · переподключаемся…"
            } catch (_: Throwable) {
                connection = "Нет соединения · переподключаемся…"
            }
            delay(retryDelay)
            retryDelay = (retryDelay * 2).coerceAtMost(10_000L)
        }
    }

    LaunchedEffect(selected?.id, messages.size) {
        if (selected != null && messages.isNotEmpty()) messageListState.animateScrollToItem(messages.lastIndex)
    }

    fun sendMessage(conversation: ChatConversationDto) {
        val body = draft.trim()
        if (body.isEmpty() || sending) return
        if (conversation.id == DEMO_CONVERSATION_ID) {
            val timestamp = "2026-11-25T09:${(34 + sampleMessages.size).toString().padStart(2, '0')}:00Z"
            val sent = ChatMessageDto(
                id = "demo-message-${Random.nextLong().toULong()}",
                conversationId = DEMO_CONVERSATION_ID,
                senderUserId = if (conversation.id == DEMO_CONVERSATION_ID) DEMO_OWNER_ID else currentUserId ?: DEMO_OWNER_ID,
                clientMessageId = "demo-client-${Random.nextLong().toULong()}",
                body = body,
                createdAt = timestamp,
            )
            val updatedMessages = sampleMessages + sent
            onDemoMessagesChange(updatedMessages)
            messages = updatedMessages
            conversations = conversations.map { if (it.id == DEMO_CONVERSATION_ID) it.copy(lastMessage = sent) else it }
            draft = ""
            return
        }
        val chatApi = api ?: return
        sending = true
        scope.launch {
            runCatching { chatApi.sendMessage(conversation.id, "mobile-${Random.nextLong().toULong()}", body) }
                .onSuccess { sent ->
                    messages = mergeMessages(messages, sent)
                    conversations = conversations.map { if (it.id == conversation.id) it.copy(lastMessage = sent) else it }
                    draft = ""
                    error = null
                }
                .onFailure { error = it.toChatMessage("Не удалось отправить сообщение") }
            sending = false
        }
    }

    ScreenFrame(MainDestination.CHATS, onCatalog, onProfile, onMatches, {}) {
        PageCard {
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (selected == null) {
                    Column(Modifier.fillMaxWidth().widthIn(max = 760.dp)) {
                        PageTitle("Чаты")
                        Text("Переписки после взаимного совпадения", color = MutedTextColor, fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp, bottom = 12.dp))
                        when {
                            loading && conversations.isEmpty() -> Text("Загружаем чаты…", color = MutedTextColor, modifier = Modifier.padding(vertical = 20.dp))
                            error != null -> {
                                Text(error.orEmpty(), color = ErrorColor, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
                                PrimaryButton("Повторить", onClick = { reload += 1 })
                                Text("Пока сервис недоступен, можно открыть пример и попробовать интерфейс.", color = MutedTextColor, fontSize = 12.sp, modifier = Modifier.padding(top = 14.dp, bottom = 6.dp))
                                ConversationRow(
                                    demoConversation.copy(lastMessage = sampleMessages.lastOrNull(), unreadCount = 1),
                                    currentUserId,
                                    isDemo = true,
                                    onClick = ::openDemoChat,
                                )
                            }
                            conversations.isEmpty() -> {
                                Column(Modifier.fillMaxWidth()) {
                                    Text("Пока нет чатов. Они появятся после взаимного мэтча.", color = MutedTextColor, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
                                    Text("Пример переписки", color = MutedTextColor, fontSize = 11.sp, modifier = Modifier.padding(bottom = 6.dp))
                                    ConversationRow(
                                        demoConversation.copy(lastMessage = sampleMessages.lastOrNull(), unreadCount = 1),
                                        currentUserId,
                                        isDemo = true,
                                        onClick = ::openDemoChat,
                                    )
                                }
                            }
                            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                items(conversations, key = ChatConversationDto::id) { conversation ->
                                    ConversationRow(conversation, currentUserId) { selected = conversation }
                                }
                            }
                        }
                    }
                } else {
                    val conversation = selected!!
                    Column(Modifier.fillMaxSize().widthIn(max = 820.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("‹", color = PrimaryButtonColor, fontSize = 34.sp, modifier = Modifier.clickable {
                                selected = null
                                error = null
                                if (conversation.id != DEMO_CONVERSATION_ID) reload += 1
                            }.padding(end = 12.dp))
                            val peer = peerName(conversation, currentUserId)
                            Avatar(peer.initials)
                            Column(Modifier.padding(start = 10.dp)) {
                                Text(peer.name, color = TextDarkColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(connection, color = MutedTextColor, fontSize = 11.sp)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        SurfaceDivider()
                        if (conversation.id == DEMO_CONVERSATION_ID) {
                            Text(
                                "Демо-чат · сообщения сохраняются только в этом примере",
                                color = MutedTextColor,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(vertical = 7.dp),
                            )
                        }
                        if (error != null) {
                            Text(error.orEmpty(), color = ErrorColor, fontSize = 13.sp, modifier = Modifier.padding(vertical = 14.dp))
                            if (messages.isEmpty()) PrimaryButton("Повторить", onClick = { reload += 1 })
                        }
                        LazyColumn(
                            state = messageListState,
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            reverseLayout = false,
                        ) {
                            if (messages.isEmpty() && loading) {
                                item { Text("Загружаем переписку…", color = MutedTextColor, modifier = Modifier.padding(12.dp)) }
                            } else if (messages.isEmpty()) {
                                item { Text("Начните переписку", color = MutedTextColor, modifier = Modifier.padding(12.dp)) }
                            }
                            val messageOwnerId = if (conversation.id == DEMO_CONVERSATION_ID) DEMO_OWNER_ID else currentUserId ?: -1
                            items(messages, key = ChatMessageDto::id) { message ->
                                MessageBubble(message.body, message.createdAt, message.senderUserId == messageOwnerId)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = draft,
                                onValueChange = { draft = it.take(4000) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { sendMessage(conversation) }),
                                placeholder = { Text("Сообщение", color = MutedTextColor, fontSize = 13.sp) },
                                shape = RoundedCornerShape(14.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            IconButton(
                                enabled = draft.isNotBlank() && !sending && (api != null || conversation.id == DEMO_CONVERSATION_ID),
                                onClick = { sendMessage(conversation) },
                                modifier = Modifier.size(48.dp).background(PrimaryButtonColor, CircleShape),
                            ) { Text("➤", color = CardBgColor, fontSize = 20.sp) }
                        }
                    }
                }
            }
        }
    }
}

private fun mergeMessages(current: List<ChatMessageDto>, incoming: ChatMessageDto): List<ChatMessageDto> =
    if (current.any { it.id == incoming.id || it.clientMessageId == incoming.clientMessageId }) current
    else (current + incoming).sortedBy(ChatMessageDto::createdAt)

private fun Throwable.toChatMessage(fallback: String): String {
    val details = message.orEmpty()
    return if (details.contains("unexpected end of stream", ignoreCase = true) ||
        details.contains("eofexception", ignoreCase = true) ||
        details.contains("no route to host", ignoreCase = true) ||
        details.contains("timeout", ignoreCase = true) ||
        details.contains("connect", ignoreCase = true) ||
        details.contains("refused", ignoreCase = true)
    ) {
        "Сервис чатов сейчас недоступен на порту 3000. Backend API работает отдельно; запустите chat-service и нажмите «Повторить»."
    } else details.ifBlank { fallback }
}

private data class PeerName(val name: String, val initials: String)

private fun peerName(conversation: ChatConversationDto, currentUserId: Int?): PeerName {
    val peer = if (currentUserId == conversation.candidate.userId) conversation.project.title else conversation.candidate.displayName
    return PeerName(peer.ifBlank { "Мэтч" }, peer.initials())
}

private fun String.initials(): String = trim().split(Regex("\\s+")).filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }.ifBlank { "MM" }

@Composable
private fun ConversationRow(conversation: ChatConversationDto, currentUserId: Int?, isDemo: Boolean = false, onClick: () -> Unit) {
    val peer = peerName(conversation, currentUserId)
    Surface(color = PanelBgColor, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(peer.initials, 48.dp)
            Column(Modifier.weight(1f).padding(start = 11.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(peer.name, color = TextDarkColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 1)
                    Text(conversation.lastMessage?.createdAt?.substringAfter('T')?.take(5).orEmpty(), color = MutedTextColor, fontSize = 10.sp)
                }
                Text(conversation.lastMessage?.body ?: "Совпадение создано — напишите первым", color = MutedTextColor, fontSize = 12.sp, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
                if (isDemo) Text("Пример · можно написать", color = PrimaryButtonColor, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
            }
            if (conversation.unreadCount > 0) {
                Surface(color = PrimaryButtonColor, shape = CircleShape, modifier = Modifier.padding(start = 8.dp)) {
                    Text(conversation.unreadCount.toString(), color = CardBgColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(text: String, timestamp: String, isMine: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start) {
        Column(Modifier.clip(RoundedCornerShape(14.dp)).background(if (isMine) PrimaryButtonColor else PanelBgColor).padding(10.dp)) {
            Text(text, color = if (isMine) CardBgColor else TextDarkColor, fontSize = 13.sp)
            Text(timestamp.substringAfter('T').take(5), color = if (isMine) CardBgColor.copy(alpha = .75f) else MutedTextColor, fontSize = 9.sp, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun Avatar(initials: String, size: Dp = 42.dp) {
    Surface(color = ProfileAvatarColor, shape = CircleShape, modifier = Modifier.size(size)) {
        Box(contentAlignment = Alignment.Center) {
            Text(initials, color = CardBgColor, fontSize = if (size < 40.dp) 10.sp else 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SurfaceDivider() {
    Spacer(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
}

@Preview(name = "Чаты · телефон", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun ChatsScreenPreview() {
    MaterialTheme { ChatsScreen({}, {}, {}) }
}
