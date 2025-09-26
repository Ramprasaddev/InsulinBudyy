package com.saveetha.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.saveetha.insulinbuddy.utils.SessionManager
import org.json.JSONObject

class ProfileSetupActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profilesetup)

        sessionManager = SessionManager(this)

        // ✅ Get username from session
        val username = sessionManager.getUsername() ?: "guest"

        // EditText fields
        val contact = findViewById<EditText>(R.id.etContact)
        val age = findViewById<EditText>(R.id.etAge)
        val weight = findViewById<EditText>(R.id.etWeight)
        val isf = findViewById<EditText>(R.id.etISF)
        val icr = findViewById<EditText>(R.id.etICR)
        val target = findViewById<EditText>(R.id.etTarget)
        val diagnosisYear = findViewById<EditText>(R.id.etDiagnosisYear)
        val dietType = findViewById<EditText>(R.id.etDietType)

        // Spinners
        val genderSpinner = findViewById<Spinner>(R.id.spinnerGender)
        val diabetesTypeSpinner = findViewById<Spinner>(R.id.spinnerDiabetesType)

        // Populate spinners
        genderSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf("Male", "Female", "Other")
        )
        diabetesTypeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf("Type 1", "Type 2")
        )

        val submitButton = findViewById<Button>(R.id.btnSubmit)

        submitButton.setOnClickListener {
            val queue = Volley.newRequestQueue(this)
            val url = "https://606tr6vg-80.inc1.devtunnels.ms/INSULIN/user_profile.php"

            val stringRequest = object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    val json = JSONObject(response)
                    Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show()

                    if (json.getString("status") == "success") {
                        // ✅ Profile saved → go to Home
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            ) {
                override fun getParams(): Map<String, String> {
                    return mapOf(
                        "username" to username,
                        "contact" to contact.text.toString(),
                        "age" to age.text.toString(),
                        "gender" to genderSpinner.selectedItem.toString(),
                        "isf" to isf.text.toString(),
                        "icr" to icr.text.toString(),
                        "target" to target.text.toString(),
                        "weight" to weight.text.toString(),
                        "type_of_diabetes" to diabetesTypeSpinner.selectedItem.toString(),
                        "diagnosis_year" to diagnosisYear.text.toString(),
                        "diet_type" to dietType.text.toString(),
                        "profile_completed" to "1"
                    )
                }
            }

            queue.add(stringRequest)
        }
    }
}
