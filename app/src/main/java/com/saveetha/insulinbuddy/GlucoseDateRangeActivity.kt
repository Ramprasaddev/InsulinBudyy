package com.saveetha.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.saveetha.insulinbuddy.utils.SessionManager

class GlucoseDateRangeActivity : AppCompatActivity() {

    private lateinit var rangeSpinner: Spinner
    private lateinit var btnViewGraph: Button

    private val ranges = listOf(
        "Select Range",
        "Today",
        "Yesterday",
        "Last 7 Days",
        "This Month",
        "Last Month",
        "Last 6 Months",
        "Last 1 Year"
    )

    private val apiKeys = listOf(
        "", "today", "yesterday", "last7days", "thismonth", "last1month", "last6months", "last1year"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glucose_date_range)

        rangeSpinner = findViewById(R.id.spinnerRange)
        btnViewGraph = findViewById(R.id.btnOpenGraph)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ranges)
        rangeSpinner.adapter = adapter

        btnViewGraph.setOnClickListener {
            val selectedIndex = rangeSpinner.selectedItemPosition
            val selectedRangeKey = apiKeys[selectedIndex]

            if (selectedRangeKey.isEmpty()) {
                Toast.makeText(this, "Please select a valid range", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sessionManager = SessionManager(this)
            val username = sessionManager.getUsername() ?: ""

            val intent = Intent(this, GlucoseGraphActivity::class.java)
            intent.putExtra("username", username)
            intent.putExtra("range", selectedRangeKey)
            startActivity(intent)
        }
    }
}
