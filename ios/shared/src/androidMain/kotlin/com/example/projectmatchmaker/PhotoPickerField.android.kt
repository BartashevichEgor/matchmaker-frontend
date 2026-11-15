package com.example.projectmatchmaker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.ByteArrayOutputStream

@Composable
actual fun PhotoPickerField(
    photoBase64: String,
    onPhotoSelected: (String) -> Unit,
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { "Не удалось открыть выбранное изображение" }
                    val source = BitmapFactory.decodeStream(input)
                    requireNotNull(source) { "Не удалось прочитать изображение" }
                    val maxSide = maxOf(source.width, source.height)
                    val scaled = if (maxSide > 640) {
                        val ratio = 640f / maxSide
                        Bitmap.createScaledBitmap(
                            source,
                            (source.width * ratio).toInt().coerceAtLeast(1),
                            (source.height * ratio).toInt().coerceAtLeast(1),
                            true,
                        )
                    } else source
                    ByteArrayOutputStream().use { output ->
                        scaled.compress(Bitmap.CompressFormat.JPEG, 82, output)
                        Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
                    }
                }
            }.onSuccess(onPhotoSelected)
        }
    }
    val preview = remember(photoBase64) {
        runCatching {
            val imageBytes = Base64.decode(photoBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }.getOrNull()
    }

    Surface(color = PanelBgColor, shape = RoundedCornerShape(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.size(width = 320.dp, height = 88.dp),
        ) {
            Spacer(Modifier.width(2.dp))
            if (preview != null) {
                Image(
                    bitmap = preview.asImageBitmap(),
                    contentDescription = "Выбранное фото профиля",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp).clip(CircleShape),
                )
            } else {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(ChipBgColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("＋", color = PrimaryButtonColor, fontSize = 28.sp)
                }
            }
            Column(horizontalAlignment = Alignment.Start) {
                Button(
                    onClick = { picker.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryButtonColor),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        if (preview == null) "Добавить фото" else "Изменить фото",
                        color = CardBgColor,
                    )
                }
                Text(
                    if (preview == null) "Можно заполнить позже" else "Фото выбрано",
                    color = MutedTextColor,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
