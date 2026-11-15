package com.example.projectmatchmaker

enum class FavoriteType { PERSON, PROJECT }

data class FavoriteItem(
    val type: FavoriteType,
    val id: String,
    val title: String,
    val subtitle: String,
    val skills: List<String>,
)
