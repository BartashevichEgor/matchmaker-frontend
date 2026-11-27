package com.example.projectmatchmaker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectmatchmaker.network.CatalogItemDto

@Composable
fun CreateProjectScreen(
    project: ProjectData,
    onProjectChange: (ProjectData) -> Unit,
    roles: List<CatalogItemDto>,
    skills: List<CatalogItemDto>,
    isSaving: Boolean,
    errorMessage: String?,
    onSaveDraft: () -> Unit,
    onPublish: () -> Unit,
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onChats: () -> Unit,
) {
    val selectedSkills = project.skills.split(',').map(String::trim).filter(String::isNotBlank)
    val canSave = project.title.trim().length >= 3 && project.description.trim().length >= 10
    val projectRoles = (teamRoleGroups.flatMap { (_, items) -> items.map(TeamRole::title) } + roles.map(CatalogItemDto::name)).distinct()
    val projectSkills = skills.map(CatalogItemDto::name)
    val roleCanBeSaved = project.role.isBlank() || roles.any { it.name.equals(project.role, ignoreCase = true) }
    val canSaveDraft = canSave && roleCanBeSaved
    val canPublish = canSave && project.role.isNotBlank() && roleCanBeSaved

    ScreenFrame(MainDestination.PROFILE, onCatalog, {}, onMatches, onChats) {
        PageCard {
            ResponsivePageColumn(phonePadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    PageTitle("Создание проекта")
                    Text(
                        "Одна карточка проекта на аккаунт. Её можно сохранить как черновик.",
                        color = MutedTextColor,
                        fontSize = 12.sp,
                    )
                    FormLabel("Название проекта")
                    FormField(project.title, { onProjectChange(project.copy(title = it)) }, "TeamFlow")
                    FormLabel("Описание")
                    FormField(project.description, { onProjectChange(project.copy(description = it)) }, "Сервис для командной работы", minLines = 3)

                    FormLabel("Кого ищете")
                    ProjectSelectField(
                        value = project.role,
                        placeholder = "Выберите специализацию",
                        options = projectRoles,
                        onSelect = { onProjectChange(project.copy(role = it)) },
                    )
                    Text(
                        "Выбранная роль попадёт в карточку проекта и будет учитываться при подборе специалистов.",
                        color = MutedTextColor,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 5.dp),
                    )

                    FormLabel("Навыки")
                    ProjectSkillsField(
                        selected = selectedSkills,
                        options = projectSkills,
                        onSelectionChange = { onProjectChange(project.copy(skills = it.joinToString(", "))) },
                    )
                    Text(
                        "Выберите навыки, которые нужны команде: они помогут настроить подбор.",
                        color = MutedTextColor,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 5.dp),
                    )

                    FormLabel("Формат работы")
                    ProjectSelectField(
                        value = project.workFormat,
                        placeholder = "Выберите формат работы",
                        options = listOf("Удалённо", "Гибридный", "В офисе"),
                        onSelect = { onProjectChange(project.copy(workFormat = it)) },
                    )
                    FormLabel("Занятость")
                    ProjectSelectField(
                        value = project.employment,
                        placeholder = "Выберите занятость",
                        options = listOf("Гибкий график", "Полная занятость", "Частичная занятость"),
                        onSelect = { onProjectChange(project.copy(employment = it)) },
                    )

                    if (!canSave) {
                        Text(
                            "Для сохранения укажите название (от 3 символов) и описание (от 10 символов).",
                            color = MutedTextColor,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    } else if (project.role.isNotBlank() && !roleCanBeSaved) {
                        Text("Эта специализация пока не поддерживается сервером. Выберите вариант из каталога ролей.", color = MutedTextColor, fontSize = 11.sp, modifier = Modifier.padding(top = 10.dp))
                    } else if (!canPublish) {
                        Text("Для публикации выберите специализацию, которую ищет команда.", color = MutedTextColor, fontSize = 11.sp, modifier = Modifier.padding(top = 10.dp))
                    }
                    if (!errorMessage.isNullOrBlank()) {
                        Text(errorMessage, color = ErrorColor, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
                    }

                    Row(
                        Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SecondaryButton(
                            if (isSaving) "Сохраняем…" else "В черновик",
                            onSaveDraft,
                            Modifier.weight(1f),
                            enabled = canSaveDraft && !isSaving,
                        )
                        PrimaryButton(
                            if (isSaving) "Сохраняем…" else "Опубликовать",
                            onPublish,
                            enabled = canPublish && !isSaving,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectSelectField(
    value: String,
    placeholder: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        Surface(
            color = InputBgColor,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
        ) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(value.ifBlank { placeholder }, color = if (value.isBlank()) MutedTextColor else TextDarkColor, fontSize = 14.sp)
                Text("⌄", color = PrimaryButtonColor, fontSize = 20.sp)
            }
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 320.dp)) {
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("Список пока недоступен", color = MutedTextColor) }, onClick = { expanded = false }, enabled = false)
            } else {
                options.distinct().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = TextDarkColor) },
                        onClick = { onSelect(option); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectSkillsField(
    selected: List<String>,
    options: List<String>,
    onSelectionChange: (List<String>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        Surface(
            color = InputBgColor,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
        ) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(selected.joinToString(", ").ifBlank { if (options.isEmpty()) "Список навыков недоступен" else "Выберите нужные навыки" }, color = if (selected.isEmpty()) MutedTextColor else TextDarkColor, fontSize = 14.sp)
                Text("⌄", color = PrimaryButtonColor, fontSize = 20.sp)
            }
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 320.dp)) {
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("Навыки не загрузились. Проверьте соединение и откройте экран снова.", color = MutedTextColor) }, onClick = { expanded = false }, enabled = false)
            }
            options.distinct().forEach { option ->
                val isSelected = option in selected
                DropdownMenuItem(
                    text = { Text("${if (isSelected) "✓" else "○"}  $option", color = TextDarkColor) },
                    onClick = {
                        onSelectionChange(if (isSelected) selected - option else selected + option)
                    },
                )
            }
        }
    }
}

@Preview(name = "Создание проекта", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CreateProjectScreenPreview() {
    MaterialTheme {
        CreateProjectScreen(ProjectData(), {}, emptyList(), emptyList(), false, null, {}, {}, {}, {}, {})
    }
}
