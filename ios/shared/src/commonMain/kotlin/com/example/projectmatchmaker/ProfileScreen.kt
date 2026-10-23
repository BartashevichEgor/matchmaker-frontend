package com.example.projectmatchmaker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

    ScreenFrame(
        selected = MainDestination.PROFILE,
        onCatalog = onCatalog,
        onProfile = {},
        onMatches = onMatches,
        onChats = onChats,
    ) {
        PageCard {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
            ) {
                PageTitle("Мой профиль")
                Spacer(Modifier.height(14.dp))

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(76.dp).background(ProfileAvatarColor, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(initials(profile.name), color = CardBgColor, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.padding(start = 16.dp)) {
                        Text(
                            profile.name.ifBlank { "Имя не заполнено" },
                            color = TextDarkColor,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "@" + profile.username.ifBlank { "username" },
                            color = MutedTextColor,
                            fontSize = 12.sp,
                        )
                        Text(profile.role.ifBlank { "Роль не выбрана" }, color = TextDarkColor, fontSize = 15.sp)
                    }
                }

                Surface(
                    color = PanelBgColor,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp).clickable(onClick = onFavorites),
                ) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("♡", color = PrimaryButtonColor, fontSize = 27.sp)
                        Column(Modifier.padding(start = 12.dp)) {
                            Text("Избранное", color = TextDarkColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Сохранённые анкеты и проекты", color = MutedTextColor, fontSize = 12.sp)
                        }
                    }
                }

                Surface(
                    color = PanelBgColor,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).clickable(onClick = onEdit),
                ) {
                    Box(Modifier.height(48.dp), contentAlignment = Alignment.Center) {
                        Text("Редактировать профиль", color = PrimaryButtonColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                FormLabel("О себе")
                Text(
                    profile.about.ifBlank { "Описание пока не заполнено" },
                    color = if (profile.about.isBlank()) MutedTextColor else TextDarkColor,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                )

                FormLabel("Навыки")
                SkillChips(profile.skills)

                ExpandableHeader("Образование и курсы", educationExpanded) {
                    educationExpanded = !educationExpanded
                }
                if (educationExpanded) {
                    Text(
                        profile.education.ifBlank { "Не указано" },
                        color = MutedTextColor,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}

private val previewProfile = ProfileData(
    username = "alina",
    firstName = "Алина",
    lastName = "Иванова",
    email = "alina@example.com",
    role = "iOS-разработчик",
    about = "Разрабатываю мобильные интерфейсы на Kotlin и Compose Multiplatform.",
    skills = listOf("Kotlin", "KMP", "Figma"),
    experience = "Учебный проект Project Matchmaker",
    education = "Студентка",
    github = "https://github.com/alina",
)

@Preview(name = "Мой профиль · телефон", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme { ProfileScreen(previewProfile, {}, {}, {}, {}, {}) }
}
