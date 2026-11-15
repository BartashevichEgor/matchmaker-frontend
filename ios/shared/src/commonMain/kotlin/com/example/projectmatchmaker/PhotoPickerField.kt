package com.example.projectmatchmaker

import androidx.compose.runtime.Composable

@Composable
expect fun PhotoPickerField(
    photoBase64: String,
    onPhotoSelected: (String) -> Unit,
)
