package com.simats.insulinbuddy


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.simats.insulinbuddy.R

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val nextBtn = findViewById<Button>(R.id.nextButton)
        nextBtn.setOnClickListener {
            startActivity(Intent(this, FeaturesActivity::class.java))
            finish() // Finish WelcomeActivity so user can't go back
        }
    }
}
