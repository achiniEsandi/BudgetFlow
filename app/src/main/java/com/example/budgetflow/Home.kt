package com.example.budgetflow

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Home : AppCompatActivity() {

    private var totalBalance: Double = 0.0
    private var totalExpenses: Double = 0.0
    private val POST_NOTIFICATIONS_REQUEST_CODE = 101
    private val CHANNEL_ID = "budget_alerts"

    private lateinit var balanceTextView: TextView
    private lateinit var expenseTextView: TextView

    private lateinit var categoryTabLayout: TabLayout
    private lateinit var categoryRecyclerView: RecyclerView

    // Broadcast receiver to refresh data when transactions are updated
    private val balanceUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadBalanceAndExpense()
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Initialize category views
        categoryTabLayout = findViewById(R.id.categoryTabLayout)
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView)

        setupCategoryTabs()
        loadCategoryData()

        // Initialize Views
        balanceTextView = findViewById(R.id.totalBalanceTextView)
        expenseTextView = findViewById(R.id.totalExpenseTextView)

        // Set up system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        // Set up FAB
        val addButton: FloatingActionButton = findViewById(R.id.addBtn)
        addButton.setOnClickListener {
            try {
                startActivity(Intent(this, AddTransaction::class.java))
            } catch (e: Exception) {
                Log.e("AddTransaction", "Error launching AddTransactionActivity", e)
            }
        }

        // Setup bottom navigation
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

                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }


                else -> false
            }
        }

        checkAndRequestNotificationPermission()
        BudgetAlertManager.createNotificationChannel(this)

        loadBalanceAndExpense()
    }


    private fun setupCategoryTabs() {
        categoryTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                loadCategoryData(tab?.position == 1) // 0=Expenses, 1=Income
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }


    private fun loadCategoryData(showIncome: Boolean = false) {
        lifecycleScope.launch {
            val transactions = withContext(Dispatchers.IO) {
                TransactionManager.getAllTransactions(this@Home)
            }

            val filtered = transactions.filter {
                if (showIncome) it.type == "Income" else it.type == "Expense"
            }

            val categories = filtered.groupBy { it.category }
                .map { (category, transactions) ->
                    val total = transactions.sumOf { it.amount }
                    val allTotal = filtered.sumOf { it.amount }
                    val percentage = if (allTotal > 0) (total / allTotal * 100).toInt() else 0

                    CategorySummary(
                        name = category,
                        amount = total,
                        percentage = percentage
                    )
                }
                .sortedByDescending { it.amount }

            categoryRecyclerView.adapter = CategoryAdapter(categories, showIncome)
        }
    }

    private fun setupTabSelection(dailyBtn: Button, weeklyBtn: Button, monthlyBtn: Button) {
        dailyBtn.setOnClickListener {
            setFilterSelection(dailyBtn, weeklyBtn, monthlyBtn)
        }
        weeklyBtn.setOnClickListener {
            setFilterSelection(weeklyBtn, dailyBtn, monthlyBtn)
        }
        monthlyBtn.setOnClickListener {
            setFilterSelection(monthlyBtn, dailyBtn, weeklyBtn)
        }

        // Default selected: weekly
        setFilterSelection(weeklyBtn, dailyBtn, monthlyBtn)
    }

    private fun setFilterSelection(selected: Button, vararg others: Button) {
        selected.isSelected = true
        others.forEach { it.isSelected = false }
        // You can add logic here to update UI or filter transactions accordingly
    }

    private fun loadBalanceAndExpense() {
        lifecycleScope.launch {
            val transactions = withContext(Dispatchers.IO) {
                TransactionManager.getAllTransactions(this@Home)
            }

            val totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
            val totalExpenses = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
            val balance = totalIncome - totalExpenses

            balanceTextView.text = "Balance: Rs. %.2f".format(balance)
            expenseTextView.text = "Expenses: Rs. %.2f".format(totalExpenses)

            // Optionally update shared preferences if needed elsewhere
            val sharedPreferences = getSharedPreferences("transaction_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit()
                .putFloat("total_balance", balance.toFloat())
                .putFloat("total_expense", totalExpenses.toFloat())
                .apply()
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    POST_NOTIFICATIONS_REQUEST_CODE
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(balanceUpdateReceiver, IntentFilter("com.example.budgetflow.UPDATE_BALANCE"))
        loadBalanceAndExpense()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(balanceUpdateReceiver)
    }
}
