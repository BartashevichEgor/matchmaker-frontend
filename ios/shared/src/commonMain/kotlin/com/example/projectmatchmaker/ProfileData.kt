package com.example.projectmatchmaker

data class ProfileData(
    val username: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: String = "",
    val about: String = "",
    val skills: List<String> = emptyList(),
    val experience: String = "",
    val education: String = "",
    val github: String = "",
) {
    val name: String
        get() = listOf(firstName.trim(), lastName.trim()).filter { it.isNotBlank() }.joinToString(" ")
}
