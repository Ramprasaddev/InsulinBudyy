package com.saveetha.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.saveetha.insulinbuddy.utils.SessionManager
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
            // show username immediately
            profileName.text = username
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
        val url = "https://606tr6vg-80.inc1.devtunnels.ms/INSULIN/get_user_profile.php"

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
                            avatarImage.setImageResource(
                                if (gender.equals("female", ignoreCase = true))
                                    R.drawable.avatar_female
                                else
                                    R.drawable.avatar_male
                            )
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
}
