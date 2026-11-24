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
import com.example.projectmatchmaker.network.ChatConversationDto
import com.example.projectmatchmaker.network.ChatMessageDto
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun ChatsScreen(
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onProfile: () -> Unit,
    api: ChatApi? = null,
    accessToken: String? = null,
    currentUserId: Int? = null,
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
    val scope = rememberCoroutineScope()
    val messageListState = rememberLazyListState()

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
            .onFailure { error = it.message ?: "Не удалось загрузить список чатов" }
        loading = false
    }

    LaunchedEffect(api, accessToken, selected?.id, reload) {
        val conversation = selected ?: return@LaunchedEffect
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
            .onFailure { error = it.message ?: "Не удалось загрузить переписку" }
        loading = false
    }

    LaunchedEffect(api, accessToken, selected?.matchId) {
        val conversation = selected ?: return@LaunchedEffect
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
        val chatApi = api ?: return
        if (body.isEmpty() || sending) return
        sending = true
        scope.launch {
            runCatching { chatApi.sendMessage(conversation.id, "mobile-${Random.nextLong().toULong()}", body) }
                .onSuccess { sent ->
                    messages = mergeMessages(messages, sent)
                    conversations = conversations.map { if (it.id == conversation.id) it.copy(lastMessage = sent) else it }
                    draft = ""
                    error = null
                }
                .onFailure { error = it.message ?: "Не удалось отправить сообщение" }
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
                            }
                            conversations.isEmpty() -> Text("Пока нет чатов. Они появятся после взаимного мэтча.", color = MutedTextColor, fontSize = 14.sp, modifier = Modifier.padding(vertical = 24.dp))
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
                                reload += 1
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
                            items(messages, key = ChatMessageDto::id) { message ->
                                MessageBubble(message.body, message.createdAt, message.senderUserId == currentUserId)
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
                                enabled = draft.isNotBlank() && !sending && api != null,
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

private data class PeerName(val name: String, val initials: String)

private fun peerName(conversation: ChatConversationDto, currentUserId: Int?): PeerName {
    val peer = if (currentUserId == conversation.candidate.userId) conversation.project.title else conversation.candidate.displayName
    return PeerName(peer.ifBlank { "Мэтч" }, peer.initials())
}

private fun String.initials(): String = trim().split(Regex("\\s+")).filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }.ifBlank { "MM" }

@Composable
private fun ConversationRow(conversation: ChatConversationDto, currentUserId: Int?, onClick: () -> Unit) {
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
