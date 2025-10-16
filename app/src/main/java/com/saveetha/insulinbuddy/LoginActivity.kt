package com.simats.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.simats.insulinbuddy.R
import com.simats.insulinbuddy.ApiClient
import com.simats.insulinbuddy.LoginRequest
import com.simats.insulinbuddy.LoginResponse
import com.simats.insulinbuddy.HomeActivity
import com.simats.insulinbuddy.SignupActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class LoginActivity : AppCompatActivity() {
    private var passwordVisible = false
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        sessionManager = SessionManager(this)

        val inputUsername = findViewById<EditText>(R.id.usernameEditText)
        val inputPassword = findViewById<EditText>(R.id.passwordEditText)
        val togglePassword = findViewById<ImageView>(R.id.togglePassword)
        val btnLogin = findViewById<Button>(R.id.loginButton)
        val signUpLink = findViewById<TextView>(R.id.signupLink)

        // Username validation (non-empty, min length 3, no spaces) – live like signup
        inputUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val u = s?.toString()?.trim() ?: ""
                inputUsername.error = when {
                    u.isEmpty() -> null // handled on submit
                    u.contains(" ") -> "No spaces allowed"
                    u.length < 3 -> "Min 3 characters"
                    else -> null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Password validation – align with signup (min 6, no spaces)
        inputPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                inputPassword.error = when {
                    password.contains(" ") -> "No spaces allowed"
                    password.isNotEmpty() && password.length < 6 -> "Min 6 characters"
                    else -> null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Toggle password visibility
        togglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            inputPassword.inputType =
                if (passwordVisible)
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                else
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            togglePassword.setImageResource(
                if (passwordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye
            )
            inputPassword.setSelection(inputPassword.text?.length ?: 0)
        }

        // Navigate to Signup
        signUpLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Login logic
        btnLogin.setOnClickListener {
            val username = inputUsername.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (inputUsername.error != null || inputPassword.error != null) {
                Toast.makeText(this, "Please fix input errors", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(username, password)
            ApiClient.apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    val resp = response.body()
                    if (resp?.status == "success") {
                        // Save username to session for future automatic login
                        sessionManager.saveUsername(username)
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                        // If we've already marked profile completed locally, skip network for faster UX
                        if (sessionManager.isProfileCompleted()) {
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            finish()
                        } else {
                            // After login, check if profile is completed
                            checkProfileCompleted(username) { completed ->
                                if (completed) {
                                    sessionManager.setProfileCompleted(true)
                                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                } else {
                                    startActivity(Intent(this@LoginActivity, ProfileSetupActivity::class.java))
                                }
                                finish()
                            }
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, resp?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun checkProfileCompleted(username: String, callback: (Boolean) -> Unit) {
        val client = okhttp3.OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = org.json.JSONObject().apply { put("username", username) }.toString().toRequestBody(mediaType)
        val request = okhttp3.Request.Builder()
            .url("http://14.139.187.229:8081/PDD-2025(9thmonth)/InsulinBuddy/get_user_profile.php")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                runOnUiThread { callback(false) }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                try {
                    if (!response.isSuccessful) {
                        runOnUiThread { callback(false) }
                        return
                    }
                    val str = response.body?.string()?.trim().orEmpty()
                    if (str.isEmpty() || !(str.startsWith("{") && str.endsWith("}"))) {
                        runOnUiThread { callback(false) }
                        return
                    }
                    val json = org.json.JSONObject(str)
                    val completed = json.optInt("profile_completed", 0) == 1
                    runOnUiThread { callback(completed) }
                } catch (_: Exception) {
                    runOnUiThread { callback(false) }
                }
            }
        })
    }
}
