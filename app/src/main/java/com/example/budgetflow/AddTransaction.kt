package com.example.budgetflow

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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

    // ✅ Separate categories
    private val incomeCategories = listOf("Salary", "Bonus", "Freelance", "Other")
    private val expenseCategories = listOf("Food", "Transport", "Bills", "Shopping", "Other")

    companion object {
        const val PREFS_NAME = "transaction_prefs"
        const val KEY_TOTAL_BALANCE = "total_balance"
        const val KEY_TOTAL_EXPENSE = "total_expense"
    }

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

        // Set default as Expense
        setCategorySpinner("Expense")

        transactionTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            val type = if (checkedId == R.id.incomeRadio) "Income" else "Expense"
            setCategorySpinner(type)
        }

        // Get the current date and one month ago
        val today = Calendar.getInstance()
        val oneMonthAgo = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1) // Set the date to one month ago
        }

        // DatePickerDialog initialization
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                datePickerButton.text = selectedDate
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )

        // Set constraints: Allow dates up to today and down to one month ago
        datePickerDialog.datePicker.maxDate = today.timeInMillis // No future dates
        datePickerDialog.datePicker.minDate = oneMonthAgo.timeInMillis // Allow dates only from the last month

        datePickerButton.setOnClickListener {
            datePickerDialog.show()
        }


        // Edit check
        val transactionId = intent.getLongExtra("transaction_id", -1L)
        if (transactionId != -1L) {
            editingTransactionId = transactionId
            loadTransactionForEdit(transactionId)
        }

        submitButton.setOnClickListener {
            handleSubmit()
            Log.d("AddTransaction", "Editing transaction: $editingTransactionId")
        }
    }

    private fun setCategorySpinner(type: String) {
        val categories = if (type == "Income") incomeCategories else expenseCategories
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun loadTransactionForEdit(id: Long) {
        val transaction = TransactionManager.getTransactionById(this, id)
        if (transaction != null) {
            val type = transaction.type
            if (type == "Income") {
                transactionTypeGroup.check(R.id.incomeRadio)
            } else {
                transactionTypeGroup.check(R.id.expenseRadio)
            }

            amountInput.setText(String.format(Locale.getDefault(), "%.2f", transaction.amount))

            // Update category list before setting spinner
            setCategorySpinner(type)
            val currentCategories = if (type == "Income") incomeCategories else expenseCategories
            val index = currentCategories.indexOf(transaction.category)
            if (index != -1) categorySpinner.setSelection(index)

            selectedDate = transaction.date
            datePickerButton.text = transaction.date
            notesInput.setText(transaction.notes)
        } else {
            Log.e("AddTransaction", "Transaction not found for ID: $id")
        }
    }

    private fun handleSubmit() {
        val selectedId = transactionTypeGroup.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a transaction type", Toast.LENGTH_SHORT).show()
            return
        }
        val type = if (selectedId == R.id.incomeRadio) "Income" else "Expense"

        val amountText = amountInput.text.toString()
        val rawNotes = notesInput.text.toString()
        val notes = if (rawNotes.isBlank()) "No notes" else rawNotes
        val category = categorySpinner.selectedItem.toString()

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDate.isBlank()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        val id = editingTransactionId ?: System.currentTimeMillis()

        val transaction = Transaction(
            id = id,
            type = type,
            amount = amount,
            category = category,
            date = selectedDate,
            notes = notes
        )

        val existing = TransactionManager.getTransactionById(this, id)
        if (existing != null) {
            reversePreviousTransaction(existing)
            TransactionManager.updateTransaction(this, transaction)
            Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
        } else {
            TransactionManager.addTransaction(this, transaction)
            Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show()
        }

        updateBalanceAndExpense(transaction)

        val intent = Intent("com.example.budgetflow.UPDATE_BALANCE")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)

        finish()
    }

    private fun reversePreviousTransaction(transaction: Transaction) {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        var totalBalance = sharedPreferences.getFloat(KEY_TOTAL_BALANCE, 0f).toDouble()
        var totalExpenses = sharedPreferences.getFloat(KEY_TOTAL_EXPENSE, 0f).toDouble()

        if (transaction.type == "Income") {
            totalBalance -= transaction.amount
        } else {
            totalExpenses -= transaction.amount
        }

        editor.putFloat(KEY_TOTAL_BALANCE, totalBalance.toFloat())
        editor.putFloat(KEY_TOTAL_EXPENSE, totalExpenses.toFloat())
        editor.apply()
    }

    private fun updateBalanceAndExpense(transaction: Transaction) {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        var totalBalance = sharedPreferences.getFloat(KEY_TOTAL_BALANCE, 0f).toDouble()
        var totalExpenses = sharedPreferences.getFloat(KEY_TOTAL_EXPENSE, 0f).toDouble()

        if (transaction.type == "Income") {
            totalBalance += transaction.amount
        } else {
            totalExpenses += transaction.amount
        }

        editor.putFloat(KEY_TOTAL_BALANCE, totalBalance.toFloat())
        editor.putFloat(KEY_TOTAL_EXPENSE, totalExpenses.toFloat())
        editor.apply()
    }
}
