package com.saveetha.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.saveetha.insulinbuddy.utils.SessionManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class PredictorActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predictor)

        sessionManager = SessionManager(this)

        val username: String = sessionManager.getUsername() ?: ""
        val glucoseEditText = findViewById<EditText>(R.id.glucoseEditText)
        val carbsEditText = findViewById<EditText>(R.id.carbsEditText)
        val activitySpinner = findViewById<Spinner>(R.id.activitySpinner)
        val mealTypeSpinner = findViewById<Spinner>(R.id.mealTypeSpinner)
        val submitBtn = findViewById<Button>(R.id.submitBtn)

        // ✅ Use custom spinner item layout for bigger font
        val activityAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            listOf("Low", "Moderate", "High")
        )
        activityAdapter.setDropDownViewResource(R.layout.spinner_item)
        activitySpinner.adapter = activityAdapter

        val mealTypeAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            listOf("Morning", "Afternoon", "Evening", "Night") // 👈 Afternoon visible
        )
        mealTypeAdapter.setDropDownViewResource(R.layout.spinner_item)
        mealTypeSpinner.adapter = mealTypeAdapter

        submitBtn.setOnClickListener {
            val currentGlucose = glucoseEditText.text.toString().toIntOrNull()
            val carbs = carbsEditText.text.toString().toIntOrNull()
            val activity = activitySpinner.selectedItem.toString()
            val timeOfDay = mealTypeSpinner.selectedItem.toString()

            if (currentGlucose == null || carbs == null) {
                Toast.makeText(this, "Please enter valid glucose and carb values", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val phpBaseUrl = "https://606tr6vg-80.inc1.devtunnels.ms/INSULIN"
            val fastapiBaseUrl = "https://adequate-lovely-mayfly.ngrok-free.app"

            fetchUserProfileAndPredict(username, currentGlucose, carbs, activity, timeOfDay, phpBaseUrl, fastapiBaseUrl)
        }
    }

    private fun fetchUserProfileAndPredict(
        username: String,
        currentGlucose: Int,
        carbs: Int,
        activity: String,
        timeOfDay: String,
        phpBaseUrl: String,
        fastapiBaseUrl: String
    ) {
        val profileUrl = "$phpBaseUrl/fetch_user_profile.php?username=$username"
        val request = Request.Builder().url(profileUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@PredictorActivity,
                        "Profile fetch failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(
                            this@PredictorActivity,
                            "Profile fetch failed with status: ${response.code}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return
                }

                val profileJson = JSONObject(response.body?.string() ?: "{}")

                if (profileJson.has("error")) {
                    runOnUiThread {
                        Toast.makeText(
                            this@PredictorActivity,
                            "Error: ${profileJson.getString("error")}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return
                }

                val gender = profileJson.optString("gender", "Male").replaceFirstChar { it.uppercase() }
                val diabetesType = profileJson.optString("type_of_diabetes", "Type1").replaceFirstChar { it.uppercase() }

                val mappedTimeOfDay = when (timeOfDay.lowercase()) {
                    "afternoon" -> "Lunch"   // 👈 map Afternoon → Lunch for backend
                    "morning" -> "Morning"
                    "evening" -> "Evening"
                    else -> "Night"
                }

                val data = JSONObject().apply {
                    put("gender", if (gender == "Female") "Female" else "Male")
                    put("age", profileJson.optInt("age", 25))
                    put("type_of_diabetes", if (diabetesType == "Type2") "Type2" else "Type1")
                    put("ICR", profileJson.optDouble("ICR", 10.0))
                    put("ISR", profileJson.optDouble("ISR", 50.0))
                    put("target_glucose", profileJson.optDouble("target_glucose", 120.0))
                    put("current_glucose", currentGlucose.toDouble())
                    put("carbs", carbs.toDouble())
                    put("activity", when (activity.lowercase()) {
                        "high" -> "High"
                        "moderate" -> "Moderate"
                        else -> "Low"
                    })
                    put("time_of_day", mappedTimeOfDay) // 👈 safe value for backend
                }

                Log.d("PredictData", data.toString())
                sendPredictRequest(data, fastapiBaseUrl)
            }
        })
    }

    private fun sendPredictRequest(data: JSONObject, fastapiBaseUrl: String) {
        val url = "$fastapiBaseUrl/predict"
        val body = RequestBody.create("application/json".toMediaTypeOrNull(), data.toString())
        val request = Request.Builder().url(url).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@PredictorActivity,
                        "Prediction failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: "{}"
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(
                            this@PredictorActivity,
                            "Prediction failed: $responseBody",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Log.e("PredictError", responseBody)
                    return
                }

                val resultJson = JSONObject(responseBody)
                val intent = Intent(this@PredictorActivity, PredictorResultActivity::class.java).apply {
                    putExtra("result", resultJson.toString())
                    putExtra("current_glucose", data.getInt("current_glucose"))
                    putExtra("carbs", data.getInt("carbs"))
                    putExtra("activity", data.getString("activity"))
                    putExtra("time_of_day", data.getString("time_of_day"))
                }
                startActivity(intent)
            }
        })
    }
}
