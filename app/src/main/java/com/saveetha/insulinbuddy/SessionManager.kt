package com.saveetha.insulinbuddy.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    fun getUsername(): String? {
        return sharedPreferences.getString("username", null)
    }

    fun saveUsername(username: String) {
        sharedPreferences.edit().putString("username", username).apply()
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}
