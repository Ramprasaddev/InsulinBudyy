package com.simats.insulinbuddy

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.simats.insulinbuddy.HomeActivity

class FeedbackActivity : AppCompatActivity() {

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        val insulinButton = findViewById<Button>(R.id.btnInsulin)
        val glucoseButton = findViewById<Button>(R.id.btnGlucose)
        val homeButton = findViewById<Button>(R.id.btnHome)

        insulinButton.setOnClickListener {
            startActivity(Intent(this, InsulinIntakeActivity::class.java))
        }

        glucoseButton.setOnClickListener {
            startActivity(Intent(this, GlucoseIntakeActivity::class.java))
        }

        homeButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}
