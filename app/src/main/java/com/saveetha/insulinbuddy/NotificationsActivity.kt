package com.simats.insulinbuddy

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        recyclerView = findViewById(R.id.notificationsRecyclerView)
        clearAllButton = findViewById(R.id.buttonClearAll)
        findViewById<TextView?>(R.id.notificationsSubtitle)?.text = "Daily reminders appear here."

        adapter = NotificationAdapter(notificationsList) { position ->
            // remove single
            notificationsList.removeAt(position)
            adapter.notifyItemRemoved(position)
            saveNotifications(this, notificationsList)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        clearAllButton.setOnClickListener {
            notificationsList.clear()
            adapter.notifyDataSetChanged()
            saveNotifications(this, notificationsList)
        }

        // Load stored notifications
        notificationsList.addAll(loadNotifications(this))
        adapter.notifyDataSetChanged()
    }
}

class NotificationAdapter(
    private val notifications: MutableList<NotificationModel>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

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
        val deleteBtn = holder.itemView.findViewById<android.widget.ImageButton?>(R.id.buttonDelete)
        deleteBtn?.setOnClickListener { onDelete(holder.adapterPosition) }
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
            "Time to record your glucose and insulin. Keep your log up to date.",
            "Daily check-in: update glucose and insulin, view your graphs.",
            "Don't forget to enter today's glucose and insulin, then review reports.",
            "Quick reminder: log glucose/insulin now and check your trends."
        )

        val message = reminderMessages.random()

        // Persist to local storage
        addNotification(context, NotificationModel(
            title = "InsulinBuddy Reminder",
            message = message,
            timestamp = System.currentTimeMillis()
        ))

        // Tap opens notifications screen
        val tapIntent = Intent(context, NotificationsActivity::class.java)
        val tapPending = PendingIntent.getActivity(
            context,
            1001,
            tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle("InsulinBuddy Reminder")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(tapPending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(Random.nextInt(), builder.build())
    }
}

// Storage helpers
private const val PREFS_NAME = "notifications_store"
private const val PREFS_KEY = "items"

fun loadNotifications(context: Context): List<NotificationModel> {
    val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val json = sp.getString(PREFS_KEY, "[]") ?: "[]"
    val arr = JSONArray(json)
    val list = mutableListOf<NotificationModel>()
    for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        list += NotificationModel(
            title = o.optString("title"),
            message = o.optString("message"),
            timestamp = o.optLong("timestamp")
        )
    }
    return list
}

fun saveNotifications(context: Context, items: List<NotificationModel>) {
    val arr = JSONArray()
    items.forEach {
        val o = JSONObject()
        o.put("title", it.title)
        o.put("message", it.message)
        o.put("timestamp", it.timestamp)
        arr.put(o)
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(PREFS_KEY, arr.toString())
        .apply()
}

fun addNotification(context: Context, item: NotificationModel) {
    val existing = loadNotifications(context).toMutableList()
    existing.add(0, item)
    saveNotifications(context, existing)
}

fun scheduleDailyReminder6am(context: Context) {
    // Backwards-compat: now schedules 4 daily times and reconciles missed ones
    scheduleDailyReminderMultiple(context, listOf(9 to 0, 13 to 0, 19 to 0, 22 to 0))
}

fun scheduleDailyReminderMultiple(context: Context, times: List<Pair<Int, Int>>) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    times.forEachIndexed { index, (hour, minute) ->
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.putExtra("slot_hour", hour)
        intent.putExtra("slot_min", minute)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            700 + index,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}

private fun todayKey(): String {
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return sdf.format(Date())
}

private const val PREFS_DELIVERED = "notifications_delivered"

private fun markDelivered(context: Context, hour: Int) {
    val sp = context.getSharedPreferences(PREFS_DELIVERED, Context.MODE_PRIVATE)
    val key = todayKey()
    val set = sp.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    set.add(hour.toString())
    sp.edit().putStringSet(key, set).apply()
}

private fun isDelivered(context: Context, hour: Int): Boolean {
    val sp = context.getSharedPreferences(PREFS_DELIVERED, Context.MODE_PRIVATE)
    val key = todayKey()
    val set = sp.getStringSet(key, emptySet()) ?: emptySet()
    return set.contains(hour.toString())
}

fun reconcileMissedNotificationsForToday(context: Context) {
    val now = Calendar.getInstance()
    val planned = listOf(9 to 0, 13 to 0, 19 to 0, 22 to 0)
    planned.forEach { (hour, minute) ->
        val slotTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (slotTime.before(now) && !isDelivered(context, hour)) {
            // Add to in-app list so user sees all four at night
            addNotification(context, NotificationModel(
                title = "InsulinBuddy Reminder",
                message = when (hour) {
                    9 -> "Morning: record glucose and insulin, then review your report."
                    13 -> "Afternoon: log lunch glucose/carbs and insulin."
                    19 -> "Evening: update glucose/insulin and check your graphs."
                    else -> "Night: final check — record today's glucose/insulin and review."
                },
                timestamp = slotTime.timeInMillis
            ))
            markDelivered(context, hour)
        }
    }
}
