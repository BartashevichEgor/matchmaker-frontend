package com.example.projectmatchmaker

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    profile: ProfileData,
    onEdit: () -> Unit,
    onFavorites: () -> Unit,
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onChats: () -> Unit,
) {
    var educationExpanded by remember { mutableStateOf(false) }

    ScreenFrame(MainDestination.PROFILE, onCatalog, {}, onMatches, onChats) {
        PageCard {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
            ) {
                PageTitle("Мой профиль")

                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(76.dp).background(ProfileAvatarColor, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            initials(profile.name),
                            color = CardBgColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(Modifier.padding(start = 18.dp)) {
                        Text(
                            profile.name.ifBlank { "Имя не заполнено" },
                            color = TextDarkColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "@" + profile.username.ifBlank { "username" },
                            color = MutedTextColor,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                        Text(
                            profile.role.ifBlank { "Роль не выбрана" },
                            color = TextDarkColor,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                }

                Surface(
                    color = PanelBgColor,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp).clickable(onClick = onFavorites),
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BookmarkButton(saved = false, onClick = onFavorites, modifier = Modifier.size(27.dp))
                        Column(Modifier.padding(start = 10.dp)) {
                            Text(
                                "Избранное",
                                color = TextDarkColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Сохранённые анкеты и проекты",
                                color = MutedTextColor,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }

                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).clickable(onClick = onEdit),
                ) {
                    Box(Modifier.height(46.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Редактировать профиль",
                            color = PrimaryButtonColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Text(
                    "О себе",
                    color = TextDarkColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                )
                Text(
                    profile.about.ifBlank { "Описание пока не заполнено" },
                    color = if (profile.about.isBlank()) MutedTextColor else TextDarkColor,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )

                Text(
                    "Навыки",
                    color = TextDarkColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 18.dp, bottom = 3.dp),
                )
                SkillChips(profile.skills)

                ExpandableHeader("Образование и курсы", educationExpanded) {
                    educationExpanded = !educationExpanded
                }
                if (educationExpanded) {
                    Text(
                        listOf(profile.education, profile.experience)
                            .filter { it.isNotBlank() }
                            .joinToString(" · ")
                            .ifBlank { "Не указано" },
                        color = MutedTextColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}

private val previewProfile = ProfileData(
    username = "veronika",
    firstName = "Вероника",
    lastName = "Соболь",
    email = "veronika@example.com",
    role = "UI/UX-дизайнер",
    about = "Создаю понятные интерфейсы. Ищу команду для полезного проекта.",
    skills = listOf("Figma", "UX", "Mermaid"),
    experience = "Учебные и личные проекты",
    education = "Студентка · дизайн интерфейсов",
)

@Preview(name = "Мой профиль · телефон", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme { ProfileScreen(previewProfile, {}, {}, {}, {}, {}) }
}
