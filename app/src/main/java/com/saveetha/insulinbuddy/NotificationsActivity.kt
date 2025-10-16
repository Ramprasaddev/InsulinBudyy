package com.simats.insulinbuddy

import android.app.*
import android.content.*
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


data class NotificationModel(
    val title: String,
    val message: String,
    val timestamp: Long
)

class NotificationsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var clearAllButton: Button
    private lateinit var adapter: NotificationAdapter
    private val notificationsList: MutableList<NotificationModel> = mutableListOf()
    private val client = OkHttpClient()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        recyclerView = findViewById(R.id.notificationsRecyclerView)
        clearAllButton = findViewById(R.id.buttonClearAll)
        adapter = NotificationAdapter(notificationsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        clearAllButton.setOnClickListener {
            notificationsList.clear()
            adapter.notifyDataSetChanged()
        }

        // Load notifications initially
        loadAIgeneratedNotifications()

        // Schedule 4 daily reminders (every 6 hours)
        scheduleDailyReminders()
    }

    private fun loadAIgeneratedNotifications() {
        coroutineScope.launch {
            try {
                val aiPrompts = listOf(
                    "Generate short motivational diabetes care reminders about insulin, glucose, carbs, and health monitoring",
                    "Create simple daily diabetes management tips",
                    "Write reminders for healthy lifestyle and glucose tracking in short sentences"
                )

                val combinedPrompt = aiPrompts.joinToString(". ")

                val requestBody = """
                    {"inputs": "$combinedPrompt. Generate 24 short reminders."}
                """.trimIndent()

                val request = Request.Builder()
                    .url("https://api-inference.huggingface.co/models/gpt2")
                    .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                val messages = extractAItexts(body)
                val now = System.currentTimeMillis()

                withContext(Dispatchers.Main) {
                    messages.forEach {
                        notificationsList.add(
                            NotificationModel(
                                title = "🤖 AI Health Reminder",
                                message = it,
                                timestamp = now
                            )
                        )
                    }
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun extractAItexts(response: String): List<String> {
        // Simplify the generated text into short readable lines
        return response
            .split(".")
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.length in 10..100 }
            .take(24)
    }

    private fun scheduleDailyReminders() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val times = listOf(6, 12, 18, 24) // every 6 hours

        for (i in times.indices) {
            val intent = Intent(this, NotificationReceiver::class.java)
            intent.putExtra("reminder_id", i)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                i,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val triggerTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, times[i])
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }
}

class NotificationAdapter(private val notifications: MutableList<NotificationModel>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val title: android.widget.TextView = itemView.findViewById(R.id.notificationTitle)
        val message: android.widget.TextView = itemView.findViewById(R.id.notificationMessage)
        val timestamp: android.widget.TextView = itemView.findViewById(R.id.notificationTimestamp)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): NotificationViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val n = notifications[position]
        holder.title.text = n.title
        holder.message.text = n.message
        holder.timestamp.text = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            .format(Date(n.timestamp))
    }

    override fun getItemCount() = notifications.size
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val channelId = "insulin_reminder_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "InsulinBuddy Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val reminderMessages = listOf(
            "💉 Please update your insulin dosage in the InsulinBuddy app.",
            "📈 Record your current glucose level for AI tracking.",
            "🍽️ Log your carb intake for better insulin prediction.",
            "🩺 Time for your daily health check and sync!"
        )

        val randomMessage = reminderMessages.random()

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("InsulinBuddy Reminder")
            .setContentText(randomMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(Random.nextInt(), builder.build())
    }
}
