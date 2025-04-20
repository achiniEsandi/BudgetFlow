package com.example.budgetflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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
        val transactions = TransactionManager.getAllTransactions(this)
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

                view.findViewById<ImageButton>(R.id.editButton).setOnClickListener {
                    val intent = Intent(this, AddTransaction::class.java)
                    intent.putExtra("transaction_id", transaction.id)
                    startActivity(intent)
                }

                view.findViewById<ImageButton>(R.id.deleteButton).setOnClickListener {
                    showDeleteConfirmation(transaction.id)
                }

                transactionListView.addView(view)
            }
        }

        updateTotal(transactions)
    }

    private fun showDeleteConfirmation(transactionId: Long) {
        Log.d("ListTransactionsActivity", "Attempting to delete transaction ID: $transactionId")
        AlertDialog.Builder(this)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                Log.d("ListTransactionsActivity", "Deleting transaction ID: $transactionId")
                TransactionManager.deleteTransaction(this, transactionId)
                Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
                loadTransactions()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun updateTotal(transactions: List<Transaction>) {
        val total = transactions.sumOf { if (it.type == "Expense") -it.amount else it.amount }
        totalAmountText.text = "Rs. %.2f".format(total)
    }
}
