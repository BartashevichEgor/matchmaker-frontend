package com.example.projectmatchmaker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBgColor = Color(0xFF2C0F12)
private val CardBgColor = Color(0xFFEFE4BE)
private val PrimaryButtonColor = Color(0xFF5B191B)
private val DisabledButtonColor = Color(0xFFA57B6C)
private val ErrorColor = Color(0xFF7A1315)
private val TextDarkColor = Color(0xFF2C0F12)

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Валидация
    val isEmailValid = email.isEmpty() || (email.contains("@") && email.contains("."))
    val isPasswordValid = password.isEmpty() || password.length >= 6

    val canSubmit = email.isNotBlank() && password.isNotBlank() && isEmailValid && isPasswordValid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Matchmaker",
                color = CardBgColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Surface(
                shape = RoundedCornerShape(28.dp),
                color = CardBgColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Вход",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkColor,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // E-mail
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "E-mail",
                            fontSize = 12.sp,
                            color = TextDarkColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("you@example.com", color = Color.Gray) },
                            singleLine = true,
                            isError = !isEmailValid,
                            trailingIcon = {
                                if (!isEmailValid) {
                                    Text("❗", fontSize = 14.sp)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                errorContainerColor = Color(0xFFFDE8E8),
                                focusedBorderColor = TextDarkColor,
                                unfocusedBorderColor = Color.Transparent,
                                errorBorderColor = ErrorColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (!isEmailValid) {
                            Text(
                                text = "Введите корректный e-mail",
                                color = ErrorColor,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Пароль
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Пароль",
                            fontSize = 12.sp,
                            color = TextDarkColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("••••••••", color = Color.Gray) },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Text(if (isPasswordVisible) "👁" else "🙈")
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = TextDarkColor,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Забыли пароль
                    TextButton(
                        onClick = { },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Забыли пароль?",
                            fontSize = 12.sp,
                            color = TextDarkColor.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Кнопка Войти
                    Button(
                        onClick = { },
                        enabled = canSubmit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryButtonColor,
                            disabledContainerColor = DisabledButtonColor,
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Войти", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Переход на регистрацию
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Нет аккаунта? ",
                            fontSize = 13.sp,
                            color = TextDarkColor
                        )
                        TextButton(
                            onClick = onNavigateToRegister,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Зарегистрироваться",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkColor
                            )
                        }
                    }
                }
            }
        }
    }
}