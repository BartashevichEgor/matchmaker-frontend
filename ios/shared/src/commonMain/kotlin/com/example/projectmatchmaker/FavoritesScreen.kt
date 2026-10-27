package com.example.projectmatchmaker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FavoritesScreen(
    favorites: Set<FavoriteItem> = emptySet(),
    onFavoriteToggle: (FavoriteItem) -> Unit = {},
    onBackToProfile: () -> Unit,
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onChats: () -> Unit,
) {
    ScreenFrame(MainDestination.PROFILE, onCatalog, onBackToProfile, onMatches, onChats) {
        PageCard {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)) {
                PageTitle("Избранное")
                Text(
                    "Сохранённое видно только тебе. Закладка не отправляет лайк.",
                    color = MutedTextColor,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                )
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp).clickable(onClick = onBackToProfile),
                ) {
                    Box(Modifier.height(46.dp), contentAlignment = Alignment.Center) {
                        Text("Назад в профиль", color = PrimaryButtonColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    Modifier.fillMaxWidth().weight(1f).padding(top = 18.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (favorites.isEmpty()) {
                        EmptyFavorites(onCatalog)
                    } else {
                        if (FavoriteItem.PERSON in favorites) {
                            FavoriteCard(
                                item = FavoriteItem.PERSON,
                                title = "Алексей Ковалёв",
                                subtitle = "Backend-разработчик",
                                skills = listOf("Python", "Django", "SQL"),
                                onRemove = { onFavoriteToggle(FavoriteItem.PERSON) },
                            )
                        }
                        if (FavoriteItem.PROJECT in favorites) {
                            FavoriteCard(
                                item = FavoriteItem.PROJECT,
                                title = "TeamFlow",
                                subtitle = "Ищем UI/UX-дизайнера",
                                skills = listOf("Figma", "UX", "UI"),
                                onRemove = { onFavoriteToggle(FavoriteItem.PROJECT) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFavorites(onCatalog: () -> Unit) {
    Surface(color = PanelBgColor, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            BookmarkButton(false, onCatalog, Modifier.size(24.dp))
            Text("Сохранённое — под рукой", color = TextDarkColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text(
                "Нажми закладку в каталоге.\nСохранённые карточки появятся ниже.",
                color = TextDarkColor,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(vertical = 14.dp),
            )
            PrimaryButton("Перейти в каталог", onCatalog)
        }
    }
}

@Composable
private fun FavoriteCard(
    item: FavoriteItem,
    title: String,
    subtitle: String,
    skills: List<String>,
    onRemove: () -> Unit,
) {
    Surface(color = PanelBgColor, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            if (item == FavoriteItem.PROJECT) {
                Box(
                    Modifier.fillMaxWidth().height(72.dp).background(SectionBgColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("TeamFlow", color = PrimaryButtonColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = if (item == FavoriteItem.PROJECT) 12.dp else 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = TextDarkColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = MutedTextColor, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                }
                BookmarkButton(true, onRemove)
            }
            SkillChips(skills)
        }
    }
}

@Preview(name = "Избранное · пусто", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    MaterialTheme { FavoritesScreen(emptySet(), {}, {}, {}, {}, {}) }
}

@Preview(name = "Избранное · сохранено", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun FavoritesSavedScreenPreview() {
    MaterialTheme { FavoritesScreen(setOf(FavoriteItem.PERSON, FavoriteItem.PROJECT), {}, {}, {}, {}, {}) }
}
