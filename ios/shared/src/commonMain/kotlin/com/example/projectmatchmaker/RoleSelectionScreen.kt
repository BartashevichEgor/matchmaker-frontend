package com.example.projectmatchmaker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val roleOptions = listOf(
    "Дизайн" to listOf(
        "UI/UX-дизайнер" to "Проектирует интерфейсы и пользовательские сценарии.",
        "UX-исследователь" to "Изучает потребности и поведение пользователей.",
        "Продуктовый дизайнер" to "Развивает продукт через дизайн и исследования.",
        "Графический дизайнер" to "Создаёт фирменный стиль и визуальные материалы.",
        "Motion-дизайнер" to "Создаёт анимацию и движение.",
    ),
    "Разработка" to listOf(
        "Frontend-разработчик" to "Создаёт пользовательскую часть сайта.",
        "Backend-разработчик" to "Разрабатывает сервер, API и базы данных.",
        "Fullstack-разработчик" to "Работает с интерфейсом и сервером.",
        "iOS-разработчик" to "Создаёт приложения для iPhone и iPad.",
        "Android-разработчик" to "Создаёт приложения для Android.",
    ),
)

@Composable
fun RoleSelectionScreen(
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    onDone: () -> Unit,
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onChats: () -> Unit,
    onProfile: () -> Unit,
) {
    ScreenFrame(MainDestination.PROFILE, onCatalog, onProfile, onMatches, onChats) {
        PageCard {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
            ) {
                PageTitle("Выбор роли")
                Text(
                    "Выбери основную роль. Под каждым названием — пояснение.",
                    color = MutedTextColor,
                    fontSize = 12.sp,
                )
                Text(
                    "Выбери вариант, затем нажми «Готово»",
                    color = MutedTextColor,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(20.dp))
                Surface(
                    color = PanelBgColor,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onDone),
                ) {
                    Text(
                        "Готово",
                        color = PrimaryButtonColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(15.dp),
                    )
                }
                roleOptions.forEach { (group, roles) ->
                    Text(
                        group,
                        color = TextDarkColor,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
                    )
                    roles.forEach { (title, description) ->
                        val selected = title == selectedRole
                        Surface(
                            color = if (selected) ChipBgColor else PanelBgColor,
                            shape = RoundedCornerShape(12.dp),
                            border = if (selected) BorderStroke(1.dp, PrimaryButtonColor) else null,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                .clickable { onRoleSelected(title) },
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(title, color = TextDarkColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(description, color = MutedTextColor, fontSize = 12.sp, lineHeight = 17.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Выбор роли · телефон", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun RoleSelectionScreenPreview() {
    MaterialTheme { RoleSelectionScreen("UI/UX-дизайнер", {}, {}, {}, {}, {}, {}) }
}
