package com.example.projectmatchmaker

data class ProfileData(
    val username: String = "",
    val email: String = "",
    val registrationMode: String = "developer",
    val firstName: String = "",
    val lastName: String = "",
    val role: String = "",
    val roleId: Int? = null,
    val about: String = "",
    val skills: List<String> = emptyList(),
    val experience: String = "",
    val education: String = "",
    val courses: String = "",
    val github: String = "",
    val backendUserId: Int? = null,
    val backendProfileId: Int? = null,
) {
    val name: String
        get() = listOf(firstName.trim(), lastName.trim()).filter { it.isNotBlank() }.joinToString(" ")
}
