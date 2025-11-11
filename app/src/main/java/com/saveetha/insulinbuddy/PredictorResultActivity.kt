package com.simats.insulinbuddy

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class PredictorResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predictor_result)

        // View references
        val aiDoseText = findViewById<TextView>(R.id.aiDoseText)
        val formulaDoseText = findViewById<TextView>(R.id.formulaDoseText)
        val detailsText = findViewById<TextView>(R.id.detailsText)
        val enteredText = findViewById<TextView>(R.id.enteredText)
        val warningsLayout = findViewById<LinearLayout>(R.id.warningsLayout)
        val homeButton = findViewById<Button>(R.id.btnHome)

        // Data from Intent
        val resultJsonString = intent.getStringExtra("result")
        val currentGlucose = intent.getIntExtra("current_glucose", 0)
        val carbs = intent.getIntExtra("carbs", 0)
        val activity = intent.getStringExtra("activity") ?: ""
        val timeOfDay = intent.getStringExtra("time_of_day") ?: ""

        if (resultJsonString != null) {
            val resultJson = JSONObject(resultJsonString)
            val aiDose = resultJson.optDouble("ai_dose", 0.0)
            val correction = resultJson.optDouble("correction", 0.0)
            val carbsDose = resultJson.optDouble("carbs_dose", 0.0)
            val message = resultJson.optString("message", "")
            val warningsArray: JSONArray = resultJson.optJSONArray("warnings") ?: JSONArray()

            // Display main AI result
            aiDoseText.text = "Predicted Dosage"
            formulaDoseText.text = String.format("%.2f units", aiDose)

            // Display calculated doses
            detailsText.text = "Correction: %.2f | Carbs Dose: %.2f".format(correction, carbsDose)

            // Display input summary
            enteredText.text = "Glucose: $currentGlucose mg/dL • Carbs: $carbs g\nActivity: $activity • Meal: $timeOfDay"

            // Show a toast message if provided
            if (message.isNotBlank()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }

            // Handle warnings if available
            warningsLayout.removeAllViews()
            for (i in 0 until warningsArray.length()) {
                val warning = warningsArray.getString(i)
                val warningTextView = TextView(this).apply {
                    text = "⚠️ $warning"
                    setTextColor(resources.getColor(android.R.color.holo_red_dark))
                    textSize = 14f
                    setPadding(4, 8, 4, 8)
                }
                warningsLayout.addView(warningTextView)
            }
        } else {
            aiDoseText.text = "Predicted Dosage"
            formulaDoseText.text = "N/A"
            detailsText.text = "Details not available"
            enteredText.text = "User input not available"
        }

        // Handle home button
        homeButton.setOnClickListener { finish() }
    }
}
