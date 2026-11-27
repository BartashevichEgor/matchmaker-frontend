package com.example.projectmatchmaker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.projectmatchmaker.network.CatalogItemDto

@Composable
fun CreateProjectScreen(
    project: ProjectData,
    onProjectChange: (ProjectData) -> Unit,
    roles: List<CatalogItemDto>,
    isSaving: Boolean,
    errorMessage: String?,
    onSaveDraft: () -> Unit,
    onPublish: () -> Unit,
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onChats: () -> Unit,
) {
    val canSave = project.title.trim().length >= 3 && project.description.trim().length >= 10
    val projectRoles = (teamRoleGroups.flatMap { (_, items) -> items.map(TeamRole::title) } + roles.map(CatalogItemDto::name)).distinct()
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
                        value = project.skills,
                        onValueChange = { onProjectChange(project.copy(skills = it)) },
                    )
                    Text(
                        "Вводи навыки через запятую. Они помогут настроить подбор.",
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
    var anchorSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val density = LocalDensity.current
    Box(Modifier.fillMaxWidth().onSizeChanged { anchorSize = it }) {
        Surface(
            color = InputBgColor,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
        ) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(value.ifBlank { placeholder }, color = if (value.isBlank()) MutedTextColor else TextDarkColor, fontSize = 14.sp)
                Text("⌄", color = PrimaryButtonColor, fontSize = 20.sp)
            }
        }
        if (expanded) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, anchorSize.height + with(density) { 6.dp.roundToPx() }),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
            ) {
                Surface(
                    color = CardBgColor,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    shadowElevation = 8.dp,
                    modifier = Modifier.width(with(density) { anchorSize.width.toDp() }).heightIn(max = 300.dp),
                ) {
                    Column(Modifier.verticalScroll(rememberScrollState()).padding(6.dp)) {
                        if (options.isEmpty()) {
                            Text("Список пока недоступен", color = MutedTextColor, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                        } else {
                            options.distinct().forEach { option ->
                                Surface(
                                    color = if (option == value) ChipBgColor else CardBgColor,
                                    shape = RoundedCornerShape(9.dp),
                                    modifier = Modifier.fillMaxWidth().clickable { onSelect(option); expanded = false },
                                ) {
                                    Text(option, color = TextDarkColor, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectSkillsField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val selected = value.split(',').map(String::trim).filter(String::isNotBlank).distinctBy { it.lowercase() }
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Например, Kotlin, Figma", color = MutedTextColor, fontSize = 14.sp) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = InputBgColor,
                unfocusedContainerColor = InputBgColor,
                focusedIndicatorColor = BorderColor,
                unfocusedIndicatorColor = BorderColor,
                focusedTextColor = TextDarkColor,
                unfocusedTextColor = TextDarkColor,
            ),
        )
        if (selected.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                selected.forEach { skill ->
                    Surface(
                        color = ChipBgColor,
                        shape = RoundedCornerShape(15.dp),
                        modifier = Modifier.clickable {
                            onValueChange(value.split(',').map(String::trim).filter { it.isNotBlank() && !it.equals(skill, ignoreCase = true) }.joinToString(", "))
                        },
                    ) {
                        Text("$skill  ×", color = TextDarkColor, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                    }
                }
            }
        }
    }
}

@Preview(name = "Создание проекта", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CreateProjectScreenPreview() {
    MaterialTheme {
        CreateProjectScreen(ProjectData(), {}, emptyList(), false, null, {}, {}, {}, {}, {})
    }
}
