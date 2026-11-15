package com.example.projectmatchmaker

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().background(DarkBgColor), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.layout.Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Matchmaker", color = CardBgColor, fontSize = 28.sp)
            Surface(color = CardBgColor, shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(top = 28.dp)) {
                androidx.compose.foundation.layout.Column(Modifier.padding(24.dp)) {
                    PageTitle(if (submitted) "Пароль обновлён" else "Новый пароль")
                    if (submitted) {
                        Text("Теперь можно войти с новым паролем.", color = TextDarkColor, fontSize = 13.sp)
                        PrimaryButton("Войти", onDone)
                    } else {
                        FormLabel("Код из письма")
                        FormField(code, { code = it.take(6) }, "123456")
                        FormLabel("Новый пароль")
                        androidx.compose.material3.OutlinedTextField(password, { password = it }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(12.dp))
                        FormLabel("Повтори пароль")
                        androidx.compose.material3.OutlinedTextField(confirm, { confirm = it }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(12.dp))
                        PrimaryButton("Сохранить пароль", { submitted = true }, code.length == 6 && password.length >= 8 && password == confirm)
                        Text("Вернуться ко входу", color = PrimaryButtonColor, modifier = Modifier.padding(top = 10.dp).clickable(onClick = onBack))
                    }
                }
            }
        }
    }
}

@Preview(name = "Новый пароль", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun ResetPasswordScreenPreview() { MaterialTheme { ResetPasswordScreen({}, {}) } }
