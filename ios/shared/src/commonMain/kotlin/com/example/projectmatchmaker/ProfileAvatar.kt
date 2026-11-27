package com.example.projectmatchmaker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
expect fun ProfileAvatar(
    photoBase64: String,
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 76.dp,
)
