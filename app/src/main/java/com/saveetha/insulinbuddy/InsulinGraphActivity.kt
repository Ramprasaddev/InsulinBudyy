package com.saveetha.insulinbuddy

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

data class InsulinStats(
    val lifetime_avg: Double = 0.0,
    val last7days_avg: Double = 0.0,
    val selected_avg: Double = 0.0
)

data class InsulinEntry(
    val date: String = "",     // yyyy-MM-dd HH:mm:ss
    val session: String = "",  // Morning/Afternoon/Night (may be empty)
    val value: Double = 0.0
)

data class InsulinResponse(
    val stats: InsulinStats? = null,
    val data: List<InsulinEntry>? = null
)

class InsulinGraphActivity : AppCompatActivity() {

    private lateinit var insulinChart: LineChart
    private lateinit var btnDownloadPDF: Button
    private lateinit var txtStats: TextView

    private val client = OkHttpClient()

    private val username: String by lazy { intent.getStringExtra("username") ?: "test_user" }
    private val currentRange: String by lazy { intent.getStringExtra("range") ?: "today" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insulin_graph)

        insulinChart = findViewById(R.id.lineChart)
        btnDownloadPDF = findViewById(R.id.btnDownloadGraph)
        txtStats = findViewById(R.id.txtStats)

        setupChartAppearance()

        val (startDate, endDate) = calculateDateRange(currentRange)
        fetchGraphData(startDate, endDate)

        btnDownloadPDF.setOnClickListener {
            saveChartAsPdf()
        }
    }

    private fun setupChartAppearance() {
        insulinChart.setNoDataText("No chart data available.")
        insulinChart.setTouchEnabled(true)
        insulinChart.isDragEnabled = true
        insulinChart.setScaleEnabled(true)
        insulinChart.axisRight.isEnabled = false
        insulinChart.description = Description().apply { text = "Insulin Intake Trends" }
        val x = insulinChart.xAxis
        x.position = XAxis.XAxisPosition.BOTTOM
        x.setDrawGridLines(false)
        x.granularity = 1f
        insulinChart.axisLeft.setDrawGridLines(true)
    }

    private fun calculateDateRange(range: String): Pair<String, String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val endDate = dateFormat.format(calendar.time)

        when (range.lowercase(Locale.getDefault())) {
            "yesterday" -> calendar.add(Calendar.DATE, -1)
            "last7days" -> calendar.add(Calendar.DATE, -6) // last 7 days including today
            "thismonth" -> calendar.set(Calendar.DAY_OF_MONTH, 1)
            "last1month" -> calendar.add(Calendar.MONTH, -1)
            "last6months" -> calendar.add(Calendar.MONTH, -6)
            "last1year" -> calendar.add(Calendar.YEAR, -1)
            // default "today" -> no change
        }

        val startDate = dateFormat.format(calendar.time)
        return Pair(startDate, endDate)
    }

    private fun fetchGraphData(startDate: String, endDate: String) {
        val url = "https://606tr6vg-80.inc1.devtunnels.ms/INSULIN/fetch_insulin_data.php" +
                "?username=${username}&start_date=${startDate}&end_date=${endDate}"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@InsulinGraphActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body == null) {
                    runOnUiThread {
                        Toast.makeText(this@InsulinGraphActivity, "Empty response from server", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                try {
                    val jsonElement: JsonElement = JsonParser.parseString(body)

                    val entriesList: List<InsulinEntry> = when {
                        jsonElement.isJsonArray -> {
                            val listType = object : TypeToken<List<InsulinEntry>>() {}.type
                            Gson().fromJson(jsonElement, listType)
                        }
                        jsonElement.isJsonObject -> {
                            val obj = jsonElement.asJsonObject
                            if (obj.has("data")) {
                                val listType = object : TypeToken<List<InsulinEntry>>() {}.type
                                Gson().fromJson(obj.getAsJsonArray("data"), listType)
                            } else {
                                val listType = object : TypeToken<List<InsulinEntry>>() {}.type
                                Gson().fromJson(jsonElement, listType) ?: emptyList()
                            }
                        }
                        else -> emptyList()
                    }

                    var stats: InsulinStats? = null
                    try {
                        val obj = JsonParser.parseString(body).asJsonObject
                        if (obj.has("stats")) {
                            stats = Gson().fromJson(obj.get("stats"), InsulinStats::class.java)
                        }
                    } catch (_: Exception) {
                        // ignore - stats not present
                    }

                    runOnUiThread {
                        updateUiWithData(entriesList, stats)
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@InsulinGraphActivity, "Parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun updateUiWithData(insulinList: List<InsulinEntry>, statsFromServer: InsulinStats?) {
        if (statsFromServer != null) {
            txtStats.text =
                "Lifetime Avg: ${formatDouble(statsFromServer.lifetime_avg)} units\n" +
                        "Last 7 Days Avg: ${formatDouble(statsFromServer.last7days_avg)} units\n" +
                        "Selected Avg: ${formatDouble(statsFromServer.selected_avg)} units"
        } else {
            val selectedAvg = if (insulinList.isNotEmpty()) insulinList.map { it.value }.average() else Double.NaN
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sevenDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }.time
            val last7 = insulinList.filter {
                try {
                    val d = sdf.parse(it.date.substring(0, 10))
                    d != null && d >= sevenDaysAgo
                } catch (_: Exception) { false }
            }
            val last7Avg = if (last7.isNotEmpty()) last7.map { it.value }.average() else Double.NaN

            txtStats.text =
                "Lifetime Avg: -- units\n" +
                        "Last 7 Days Avg: ${if (!last7Avg.isNaN()) formatDouble(last7Avg) else "--"} units\n" +
                        "Selected Avg: ${if (!selectedAvg.isNaN()) formatDouble(selectedAvg) else "--"} units"
        }

        if (insulinList.isEmpty()) {
            insulinChart.clear()
            insulinChart.invalidate()
            Toast.makeText(this, "No data available for selected range", Toast.LENGTH_SHORT).show()
            return
        }

        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        insulinList.forEachIndexed { idx, item ->
            entries.add(Entry(idx.toFloat(), item.value.toFloat()))

            val label = if (currentRange.equals("today", true) || currentRange.equals("yesterday", true)) {
                if (item.session.isNullOrBlank()) getTimeOfDay(item.date) else item.session
            } else {
                item.date.substring(0, 10) // Just show date without time
            }
            labels.add(label)
        }

        val dataSet = LineDataSet(entries, "Insulin Intake (units)").apply {
            color = ContextCompat.getColor(this@InsulinGraphActivity, R.color.teal_700)
            setDrawCircles(true)
            circleRadius = 5f
            setDrawValues(true)
            valueTextSize = 10f
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.15f
        }

        insulinChart.data = LineData(dataSet)

        insulinChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            labelRotationAngle = -45f
            setDrawGridLines(false)
            setLabelCount(labels.size, true)
        }

        insulinChart.axisRight.isEnabled = false
        insulinChart.description = Description().apply { text = "Insulin Intake Over Time" }
        insulinChart.invalidate()
    }

    private fun getTimeOfDay(dateTime: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(dateTime)
            val hour = date.hours
            when {
                hour in 5..11 -> "Morning"
                hour in 12..17 -> "Afternoon"
                else -> "Night"
            }
        } catch (e: Exception) {
            "Reading"
        }
    }

    private fun formatDouble(d: Double): String {
        return if (d.isNaN()) "--" else String.format(Locale.getDefault(), "%.1f", d)
    }

    private fun saveChartAsPdf() {
        insulinChart.post {
            try {
                val b = Bitmap.createBitmap(insulinChart.width, insulinChart.height, Bitmap.Config.ARGB_8888)
                val c = Canvas(b)
                insulinChart.draw(c)

                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(b.width, b.height, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawBitmap(b, 0f, 0f, null)

                // Add text below the chart
               val paint = Paint().apply {
                   color = android.graphics.Color.BLACK
                   textSize = 12f
               }
                canvas.drawText(txtStats.text.toString(), 20f, b.height + 50f, paint)

                pdfDocument.finishPage(page)

                val pdfDir = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "InsulinReports")
                if (!pdfDir.exists()) pdfDir.mkdirs()
                val file = File(pdfDir, "insulin_report_${System.currentTimeMillis()}.pdf")
                val fos = FileOutputStream(file)
                pdfDocument.writeTo(fos)
                fos.close()
                pdfDocument.close()

                Toast.makeText(this, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}