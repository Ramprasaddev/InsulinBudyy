package com.simats.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.insulinbuddy.R
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.simats.insulinbuddy.LoginActivity
import com.simats.insulinbuddy.SessionManager
import com.simats.insulinbuddy.NotificationsActivity


class HomeActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sessionManager = SessionManager(this)

        val userNameText = findViewById<TextView>(R.id.userNameText)
        val buttonPredictor = findViewById<Button>(R.id.buttonPredictor)
        val buttonMonitor = findViewById<Button>(R.id.buttonMonitor)
        val buttonFeedback = findViewById<Button>(R.id.buttonFeedback)
        val notificationIcon = findViewById<ImageView>(R.id.notificationIcon)
        val menuIcon = findViewById<ImageView>(R.id.menuIcon)

        val username = sessionManager.getUsername()
        if (username == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        userNameText.text = "Welcome $username!"

        // Gate Home features until profile is completed
        if (!sessionManager.isProfileCompleted()) {
            checkProfileCompleted(username) { completed ->
                if (!completed) {
                    Toast.makeText(this, "Please complete your profile.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, ProfileSetupActivity::class.java))
                    finish()
                    return@checkProfileCompleted
                } else {
                    sessionManager.setProfileCompleted(true)
                }
            }
        }

        notificationIcon.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        menuIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        buttonPredictor.setOnClickListener {
            startActivity(Intent(this, PredictorActivity::class.java))
        }

        buttonMonitor.setOnClickListener {
            startActivity(Intent(this, MonitorActivity::class.java))
        }

        buttonFeedback.setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
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
                val str = response.body?.string() ?: "{}"
                val json = org.json.JSONObject(str)
                val completed = json.optInt("profile_completed", 0) == 1
                runOnUiThread { callback(completed) }
            }
        })
    }
}
