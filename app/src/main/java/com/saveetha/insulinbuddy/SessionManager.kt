package com.simats.insulinbuddy

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

    fun setProfileCompleted(completed: Boolean) {
        sharedPreferences.edit().putBoolean("profile_completed", completed).apply()
    }

    fun isProfileCompleted(): Boolean {
        return sharedPreferences.getBoolean("profile_completed", false)
    }

    fun setGender(gender: String) {
        sharedPreferences.edit().putString("gender", gender).apply()
    }

    fun getGender(): String? {
        return sharedPreferences.getString("gender", null)
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}
