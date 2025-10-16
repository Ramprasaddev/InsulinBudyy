package com.simats.insulinbuddy

data class SignUpRequest(
    val full_name: String,
    val username: String,
    val email: String,
    val password: String
)
