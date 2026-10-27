package com.example.projectmatchmaker

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import projectmatchmaker.shared.generated.resources.Res
import projectmatchmaker.shared.generated.resources.bg_circle_bottom
import projectmatchmaker.shared.generated.resources.bg_circle_top
import projectmatchmaker.shared.generated.resources.icon_chat
import projectmatchmaker.shared.generated.resources.icon_eye
import projectmatchmaker.shared.generated.resources.icon_grid
import projectmatchmaker.shared.generated.resources.icon_heart
import projectmatchmaker.shared.generated.resources.icon_user
import projectmatchmaker.shared.generated.resources.matchmaker_logo

enum class MainDestination { CATALOG, MATCHES, CHATS, PROFILE }

@Composable
fun ScreenFrame(
    selected: MainDestination,
    onCatalog: () -> Unit,
    onProfile: () -> Unit,
    onMatches: () -> Unit = {},
    onChats: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(DarkBgColor)) {
        Image(
            painterResource(Res.drawable.bg_circle_top),
            contentDescription = null,
            modifier = Modifier.size(226.dp).offset((-50).dp, (-55).dp),
        )
        Image(
            painterResource(Res.drawable.bg_circle_bottom),
            contentDescription = null,
            modifier = Modifier.size(293.dp).align(Alignment.BottomEnd).offset(82.dp, 82.dp),
        )
        Column(Modifier.fillMaxSize()) {
            BrandHeader()
            Box(Modifier.weight(1f)) { content() }
            BottomNavigation(selected, onCatalog, onMatches, onChats, onProfile)
        }
    }
}

@Composable
fun BrandHeader() {
    Row(
        Modifier.fillMaxWidth().height(76.dp).padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.matchmaker_logo),
            contentDescription = "Логотип Matchmaker",
            modifier = Modifier.size(width = 34.dp, height = 29.dp),
        )
        Spacer(Modifier.size(11.dp))
        Text("Matchmaker", color = CardBgColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BottomNavigation(
    selected: MainDestination,
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onChats: () -> Unit,
    onProfile: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(76.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavItem("Каталог", Res.drawable.icon_grid, selected == MainDestination.CATALOG, onCatalog)
        BottomNavItem("Мэтчи", Res.drawable.icon_heart, selected == MainDestination.MATCHES, onMatches)
        BottomNavItem("Чаты", Res.drawable.icon_chat, selected == MainDestination.CHATS, onChats)
        BottomNavItem("Профиль", Res.drawable.icon_user, selected == MainDestination.PROFILE, onProfile)
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) CardBgColor else NavigationMutedColor
    Column(
        Modifier.width(97.5.dp).clickable(onClick = onClick).padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Image(painterResource(icon), contentDescription = label, modifier = Modifier.size(22.dp))
        Text(label, color = color, fontSize = 12.sp)
    }
}

@Composable
fun PageCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        color = CardBgColor,
        shape = RoundedCornerShape(24.dp),
        content = content,
    )
}

@Composable
fun PageTitle(text: String) {
    Text(
        text,
        color = TextDarkColor,
        fontSize = 25.sp,
        lineHeight = 35.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
fun StepProgress(progress: Float) {
    Box(
        Modifier.fillMaxWidth().padding(vertical = 12.dp).height(4.dp)
            .background(ChipBgColor, RoundedCornerShape(2.dp)),
    ) {
        Box(
            Modifier.fillMaxWidth(progress).height(4.dp)
                .background(PrimaryButtonColor, RoundedCornerShape(2.dp)),
        )
    }
}

@Composable
fun FormLabel(text: String) {
    Text(
        text,
        color = TextDarkColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 12.dp, bottom = 7.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = MutedTextColor, fontSize = 15.sp) },
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = InputBgColor,
            unfocusedContainerColor = InputBgColor,
            focusedIndicatorColor = BorderColor,
            unfocusedIndicatorColor = BorderColor,
            focusedTextColor = TextDarkColor,
            unfocusedTextColor = TextDarkColor,
        ),
    )
}

@Composable
fun SkillChips(skills: List<String>, onRemove: ((String) -> Unit)? = null) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
        skills.take(4).forEach { skill ->
            Surface(
                color = ChipBgColor,
                shape = RoundedCornerShape(15.dp),
                modifier = if (onRemove == null) Modifier else Modifier.clickable { onRemove(skill) },
            ) {
                Text(
                    if (onRemove == null) skill else "$skill  ×",
                    color = TextDarkColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }
    }
}

@Composable
fun ExpandableHeader(title: String, expanded: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 12.dp)
            .background(SectionBgColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = TextDarkColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(if (expanded) "⌃" else "⌄", color = PrimaryButtonColor, fontSize = 22.sp)
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryButtonColor,
            disabledContainerColor = DisabledButtonColor,
        ),
    ) {
        Text(text, color = CardBgColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PasswordVisibilityButton(
    passwordVisible: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(Res.drawable.icon_eye),
                contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                modifier = Modifier.fillMaxSize(),
            )
            if (!passwordVisible) {
                Canvas(Modifier.fillMaxSize()) {
                    drawLine(
                        color = TextDarkColor,
                        start = Offset(4.dp.toPx(), size.height - 4.dp.toPx()),
                        end = Offset(size.width - 4.dp.toPx(), 4.dp.toPx()),
                        strokeWidth = 1.8.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}

fun initials(name: String): String = name
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString("") { it.first().uppercase() }
    .ifBlank { "П" }
