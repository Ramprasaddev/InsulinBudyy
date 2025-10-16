package com.simats.insulinbuddy

data class LoginResponse(
    val status: String,
    val message: String? = null,
    val user_id: Int? = null,
    val name: String? = null,
    val email: String? = null
)
