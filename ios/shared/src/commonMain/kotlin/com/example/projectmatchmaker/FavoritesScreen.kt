package com.example.projectmatchmaker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FavoritesScreen(
    onBackToProfile: () -> Unit,
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onChats: () -> Unit,
) {
    ScreenFrame(MainDestination.PROFILE, onCatalog, onBackToProfile, onMatches, onChats) {
        PageCard {
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                PageTitle("Избранное")
                Text(
                    "Сохранённое видно только тебе. Закладка не отправляет лайк.",
                    color = MutedTextColor,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
                Surface(
                    color = PanelBgColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(top = 24.dp),
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("♡", color = PrimaryButtonColor, fontSize = 28.sp)
                        Text(
                            "Сохранённое — под рукой",
                            color = TextDarkColor,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Нажми закладку в каталоге. Сохранённые карточки появятся здесь.",
                            color = TextDarkColor,
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                        PrimaryButton("Перейти в каталог", onCatalog)
                    }
                }
            }
        }
    }
}

@Preview(name = "Избранное · телефон", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    MaterialTheme { FavoritesScreen({}, {}, {}, {}) }
}
