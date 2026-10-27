package com.example.projectmatchmaker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun VerifyEmailScreen(
    email: String,
    onVerify: suspend (String) -> Result<Unit>,
    onVerified: () -> Unit,
    onResend: suspend () -> Result<Unit>,
    onBack: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        Modifier.fillMaxSize().background(DarkBgColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxWidth().widthIn(max = 440.dp)
                .verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Matchmaker",
                color = CardBgColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 38.dp),
            )
            Surface(
                color = CardBgColor,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Подтвердите e-mail",
                        color = TextDarkColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Column(Modifier.fillMaxWidth()) {
                        FormLabel("Код подтверждения")
                        FormField(
                            value = code,
                            onValueChange = { code = it.filter(Char::isDigit).take(6) },
                            placeholder = "123456",
                        )

                        FormLabel("Отправлено на e-mail")
                        Surface(
                            color = InputBgColor,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderColor),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                        ) {
                            Box(Modifier.fillMaxSize().padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                                Text(email.ifBlank { "you@example.com" }, color = TextDarkColor, fontSize = 15.sp)
                            }
                        }
                    }

                    message?.let {
                        Text(
                            it,
                            color = if (isError) ErrorColor else TextDarkColor,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    if (isLoading) {
                        Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryButtonColor)
                        }
                    } else {
                        PrimaryButton(
                            text = "Подтвердить",
                            onClick = {
                                scope.launch {
                                    message = null
                                    isLoading = true
                                    val result = onVerify(code)
                                    isLoading = false
                                    result.onSuccess { onVerified() }.onFailure {
                                        isError = true
                                        message = it.message ?: "Не удалось подтвердить код"
                                    }
                                }
                            },
                            enabled = code.length == 6,
                        )
                    }

                    Text(
                        if (isResending) "Отправляем…" else "Отправить код повторно",
                        color = PrimaryButtonColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = !isResending) {
                            scope.launch {
                                message = null
                                isResending = true
                                val result = onResend()
                                isResending = false
                                result.onSuccess {
                                    isError = false
                                    message = "Новый код создан. Проверьте письмо или локальную папку outbox."
                                }.onFailure {
                                    isError = true
                                    message = it.message ?: "Не удалось отправить код"
                                }
                            }
                        }.padding(8.dp),
                    )
                    Text(
                        "Изменить e-mail",
                        color = MutedTextColor,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable(onClick = onBack).padding(8.dp),
                    )
                }
            }
        }
    }
}

@Preview(name = "Подтверждение e-mail · телефон", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun VerifyEmailScreenPreview() {
    MaterialTheme {
        VerifyEmailScreen(
            email = "veronika@example.com",
            onVerify = { Result.success(Unit) },
            onVerified = {},
            onResend = { Result.success(Unit) },
            onBack = {},
        )
    }
}
