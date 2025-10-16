package com.simats.insulinbuddy

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.simats.insulinbuddy.SessionManager
import org.json.JSONObject

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profilesetup)

        sessionManager = SessionManager(this)
        val username = sessionManager.getUsername() ?: "guest"

        // EditTexts
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

        // Live input restrictions (like Signup page)
        fun noSpacesFilter(): InputFilter = object : InputFilter {
            override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
                if (source == null) return null
                return if (source.any { it.isWhitespace() }) source.filterNot { it.isWhitespace() } else null
            }
        }

        contact.filters = arrayOf(InputFilter.LengthFilter(10))
        age.filters = arrayOf(InputFilter.LengthFilter(3))
        diagnosisYear.filters = arrayOf(InputFilter.LengthFilter(4))

        // Contact validation (10 digits)
        contact.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val t = s?.toString().orEmpty()
                contact.error = when {
                    t.isEmpty() -> null
                    !t.matches(Regex("^[0-9]{10}$")) -> "Enter 10-digit number"
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Age validation (1–120)
        age.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toIntOrNull()
                age.error = when {
                    s.isNullOrEmpty() -> null
                    v == null || v !in 1..120 -> "Age 1–120"
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Weight (20–300)
        weight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toFloatOrNull()
                weight.error = when {
                    s.isNullOrEmpty() -> null
                    v == null || v !in 20f..300f -> "Weight 20–300 kg"
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ISF (1–200)
        isf.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toFloatOrNull()
                isf.error = when {
                    s.isNullOrEmpty() -> null
                    v == null || v !in 1f..200f -> "ISF 1–200"
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ICR (1–100)
        icr.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toFloatOrNull()
                icr.error = when {
                    s.isNullOrEmpty() -> null
                    v == null || v !in 1f..100f -> "ICR 1–100"
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Target (70–180)
        target.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toFloatOrNull()
                target.error = when {
                    s.isNullOrEmpty() -> null
                    v == null || v !in 70f..180f -> "Target 70–180"
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Diagnosis year
        diagnosisYear.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toIntOrNull()
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                diagnosisYear.error = when {
                    s.isNullOrEmpty() -> null
                    v == null || v < 1900 || v > currentYear -> "Invalid year"
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Diet type
        dietType.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val t = s?.toString()?.trim().orEmpty()
                dietType.error = if (t.isEmpty()) null else null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        submitButton.setOnClickListener {
            if (!validateInputs(contact, age, weight, isf, icr, target, diagnosisYear, dietType)) {
                return@setOnClickListener
            }

            val queue = Volley.newRequestQueue(this)
            val url = "http://14.139.187.229:8081/PDD-2025(9thmonth)/InsulinBuddy/user_profile.php"

            val stringRequest = object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    try {
                        val body = response?.trim().orEmpty()
                        if (body.isEmpty() || !(body.startsWith("{") || body.startsWith("["))) {
                            Toast.makeText(this, "Server returned invalid response", Toast.LENGTH_LONG).show()
                            return@Listener
                        }
                        val json = JSONObject(body)
                        val msg = json.optString("message", "Profile saved")
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        if (json.optString("status") == "success") {
                            // Mark profile completed in session for persistence across launches
                            sessionManager.setProfileCompleted(true)
                            // Navigate to Home; Home will verify profile_completed==1 and allow access
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Failed to parse server response", Toast.LENGTH_LONG).show()
                    }
                },
                Response.ErrorListener { error ->
                    val resp = error.networkResponse
                    val status = resp?.statusCode
                    val body = try {
                        resp?.data?.toString(Charsets.UTF_8)
                    } catch (e: Exception) { null }
                    val msg = when {
                        status != null && body != null -> "HTTP $status: $body"
                        status != null -> "HTTP $status"
                        else -> "Network error"
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
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

    // ✅ Validation Function with Shake + Red Border
    private fun validateInputs(
        contact: EditText,
        age: EditText,
        weight: EditText,
        isf: EditText,
        icr: EditText,
        target: EditText,
        diagnosisYear: EditText,
        dietType: EditText
    ): Boolean {

        val shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake)
        var isValid = true

        fun showError(field: EditText, message: String) {
            field.error = message
            field.startAnimation(shakeAnim)
            field.background.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            field.requestFocus()
            isValid = false
        }

        // Contact validation
        val contactText = contact.text.toString()
        if (contactText.isEmpty() || !contactText.matches(Regex("^[0-9]{10}$"))) {
            showError(contact, "Enter a valid 10-digit contact number")
            return false
        } else contact.background.clearColorFilter()

        // Age validation
        val ageValue = age.text.toString().toIntOrNull()
        if (ageValue == null || ageValue !in 1..120) {
            showError(age, "Enter a valid age (1–120)")
            return false
        } else age.background.clearColorFilter()

        // Weight validation
        val weightValue = weight.text.toString().toFloatOrNull()
        if (weightValue == null || weightValue !in 20f..300f) {
            showError(weight, "Enter a valid weight (20–300 kg)")
            return false
        } else weight.background.clearColorFilter()

        // ISF validation
        val isfValue = isf.text.toString().toFloatOrNull()
        if (isfValue == null || isfValue !in 1f..200f) {
            showError(isf, "Enter valid ISF (1–200)")
            return false
        } else isf.background.clearColorFilter()

        // ICR validation
        val icrValue = icr.text.toString().toFloatOrNull()
        if (icrValue == null || icrValue !in 1f..100f) {
            showError(icr, "Enter valid ICR (1–100)")
            return false
        } else icr.background.clearColorFilter()

        // Target glucose validation
        val targetValue = target.text.toString().toFloatOrNull()
        if (targetValue == null || targetValue !in 70f..180f) {
            showError(target, "Enter valid glucose target (70–180 mg/dL)")
            return false
        } else target.background.clearColorFilter()

        // Diagnosis year validation
        val yearValue = diagnosisYear.text.toString().toIntOrNull()
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        if (yearValue == null || yearValue < 1900 || yearValue > currentYear) {
            showError(diagnosisYear, "Enter a valid diagnosis year")
            return false
        } else diagnosisYear.background.clearColorFilter()

        // Diet type validation
        if (dietType.text.toString().isEmpty()) {
            showError(dietType, "Enter your diet type")
            return false
        } else dietType.background.clearColorFilter()

        return isValid
    }
}
