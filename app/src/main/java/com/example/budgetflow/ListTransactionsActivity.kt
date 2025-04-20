package com.example.budgetflow

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListTransactionsActivity : AppCompatActivity() {

    private lateinit var transactionListView: LinearLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var totalAmountText: TextView
    private lateinit var addButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_transactions)

        val rootLayout: View = findViewById(R.id.mainLayout) ?: findViewById(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        transactionListView = findViewById(R.id.transactionListView)
        emptyState = findViewById(R.id.emptyState)
        totalAmountText = findViewById(R.id.totalAmountText)
        addButton = findViewById(R.id.addButton)

        addButton.setOnClickListener {
            startActivity(Intent(this, AddTransaction::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    private fun loadTransactions() {
        lifecycleScope.launch {
            val transactions = withContext(Dispatchers.IO) {
                TransactionManager.getAllTransactions(this@ListTransactionsActivity)
            }
            updateTransactionList(transactions)
        }
    }

    private fun updateTransactionList(transactions: List<Transaction>) {
        transactionListView.removeAllViews()

        if (transactions.isEmpty()) {
            emptyState.visibility = View.VISIBLE
        } else {
            emptyState.visibility = View.GONE
            for (transaction in transactions) {
                val view = LayoutInflater.from(this)
                    .inflate(R.layout.item_transaction, transactionListView, false)

                view.findViewById<TextView>(R.id.transactionType).text = transaction.type
                view.findViewById<TextView>(R.id.transactionAmount).text = "Rs. %.2f".format(transaction.amount)
                view.findViewById<TextView>(R.id.transactionCategory).text = transaction.category
                view.findViewById<TextView>(R.id.transactionDate).text = transaction.date
                view.findViewById<TextView>(R.id.transactionNotes).text = transaction.notes

                // Edit button logic
                view.findViewById<ImageButton>(R.id.editButton).setOnClickListener {
                    val intent = Intent(this, AddTransaction::class.java)
                    intent.putExtra("transaction_id", transaction.id)
                    startActivity(intent)
                }

                // Delete button logic
                view.findViewById<ImageButton>(R.id.deleteButton).setOnClickListener {
                    showDeleteConfirmation(transaction.id)
                }

                transactionListView.addView(view)
            }
        }

        updateTotal(transactions)
    }

    private fun showDeleteConfirmation(transactionId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTransaction(transactionId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTransaction(transactionId: Long) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                // Deleting transaction from database
                TransactionManager.deleteTransaction(this@ListTransactionsActivity, transactionId)
            }

            // Refresh transaction list after deletion
            Toast.makeText(this@ListTransactionsActivity, "Transaction deleted", Toast.LENGTH_SHORT).show()
            loadTransactions()
        }
    }

    private fun updateTotal(transactions: List<Transaction>) {
        // Calculate total income and total expenses
        val totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == "Expense" }.sumOf { it.amount }

        // Calculate the balance (income - expenses)
        val balance = totalIncome - totalExpenses

        // Set the total amount text as the balance
        totalAmountText.text = "Rs. %.2f".format(balance)

        // Optionally, update a label to show if it's positive, negative, or zero
        if (balance > 0) {
            totalAmountText.setTextColor(resources.getColor(R.color.green))  // For positive balance
        } else if (balance < 0) {
            totalAmountText.setTextColor(resources.getColor(R.color.red))    // For negative balance (expenses)
        } else {
            totalAmountText.setTextColor(resources.getColor(R.color.black))  // For zero balance
        }
    }

}
