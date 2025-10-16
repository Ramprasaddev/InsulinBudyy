package com.simats.insulinbuddy

import android.graphics.Bitmap
import android.graphics.Canvas
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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// Use Double for stats because Gson numeric defaults to Double
data class GlucoseStats(
    val lifetime_avg: Double = 0.0,
    val last7days_avg: Double = 0.0,
    val selected_avg: Double = 0.0
)

data class GlucoseEntry(
    val date: String = "",     // yyyy-MM-dd
    val session: String = "",  // Morning/Afternoon/Night (may be empty)
    val value: Double = 0.0
)

data class GlucoseResponse(
    val stats: GlucoseStats? = null,
    val data: List<GlucoseEntry>? = null
)

class GlucoseGraphActivity : AppCompatActivity() {

    private lateinit var glucoseChart: LineChart
    private lateinit var btnDownloadPDF: Button
    private lateinit var txtStats: TextView

    private val client = OkHttpClient()

    // Replace with actual username passed via Intent if available
    private val username: String by lazy { intent.getStringExtra("username") ?: "test_user" }
    // Range string (today, yesterday, last7days, thismonth...) passed through Intent ("range")
    private val currentRange: String by lazy { intent.getStringExtra("range") ?: "today" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glucose_graph) // make sure XML matches below

        glucoseChart = findViewById(R.id.glucoseChart)
        btnDownloadPDF = findViewById(R.id.btnDownloadPDF)
        txtStats = findViewById(R.id.txtStats)

        setupChartAppearance()

        // Calculate start and end dates using same logic as server expects
        val (startDate, endDate) = calculateDateRange(currentRange)
        fetchGraphData(startDate, endDate)

        btnDownloadPDF.setOnClickListener {
            // no WRITE_EXTERNAL_STORAGE permission needed for getExternalFilesDir()
            saveChartAsPdf()
        }
    }

    private fun setupChartAppearance() {
        glucoseChart.setNoDataText("No chart data available.")
        glucoseChart.setTouchEnabled(true)
        glucoseChart.isDragEnabled = true
        glucoseChart.setScaleEnabled(true)
        glucoseChart.axisRight.isEnabled = false
        glucoseChart.description = Description().apply { text = "Glucose Level Trends" }
        val x = glucoseChart.xAxis
        x.position = XAxis.XAxisPosition.BOTTOM
        x.setDrawGridLines(false)
        x.granularity = 1f
        glucoseChart.axisLeft.setDrawGridLines(true)
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
        val url =
            "http://14.139.187.229:8081/PDD-2025(9thmonth)/InsulinBuddy/fetch_glucose_data.php" +
                    "?username=${username}&start_date=${startDate}&end_date=${endDate}"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@GlucoseGraphActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body == null) {
                    runOnUiThread {
                        Toast.makeText(this@GlucoseGraphActivity, "Empty response from server", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                try {
                    val jsonElement: JsonElement = JsonParser.parseString(body)

                    // Determine format:
                    // - If array: old format -> array of {date, value} or {date, session, value}
                    // - If object with "data" and "stats": new format
                    val entriesList: List<GlucoseEntry> = when {
                        jsonElement.isJsonArray -> {
                            // parse array directly
                            val listType = object : TypeToken<List<GlucoseEntry>>() {}.type
                            Gson().fromJson(jsonElement, listType)
                        }
                        jsonElement.isJsonObject -> {
                            val obj = jsonElement.asJsonObject
                            if (obj.has("data")) {
                                val listType = object : TypeToken<List<GlucoseEntry>>() {}.type
                                Gson().fromJson(obj.getAsJsonArray("data"), listType)
                            } else {
                                // fallback: maybe it's an object with single record or different structure
                                // try parse "data" as single item
                                val listType = object : TypeToken<List<GlucoseEntry>>() {}.type
                                Gson().fromJson(jsonElement, listType) ?: emptyList()
                            }
                        }
                        else -> emptyList()
                    }

                    // Parse stats if present
                    var stats: GlucoseStats? = null
                    try {
                        val obj = JsonParser.parseString(body).asJsonObject
                        if (obj.has("stats")) {
                            stats = Gson().fromJson(obj.get("stats"), GlucoseStats::class.java)
                        }
                    } catch (_: Exception) {
                        // ignore - stats not present
                    }

                    runOnUiThread {
                        updateUiWithData(entriesList, stats)
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@GlucoseGraphActivity, "Parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun updateUiWithData(glucoseList: List<GlucoseEntry>, statsFromServer: GlucoseStats?) {
        // Set stats text (use server stats if available, else compute what we can)
        if (statsFromServer != null) {
            txtStats.text =
                "Lifetime Avg: ${formatDouble(statsFromServer.lifetime_avg)} mg/dL\n" +
                        "Last 7 Days Avg: ${formatDouble(statsFromServer.last7days_avg)} mg/dL\n" +
                        "Selected Avg: ${formatDouble(statsFromServer.selected_avg)} mg/dL"
        } else {
            // compute selected avg
            val selectedAvg = if (glucoseList.isNotEmpty()) glucoseList.map { it.value }.average() else Double.NaN
            // compute last7days avg from returned list (best effort)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sevenDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }.time
            val last7 = glucoseList.filter {
                try {
                    val d = sdf.parse(it.date)
                    d != null && d >= sevenDaysAgo
                } catch (_: Exception) { false }
            }
            val last7Avg = if (last7.isNotEmpty()) last7.map { it.value }.average() else Double.NaN

            txtStats.text =
                "Lifetime Avg: -- mg/dL\n" +
                        "Last 7 Days Avg: ${if (!last7Avg.isNaN()) formatDouble(last7Avg) else "--"} mg/dL\n" +
                        "Selected Avg: ${if (!selectedAvg.isNaN()) formatDouble(selectedAvg) else "--"} mg/dL"
        }

        // Build chart entries and labels
        if (glucoseList.isEmpty()) {
            glucoseChart.clear()
            glucoseChart.invalidate()
            Toast.makeText(this, "No data available for selected range", Toast.LENGTH_SHORT).show()
            return
        }

        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        val today = getTodayDate()
        val yesterday = getYesterdayDate()

        glucoseList.forEachIndexed { idx, item ->
            // x = index, y = value
            entries.add(Entry(idx.toFloat(), item.value.toFloat()))

            // For today/yesterday show session label; otherwise show date
            val label = if (currentRange.equals("today", true) || currentRange.equals(
                    "yesterday",
                    true
                )
            ) {
                // If session is empty, but date is today/yesterday, fall back to time-of-day text
                if (item.session.isNullOrBlank()) "Reading" else item.session
            } else {
                item.date
            }
            labels.add(label)
        }

        val dataSet = LineDataSet(entries, "Glucose Level (mg/dL)").apply {
            color = ContextCompat.getColor(this@GlucoseGraphActivity, R.color.teal_700)
            setDrawCircles(true)
            circleRadius = 5f
            setDrawValues(true)        // show values above points
            valueTextSize = 10f
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.15f
        }

        glucoseChart.data = LineData(dataSet)

        // Configure X-axis with custom labels
        glucoseChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            labelRotationAngle = -45f
            setDrawGridLines(false)
            // show exactly as many labels as data points
            setLabelCount(labels.size, true)
        }

        glucoseChart.axisRight.isEnabled = false
        glucoseChart.description = Description().apply { text = "Glucose Over Time" }
        glucoseChart.invalidate()
    }

    private fun formatDouble(d: Double): String {
        // show with 1 or 2 decimals based on value
        return if (d.isNaN()) "--" else String.format(Locale.getDefault(), "%.2f", d)
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getYesterdayDate(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }

    private fun saveChartAsPdf() {
        // Ensure chart has been laid out
        glucoseChart.post {
            try {
                // capture bitmap of the chart
                val b = Bitmap.createBitmap(glucoseChart.width, glucoseChart.height, Bitmap.Config.ARGB_8888)
                val c = Canvas(b)
                glucoseChart.draw(c)

                // create PDF document and a single page with chart size
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(b.width, b.height, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawBitmap(b, 0f, 0f, null)
                pdfDocument.finishPage(page)

                // Save to app-specific documents folder
                val pdfDir = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GlucoseReports")
                if (!pdfDir.exists()) pdfDir.mkdirs()
                val file = File(pdfDir, "glucose_report_${System.currentTimeMillis()}.pdf")
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
