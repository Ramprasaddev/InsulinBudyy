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

class InsulinIntakeActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insulin_intake)

        val editTextInsulinDosage = findViewById<EditText>(R.id.editInsulinDosage)
        val editTextNote = findViewById<EditText>(R.id.editNote)  // note field
        val buttonSubmitInsulin = findViewById<Button>(R.id.btnSubmitInsulin)

        buttonSubmitInsulin.setOnClickListener {
            val dosage = editTextInsulinDosage.text.toString().trim()
            val note = editTextNote.text.toString().trim()

            if (dosage.isNotEmpty()) {
                val sessionManager = SessionManager(this)
                val username = sessionManager.getUsername()
                if (username == null) {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val json = JSONObject().apply {
                    put("username", username)
                    put("intake_value", dosage.toDouble())  // send as number
                    put("note", note)  // can be empty
                }

                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("http://14.139.187.229:8081/PDD-2025(9thmonth)/InsulinBuddy/add_insulin_intake.php")
                    .post(requestBody)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(
                                this@InsulinIntakeActivity,
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
                                val status = jsonResponse.optString("status", "error")
                                val message = jsonResponse.optString("message", "No message")

                                if (status == "success") {
                                    Toast.makeText(
                                        applicationContext,
                                        "Insulin intake submitted successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish() // Close activity after success
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "Server response: $message",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    applicationContext,
                                    "Unexpected response format",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                })
            } else {
                Toast.makeText(this, "Please enter a dosage", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
