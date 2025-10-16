package com.simats.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.simats.insulinbuddy.SessionManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    private lateinit var avatarImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        avatarImage = findViewById(R.id.avatarImage)
        profileName = findViewById(R.id.profileName)

        val editProfile = findViewById<TextView>(R.id.editProfileText)
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        val userSupportLayout = findViewById<LinearLayout>(R.id.userSupportLayout)
        val aboutLayout = findViewById<LinearLayout>(R.id.aboutLayout)

        sessionManager = SessionManager(this)
        val username = sessionManager.getUsername()
        if (username != null) {
            // Show cached username and gender immediately for fast UX
            profileName.text = username
            val cachedGender = sessionManager.getGender()
            if (cachedGender != null) {
                setAvatar(cachedGender)
            }
            // Also refresh from server in background to keep it up to date
            fetchGenderFromServer(username)
        } else {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        editProfile.setOnClickListener {
            startActivity(Intent(this, EditProfile::class.java))
        }

        logoutButton.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        userSupportLayout.setOnClickListener {
            startActivity(Intent(this, UserSupportActivity::class.java))
        }

        aboutLayout.setOnClickListener {
            startActivity(Intent(this, AboutUsActivity::class.java))
        }
    }

    private fun fetchGenderFromServer(username: String) {
        val url = "http://14.139.187.229:8081/PDD-2025(9thmonth)/InsulinBuddy/get_user_profile.php"

        val json = JSONObject().apply {
            put("username", username)
        }

        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = RequestBody.create(mediaType, json.toString())
        val request = Request.Builder().url(url).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                try {
                    val jsonObject = JSONObject(responseBody ?: "{}")

                    runOnUiThread {
                        if (jsonObject.optString("status") == "success") {
                            val gender = jsonObject.optString("gender", "male")
                            sessionManager.setGender(if (gender.equals("female", true)) "Female" else "Male")
                            setAvatar(gender)
                        } else {
                            Toast.makeText(this@ProfileActivity, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Invalid server response: $responseBody",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

    private fun setAvatar(gender: String) {
        val isFemale = gender.equals("female", true) || gender.equals("Female", true)
        avatarImage.setImageResource(if (isFemale) R.drawable.avatar_female else R.drawable.avatar_male)
    }
}
