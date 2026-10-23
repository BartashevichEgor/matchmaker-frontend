package com.example.projectmatchmaker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatsScreen(
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onProfile: () -> Unit,
) {
    ScreenFrame(MainDestination.CHATS, onCatalog, onProfile, onMatches, {}) {
        PageCard {
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                PageTitle("Чаты")
                Text(
                    "После мэтча здесь появится переписка с участником команды.",
                    color = MutedTextColor,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Preview(name = "Чаты · телефон", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun ChatsScreenPreview() {
    MaterialTheme { ChatsScreen({}, {}, {}) }
}
