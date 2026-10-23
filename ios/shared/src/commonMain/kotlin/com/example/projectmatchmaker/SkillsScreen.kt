package com.example.projectmatchmaker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
fun SkillsScreen(
    profile: ProfileData,
    onProfileChange: (ProfileData) -> Unit,
    canCreate: Boolean,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onEducationSelect: () -> Unit,
    onCatalog: () -> Unit,
    onMatches: () -> Unit = {},
    onChats: () -> Unit = {},
) {
    var skillText by remember { mutableStateOf("") }
    var coursesExpanded by remember { mutableStateOf(false) }

    ScreenFrame(
        selected = MainDestination.PROFILE,
        onCatalog = onCatalog,
        onProfile = {},
        onMatches = onMatches,
        onChats = onChats,
    ) {
        PageCard {
            Column(Modifier.fillMaxSize().padding(12.dp)) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    PageTitle("Создание профиля")
                    Text("Шаг 2 из 2 · Навыки и образование", color = MutedTextColor, fontSize = 12.sp)
                    StepProgress(1f)

                    FormLabel("Навыки *")
                    if (profile.skills.isNotEmpty()) {
                        SkillChips(profile.skills) { removed ->
                            onProfileChange(profile.copy(skills = profile.skills - removed))
                        }
                    }
                    Box(Modifier.padding(top = 4.dp)) {
                        FormField(skillText, { skillText = it }, "Добавить навык")
                        Text(
                            "+",
                            color = PrimaryButtonColor,
                            fontSize = 30.sp,
                            modifier = Modifier.align(Alignment.CenterEnd).clickable {
                                val skill = skillText.trim()
                                if (skill.isNotEmpty() && profile.skills.none { it.equals(skill, true) }) {
                                    onProfileChange(profile.copy(skills = profile.skills + skill))
                                    skillText = ""
                                }
                            }.padding(horizontal = 15.dp),
                        )
                    }

                    FormLabel("Опыт работы")
                    FormField(
                        profile.experience,
                        { onProfileChange(profile.copy(experience = it)) },
                        "Учебные и личные проекты",
                    )

                    ExpandableHeader(
                        profile.education.ifBlank { "Образование" },
                        expanded = false,
                        onClick = onEducationSelect,
                    )
                    ExpandableHeader("Курсы и сертификаты", coursesExpanded) {
                        coursesExpanded = !coursesExpanded
                    }
                    if (coursesExpanded) {
                        Text(
                            "Этот раздел можно заполнить позже",
                            color = MutedTextColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PanelBgColor),
                        border = BorderStroke(1.dp, BorderColor),
                    ) {
                        Text("Назад", color = PrimaryButtonColor, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.weight(2f)) {
                        PrimaryButton("Сохранить профиль", onCreate, canCreate)
                    }
                }
            }
        }
    }
}

@Preview(name = "Профиль · шаг 2", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun SkillsScreenPreview() {
    MaterialTheme {
        SkillsScreen(
            ProfileData(skills = listOf("Figma", "UX", "Mermaid"), experience = "Учебные и личные проекты"),
            {},
            true,
            {},
            {},
            {},
            {},
        )
    }
}
