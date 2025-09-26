package com.saveetha.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class PredictorResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predictor_result)

        val formulaDoseText = findViewById<TextView>(R.id.formulaDoseText)
        val aiDoseText = findViewById<TextView>(R.id.aiDoseText)
        val enteredText = findViewById<TextView>(R.id.enteredText)
        val detailsText = findViewById<TextView>(R.id.detailsText)
        val homeButton = findViewById<ImageButton>(R.id.homeButton)

        val resultJsonString = intent.getStringExtra("result")
        val currentGlucose = intent.getIntExtra("current_glucose", 0)
        val carbs = intent.getIntExtra("carbs", 0)
        val activity = intent.getStringExtra("activity") ?: ""
        val timeOfDay = intent.getStringExtra("time_of_day") ?: ""

        if (resultJsonString != null) {
            val resultJson = JSONObject(resultJsonString)
            val correction = resultJson.optDouble("correction", 0.0)
            val carbsDose = resultJson.optDouble("carbs_dose", 0.0)
            val aiDose = resultJson.optDouble("ai_dose", 0.0)
            val status = resultJson.optString("status", "healthy")

            val formattedCorrection = String.format("%.2f", correction)
            val formattedCarbsDose = String.format("%.2f", carbsDose)
            val formattedAiDose = String.format("%.2f", aiDose)

            formulaDoseText.text = "Correction: $formattedCorrection • Carbs: $formattedCarbsDose"
            aiDoseText.text = "AI suggested dose: $formattedAiDose units\nStatus: $status"
        } else {
            formulaDoseText.text = "Calculation not available"
            aiDoseText.text = "AI suggestion not available"
        }

        enteredText.text = "Entered: Glucose — $currentGlucose mg/dL • Carbs — $carbs g"
        detailsText.text = "Activity: $activity | Meal: $timeOfDay"

        // 🔹 Navigate to HomeActivity when Home button is clicked
        homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
