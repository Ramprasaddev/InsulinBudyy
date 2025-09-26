package com.saveetha.insulinbuddy

import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class SignupActivity : AppCompatActivity() {

    private lateinit var nameField: EditText
    private lateinit var usernameField: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var reenterPasswordField: EditText
    private lateinit var signupButton: Button
    private lateinit var backToLoginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        nameField = findViewById(R.id.nameEditText)
        usernameField = findViewById(R.id.usernameEditText)
        emailField = findViewById(R.id.emailEditText)
        passwordField = findViewById(R.id.passwordEditText)
        reenterPasswordField = findViewById(R.id.reenterPasswordEditText)
        signupButton = findViewById(R.id.submitButton)
        backToLoginButton = findViewById(R.id.backToLoginButton)

        signupButton.setOnClickListener {
            val name = nameField.text.toString().trim()
            val username = usernameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()
            val rePassword = reenterPasswordField.text.toString()

            when {
                name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || rePassword.isEmpty() ->
                    showToast("Please fill in all fields")
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    showToast("Enter a valid email address")
                !isStrongPassword(password) ->
                    showToast("Password must include letter, number, and special character")
                password != rePassword ->
                    showToast("Passwords do not match")
                else -> {
                    // 🔄 Call backend API
                    registerUser(name, username, email, password)
                }
            }
        }

        backToLoginButton.setOnClickListener {
            finish()
        }
    }

    private fun isStrongPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{6,}$")
        return regex.matches(password)
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerUser(fullName: String, username: String, email: String, password: String) {
        thread {
            try {
                val url = URL("https://606tr6vg-80.inc1.devtunnels.ms/INSULIN/signup.php") // 🟢 Replace with your actual URL
                val jsonParam = JSONObject().apply {
                    put("full_name", fullName)
                    put("username", username)
                    put("email", email)
                    put("password", password)
                }

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true

                OutputStreamWriter(connection.outputStream).use {
                    it.write(jsonParam.toString())
                    it.flush()
                }

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)

                val status = jsonResponse.getString("status")
                val message = jsonResponse.getString("message")

                showToast(message)

            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Something went wrong: ${e.localizedMessage}")
            }
        }
    }
}
