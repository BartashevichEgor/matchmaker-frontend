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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun CreateProfileScreen(
    profile: ProfileData,
    onProfileChange: (ProfileData) -> Unit,
    canContinue: Boolean,
    onNext: () -> Unit,
    onRoleSelect: () -> Unit,
    onCatalog: () -> Unit,
    onMatches: () -> Unit = {},
    onChats: () -> Unit = {},
) {
    var optionalExpanded by remember { mutableStateOf(false) }

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
                    Text("Шаг 1 из 2 · Основное", color = MutedTextColor, fontSize = 12.sp)
                    StepProgress(0.5f)

                    FormLabel("Имя *")
                    FormField(
                        profile.firstName,
                        { onProfileChange(profile.copy(firstName = it)) },
                        "Вероника",
                    )

                    FormLabel("Фамилия *")
                    FormField(
                        profile.lastName,
                        { onProfileChange(profile.copy(lastName = it)) },
                        "Соболь",
                    )

                    FormLabel("Роль в команде *")
                    Row(
                        Modifier.fillMaxWidth().height(52.dp)
                            .background(InputBgColor, RoundedCornerShape(12.dp))
                            .clickable(onClick = onRoleSelect)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            profile.role.ifBlank { "Выберите роль" },
                            color = if (profile.role.isBlank()) MutedTextColor else TextDarkColor,
                            fontSize = 14.sp,
                        )
                        Text("⌄", color = PrimaryButtonColor, fontSize = 22.sp)
                    }
                    Text(
                        "Что означают роли?",
                        color = PrimaryButtonColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )

                    ExpandableHeader("Фото и о себе · необязательно", optionalExpanded) {
                        optionalExpanded = !optionalExpanded
                    }
                    if (optionalExpanded) {
                        FormLabel("О себе")
                        FormField(
                            profile.about,
                            { onProfileChange(profile.copy(about = it)) },
                            "Расскажите, чем вы занимаетесь",
                            minLines = 3,
                        )
                    }
                    Text(
                        if (canContinue) "* Обязательные поля заполнены" else "* Обязательные поля",
                        color = if (canContinue) MutedTextColor else ErrorColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                }
                PrimaryButton("Далее: навыки и образование", onNext, canContinue)
            }
        }
    }
}

@Preview(name = "Профиль · шаг 1", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun CreateProfileScreenPreview() {
    MaterialTheme {
        CreateProfileScreen(ProfileData(), {}, false, {}, {}, {})
    }
}
