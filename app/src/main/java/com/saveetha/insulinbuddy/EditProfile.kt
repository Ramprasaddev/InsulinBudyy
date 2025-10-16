package com.simats.insulinbuddy

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.simats.insulinbuddy.SessionManager
import com.simats.insulinbuddy.R

class EditProfile : AppCompatActivity() {

    private lateinit var etContact: EditText
    private lateinit var etAge: EditText

    private lateinit var etISF: EditText
    private lateinit var etICR: EditText
    private lateinit var etTarget: EditText
    private lateinit var etWeight: EditText
    private lateinit var btnUpdate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val sessionManager = SessionManager(this)
        val username = sessionManager.getUsername()

        if (username == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Bind views
        etContact = findViewById(R.id.etContact)
        etAge = findViewById(R.id.etAge)

        etISF = findViewById(R.id.etISF)
        etICR = findViewById(R.id.etICR)
        etTarget = findViewById(R.id.etTarget)
        etWeight = findViewById(R.id.etWeight)
        btnUpdate = findViewById(R.id.btnUpdateProfile)

        btnUpdate.setOnClickListener {
            val contact = etContact.text.toString().trim()
            val age = etAge.text.toString().trim()

            val isf = etISF.text.toString().trim()
            val icr = etICR.text.toString().trim()
            val target = etTarget.text.toString().trim()
            val weight = etWeight.text.toString().trim()

            if (contact.isEmpty() || age.isEmpty() ||
                isf.isEmpty() || icr.isEmpty() || target.isEmpty() || weight.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateProfile(username, contact, age,  isf, icr, target, weight)
        }
    }

    private fun updateProfile(
        username: String, contact: String, age: String,
        isf: String, icr: String, target: String, weight: String
    ) {
        val url = "http://14.139.187.229:8081/PDD-2025(9thmonth)/InsulinBuddy/edit_profile.php"
        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(this, "Update failed: ${error.message}", Toast.LENGTH_SHORT).show()
                error.printStackTrace()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username
                params["contact"] = contact
                params["age"] = age
                params["isf"] = isf
                params["icr"] = icr
                params["target"] = target
                params["weight"] = weight
                return params
            }
        }

        requestQueue.add(stringRequest)
    }
}
