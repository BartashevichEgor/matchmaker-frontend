package com.example.projectmatchmaker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
    onCreateProject: () -> Unit = {},
    onManageProject: () -> Unit = {},
    hasProject: Boolean = false,
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onChats: () -> Unit,
    onModeChange: (String) -> Unit = {},
    projectPublished: Boolean = false,
) {
    var educationExpanded by remember { mutableStateOf(false) }
    val projectMode = profile.registrationMode == "project"

    ScreenFrame(MainDestination.PROFILE, onCatalog, {}, onMatches, onChats) {
        PageCard {
            ResponsivePageColumn(
                phonePadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            ) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    PageTitle("Мой профиль")

                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfileAvatar(
                            photoBase64 = profile.photoBase64,
                            initials = initials(profile.name),
                            modifier = Modifier,
                            size = 76.dp,
                        )
                        Column(Modifier.padding(start = 16.dp)) {
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
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            Text(
                                "Активный режим",
                                color = TextDarkColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                            ProfileModeToggle(projectMode = projectMode) { selected ->
                                onModeChange(if (selected) "project" else "developer")
                            }
                            Text(
                                if (projectMode) "В каталоге доступны специалисты." else "В каталоге доступны проекты.",
                                color = MutedTextColor,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 7.dp),
                            )
                            Text(
                                "Персональная анкета, Опубликовано",
                                color = TextDarkColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            Text(
                                "Мой проект, " + when {
                                    !hasProject -> "Не создан"
                                    projectPublished -> "Опубликовано"
                                    else -> "Черновик"
                                },
                                color = TextDarkColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 5.dp),
                            )
                            OutlineProfileButton(
                                text = if (hasProject) "Управлять проектом" else "Создать проект",
                                onClick = if (hasProject) onManageProject else onCreateProject,
                                modifier = Modifier.padding(top = 7.dp),
                            )
                        }
                    }

                    Surface(
                        color = PanelBgColor,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).clickable(onClick = onFavorites),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BookmarkButton(saved = false, onClick = onFavorites, modifier = Modifier.size(26.dp))
                            Column(Modifier.padding(start = 8.dp)) {
                                Text("Избранное", color = TextDarkColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Сохранённые анкеты и проекты", color = MutedTextColor, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }

                    OutlineProfileButton(
                        text = "Редактировать профиль",
                        onClick = onEdit,
                        modifier = Modifier.padding(top = 10.dp),
                    )

                    Text(
                        "О себе",
                        color = TextDarkColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 18.dp, bottom = 6.dp),
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
                        modifier = Modifier.padding(top = 16.dp, bottom = 3.dp),
                    )
                    SkillChips(profile.skills)

                    ExpandableHeader("Образование и курсы", educationExpanded) {
                        educationExpanded = !educationExpanded
                    }
                    if (educationExpanded) {
                        Text(
                            listOf(profile.education, profile.courses, profile.experience)
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
}

@Composable
private fun ProfileModeToggle(projectMode: Boolean, onModeChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(36.dp).background(SectionBgColor, RoundedCornerShape(10.dp)).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ProfileModeButton("Персональный", selected = !projectMode, Modifier.weight(1f)) { onModeChange(false) }
        ProfileModeButton("Проектный", selected = projectMode, Modifier.weight(1f)) { onModeChange(true) }
    }
}

@Composable
private fun ProfileModeButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.fillMaxHeight().clickable(onClick = onClick),
        color = if (selected) PrimaryButtonColor else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = if (selected) null else BorderStroke(1.dp, BorderColor),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                color = if (selected) CardBgColor else PrimaryButtonColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun OutlineProfileButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().height(38.dp).clickable(onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BorderColor),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = PrimaryButtonColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
    MaterialTheme { ProfileScreen(previewProfile, {}, {}, {}, {}, false, {}, {}, {}) }
}

@Preview(name = "Мой профиль · планшет", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
fun ProfileTabletScreenPreview() {
    MaterialTheme { ProfileScreen(previewProfile, {}, {}, {}, {}, false, {}, {}, {}) }
}
