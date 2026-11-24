package com.example.projectmatchmaker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import org.jetbrains.skia.Image as SkiaImage
import platform.Foundation.NSData
import kotlinx.cinterop.reinterpret

@Composable
actual fun ProfileAvatar(photoBase64: String, initials: String, modifier: Modifier, size: Dp) {
    val bitmap = remember(photoBase64) {
        runCatching {
            val data = NSData.create(base64EncodedString = photoBase64, options = 0u) ?: return@runCatching null
            SkiaImage.makeFromEncoded(data.toByteArray()).asImageBitmap()
        }.getOrNull()
    }
    Box(modifier.size(size).clip(CircleShape).background(ProfileAvatarColor), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(bitmap, "Фото профиля", contentScale = ContentScale.Crop, modifier = Modifier.size(size).clip(CircleShape))
        } else {
            Text(initials, color = CardBgColor, fontSize = (size.value * .32f).sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun NSData.toByteArray(): ByteArray {
    val result = ByteArray(length.toInt())
    for (index in result.indices) result[index] = bytes!!.reinterpret<kotlinx.cinterop.ByteVar>()[index]
    return result
}
