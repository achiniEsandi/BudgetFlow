package com.example.budgetflow

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Home : AppCompatActivity() {

    private var totalBalance: Double = 0.0
    private var totalExpenses: Double = 0.0
    private var monthlyBudget: Double = 0.0
    private val POST_NOTIFICATIONS_REQUEST_CODE = 101
    private val CHANNEL_ID = "budget_alerts"

    private lateinit var balanceTextView: TextView
    private lateinit var expenseTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Bind UI elements
        balanceTextView = findViewById(R.id.totalBalanceTextView)
        expenseTextView = findViewById(R.id.totalExpenseTextView)

        checkAndRequestNotificationPermission()
        BudgetAlertManager.createNotificationChannel(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transactions -> {
                    startActivity(Intent(this, ListTransactionsActivity::class.java))
                    true
                }

                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    true
                }

                else -> false
            }
        }

        val addButton: FloatingActionButton = findViewById(R.id.addBtn)
        addButton.setOnClickListener {
            try {
                val intent = Intent(this, AddTransaction::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("AddTransaction", "Error launching AddTransactionActivity", e)
            }
        }

        val dailyBtn = findViewById<Button>(R.id.dailyBtn)
        val weeklyBtn = findViewById<Button>(R.id.weeklyBtn)
        val monthlyBtn = findViewById<Button>(R.id.monthlyBtn)
        setupTabSelection(dailyBtn, weeklyBtn, monthlyBtn)

        loadBalanceAndExpense()
    }

    private fun setupTabSelection(dailyBtn: Button, weeklyBtn: Button, monthlyBtn: Button) {
        dailyBtn.setOnClickListener {
            dailyBtn.isSelected = true
            weeklyBtn.isSelected = false
            monthlyBtn.isSelected = false
        }

        weeklyBtn.setOnClickListener {
            dailyBtn.isSelected = false
            weeklyBtn.isSelected = true
            monthlyBtn.isSelected = false
        }

        monthlyBtn.setOnClickListener {
            dailyBtn.isSelected = false
            weeklyBtn.isSelected = false
            monthlyBtn.isSelected = true
        }

        weeklyBtn.isSelected = true
    }

    private fun loadBalanceAndExpense() {
        val sharedPreferences = getSharedPreferences("transaction_prefs", Context.MODE_PRIVATE)
        totalBalance = sharedPreferences.getFloat("total_balance", 0.0f).toDouble()
        totalExpenses = sharedPreferences.getFloat("total_expense", 0.0f).toDouble()
        monthlyBudget = sharedPreferences.getFloat("monthly_budget", 0.0f).toDouble()

        updateUI()
        checkBudgetExceeded()
    }

    private fun updateBalanceAndExpense(transaction: Transaction) {
        if (transaction.type.equals("Expense", ignoreCase = true)) {
            totalExpenses += transaction.amount
        } else {
            totalBalance += transaction.amount
        }

        val sharedPreferences = getSharedPreferences("transaction_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("total_balance", totalBalance.toFloat())
        editor.putFloat("total_expense", totalExpenses.toFloat())
        editor.apply()

        updateUI()
        checkBudgetExceeded()
    }

    private val balanceUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.budgetflow.UPDATE_BALANCE") {
                updateBalanceFromPrefs()
            }
        }
    }

    private fun updateUI() {
        balanceTextView.text = "Total Balance: ₹${"%.2f".format(totalBalance)}"
        expenseTextView.text = "Total Expenses: ₹${"%.2f".format(totalExpenses)}"
    }

    fun addTransaction(transaction: Transaction) {
        updateBalanceAndExpense(transaction)
    }

    fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    POST_NOTIFICATIONS_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == POST_NOTIFICATIONS_REQUEST_CODE) {
            val msg = if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                "Notification permission granted"
            } else {
                "Notification permission denied"
            }
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkBudgetExceeded() {
        BudgetAlertManager.checkAndNotifyBudget(this, monthlyBudget, totalExpenses)
    }

    private fun updateBalanceFromPrefs() {
        val sharedPreferences = getSharedPreferences("transaction_prefs", MODE_PRIVATE)
        totalBalance = sharedPreferences.getFloat("total_balance", 0f).toDouble()
        totalExpenses = sharedPreferences.getFloat("total_expense", 0f).toDouble()
        updateUI()
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            balanceUpdateReceiver,
            IntentFilter("com.example.budgetflow.UPDATE_BALANCE")
        )
    }

    override fun onResume() {
        super.onResume()
        loadBalanceAndExpense()
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(balanceUpdateReceiver)
    }
}
