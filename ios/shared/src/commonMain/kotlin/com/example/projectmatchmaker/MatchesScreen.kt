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
fun MatchesScreen(
    onCatalog: () -> Unit,
    onChats: () -> Unit,
    onProfile: () -> Unit,
) {
    ScreenFrame(MainDestination.MATCHES, onCatalog, onProfile, {}, onChats) {
        PageCard {
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                PageTitle("Мэтчи")
                Text(
                    "Здесь появятся взаимные совпадения с людьми и проектами.",
                    color = MutedTextColor,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Preview(name = "Мэтчи · телефон", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun MatchesScreenPreview() {
    MaterialTheme { MatchesScreen({}, {}, {}) }
}
