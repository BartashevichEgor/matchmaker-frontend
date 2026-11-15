package com.example.projectmatchmaker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onSendCode: suspend (String) -> Result<Unit> = { Result.success(Unit) },
) {
    var email by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var sent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize().background(DarkBgColor), contentAlignment = Alignment.Center) {
        Column(
            Modifier.fillMaxWidth().widthIn(max = 440.dp)
                .verticalScroll(rememberScrollState()).padding(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("Matchmaker", color = CardBgColor, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Surface(color = CardBgColor, shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(if (sent) "Проверь почту" else "Восстановить пароль", color = TextDarkColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (sent) "Если аккаунт с таким адресом существует, мы отправили код восстановления."
                        else "Укажи e-mail, который использовался при регистрации.",
                        color = MutedTextColor,
                        fontSize = 13.sp,
                    )
                    if (!sent) {
                        FormField(email, { email = it; error = null }, "you@example.com")
                        error?.let { Text(it, color = ErrorColor, fontSize = 12.sp) }
                        PrimaryButton("Отправить", {
                            scope.launch {
                                busy = true
                                val result = onSendCode(email.trim())
                                busy = false
                                result.onSuccess { sent = true }.onFailure { error = it.message ?: "Не удалось отправить код" }
                            }
                        }, email.contains("@") && !busy)
                    } else {
                        PrimaryButton("Открыть экран ввода кода", onBack)
                    }
                    Text(
                        "Вернуться ко входу",
                        color = PrimaryButtonColor,
                        modifier = Modifier.align(Alignment.CenterHorizontally).clickable(onClick = onBack).padding(8.dp),
                    )
                }
            }
        }
    }
}

@Preview(name = "Восстановление пароля · телефон", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    MaterialTheme { ForgotPasswordScreen({}) }
}
