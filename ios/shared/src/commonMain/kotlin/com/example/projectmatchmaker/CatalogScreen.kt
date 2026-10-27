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

@Composable
fun CatalogScreen(
    favorites: Set<FavoriteItem> = emptySet(),
    onFavoriteToggle: (FavoriteItem) -> Unit = {},
    showProjectsInitially: Boolean = false,
    onCatalog: () -> Unit,
    onProfile: () -> Unit,
    onMatches: () -> Unit,
    onChats: () -> Unit,
) {
    var tab by remember {
        mutableStateOf(if (showProjectsInitially) CatalogTab.PROJECTS else CatalogTab.PEOPLE)
    }
    ScreenFrame(MainDestination.CATALOG, onCatalog, onProfile, onMatches, onChats) {
        PageCard {
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    PageTitle("Каталог")
                    FilterIcon()
                }
                Text("Находи людей и проекты для своей команды", color = MutedTextColor, fontSize = 11.sp, modifier = Modifier.padding(bottom = 14.dp))
                CatalogTabs(tab) { tab = it }
                Spacer(Modifier.height(14.dp))
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    if (tab == CatalogTab.PEOPLE) {
                        PersonCatalogCard(FavoriteItem.PERSON in favorites) { onFavoriteToggle(FavoriteItem.PERSON) }
                    } else {
                        ProjectCatalogCard(FavoriteItem.PROJECT in favorites) { onFavoriteToggle(FavoriteItem.PROJECT) }
                    }
                    Text("Закладка — сохранить без лайка.", color = MutedTextColor, fontSize = 10.sp, modifier = Modifier.padding(top = 22.dp))
                }
                SwipeActions()
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
private fun PersonCatalogCard(saved: Boolean, onSave: () -> Unit) {
    Surface(color = PanelBgColor, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.fillMaxWidth().height(112.dp).background(ProfileAvatarColor, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Surface(color = PrimaryButtonColor, shape = RoundedCornerShape(16.dp), modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)) {
                    Text("87% совпадение", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp))
                }
                Text("АК", color = CardBgColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Алексей Ковалёв", color = TextDarkColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                BookmarkButton(saved, onSave)
            }
            Text("Backend-разработчик", color = MutedTextColor, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            SkillChips(listOf("Python", "Django", "SQL"))
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
private fun ProjectCatalogCard(saved: Boolean, onSave: () -> Unit) {
    Surface(color = PanelBgColor, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.fillMaxWidth().height(100.dp).background(SectionBgColor, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TeamFlow", color = PrimaryButtonColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Команда для учебного проекта", color = TextDarkColor, fontSize = 11.sp)
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("TeamFlow", color = TextDarkColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                BookmarkButton(saved, onSave)
            }
            Text("Ищем UI/UX-дизайнера", color = MutedTextColor, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            SkillChips(listOf("Figma", "UX", "UI"))
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
private fun SwipeActions() {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        SwipeActionButton(false, "Не интересует")
        SwipeActionButton(true, "Выразить интерес")
    }
}

@Composable
private fun SwipeActionButton(liked: Boolean, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(if (liked) 58.dp else 54.dp).clickable { },
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
            favorites = setOf(FavoriteItem.PERSON),
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
            favorites = setOf(FavoriteItem.PROJECT),
            showProjectsInitially = true,
            onCatalog = {},
            onProfile = {},
            onMatches = {},
            onChats = {},
        )
    }
}
