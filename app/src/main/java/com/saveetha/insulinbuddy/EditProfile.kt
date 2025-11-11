package com.simats.insulinbuddy

import android.os.Bundle
import android.widget.*
import android.text.InputFilter
import android.text.TextWatcher
import android.text.Editable
import android.text.method.DigitsKeyListener
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

        // Live input restrictions
        etContact.keyListener = DigitsKeyListener.getInstance("0123456789")
        etContact.filters = arrayOf(InputFilter.LengthFilter(10))
        etAge.keyListener = DigitsKeyListener.getInstance("0123456789")
        etAge.filters = arrayOf(InputFilter.LengthFilter(3))

        addLiveValidation()

        btnUpdate.setOnClickListener {
            val contact = etContact.text.toString().trim()
            val ageStr = etAge.text.toString().trim()
            val isfStr = etISF.text.toString().trim()
            val icrStr = etICR.text.toString().trim()
            val targetStr = etTarget.text.toString().trim()
            val weightStr = etWeight.text.toString().trim()

            // Basic empty validation
            if (contact.isEmpty() || ageStr.isEmpty() || isfStr.isEmpty() || icrStr.isEmpty() || targetStr.isEmpty() || weightStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Contact: 10-15 digits
            if (!contact.matches(Regex("^[0-9]{10,15}$"))) {
                etContact.error = "Enter valid contact (10-15 digits)"
                etContact.requestFocus()
                return@setOnClickListener
            }

            // Age: 1..120
            val ageVal = ageStr.toIntOrNull()
            if (ageVal == null || ageVal !in 1..120) {
                etAge.error = "Age must be between 1 and 120"
                etAge.requestFocus()
                return@setOnClickListener
            }

            // ISF: 1..300
            val isfVal = isfStr.toDoubleOrNull()
            if (isfVal == null || isfVal <= 0.0 || isfVal > 300.0) {
                etISF.error = "ISF must be between 1 and 300"
                etISF.requestFocus()
                return@setOnClickListener
            }

            // ICR: 1..100
            val icrVal = icrStr.toDoubleOrNull()
            if (icrVal == null || icrVal <= 0.0 || icrVal > 100.0) {
                etICR.error = "ICR must be between 1 and 100"
                etICR.requestFocus()
                return@setOnClickListener
            }

            // Target glucose: 70..250 mg/dL
            val targetVal = targetStr.toDoubleOrNull()
            if (targetVal == null || targetVal < 70.0 || targetVal > 250.0) {
                etTarget.error = "Target should be 70-250 mg/dL"
                etTarget.requestFocus()
                return@setOnClickListener
            }

            // Weight: 10..400 kg
            val weightVal = weightStr.toDoubleOrNull()
            if (weightVal == null || weightVal < 10.0 || weightVal > 400.0) {
                etWeight.error = "Weight should be 10-400 kg"
                etWeight.requestFocus()
                return@setOnClickListener
            }

            updateProfile(
                username,
                contact,
                ageVal.toString(),
                isfVal.toString(),
                icrVal.toString(),
                targetVal.toString(),
                weightVal.toString()
            )
        }
    }

    private fun addLiveValidation() {
        etContact.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val txt = s?.toString() ?: ""
                etContact.error = when {
                    txt.isEmpty() -> null
                    txt.length != 10 -> "Mobile number must be 10 digits"
                    else -> null
                }
            }
        })

        etAge.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toIntOrNull()
                etAge.error = when {
                    s.isNullOrEmpty() -> null
                    v == null -> "Enter valid age"
                    v < 1 || v > 150 -> "Age must be 1-150"
                    else -> null
                }
            }
        })

        etISF.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toDoubleOrNull()
                etISF.error = when {
                    s.isNullOrEmpty() -> null
                    v == null -> "Enter a number"
                    v <= 0.0 || v > 300.0 -> "ISF must be 1-300"
                    else -> null
                }
            }
        })

        etICR.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toDoubleOrNull()
                etICR.error = when {
                    s.isNullOrEmpty() -> null
                    v == null -> "Enter a number"
                    v <= 0.0 || v > 100.0 -> "ICR must be 1-100"
                    else -> null
                }
            }
        })

        etTarget.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toDoubleOrNull()
                etTarget.error = when {
                    s.isNullOrEmpty() -> null
                    v == null -> "Enter a number"
                    v < 70.0 || v > 250.0 -> "Target 70-250 mg/dL"
                    else -> null
                }
            }
        })

        etWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toDoubleOrNull()
                etWeight.error = when {
                    s.isNullOrEmpty() -> null
                    v == null -> "Enter a number"
                    v < 10.0 || v > 400.0 -> "Weight 10-400 kg"
                    else -> null
                }
            }
        })
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
