package com.example.budgetflow

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.budgetflow.AddTransaction.Companion.KEY_TOTAL_EXPENSE
import com.example.budgetflow.AddTransaction.Companion.PREFS_NAME as TX_PREFS

class BudgetActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "budget_prefs"
        private const val KEY_BUDGET = "budget"
        private const val KEY_CURRENCY = "currency"
    }

    private lateinit var etBudget: EditText
    private lateinit var spinnerCurrency: Spinner
    private lateinit var btnSaveBudget: Button
    private lateinit var tvBudgetStatus: TextView
    private lateinit var budgetProgressBar: ProgressBar

    // Your list of options
    private val currencyOptions = arrayOf("Rs.", "$", "€", "£", "¥")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        // Bind views
        etBudget          = findViewById(R.id.etBudget)
        spinnerCurrency   = findViewById(R.id.spinnerCurrency)
        btnSaveBudget     = findViewById(R.id.btnSaveBudget)
        tvBudgetStatus    = findViewById(R.id.tvBudgetStatus)
        budgetProgressBar = findViewById(R.id.budgetProgressBar)

        // 1) Spinner setup + load last‐saved currency
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = adapter

        loadPrefsIntoUI()

        // 2) Whenever the user picks a new currency, save it & redraw
        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                saveCurrency(currencyOptions[pos])
                updateBudgetUI()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 3) On “Save Budget” click, store budget & refresh
        btnSaveBudget.setOnClickListener {
            val b = etBudget.text.toString().toDoubleOrNull()
            if (b != null && b >= 0) {
                saveBudget(b)
                updateBudgetUI()
                Toast.makeText(this, "Budget saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Enter a valid budget", Toast.LENGTH_SHORT).show()
            }
        }

        // 4) (Optional) set up notifications
        BudgetAlertManager.createNotificationChannel(this)
        requestNotificationPermission()

        // Initial draw
        updateBudgetUI()
    }

    private fun loadPrefsIntoUI() {
        val sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Load budget into EditText
        val savedBudget = sp.getFloat(KEY_BUDGET, 0f)
        etBudget.setText(if (savedBudget > 0f) savedBudget.toString() else "")

        // Load currency into Spinner
        val savedCurr = sp.getString(KEY_CURRENCY, currencyOptions[0])!!
        spinnerCurrency.setSelection(currencyOptions.indexOf(savedCurr).coerceAtLeast(0))
    }

    private fun saveCurrency(cur: String) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CURRENCY, cur)
            .apply()
    }

    private fun saveBudget(amount: Double) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_BUDGET, amount.toFloat())
            .apply()
    }

    private fun getSavedBudget(): Double =
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_BUDGET, 0f).toDouble()

    private fun getSavedCurrency(): String =
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CURRENCY, currencyOptions[0])!!

    private fun getTotalExpenses(): Double =
        getSharedPreferences(TX_PREFS, MODE_PRIVATE)
            .getFloat(KEY_TOTAL_EXPENSE, 0f).toDouble()

    private fun updateBudgetUI() {
        val budget   = getSavedBudget()
        val spent    = getTotalExpenses()
        val remain   = budget - spent
        val currSym  = getSavedCurrency()

        // Format with two decimals
        fun D(d: Double) = String.format("%.2f", d)

        tvBudgetStatus.text =
            "Remaining: $currSym${D(remain)} / $currSym${D(budget)}"

        // update progress bar (0–100% of spent/budget)
        val pct = if (budget > 0) ((spent / budget) * 100).toInt().coerceIn(0,100) else 0
        budgetProgressBar.progress = pct

        // And push any budget‐alerts you’ve set up...
        BudgetAlertManager.checkAndNotifyBudget(
            this, budget, spent, tvBudgetStatus, budgetProgressBar
        )
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }
}
