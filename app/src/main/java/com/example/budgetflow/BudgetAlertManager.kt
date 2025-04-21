package com.example.budgetflow

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

object BudgetAlertManager {

    private const val CHANNEL_ID = "budget_alerts"
    private const val NOTIFICATION_ID = 101

    // Function to create a notification channel for Android versions >= Oreo
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Budget Alerts"
            val descriptionText = "Alerts when you exceed your monthly budget"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Function to check and notify when spending nears or exceeds the budget
    fun checkAndNotifyBudget(
        context: Context,
        budget: Double,
        totalExpenses: Double,
        statusTextView: TextView? = null,
        progressBar: ProgressBar? = null
    ) {
        val remaining = budget - totalExpenses
        val percentUsed = if (budget > 0) ((totalExpenses / budget) * 100).toInt() else 0

        // Update UI if provided
        statusTextView?.text = "Remaining: Rs%.2f / Rs%.2f".format(remaining, budget)
        progressBar?.progress = percentUsed.coerceAtMost(100)

        // Set color based on the progress
        progressBar?.let {
            val color = when {
                percentUsed >= 100 -> android.R.color.holo_red_dark  // Exceeded budget
                percentUsed >= 80 -> android.R.color.holo_orange_dark  // Warning nearing budget
                else -> android.R.color.holo_green_dark  // Safe zone
            }
            it.progressDrawable.setColorFilter(ContextCompat.getColor(context, color), android.graphics.PorterDuff.Mode.SRC_IN)
        }

        // Send a notification when the budget is exceeded
        if (percentUsed >= 100) {
            sendBudgetExceededNotification(context)
        }
        // Send a warning notification when 80% or more of the budget is spent
        else if (percentUsed >= 80) {
            sendBudgetWarningNotification(context)
        }
    }

    // Function to send a notification when the budget is exceeded
    private fun sendBudgetExceededNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Budget Alert")
            .setContentText("You’ve exceeded your monthly budget!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    // Function to send a warning notification when nearing the budget (>= 80%)
    private fun sendBudgetWarningNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Budget Warning")
            .setContentText("You’re approaching your budget limit!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
