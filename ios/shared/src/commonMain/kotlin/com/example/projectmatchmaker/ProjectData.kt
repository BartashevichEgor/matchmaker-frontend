package com.example.projectmatchmaker

data class ProjectData(
    val title: String = "",
    val description: String = "",
    val role: String = "",
    val skills: String = "",
    val workFormat: String = "Удалённо",
    val employment: String = "Гибкий график",
    val published: Boolean = false,
    val visible: Boolean = true,
)
