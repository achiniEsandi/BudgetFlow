package com.example.budgetflow

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class BudgetActivity : AppCompatActivity() {

    private lateinit var etBudget: EditText
    private lateinit var btnSave: Button
    private lateinit var spinnerCurrency: Spinner
    private lateinit var tvBudgetStatus: TextView
    private lateinit var budgetProgressBar: ProgressBar

    private val CHANNEL_ID = "budget_alerts"
    private val NOTIFICATION_PERMISSION_CODE = 1001

    private val currencyOptions = arrayOf("Rs.", "$", "€", "£", "¥")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        etBudget = findViewById(R.id.etBudget)
        btnSave = findViewById(R.id.btnSaveBudget)
        spinnerCurrency = findViewById(R.id.spinnerCurrency)
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus)
        budgetProgressBar = findViewById(R.id.budgetProgressBar)

        setupCurrencySpinner()
        loadSavedCurrency()

        BudgetAlertManager.createNotificationChannel(this)
        requestNotificationPermission()

        btnSave.setOnClickListener {
            val budget = etBudget.text.toString().toDoubleOrNull()
            if (budget != null && budget > 0) {
                saveBudget(budget)
                updateBudgetUI()
            } else {
                Toast.makeText(this, "Enter a valid budget amount", Toast.LENGTH_SHORT).show()
            }
        }

        updateBudgetUI()
    }

    private fun setupCurrencySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = adapter
    }

    private fun loadSavedCurrency() {
        val sharedPref = getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
        val savedCurrency = sharedPref.getString("currency", currencyOptions[0])
        val currencyIndex = currencyOptions.indexOf(savedCurrency)
        spinnerCurrency.setSelection(currencyIndex)
    }

    private fun saveBudget(budget: Double) {
        val sharedPref = getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Save budget amount
        editor.putFloat("budget", budget.toFloat())

        // Save selected currency
        val selectedCurrency = spinnerCurrency.selectedItem.toString()
        editor.putString("currency", selectedCurrency)

        editor.apply()
        Toast.makeText(this, "Budget saved!", Toast.LENGTH_SHORT).show()
    }

    private fun getSavedBudget(): Double {
        val sharedPref = getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
        return sharedPref.getFloat("budget", 0f).toDouble()
    }

    private fun updateBudgetUI() {
        val budget = getSavedBudget()
        val totalExpenses = getTotalExpenses()

        BudgetAlertManager.checkAndNotifyBudget(
            this,
            budget,
            totalExpenses,
            tvBudgetStatus,
            budgetProgressBar
        )
    }

    private fun getTotalExpenses(): Double {
        // Replace with the actual method of calculating the total expenses.
        // For now, a dummy value is returned.
        return 500.0 // Dummy value for total expenses
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_CODE
            )
        }
    }
}
