package com.example.projectmatchmaker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.util.Base64
import javax.swing.JFileChooser

@Composable
actual fun PhotoPickerField(
    photoBase64: String,
    onPhotoSelected: (String) -> Unit,
) {
    Surface(color = PanelBgColor, shape = RoundedCornerShape(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.size(width = 320.dp, height = 88.dp),
        ) {
            Spacer(Modifier.width(2.dp))
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(ChipBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (photoBase64.isBlank()) "＋" else "✓", color = PrimaryButtonColor, fontSize = 26.sp)
            }
            Column(horizontalAlignment = Alignment.Start) {
                Button(
                    onClick = {
                        val chooser = JFileChooser().apply {
                            dialogTitle = "Выберите фото профиля"
                            fileSelectionMode = JFileChooser.FILES_ONLY
                        }
                        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                            onPhotoSelected(Base64.getEncoder().encodeToString(File(chooser.selectedFile.path).readBytes()))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryButtonColor),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        if (photoBase64.isBlank()) "Добавить фото" else "Изменить фото",
                        color = CardBgColor,
                    )
                }
                Text(
                    if (photoBase64.isBlank()) "Можно заполнить позже" else "Фото выбрано",
                    color = MutedTextColor,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
