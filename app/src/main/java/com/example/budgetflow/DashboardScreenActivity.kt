package com.example.budgetflow

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetflow.Transaction
import com.example.budgetflow.TransactionManager
import java.text.NumberFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvTotalBalance: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalSavings: TextView
    private lateinit var recentTransactionsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_screen)

        // Initialize UI elements
        tvTotalBalance = findViewById(R.id.tvTotalBalance)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalSavings = findViewById(R.id.tvTotalSavings)
        recentTransactionsContainer = findViewById(R.id.recentTransactionsContainer)

        // Load dashboard data
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val transactions = TransactionManager.getAllTransactions(this)
        var totalIncome = 0.0
        var totalExpense = 0.0
        var totalSavings = 0.0

        for (transaction in transactions) {
            if (transaction.type.equals("income", ignoreCase = true)) {
                totalIncome += transaction.amount
            } else if (transaction.type.equals("expense", ignoreCase = true)) {
                totalExpense += transaction.amount
            }

            if (transaction.category.equals("savings", ignoreCase = true)) {
                totalSavings += transaction.amount
            }
        }

        val totalBalance = totalIncome - totalExpense

        // Update UI
        tvTotalBalance.text = formatCurrency(totalBalance)
        tvTotalIncome.text = formatCurrency(totalIncome)
        tvTotalSavings.text = formatCurrency(totalSavings)

        // Show recent 3 transactions
        val recentTransactions = transactions.takeLast(3).reversed()
        recentTransactionsContainer.removeAllViews()
        for (transaction in recentTransactions) {
            val itemView = TextView(this).apply {
                text = "${transaction.category} - ${formatCurrency(transaction.amount)}"
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.black, null))
                setPadding(8, 8, 8, 8)
            }
            recentTransactionsContainer.addView(itemView)
        }
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        return format.format(amount)
    }
}
