package com.example.budgetflow

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {

    private var totalBalance: Double = 0.0
    private var totalExpenses: Double = 0.0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(com.example.budgetflow.R.layout.activity_home)

        // Adjust for system bars (edge-to-edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(com.example.budgetflow.R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup BottomNavigationView for navigating between screens
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transactions -> {
                    val intent = Intent(this, ListTransactionsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Floating Action Button to navigate to AddTransaction
        val addButton: FloatingActionButton = findViewById(R.id.addBtn)
        addButton.visibility = View.VISIBLE
        addButton.isEnabled = true

        addButton.setOnClickListener {
            try {
                Log.d("AddTransaction", "Navigating to AddTransactionActivity")
                val intent = Intent(this, AddTransaction::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("AddTransaction", "Error launching AddTransactionActivity", e)
            }
        }

        // Setup tab buttons (Daily, Weekly, Monthly)
        val dailyBtn = findViewById<Button>(com.example.budgetflow.R.id.dailyBtn)
        val weeklyBtn = findViewById<Button>(com.example.budgetflow.R.id.weeklyBtn)
        val monthlyBtn = findViewById<Button>(com.example.budgetflow.R.id.monthlyBtn)

        setupTabSelection(dailyBtn, weeklyBtn, monthlyBtn)

        // Load saved totals (balance and expenses) when screen is created or resumed
        loadBalanceAndExpense()
    }

    private fun setupTabSelection(dailyBtn: Button, weeklyBtn: Button, monthlyBtn: Button) {
        dailyBtn.setOnClickListener {
            dailyBtn.isSelected = true
            weeklyBtn.isSelected = false
            monthlyBtn.isSelected = false
            // Load daily data
        }

        weeklyBtn.setOnClickListener {
            dailyBtn.isSelected = false
            weeklyBtn.isSelected = true
            monthlyBtn.isSelected = false
            // Load weekly data
        }

        monthlyBtn.setOnClickListener {
            dailyBtn.isSelected = false
            weeklyBtn.isSelected = false
            monthlyBtn.isSelected = true
            // Load monthly data
        }

        // Set weekly as default selected
        weeklyBtn.isSelected = true
    }

    private fun loadBalanceAndExpense() {
        // Match prefs name and keys with TransactionManager
        val sharedPreferences = getSharedPreferences("transaction_prefs", Context.MODE_PRIVATE)
        totalBalance = sharedPreferences.getFloat("total_balance", 0.0f).toDouble()
        totalExpenses = sharedPreferences.getFloat("total_expense", 0.0f).toDouble()

        updateUI()
    }

    private fun updateBalanceAndExpense(transaction: Transaction) {
        // Update the balance and expenses when a new transaction is added
        if (transaction.type == "expense") {
            totalExpenses += transaction.amount
        } else {
            totalBalance += transaction.amount
        }

        // Save the updated values to SharedPreferences
        val sharedPreferences = getSharedPreferences("transaction_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("total_balance", totalBalance.toFloat())
        editor.putFloat("total_expense", totalExpenses.toFloat()) // Ensure this key matches the one in loadBalanceAndExpense()
        editor.apply()

        // Update the UI with the latest values
        updateUI()
    }

    private fun updateUI() {
        val totalBalanceTextView: TextView = findViewById(R.id.totalBalanceTextView)
        val totalExpenseTextView: TextView = findViewById(R.id.totalExpenseTextView)

        // Update the UI components with the latest values
        totalBalanceTextView.text = "Total Balance: Rs.${"%.2f".format(totalBalance)}"
        totalExpenseTextView.text = "Total Expenses: Rs.${"%.2f".format(totalExpenses)}"
    }

    // This method can be called from AddTransactionActivity to update the balance and expenses
    fun addTransaction(transaction: Transaction) {
        updateBalanceAndExpense(transaction)
    }

    override fun onResume() {
        super.onResume()
        // Reload balance and expenses when the activity is resumed
        loadBalanceAndExpense()
    }
}
