package com.example.projectmatchmaker

data class ProjectData(
    val id: Int? = null,
    val title: String = "",
    val description: String = "",
    val role: String = "",
    val skills: String = "",
    val workFormat: String = "",
    val employment: String = "",
    val published: Boolean = false,
    val visible: Boolean = true,
    val visibility: String = "draft",
)
