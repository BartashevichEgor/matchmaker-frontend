package com.example.projectmatchmaker

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateProjectScreen(
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
                androidx.compose.foundation.layout.Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                ) {
                    PageTitle("Создание проекта")
                    androidx.compose.material3.Text(
                        "Одна карточка проекта на аккаунт. Её можно сохранить как черновик.",
                        color = MutedTextColor,
                        fontSize = 12.sp,
                    )
                    FormLabel("Название проекта")
                    FormField(project.title, { onProjectChange(project.copy(title = it)) }, "TeamFlow")
                    FormLabel("Описание")
                    FormField(project.description, { onProjectChange(project.copy(description = it)) }, "Сервис для командной работы")
                    FormLabel("Кого ищете")
                    FormField(project.role, { onProjectChange(project.copy(role = it)) }, "Backend-разработчик")
                    FormLabel("Навыки")
                    FormField(project.skills, { onProjectChange(project.copy(skills = it)) }, "Python, SQL")
                    FormLabel("Формат работы")
                    FormField(project.workFormat, { onProjectChange(project.copy(workFormat = it)) }, "Удалённо")
                    FormLabel("Занятость")
                    FormField(project.employment, { onProjectChange(project.copy(employment = it)) }, "Гибкий график")
                    androidx.compose.foundation.layout.Row(
                        Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
                    ) {
                        SecondaryButton("В черновик", onBack, Modifier.weight(1f))
                        PrimaryButton("Опубликовать", onSave, project.title.isNotBlank(), Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Preview(name = "Создание проекта", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CreateProjectScreenPreview() {
    MaterialTheme { CreateProjectScreen(ProjectData(), {}, {}, {}, {}, {}, {}) }
}
