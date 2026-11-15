package com.example.projectmatchmaker

private val emailAddressPattern =
    Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

internal fun isValidEmailAddress(value: String): Boolean =
    emailAddressPattern.matches(value.trim())
