package com.simats.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.simats.insulinbuddy.R
import com.simats.insulinbuddy.LoginActivity

class FeaturesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_features)

        findViewById<Button>(R.id.btn_get_started).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Finish FeaturesActivity so user can't go back
        }
    }
}
