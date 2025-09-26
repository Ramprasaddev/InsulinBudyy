package com.saveetha.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.saveetha.insulinbuddy.utils.SessionManager
import java.util.concurrent.TimeUnit
import java.util.Calendar

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
            // Not logged in
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        userNameText.text = "Welcome $username!"
        notificationIcon.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        userNameText.text = "Welcome $username!"

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

        notificationIcon.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        // 🔹 Schedule reminders here (inside onCreate!)

    }


}
