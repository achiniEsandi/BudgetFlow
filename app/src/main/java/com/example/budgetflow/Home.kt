package com.example.budgetflow

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(com.example.budgetflow.R.layout.activity_home) // Avoid using android.R

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(com.example.budgetflow.R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transactions -> {
                    val intent = Intent(this, ListTransactionsActivity::class.java)
                    startActivity(intent)
                    true
                }
                // handle other nav items here
                else -> false
            }
        }

        val addButton: FloatingActionButton = findViewById(R.id.addBtn)
        // Ensure the button is visible and clickable
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




        val dailyBtn = findViewById<Button>(com.example.budgetflow.R.id.dailyBtn)
        val weeklyBtn = findViewById<Button>(com.example.budgetflow.R.id.weeklyBtn)
        val monthlyBtn = findViewById<Button>(com.example.budgetflow.R.id.monthlyBtn)

        setupTabSelection(dailyBtn, weeklyBtn, monthlyBtn)
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
}
