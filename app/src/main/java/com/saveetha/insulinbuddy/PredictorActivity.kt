package com.simats.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import android.text.InputFilter
import android.text.TextWatcher
import android.text.Editable
import android.text.method.DigitsKeyListener
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class PredictorActivity : AppCompatActivity() {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .callTimeout(120, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private lateinit var sessionManager: SessionManager

    private var icr: Double? = null
    private var isr: Double? = null
    private var targetGlucose: Double? = null
    private var age: Int? = null
    private var gender: String? = null
    private var diabetesType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predictor)

        sessionManager = SessionManager(this)
        val username = sessionManager.getUsername() ?: ""

        if (username.isNotEmpty()) {
            fetchUserProfile(username)
        }

        val glucoseEditText = findViewById<EditText>(R.id.glucoseEditText)
        val carbsEditText = findViewById<EditText>(R.id.carbsEditText)
        val activityEditText = findViewById<EditText>(R.id.activityEditText)
        val stressSpinner = findViewById<Spinner>(R.id.stressSpinner)
        val mealSpinner = findViewById<Spinner>(R.id.mealTypeSpinner)
        val notesEditText = findViewById<EditText>(R.id.notesEditText)
        val submitBtn = findViewById<Button>(R.id.submitBtn)

        // Dropdown setup
        stressSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Low", "Moderate", "High")
        )

        mealSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Morning", "Lunch", "Evening", "Night")
        )

        submitBtn.setOnClickListener {
            val glucoseStr = glucoseEditText.text.toString().trim()
            val carbsStr = carbsEditText.text.toString().trim()
            val activityStr = activityEditText.text.toString().trim()
            val stress = stressSpinner.selectedItem.toString()
            val mealType = mealSpinner.selectedItem.toString()
            val notes = notesEditText.text.toString()

            // Empty checks
            if (glucoseStr.isEmpty() || carbsStr.isEmpty() || activityStr.isEmpty()) {
                Toast.makeText(this, "Enter glucose, carbs, and activity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ranged validations
            val glucose = glucoseStr.toIntOrNull()
            if (glucose == null || glucose < 40 || glucose > 600) {
                glucoseEditText.error = "Glucose must be 40-600"
                glucoseEditText.requestFocus()
                return@setOnClickListener
            }

            val carbs = carbsStr.toIntOrNull()
            if (carbs == null || carbs < 0 || carbs > 500) {
                carbsEditText.error = "Carbs must be 0-500 g"
                carbsEditText.requestFocus()
                return@setOnClickListener
            }

            val activity = activityStr.toIntOrNull()
            if (activity == null || activity < 0 || activity > 600) {
                activityEditText.error = "Activity minutes must be 0-600"
                activityEditText.requestFocus()
                return@setOnClickListener
            }

            sendPredictionRequest(username, glucose, carbs, activity, stress, mealType, notes)
        }

        // Live input restrictions and validation hints
        glucoseEditText.keyListener = DigitsKeyListener.getInstance("0123456789")
        glucoseEditText.filters = arrayOf(InputFilter.LengthFilter(3))
        carbsEditText.keyListener = DigitsKeyListener.getInstance("0123456789")
        carbsEditText.filters = arrayOf(InputFilter.LengthFilter(3))
        activityEditText.keyListener = DigitsKeyListener.getInstance("0123456789")
        activityEditText.filters = arrayOf(InputFilter.LengthFilter(3))

        glucoseEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toIntOrNull()
                glucoseEditText.error = when {
                    s.isNullOrEmpty() -> null
                    v == null -> "Enter valid glucose"
                    v < 40 || v > 600 -> "Glucose must be 40-600"
                    else -> null
                }
            }
        })

        carbsEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toIntOrNull()
                carbsEditText.error = when {
                    s.isNullOrEmpty() -> null
                    v == null -> "Enter valid carbs"
                    v < 0 || v > 500 -> "Carbs must be 0-500 g"
                    else -> null
                }
            }
        })

        activityEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toIntOrNull()
                activityEditText.error = when {
                    s.isNullOrEmpty() -> null
                    v == null -> "Enter valid minutes"
                    v < 0 || v > 600 -> "Activity must be 0-600"
                    else -> null
                }
            }
        })
    }

    private fun sendPredictionRequest(
        username: String,
        glucose: Int,
        carbs: Int,
        activityMinutes: Int,
        stress: String,
        mealType: String,
        notes: String
    ) {
        val apiUrl = "https://insulin-dose-v7oc.onrender.com/predict"

        val activityLevel = when {
            activityMinutes <= 15 -> "Low"
            activityMinutes <= 45 -> "Moderate"
            else -> "High"
        }

        val timeOfDay = when (mealType) {
            "Morning" -> "Morning"
            "Afternoon" -> "Lunch"
            "Evening" -> "Evening"
            "Night" -> "Night"
            else -> "Lunch"
        }

        val missing = mutableListOf<String>()
        if (icr == null) missing += "ICR"
        if (isr == null) missing += "ISR"
        if (targetGlucose == null) missing += "target_glucose"
        if (age == null) missing += "age"
        if (gender == null) missing += "gender"
        if (diabetesType == null) missing += "type_of_diabetes"

        if (missing.isNotEmpty()) {
            runOnUiThread {
                Toast.makeText(
                    this@PredictorActivity,
                    "Profile incomplete (${missing.joinToString()}). Trying to refresh...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            fetchUserProfile(username)
            return
        }

        val payload = JSONObject().apply {
            put("ICR", icr!!)
            put("ISR", isr!!)
            put("target_glucose", targetGlucose!!)
            put("current_glucose", glucose.toDouble())
            put("carbs", carbs.toDouble())
            put("age", age!!)
            put("gender", gender!!)
            put("type_of_diabetes", diabetesType!!)
            put("activity", activityLevel)
            put("time_of_day", timeOfDay)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = payload.toString().toRequestBody(mediaType)
        val request = Request.Builder().url(apiUrl).post(body).build()

        performPredictionCall(request, glucose, carbs, activityLevel, timeOfDay, attempt = 1)
    }

    private fun performPredictionCall(
        request: Request,
        glucose: Int,
        carbs: Int,
        activityLevel: String,
        timeOfDay: String,
        attempt: Int
    ) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val isTimeout = e is SocketTimeoutException
                if (isTimeout && attempt == 1) {
                    runOnUiThread {
                        Toast.makeText(
                            this@PredictorActivity,
                            "Server is waking up, retrying...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    performPredictionCall(request, glucose, carbs, activityLevel, timeOfDay, attempt = 2)
                    return
                }

                runOnUiThread {
                    val msg = if (isTimeout) {
                        "Prediction timed out. Please try again in a few seconds."
                    } else {
                        "Prediction failed: ${e.message}"
                    }
                    Toast.makeText(this@PredictorActivity, msg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: "{}"
                Log.d("PredictResponse", responseBody)
                try {
                    val intent = Intent(this@PredictorActivity, PredictorResultActivity::class.java).apply {
                        putExtra("result", responseBody)
                        putExtra("current_glucose", glucose)
                        putExtra("carbs", carbs)
                        putExtra("activity", activityLevel)
                        putExtra("time_of_day", timeOfDay)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@PredictorActivity, "Error parsing prediction", Toast.LENGTH_LONG).show()
                        Log.e("PredictError", e.toString())
                    }
                }
            }
        })
    }

    private fun fetchUserProfile(username: String) {
        // ✅ Use 10.0.2.2 for Emulator OR replace with your system's IP if testing on real device
        val url = "http://14.139.187.229:8081/PDD-2025(9thmonth)/InsulinBuddy/get_user_profile.php"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = JSONObject().apply { put("username", username) }.toString().toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w("PredictorActivity", "Profile fetch failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val resp = response.body?.string() ?: return
                try {
                    val json = JSONObject(resp)
                    if (json.optString("status") == "success") {
                        gender = json.optString("gender", "").replaceFirstChar { it.uppercase() }

                        val t = json.optString("diabetes_type", "")
                        diabetesType = when {
                            t.equals("Type1", ignoreCase = true) -> "Type1"
                            t.equals("Type2", ignoreCase = true) -> "Type2"
                            else -> null
                        }


                        age = json.optInt("age", -1).let { if (it >= 0) it else null }
                        icr = json.optDouble("icr", Double.NaN).let { if (it.isNaN()) null else it }
                        isr = json.optDouble("isr", Double.NaN).let { if (it.isNaN()) null else it }
                        targetGlucose = json.optDouble("target_glucose", Double.NaN).let { if (it.isNaN()) null else it }

                        runOnUiThread {
                            Toast.makeText(
                                this@PredictorActivity,
                                "Profile loaded successfully for $username",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Log.w("PredictorActivity", "Profile not found for $username")
                    }
                } catch (e: Exception) {
                    Log.e("PredictorActivity", "Error parsing profile JSON: ${e.message}")
                }
            }
        })
    }
}
