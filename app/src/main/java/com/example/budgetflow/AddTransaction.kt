package com.example.budgetflow

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.*

class AddTransaction : AppCompatActivity() {

    private lateinit var transactionTypeGroup: RadioGroup
    private lateinit var amountInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var datePickerButton: Button
    private lateinit var notesInput: EditText
    private lateinit var submitButton: Button

    private var selectedDate: String = ""
    private var editingTransactionId: Long? = null

    private val transactionCategories = listOf(
        "Food", "Transport", "Entertainment", "Bills", "Shopping", "Income", "Others"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        transactionTypeGroup = findViewById(R.id.transactionTypeGroup)
        amountInput = findViewById(R.id.amountInput)
        categorySpinner = findViewById(R.id.categorySpinner)
        datePickerButton = findViewById(R.id.datePickerButton)
        notesInput = findViewById(R.id.notesInput)
        submitButton = findViewById(R.id.submitButton)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, transactionCategories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        datePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val date = "$dayOfMonth-${month + 1}-$year"
                    selectedDate = date
                    datePickerButton.text = date
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Load for edit
        editingTransactionId = intent.getLongExtra("transaction_id", -1)
        if (editingTransactionId != -1L) {
            val transaction = TransactionManager.getTransactionById(this, editingTransactionId!!)
            if (transaction != null) {
                val radioId = if (transaction.type == "Income") R.id.incomeRadio else R.id.expenseRadio
                transactionTypeGroup.check(radioId)
                amountInput.setText(transaction.amount.toString())
                categorySpinner.setSelection(transactionCategories.indexOf(transaction.category))
                datePickerButton.text = transaction.date
                selectedDate = transaction.date
                notesInput.setText(transaction.notes)
            }
        }

        submitButton.setOnClickListener {
            val type = if (transactionTypeGroup.checkedRadioButtonId == R.id.incomeRadio) "Income" else "Expense"
            val amount = amountInput.text.toString().toDoubleOrNull()
            val category = categorySpinner.selectedItem.toString()
            val notes = notesInput.text.toString()

            if (amount == null) {
                Toast.makeText(this, "Enter valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = Transaction(
                id = editingTransactionId ?: System.currentTimeMillis(),
                type = type,
                amount = amount,
                category = category,
                date = selectedDate,
                notes = notes
            )

            if (editingTransactionId != null && editingTransactionId != -1L) {
                TransactionManager.updateTransaction(this, transaction)
                Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
            } else {
                TransactionManager.addTransaction(this, transaction)
                Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
    }
}
