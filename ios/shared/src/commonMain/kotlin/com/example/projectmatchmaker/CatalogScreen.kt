package com.example.projectmatchmaker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class CatalogTab { PROJECTS, PEOPLE }
private enum class CatalogWorkFormat(val label: String) {
    ANY("Любой формат"), REMOTE("Удалённо"), HYBRID("Гибридный"), OFFICE("В офисе")
}

private data class CatalogFilter(
    val role: String = "Все роли",
    val skill: String = "Любые навыки",
    val workFormat: CatalogWorkFormat = CatalogWorkFormat.ANY,
)

private val demoPeople = listOf(
    CatalogPersonItem("Алексей Ковалёв", "Backend-разработчик", "Люблю чистый код. Ищу команду для полезного проекта.", listOf("Python", "Django", "SQL"), 87, workFormat = "Удалённо"),
    CatalogPersonItem("Мария Волкова", "Frontend-разработчик", "Создаю удобные интерфейсы и хочу развивать продукт вместе с командой.", listOf("React", "TypeScript", "Figma"), 82, workFormat = "Гибридный"),
    CatalogPersonItem("Илья Смирнов", "Android-разработчик", "Разрабатываю мобильные приложения, открыт к учебным и командным проектам.", listOf("Kotlin", "Compose", "Ktor"), 79, workFormat = "Удалённо"),
)

private val demoProjects = listOf(
    CatalogProjectItem("TeamFlow", "Команда для учебного проекта", "Ищем UI/UX-дизайнера", listOf("Figma", "UX", "UI"), 87, workFormat = "Удалённо"),
    CatalogProjectItem("EcoTrack", "Приложение для экологичных привычек", "Ищем Android-разработчика", listOf("Kotlin", "Compose", "API"), 81, workFormat = "Гибридный"),
    CatalogProjectItem("StudyBuddy", "Сервис для совместной подготовки к учёбе", "Ищем frontend-разработчика", listOf("React", "TypeScript", "UI"), 76, workFormat = "В офисе"),
)

data class CatalogProjectItem(
    val title: String,
    val description: String,
    val role: String,
    val skills: List<String>,
    val score: Int? = null,
    val id: Int? = null,
    val workFormat: String? = null,
)

data class CatalogPersonItem(
    val name: String,
    val role: String,
    val bio: String,
    val skills: List<String>,
    val score: Int? = null,
    val id: Int? = null,
    val workFormat: String? = null,
)

@Composable
fun CatalogScreen(
    favorites: Set<FavoriteItem> = emptySet(),
    onFavoriteToggle: (FavoriteItem) -> Unit = {},
    showProjectsInitially: Boolean = false,
    showProjectsTab: Boolean = true,
    showPeopleTab: Boolean = true,
    onCatalog: () -> Unit,
    onProfile: () -> Unit,
    onMatches: () -> Unit,
    onChats: () -> Unit,
    projects: List<CatalogProjectItem> = emptyList(),
    people: List<CatalogPersonItem> = emptyList(),
    backendEnabled: Boolean = false,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onSwipeProject: (Int, String) -> Unit = { _, _ -> },
    onSwipePerson: (Int, String) -> Unit = { _, _ -> },
) {
    var selectedTab by remember(showProjectsInitially) {
        mutableStateOf(if (showProjectsInitially) CatalogTab.PROJECTS else CatalogTab.PEOPLE)
    }
    val canSwitchTabs = showProjectsTab && showPeopleTab
    val tab = when {
        showProjectsTab && !showPeopleTab -> CatalogTab.PROJECTS
        showPeopleTab && !showProjectsTab -> CatalogTab.PEOPLE
        else -> selectedTab
    }
    var cardIndex by remember { mutableStateOf(0) }
    var showFilters by remember { mutableStateOf(false) }
    var appliedFilter by remember { mutableStateOf(CatalogFilter()) }
    var draftFilter by remember { mutableStateOf(CatalogFilter()) }
    var showExamples by remember { mutableStateOf(false) }
    val hasCardsForMode = if (tab == CatalogTab.PROJECTS) projects.isNotEmpty() else people.isNotEmpty()
    val demoMode = showExamples || (!hasCardsForMode && !isLoading)
    val availableProjects = if (demoMode) demoProjects else projects
    val availablePeople = if (demoMode) demoPeople else people
    val filteredProjects = availableProjects.filter { item ->
        (appliedFilter.role == "Все роли" || item.role.contains(appliedFilter.role, ignoreCase = true)) &&
            (appliedFilter.skill == "Любые навыки" || item.skills.any { it.equals(appliedFilter.skill, ignoreCase = true) }) &&
            (appliedFilter.workFormat == CatalogWorkFormat.ANY || item.workFormat.equals(appliedFilter.workFormat.label, ignoreCase = true))
    }
    val filteredPeople = availablePeople.filter { item ->
        (appliedFilter.role == "Все роли" || item.role.contains(appliedFilter.role, ignoreCase = true)) &&
            (appliedFilter.skill == "Любые навыки" || item.skills.any { it.equals(appliedFilter.skill, ignoreCase = true) }) &&
            (appliedFilter.workFormat == CatalogWorkFormat.ANY || item.workFormat.equals(appliedFilter.workFormat.label, ignoreCase = true))
    }
    val visibleProject = filteredProjects.getOrNull(cardIndex)
    val visiblePerson = filteredPeople.getOrNull(cardIndex)
    val noVisibleCard = if (tab == CatalogTab.PROJECTS) visibleProject == null else visiblePerson == null
    ScreenFrame(MainDestination.CATALOG, onCatalog, onProfile, onMatches, onChats) {
        PageCard {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val isTablet = maxWidth >= 600.dp
                val horizontalPadding = if (isTablet) 64.dp else 16.dp
                Column(
                    Modifier.fillMaxSize().padding(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = if (isTablet) 28.dp else 14.dp,
                        bottom = 14.dp,
                    ),
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        PageTitle("Каталог")
                        Box(Modifier.size(36.dp).clickable {
                            draftFilter = appliedFilter
                            showFilters = true
                        }, contentAlignment = Alignment.Center) { FilterIcon() }
                    }
                    if (showFilters) {
                        CatalogFilters(
                            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                            filter = draftFilter,
                            roles = (if (tab == CatalogTab.PROJECTS) filteredRoleNames(availableProjects.map { it.role }) else filteredRoleNames(availablePeople.map { it.role })),
                            skills = (if (tab == CatalogTab.PROJECTS) availableProjects.flatMap { it.skills } else availablePeople.flatMap { it.skills }).distinct().sorted(),
                            onFilterChange = { draftFilter = it },
                            onApply = {
                                appliedFilter = draftFilter
                                cardIndex = 0
                                showFilters = false
                            },
                            onBack = { showFilters = false },
                        )
                    } else {
                    Text("Находи людей и проекты для своей команды", color = MutedTextColor, fontSize = 11.sp, modifier = Modifier.padding(bottom = 14.dp))
                    if (canSwitchTabs) {
                        CatalogTabs(tab) {
                            selectedTab = it
                            cardIndex = 0
                            appliedFilter = CatalogFilter()
                            draftFilter = CatalogFilter()
                        }
                    } else {
                        Surface(
                            color = SectionBgColor,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.padding(bottom = 8.dp),
                        ) {
                            Text(
                                if (tab == CatalogTab.PROJECTS) "Проекты для вашей анкеты" else "Специалисты для вашего проекта",
                                color = PrimaryButtonColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                            )
                        }
                    }
                    Text(
                        if (tab == CatalogTab.PROJECTS) {
                            "Подбираем команды для вашей анкеты."
                        } else {
                            "Подбираем специалистов для вашего проекта."
                        },
                        color = MutedTextColor,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        modifier = Modifier.padding(top = 5.dp, bottom = 10.dp),
                    )
                    if (isLoading) {
                        Text("Загружаем карточки…", color = TextDarkColor, fontSize = 13.sp, modifier = Modifier.padding(vertical = 16.dp))
                    } else if (errorMessage != null) {
                        Text(errorMessage, color = ErrorColor, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                        PrimaryButton("Повторить загрузку", onRetry)
                    }
                    if (!demoMode && noVisibleCard && !isLoading) {
                        Text(
                            if (errorMessage != null) "Пока можно открыть примеры и проверить фильтры." else "Пока нет опубликованных анкет для этого режима.",
                            color = MutedTextColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 10.dp, bottom = 8.dp),
                        )
                        PrimaryButton("Посмотреть примеры", onClick = {
                            showExamples = true
                            cardIndex = 0
                            appliedFilter = CatalogFilter()
                            draftFilter = CatalogFilter()
                        })
                    }
                    if (demoMode) {
                        Text("Примеры · фильтры и закладки работают локально", color = MutedTextColor, fontSize = 10.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    if (!isLoading && noVisibleCard && !(backendEnabled && !demoMode)) {
                        val hasFilters = appliedFilter != CatalogFilter()
                        Text(
                            if (hasFilters) "По выбранным фильтрам ничего не найдено." else "Карточки закончились. Можно начать просмотр заново.",
                            color = TextDarkColor,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f).padding(top = 20.dp),
                        )
                        if (hasFilters) {
                            PrimaryButton("Сбросить фильтры", onClick = { appliedFilter = CatalogFilter(); draftFilter = CatalogFilter(); cardIndex = 0 })
                        } else {
                            PrimaryButton("Посмотреть сначала", onClick = { cardIndex = 0 })
                        }
                    } else if (!isLoading && isTablet) {
                        Row(
                            Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                                CatalogCard(tab, favorites, onFavoriteToggle, visibleProject, visiblePerson)
                                Text("Закладка - сохранить без лайка.", color = MutedTextColor, fontSize = 10.sp, modifier = Modifier.padding(top = 22.dp))
                            }
                        }
                    } else if (!isLoading) {
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            CatalogCard(tab, favorites, onFavoriteToggle, visibleProject, visiblePerson)
                            Text("Закладка - сохранить без лайка.", color = MutedTextColor, fontSize = 10.sp, modifier = Modifier.padding(top = 22.dp))
                        }
                    }
                    SwipeActions(
                        enabled = !isLoading && !noVisibleCard,
                        onPass = {
                            if (backendEnabled && !demoMode) {
                                if (tab == CatalogTab.PROJECTS) visibleProject?.id?.let { onSwipeProject(it, "pass") }
                                else visiblePerson?.id?.let { onSwipePerson(it, "pass") }
                            }
                            cardIndex += 1
                        },
                        onLike = {
                            if (backendEnabled && !demoMode) {
                                if (tab == CatalogTab.PROJECTS) visibleProject?.id?.let { onSwipeProject(it, "like") }
                                else visiblePerson?.id?.let { onSwipePerson(it, "like") }
                            }
                            cardIndex += 1
                        },
                    )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogCard(
    tab: CatalogTab,
    favorites: Set<FavoriteItem>,
    onFavoriteToggle: (FavoriteItem) -> Unit,
    project: CatalogProjectItem?,
    person: CatalogPersonItem?,
) {
    if (tab == CatalogTab.PEOPLE) {
        PersonCatalogCard(
            saved = person?.let { it.toFavoriteItem() in favorites } ?: false,
            person = person,
        ) { person?.toFavoriteItem()?.let(onFavoriteToggle) }
    } else {
        ProjectCatalogCard(
            saved = project?.let { it.toFavoriteItem() in favorites } ?: false,
            project = project,
        ) { project?.toFavoriteItem()?.let(onFavoriteToggle) }
    }
}

private fun CatalogPersonItem.toFavoriteItem() = FavoriteItem(
    type = FavoriteType.PERSON,
    id = id?.toString() ?: "demo:$name",
    title = name,
    subtitle = role,
    skills = skills,
)

private fun CatalogProjectItem.toFavoriteItem() = FavoriteItem(
    type = FavoriteType.PROJECT,
    id = id?.toString() ?: "demo:$title",
    title = title,
    subtitle = role,
    skills = skills,
)

@Composable
private fun filteredRoleNames(roles: List<String>) = roles.distinct().sorted()

@Composable
private fun CatalogFilters(
    modifier: Modifier = Modifier,
    filter: CatalogFilter,
    roles: List<String>,
    skills: List<String>,
    onFilterChange: (CatalogFilter) -> Unit,
    onApply: () -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier.padding(4.dp)) {
        Text("Фильтры", color = TextDarkColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Подбери анкеты по роли, навыку и формату работы", color = MutedTextColor, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp, bottom = 16.dp))
        FilterDropdown("Роль", filter.role, listOf("Все роли") + roles) { onFilterChange(filter.copy(role = it)) }
        FilterDropdown("Навыки", filter.skill, listOf("Любые навыки") + skills) { onFilterChange(filter.copy(skill = it)) }
        FilterDropdown("Формат работы", filter.workFormat.label, CatalogWorkFormat.values().map { it.label }) { label ->
            onFilterChange(filter.copy(workFormat = CatalogWorkFormat.values().first { it.label == label }))
        }
        PrimaryButton("Применить", onApply, modifier = Modifier.fillMaxWidth().padding(top = 14.dp))
        SecondaryButton("Назад", onBack, Modifier.fillMaxWidth().padding(top = 8.dp))
    }
}

@Composable
private fun FilterDropdown(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember(label) { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(label, color = TextDarkColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 7.dp))
        Box {
            Surface(
                color = InputBgColor, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(selected, color = TextDarkColor, fontSize = 14.sp)
                    Text("⌄", color = PrimaryButtonColor, fontSize = 16.sp)
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 280.dp).background(CardBgColor)) {
                options.forEach { option ->
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
private fun CatalogTabs(selected: CatalogTab, onSelect: (CatalogTab) -> Unit) {
    Row(Modifier.fillMaxWidth().height(44.dp).background(SectionBgColor, RoundedCornerShape(12.dp)).padding(3.dp)) {
        CatalogTabButton("Специалисты", selected == CatalogTab.PEOPLE, Modifier.weight(1f)) { onSelect(CatalogTab.PEOPLE) }
        CatalogTabButton("Проекты", selected == CatalogTab.PROJECTS, Modifier.weight(1f)) { onSelect(CatalogTab.PROJECTS) }
    }
}

@Composable
private fun CatalogTabButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.fillMaxHeight().background(if (selected) PrimaryButtonColor else Color.Transparent, RoundedCornerShape(9.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (selected) CardBgColor else TextDarkColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PersonCatalogCard(saved: Boolean, person: CatalogPersonItem? = null, onSave: () -> Unit) {
    Surface(color = PanelBgColor, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.fillMaxWidth().height(112.dp).background(ProfileAvatarColor, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Surface(color = PrimaryButtonColor, shape = RoundedCornerShape(16.dp), modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)) {
                    Text("87% совпадение", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp))
                }
                Text(initials(person?.name ?: "Алексей Ковалёв"), color = CardBgColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(person?.name ?: "Алексей Ковалёв", color = TextDarkColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                BookmarkButton(saved, onSave)
            }
            Text(person?.role ?: "Backend-разработчик", color = MutedTextColor, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            SkillChips(person?.skills ?: listOf("Python", "Django", "SQL"))
            ExpandableHeader("О себе", false) {}
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.padding(top = 12.dp).clickable { },
            ) {
                Text("Открыть анкету", color = PrimaryButtonColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp))
            }
        }
    }
}

@Composable
private fun ProjectCatalogCard(saved: Boolean, project: CatalogProjectItem? = null, onSave: () -> Unit) {
    Surface(color = PanelBgColor, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.fillMaxWidth().height(100.dp).background(SectionBgColor, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(project?.title ?: "TeamFlow", color = PrimaryButtonColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(project?.description ?: "Команда для учебного проекта", color = TextDarkColor, fontSize = 11.sp)
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(project?.title ?: "TeamFlow", color = TextDarkColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                BookmarkButton(saved, onSave)
            }
            Text(project?.role ?: "Ищем UI/UX-дизайнера", color = MutedTextColor, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            SkillChips(project?.skills ?: listOf("Figma", "UX", "UI"))
            ExpandableHeader("Описание проекта", false) {}
        }
    }
}

@Composable
internal fun BookmarkButton(saved: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.size(34.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(width = 18.dp, height = 23.dp)) {
            val stroke = 1.7.dp.toPx()
            val path = Path().apply {
                moveTo(stroke, stroke)
                lineTo(size.width - stroke, stroke)
                lineTo(size.width - stroke, size.height - stroke)
                lineTo(size.width / 2, size.height * 0.72f)
                lineTo(stroke, size.height - stroke)
                close()
            }
            if (saved) drawPath(path, PrimaryButtonColor) else drawPath(path, PrimaryButtonColor, style = Stroke(stroke, cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun SwipeActions(enabled: Boolean = true, onPass: () -> Unit = {}, onLike: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        SwipeActionButton(false, "Не интересует", enabled, onPass)
        SwipeActionButton(true, "Выразить интерес", enabled, onLike)
    }
}

@Composable
private fun SwipeActionButton(liked: Boolean, label: String, enabled: Boolean = true, onClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(if (liked) 58.dp else 54.dp).clickable(enabled = enabled, onClick = onClick),
            color = if (liked) PrimaryButtonColor else CardBgColor,
            shape = CircleShape,
            border = if (liked) null else BorderStroke(1.dp, MutedTextColor),
        ) {
            Canvas(Modifier.padding(15.dp)) {
                val color = if (liked) CardBgColor else PrimaryButtonColor
                val stroke = 1.8.dp.toPx()
                if (liked) {
                    val p = Path().apply {
                        moveTo(size.width / 2, size.height * 0.88f)
                        cubicTo(size.width * 0.37f, size.height * 0.72f, size.width * 0.08f, size.height * 0.55f, size.width * 0.08f, size.height * 0.3f)
                        cubicTo(size.width * 0.08f, 0f, size.width * 0.38f, 0f, size.width / 2, size.height * 0.22f)
                        cubicTo(size.width * 0.62f, 0f, size.width * 0.92f, 0f, size.width * 0.92f, size.height * 0.3f)
                        cubicTo(size.width * 0.92f, size.height * 0.55f, size.width * 0.63f, size.height * 0.72f, size.width / 2, size.height * 0.88f)
                    }
                    drawPath(p, color, style = Stroke(stroke, cap = StrokeCap.Round))
                } else {
                    drawLine(color, Offset(2.dp.toPx(), 2.dp.toPx()), Offset(size.width - 2.dp.toPx(), size.height - 2.dp.toPx()), stroke, StrokeCap.Round)
                    drawLine(color, Offset(size.width - 2.dp.toPx(), 2.dp.toPx()), Offset(2.dp.toPx(), size.height - 2.dp.toPx()), stroke, StrokeCap.Round)
                }
            }
        }
        Text(label, color = PrimaryButtonColor, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun FilterIcon() {
    Canvas(Modifier.size(28.dp)) {
        val stroke = 1.6.dp.toPx()
        listOf(7f, 14f, 21f).forEachIndexed { index, y ->
            drawLine(PrimaryButtonColor, Offset(3.dp.toPx(), y.dp.toPx()), Offset(25.dp.toPx(), y.dp.toPx()), stroke, StrokeCap.Round)
            val x = listOf(10, 19, 13)[index].dp.toPx()
            drawCircle(CardBgColor, 3.dp.toPx(), Offset(x, y.dp.toPx()))
            drawCircle(PrimaryButtonColor, 2.dp.toPx(), Offset(x, y.dp.toPx()))
        }
    }
}

@Preview(name = "Каталог · телефон", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun CatalogScreenPreview() {
    MaterialTheme {
        CatalogScreen(
            favorites = setOf(FavoriteItem(FavoriteType.PERSON, "sample-person", "Алексей Ковалёв", "Backend-разработчик", listOf("Python"))),
            showProjectsInitially = true,
            onCatalog = {},
            onProfile = {},
            onMatches = {},
            onChats = {},
        )
    }
}

@Preview(name = "Каталог проектов · телефон", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun ProjectCatalogScreenPreview() {
    MaterialTheme {
        CatalogScreen(
            favorites = setOf(FavoriteItem(FavoriteType.PROJECT, "sample-project", "TeamFlow", "Ищем UI/UX-дизайнера", listOf("Figma"))),
            showProjectsInitially = true,
            onCatalog = {},
            onProfile = {},
            onMatches = {},
            onChats = {},
        )
    }
}

@Preview(name = "Каталог · планшет", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
fun CatalogTabletScreenPreview() {
    MaterialTheme {
        CatalogScreen(
            favorites = setOf(FavoriteItem(FavoriteType.PERSON, "sample-person", "Алексей Ковалёв", "Backend-разработчик", listOf("Python"))),
            onCatalog = {},
            onProfile = {},
            onMatches = {},
            onChats = {},
        )
    }
}
