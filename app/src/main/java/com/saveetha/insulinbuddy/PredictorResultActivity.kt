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

        val aiDoseText = findViewById<TextView>(R.id.aiDoseText)
        val detailsText = findViewById<TextView>(R.id.detailsText)
        val enteredText = findViewById<TextView>(R.id.enteredText)
        val warningsLayout = findViewById<LinearLayout>(R.id.warningsLayout)

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

            aiDoseText.text = "Predicted Dosage: ${String.format("%.2f", aiDose)} units"
            detailsText.text = "Correction: ${String.format("%.2f", correction)} | Carbs Dose: ${String.format("%.2f", carbsDose)}"
            enteredText.text = "Entered: Glucose — $currentGlucose mg/dL • Carbs — $carbs g • Activity — $activity • Meal: $timeOfDay"

            if (message.isNotBlank()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }

            warningsLayout.removeAllViews()
            val warningsArray: JSONArray = resultJson.optJSONArray("warnings") ?: JSONArray()
            for (i in 0 until warningsArray.length()) {
                val warning = warningsArray.getString(i)
                val warningTextView = TextView(this).apply {
                    text = "• $warning"
                    setTextColor(resources.getColor(android.R.color.holo_red_dark))
                    textSize = 14f
                }
                warningsLayout.addView(warningTextView)
            }
        } else {
            aiDoseText.text = "Predicted Dosage: N/A"
            detailsText.text = "Details not available"
            enteredText.text = "User input not available"
        }
    }
}
