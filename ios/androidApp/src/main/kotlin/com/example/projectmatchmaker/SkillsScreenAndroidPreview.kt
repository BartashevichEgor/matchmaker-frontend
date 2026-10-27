package com.example.projectmatchmaker

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Android-обёртка нужна только для предпросмотра в Android Studio.
 * Сам экран и его логика остаются в shared/commonMain.
 */
@Preview(
    name = "Навыки и опыт",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
)
@Composable
fun SkillsScreenAndroidPreview() {
    SkillsScreen(
        profile = ProfileData(
            firstName = "Алина",
            role = "iOS-разработчик",
            skills = listOf("Kotlin", "KMP", "Compose"),
            experience = "Учебные и командные проекты",
            education = "Студентка",
        ),
        onProfileChange = {},
        canCreate = true,
        onBack = {},
        onCreate = {},
        onEducationSelect = {},
        onCatalog = {},
    )
}
