package com.example.budgetflow

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
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

        // View initialization
        transactionTypeGroup = findViewById(R.id.transactionTypeGroup)
        amountInput = findViewById(R.id.amountInput)
        categorySpinner = findViewById(R.id.categorySpinner)
        datePickerButton = findViewById(R.id.datePickerButton)
        notesInput = findViewById(R.id.notesInput)
        submitButton = findViewById(R.id.submitButton)

        // Spinner setup
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, transactionCategories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Date picker dialog setup
        datePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, dayOfMonth)
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    selectedDate = format.format(cal.time)
                    datePickerButton.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Check if editing
        editingTransactionId = intent.getLongExtra("transaction_id", -1)
        if (editingTransactionId != -1L) {
            loadTransactionForEdit(editingTransactionId!!)
        }

        // Submit logic
        submitButton.setOnClickListener {
            handleSubmit()
        }
    }

    private fun loadTransactionForEdit(id: Long) {
        val transaction = TransactionManager.getTransactionById(this, id)
        if (transaction != null) {
            if (transaction.type == "Income") {
                transactionTypeGroup.check(R.id.incomeRadio)
            } else {
                transactionTypeGroup.check(R.id.expenseRadio)
            }
            amountInput.setText(transaction.amount.toString())
            categorySpinner.setSelection(transactionCategories.indexOf(transaction.category))
            selectedDate = transaction.date
            datePickerButton.text = transaction.date
            notesInput.setText(transaction.notes)
        }
    }

    private fun handleSubmit() {
        val selectedType = if (transactionTypeGroup.checkedRadioButtonId == R.id.incomeRadio) "Income" else "Expense"
        val amountText = amountInput.text.toString()
        val notes = notesInput.text.toString()
        val category = categorySpinner.selectedItem.toString()

        // Validations
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDate.isBlank()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        // Improved ID logic
        val transactionId = if (editingTransactionId != null && editingTransactionId != -1L)
            editingTransactionId!!
        else
            System.currentTimeMillis()

        val transaction = Transaction(
            id = transactionId,
            type = selectedType,
            amount = amount,
            category = category,
            date = selectedDate,
            notes = notes
        )

        // Save or update transaction
        if (editingTransactionId != null && editingTransactionId != -1L) {
            TransactionManager.updateTransaction(this, transaction)
            Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
        } else {
            TransactionManager.addTransaction(this, transaction)
            Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show()
        }

        // Update the total balance and expenses in SharedPreferences
        updateBalanceAndExpense(transaction)

        finish()
    }

    private fun updateBalanceAndExpense(transaction: Transaction) {
        // Get SharedPreferences and update balance and expenses
        val sharedPreferences = getSharedPreferences("transaction_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Retrieve current balance and expenses
        var totalBalance = sharedPreferences.getFloat("total_balance", 0f).toDouble()
        var totalExpenses = sharedPreferences.getFloat("total_expense", 0f).toDouble()

        // Update balance and expenses based on the transaction type
        if (transaction.type == "Income") {
            totalBalance += transaction.amount
        } else {
            totalExpenses += transaction.amount
        }

        // Save updated values
        editor.putFloat("total_balance", totalBalance.toFloat())
        editor.putFloat("total_expense", totalExpenses.toFloat())
        editor.apply()

        // Optionally, update the Home screen immediately by sending a broadcast or directly updating SharedPreferences
    }
}
