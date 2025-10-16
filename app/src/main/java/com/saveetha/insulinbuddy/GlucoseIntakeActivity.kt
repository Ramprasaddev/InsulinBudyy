package com.simats.insulinbuddy

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.insulinbuddy.SessionManager
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class GlucoseIntakeActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glucose_intake)

        val editTextGlucoseLevel = findViewById<EditText>(R.id.editGlucoseIntake)
        val editTextNote = findViewById<EditText>(R.id.editNote)
        val buttonSubmitGlucose = findViewById<Button>(R.id.btnSubmitGlucose)

        buttonSubmitGlucose.setOnClickListener {
            val glucose = editTextGlucoseLevel.text.toString().trim()
            val note = editTextNote.text.toString().trim()

            if (glucose.isNotEmpty()) {
                val sessionManager = SessionManager(this)
                val username = sessionManager.getUsername()
                if (username == null) {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val json = JSONObject().apply {
                    put("username", username)
                    put("glucose_value", glucose)
                    put("note", note)
                }

                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("http://14.139.187.229:8081/PDD-2025(9thmonth)/InsulinBuddy/add_glucose_level.php")
                    .post(requestBody)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(
                                this@GlucoseIntakeActivity,
                                "Failed to send data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val res = response.body?.string()
                        runOnUiThread {
                            try {
                                val jsonResponse = JSONObject(res ?: "")
                                val success = jsonResponse.optBoolean("success", false)
                                val message = jsonResponse.optString("message", "No message")
                                if (success) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Glucose submitted successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "Server response: $message",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    applicationContext,
                                    "Error parsing server response",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                })
            } else {
                Toast.makeText(this, "Please enter a value", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
