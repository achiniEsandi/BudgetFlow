package com.example.budgetflow

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class BudgetActivity : AppCompatActivity() {

    private lateinit var etBudget: EditText
    private lateinit var btnSave: Button
    private lateinit var tvBudgetStatus: TextView
    private lateinit var budgetProgressBar: ProgressBar

    private val CHANNEL_ID = "budget_alerts"
    private val NOTIFICATION_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        etBudget = findViewById(R.id.etBudget)
        btnSave = findViewById(R.id.btnSaveBudget)
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus)
        budgetProgressBar = findViewById(R.id.budgetProgressBar)

        BudgetAlertManager.createNotificationChannel(this)
        requestNotificationPermission()

        btnSave.setOnClickListener {
            val budget = etBudget.text.toString().toDoubleOrNull()
            if (budget != null && budget > 0) {
                BudgetUtils.saveBudget(this, budget)
                Toast.makeText(this, "Budget saved!", Toast.LENGTH_SHORT).show()
                updateBudgetUI()
            } else {
                Toast.makeText(this, "Enter a valid budget amount", Toast.LENGTH_SHORT).show()
            }
        }

        updateBudgetUI()
    }

    private fun updateBudgetUI() {
        val budget = BudgetUtils.getBudget(this)
        val totalExpenses = TransactionManager.getMonthlyExpenses(this)

        BudgetAlertManager.checkAndNotifyBudget(
            this,
            budget,
            totalExpenses,
            tvBudgetStatus,
            budgetProgressBar
        )
    }



    private fun sendBudgetExceededNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return // Do not show if permission is not granted
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // ✅ Use custom notification icon
            .setContentTitle("Budget Alert")
            .setContentText("You’ve exceeded your monthly budget!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(101, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Budget Alerts"
            val descriptionText = "Notifications when you exceed your budget"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_CODE
            )
        }
    }
}
