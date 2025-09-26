package com.saveetha.insulinbuddy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.saveetha.insulinbuddy.R

class UserSupportActivity : AppCompatActivity() {

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_support)

        val videoMap = mapOf(
            R.id.btnVideo1 to "https://www.youtube.com/watch?v=RCFXujhJcSE",        // Basics of Insulin
            R.id.btnVideo2 to "https://www.youtube.com/watch?v=RcUspg3jb8Q",       // Pen Injections
            R.id.btnVideo3 to "https://www.youtube.com/watch?v=McxO3oOkzc4",       // Insulin-to-Carb Ratio
            R.id.btnVideo4 to "https://www.youtube.com/watch?v=v7xsm0-MO_E",       // Insulin Sensitivity Factor
            R.id.btnVideo5 to "https://www.youtube.com/watch?v=Fp4b80MqC6Q",       // Insulin in Type 2
            R.id.btnVideo6 to "https://www.youtube.com/watch?v=agOzxh3zQb0",       // Type 1 Overview (JDRF)
            R.id.btnVideo7 to "https://www.youtube.com/watch?v=WM3v85H1jX0",       // Hypoglycemia Treatment
            R.id.btnVideo8 to "https://www.youtube.com/watch?v=xtbjNvj7k9M",       // Hyperglycemia Treatment
            R.id.btnVideo9 to "https://www.youtube.com/watch?v=w2ZeG0NSK7A",       // Rotate Injection Sites
            R.id.btnVideo10 to "https://www.youtube.com/watch?v=n47d47Aoapg"       // Correction Dose for High BG
        )

        val articleMap = mapOf(
            R.id.btnArticle1 to "https://www.healthline.com/health/type-2-diabetes/insulin#administration-and-dosage",
            R.id.btnArticle2 to "https://www.healthline.com/health/diabetes/insulin-injection-sites",
            R.id.btnArticle3 to "https://www.healthline.com/health/diabetes/insulin-to-carb-ratio",
            R.id.btnArticle4 to "https://www.healthline.com/health/insulin-sensitivity-factor",
            R.id.btnArticle5 to "https://www.healthline.com/health/type-1-diabetes",
            R.id.btnArticle6 to "https://www.healthline.com/health/type-2-diabetes",
            R.id.btnArticle7 to "https://www.healthline.com/health/hypoglycemia",
            R.id.btnArticle8 to "https://www.healthline.com/health/hyperglycemia",
            R.id.btnArticle9 to "https://www.healthline.com/diabetesmine/insulin-storage-tips",
            R.id.btnArticle10 to "https://www.healthline.com/health/diabetes/diabetes-and-exercise"
        )

        val clicker = View.OnClickListener { v ->
            videoMap[v.id]?.let { openUrl(it) }
            articleMap[v.id]?.let { openUrl(it) }
        }

        (videoMap.keys + articleMap.keys).forEach { id ->
            findViewById<Button>(id).setOnClickListener(clicker)
        }
    }
}
