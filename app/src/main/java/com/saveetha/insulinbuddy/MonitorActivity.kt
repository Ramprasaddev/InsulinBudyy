package com.saveetha.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MonitorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)

        findViewById<Button>(R.id.btnInsulinMonitor).setOnClickListener {
            startActivity(Intent(this, InsulinDateRangeActivity::class.java))
        }

        findViewById<Button>(R.id.btnGlucoseMonitor).setOnClickListener {
            startActivity(Intent(this, GlucoseDateRangeActivity::class.java))
        }
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
    }
} }
