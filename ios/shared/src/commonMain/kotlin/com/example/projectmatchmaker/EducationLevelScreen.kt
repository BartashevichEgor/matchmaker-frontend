package com.example.projectmatchmaker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val educationLevels = listOf(
    "Среднее общее",
    "Профессионально-техническое",
    "Среднее специальное / колледж",
    "Неоконченное высшее",
    "Высшее · бакалавриат",
    "Высшее · специалитет",
    "Магистратура",
    "Аспирантура / докторантура",
    "Предпочитаю не указывать",
)

@Composable
fun EducationLevelScreen(
    selected: String,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onChats: () -> Unit,
    onProfile: () -> Unit,
) {
    ScreenFrame(MainDestination.PROFILE, onCatalog, onProfile, onMatches, onChats) {
        PageCard {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
                PageTitle("Уровень образования")
                Text(
                    "Выбери уровень. Учишься сейчас? Укажи это в статусе обучения.",
                    color = MutedTextColor,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
                educationLevels.forEach { level ->
                    Surface(
                        color = if (level == selected) ChipBgColor else PanelBgColor,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (level == selected) PrimaryButtonColor else BorderColor),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable {
                            onSelect(level)
                            onBack()
                        },
                    ) {
                        Text(
                            level,
                            color = PrimaryButtonColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(15.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Образование · телефон", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun EducationLevelScreenPreview() {
    MaterialTheme { EducationLevelScreen("Высшее · бакалавриат", {}, {}, {}, {}, {}, {}) }
}
