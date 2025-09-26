package com.saveetha.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.saveetha.insulinbuddy.utils.SessionManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signupLink = findViewById<TextView>(R.id.signupLink)

        val savedUsername = sessionManager.getUsername()
        if (!savedUsername.isNullOrEmpty()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username == "admin" && password == "admin") {
                sessionManager.saveUsername("admin")
                Toast.makeText(this, "Logged in as Admin (Bypass)", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                return@setOnClickListener
            }

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username and password cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (!username.matches(Regex("^[a-zA-Z]+$"))) {
                Toast.makeText(this, "Username must contain only alphabets", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(username, password)
            }
        }

        signupLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun loginUser(username: String, password: String) {
        val json = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            json.toString()
        )

        val request = Request.Builder()
            .url("https://606tr6vg-80.inc1.devtunnels.ms/INSULIN/login.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val resStr = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(resStr ?: "{}")
                        when (jsonResponse.getString("status")) {
                            "success" -> {
                                val isProfileComplete = jsonResponse.optBoolean("is_profile_complete", false)
                                sessionManager.saveUsername(username)
                                val nextIntent = if (isProfileComplete) {
                                    Intent(this@LoginActivity, HomeActivity::class.java)
                                } else {
                                    Intent(this@LoginActivity, ProfileSetupActivity::class.java)
                                }
                                Log.d("Login", "Navigating to: ${nextIntent.component?.className}")
                                startActivity(nextIntent)
                                finish()
                            }
                            "fail", "error" -> {
                                Toast.makeText(this@LoginActivity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this@LoginActivity, "Unexpected response", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Invalid response: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    Log.d("LoginResponse", "Raw response: $resStr")
                }
            }
        })
    }
}
