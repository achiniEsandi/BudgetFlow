package com.example.budgetflow

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var incomeTextView: TextView
    private lateinit var expenseTextView: TextView
    private lateinit var balanceTextView: TextView
    private lateinit var pieChart: PieChartView

    private val ADD_TRANSACTION_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val intent = Intent(this, AddTransaction::class.java)
        startActivityForResult(intent, ADD_TRANSACTION_REQUEST)


        // Initialize views
        incomeTextView = findViewById(R.id.totalIncome)
        expenseTextView = findViewById(R.id.totalExpenses)
        balanceTextView = findViewById(R.id.balance)
        pieChart = findViewById(R.id.pieChartCategory)

        updateDashboard()
        updatePieChart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_TRANSACTION_REQUEST && resultCode == RESULT_OK) {
            updateDashboard()
            updatePieChart()
        }
    }


    override fun onResume() {
        super.onResume()
        updateDashboard()
        updatePieChart()
    }

    private fun updateDashboard() {
        val totalIncome = TransactionManager.getTotalIncome(this)
        val totalExpense = TransactionManager.getTotalExpense(this)
        val totalBalance = totalIncome - totalExpense

        incomeTextView.text = "Total Income - Rs.%.2f".format(totalIncome)
        expenseTextView.text = "Total Expense - Rs.%.2f".format(totalExpense)
        balanceTextView.text = "Total Balanc - Rs.%.2f".format(totalBalance)

        Log.d("DashboardCheck", "Updated Dashboard with Income: $totalIncome, Expense: $totalExpense, Balance: $totalBalance")
    }

    private fun updatePieChart() {
        val transactions = TransactionManager.getTransactions(this)

        val pieSlices = mutableListOf<PieChartView.PieSlice>()
        val colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA)
        var colorIndex = 0

        val expenseByCategory = transactions
            .filter { it.type == "Expense" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        for ((category, amount) in expenseByCategory) {
            val color = colors[colorIndex % colors.size]
            pieSlices.add(
                PieChartView.PieSlice(
                    label = category,
                    value = amount.toFloat(),
                    color = color
                )
            )
            colorIndex++
        }
        pieChart.visibility = View.VISIBLE
        pieChart.setData(pieSlices)
    }
}
