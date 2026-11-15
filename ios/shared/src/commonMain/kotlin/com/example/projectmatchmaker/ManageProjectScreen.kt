package com.example.projectmatchmaker

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ManageProjectScreen(
    project: ProjectData,
    onProjectChange: (ProjectData) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onChats: () -> Unit,
) {
    ScreenFrame(MainDestination.PROFILE, onCatalog, {}, onMatches, onChats) {
        PageCard {
            ResponsivePageColumn(phonePadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
                androidx.compose.foundation.layout.Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    PageTitle("Управление проектом")
                    Text("Здесь можно редактировать проект, менять видимость и закрывать набор.", color = MutedTextColor, fontSize = 12.sp)
                    FormLabel("Видимость карточки")
                    SecondaryButton(if (project.visible) "Опубликовано" else "Скрыто", { onProjectChange(project.copy(visible = !project.visible)) })
                    FormLabel("Статус набора")
                    SecondaryButton(if (project.published) "Закрыть набор" else "Открыть набор", { onProjectChange(project.copy(published = !project.published)) })
                    FormLabel("Название проекта")
                    FormField(project.title, { onProjectChange(project.copy(title = it)) }, "TeamFlow")
                    FormLabel("Описание")
                    FormField(project.description, { onProjectChange(project.copy(description = it)) }, "Сервис для командной работы")
                    FormLabel("Кого ищете")
                    FormField(project.role, { onProjectChange(project.copy(role = it)) }, "Backend-разработчик")
                    FormLabel("Навыки")
                    FormField(project.skills, { onProjectChange(project.copy(skills = it)) }, "Python, SQL")
                    androidx.compose.foundation.layout.Row(
                        Modifier.fillMaxWidth().padding(top = 18.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
                    ) {
                        SecondaryButton("Скрыть", onBack, Modifier.weight(1f))
                        PrimaryButton("Сохранить", onSave, project.title.isNotBlank(), Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Preview(name = "Управление проектом", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun ManageProjectScreenPreview() {
    MaterialTheme {
        ManageProjectScreen(
            ProjectData(
                title = "TeamFlow",
                description = "Сервис",
                role = "Backend-разработчик",
                skills = "Python, SQL",
            ),
            {}, {}, {}, {}, {}, {},
        )
    }
}
