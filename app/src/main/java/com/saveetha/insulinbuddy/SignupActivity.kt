package com.simats.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.simats.insulinbuddy.R
import com.simats.insulinbuddy.ApiClient
import com.simats.insulinbuddy.SignUpRequest
import com.simats.insulinbuddy.SignUpResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {

    private lateinit var inputFullName: EditText
    private lateinit var inputUsername: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var inputConfirmPassword: EditText
    private lateinit var togglePassword: ImageView
    private var isPasswordVisible = false
    private lateinit var btnCreateAccount: Button
    private lateinit var linkLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        inputFullName = findViewById(R.id.nameEditText)
        inputUsername = findViewById(R.id.usernameEditText)
        inputEmail = findViewById(R.id.emailEditText)
        inputPassword = findViewById(R.id.passwordEditText)
        inputConfirmPassword = findViewById(R.id.reenterPasswordEditText)
        togglePassword = findViewById(R.id.togglePassword)
        btnCreateAccount = findViewById(R.id.submitButton)
        linkLogin = findViewById(R.id.backToLoginButton)

        // 🔹 Toggle Password Visibility
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                inputPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_eye)
            } else {
                inputPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_eye_off)
            }
            inputPassword.setSelection(inputPassword.text.length)
        }

        // 🔹 Name validation
        inputFullName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val name = s.toString().trim()
                if (name.isNotEmpty() && !name.matches(Regex("^[A-Za-z ]+$"))) {
                    inputFullName.error = "Only letters allowed"
                } else {
                    inputFullName.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 🔹 Email validation
        val emailRegex = Regex(
            "^(?![.])[A-Za-z0-9+_.-]+(?<![.])@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.(com|in|org|net|edu|gov|co|io|tech|ai))$"
        )
        inputEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                if (email.isNotEmpty() && !emailRegex.matches(email)) {
                    inputEmail.error = "Invalid email format"
                } else {
                    inputEmail.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 🔹 Password validation
        inputPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                when {
                    password.contains(" ") -> inputPassword.error = "No spaces allowed"
                    password.isNotEmpty() && password.length < 6 -> inputPassword.error = "Min 6 characters"
                    else -> inputPassword.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 🔹 Signup button click
        btnCreateAccount.setOnClickListener {
            Log.d("SignupActivity", "Sign up button clicked")
            val fullName = inputFullName.text.toString().trim()
            val username = inputUsername.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()
            val confirm = inputConfirmPassword.text.toString().trim()

            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (inputFullName.error != null || inputEmail.error != null || inputPassword.error != null) {
                Toast.makeText(this, "Please fix input errors.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = SignUpRequest(fullName, username, email, password)

            btnCreateAccount.isEnabled = false
            Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show()
            Log.d("SignupActivity", "Sending signup request: fullName=$fullName, username=$username, email=$email")
            ApiClient.apiService.signup(request).enqueue(object : Callback<SignUpResponse> {
                override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                    btnCreateAccount.isEnabled = true
                    val resp = response.body()
                    Log.d("SignupActivity", "Signup response code=${response.code()} body=$resp")
                    if (resp?.status == "success") {
                        Toast.makeText(this@SignupActivity, "Signup successful!", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        val msg = resp?.message ?: (response.errorBody()?.string() ?: "Signup failed.")
                        Log.e("SignupActivity", "Signup failed: $msg")
                        Toast.makeText(this@SignupActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                    btnCreateAccount.isEnabled = true
                    Log.e("SignupActivity", "Network error", t)
                    Toast.makeText(this@SignupActivity, "Network error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            })
        }

        linkLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
