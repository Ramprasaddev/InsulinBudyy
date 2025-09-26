package com.saveetha.insulinbuddy

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Simple Notification model
data class NotificationModel(
    val title: String,
    val message: String,
    val timestamp: Long
)

// RecyclerView Adapter
class NotificationAdapter(val notifications: MutableList<NotificationModel>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.notificationTitle)
        val message: TextView = itemView.findViewById(R.id.notificationMessage)
        val timestamp: TextView = itemView.findViewById(R.id.notificationTimestamp)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): NotificationViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.title.text = notification.title
        holder.message.text = notification.message
        holder.timestamp.text = java.text.SimpleDateFormat(
            "MMM d, h:mm a",
            java.util.Locale.getDefault()
        ).format(java.util.Date(notification.timestamp))
    }

    override fun getItemCount() = notifications.size

    fun getNotificationAt(position: Int): NotificationModel {
        return notifications[position]
    }
}

// Main Activity
class NotificationsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var clearAllButton: Button
    private lateinit var insertFakeDataButton: Button
    private lateinit var adapter: NotificationAdapter
    private val notificationsList: MutableList<NotificationModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        recyclerView = findViewById(R.id.notificationsRecyclerView)
        clearAllButton = findViewById(R.id.buttonClearAll)
        insertFakeDataButton = findViewById(R.id.buttonInsertFakeData)

        adapter = NotificationAdapter(notificationsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Clear all
        clearAllButton.setOnClickListener {
            notificationsList.clear()
            adapter.notifyDataSetChanged()
        }

        // Insert 4 fake notifications
        insertFakeDataButton.setOnClickListener {
            val fakeNotifications = listOf(
                NotificationModel(
                    title = "⏰ Insulin Reminder",
                    message = "It’s time to take your scheduled insulin dose.",
                    timestamp = System.currentTimeMillis()
                ),
                NotificationModel(
                    title = "📊 Glucose Check Alert",
                    message = "Please record your blood glucose level before lunch.",
                    timestamp = System.currentTimeMillis()
                ),
                NotificationModel(
                    title = "🍽️ Carb Logging",
                    message = "Don’t forget to log your meal carbs for accurate insulin prediction.",
                    timestamp = System.currentTimeMillis()
                ),
                NotificationModel(
                    title = "📑 Weekly Health Summary",
                    message = "Your weekly insulin & glucose summary report is ready.",
                    timestamp = System.currentTimeMillis()
                )
            )
            notificationsList.addAll(fakeNotifications)
            adapter.notifyDataSetChanged()
        }

        // Swipe to delete one notification
        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                notificationsList.removeAt(vh.adapterPosition)
                adapter.notifyItemRemoved(vh.adapterPosition)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}
