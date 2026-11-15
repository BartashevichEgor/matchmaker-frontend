package com.example.projectmatchmaker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatsScreen(
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onProfile: () -> Unit,
    showIncomingMatchChat: Boolean = false,
) {
    val chatItems = if (showIncomingMatchChat) demoChats + demoIncomingChat else demoChats
    var selectedId by remember(showIncomingMatchChat) {
        mutableStateOf(if (showIncomingMatchChat) demoIncomingChat.id else demoChats.first().id)
    }
    var draft by remember { mutableStateOf("") }
    var sentMessages by remember { mutableStateOf(emptyMap<String, List<DemoMessage>>()) }
    val selectedChat = chatItems.firstOrNull { it.id == selectedId } ?: chatItems.first()
    val messages = selectedChat.messages + sentMessages[selectedChat.id].orEmpty()

    ScreenFrame(MainDestination.CHATS, onCatalog, onProfile, onMatches, {}) {
        PageCard {
            Column(Modifier.fillMaxSize().padding(18.dp)) {
                PageTitle("Чаты")
                Text("Обсуждай идеи после взаимного совпадения", color = MutedTextColor, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth().height(104.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(chatItems, key = { it.id }) { chat ->
                        ChatPreview(chat, selected = chat.id == selectedId) { selectedId = chat.id }
                    }
                }

                Spacer(Modifier.height(10.dp))
                SurfaceDivider()
                Row(Modifier.weight(1f).fillMaxWidth().padding(top = 10.dp)) {
                    Column(Modifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(selectedChat.initials)
                            Column(Modifier.padding(start = 10.dp)) {
                                Text(selectedChat.name, color = TextDarkColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("В сети · демо-режим", color = MutedTextColor, fontSize = 11.sp)
                            }
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            items(messages) { message -> MessageBubble(message) }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = draft,
                                onValueChange = { draft = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("Сообщение", color = MutedTextColor, fontSize = 13.sp) },
                                shape = RoundedCornerShape(14.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            IconButton(
                                enabled = draft.isNotBlank(),
                                onClick = {
                                    val text = draft.trim()
                                    sentMessages = sentMessages + (selectedChat.id to (
                                        sentMessages[selectedChat.id].orEmpty() + DemoMessage("Вы", text, "сейчас", true)
                                    ))
                                    draft = ""
                                },
                                modifier = Modifier.size(48.dp).background(PrimaryButtonColor, CircleShape),
                            ) {
                                Text("➤", color = CardBgColor, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatPreview(chat: DemoChat, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.width(122.dp).clip(RoundedCornerShape(12.dp))
            .background(if (selected) SectionBgColor else PanelBgColor)
            .clickable(onClick = onClick).padding(9.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(chat.initials, size = 30.dp)
            Text(
                chat.name,
                color = TextDarkColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp),
                maxLines = 1,
            )
        }
        Text(chat.lastMessage, color = MutedTextColor, fontSize = 10.sp, maxLines = 2, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun MessageBubble(message: DemoMessage) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start) {
        Column(
            Modifier.clip(RoundedCornerShape(14.dp))
                .background(if (message.isMine) PrimaryButtonColor else PanelBgColor).padding(10.dp),
        ) {
            Text(message.text, color = if (message.isMine) CardBgColor else TextDarkColor, fontSize = 13.sp)
            Text(
                message.time,
                color = if (message.isMine) CardBgColor.copy(alpha = .75f) else MutedTextColor,
                fontSize = 9.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

@Composable
private fun Avatar(initials: String, size: Dp = 42.dp) {
    Surface(color = ProfileAvatarColor, shape = CircleShape, modifier = Modifier.size(size)) {
        Text(
            initials,
            color = CardBgColor,
            fontSize = if (size < 40.dp) 10.sp else 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (size < 40.dp) 9.dp else 12.dp),
        )
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
