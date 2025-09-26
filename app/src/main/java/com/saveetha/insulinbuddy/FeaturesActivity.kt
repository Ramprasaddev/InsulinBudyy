package com.saveetha.insulinbuddy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class FeaturesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_features)

        findViewById<Button>(R.id.btn_get_started).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
