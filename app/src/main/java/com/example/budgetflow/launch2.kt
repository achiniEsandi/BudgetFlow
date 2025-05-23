package com.example.budgetflow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class launch2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_launch2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val next: Button = findViewById(R.id.loginbtn)
        next.setOnClickListener {
            // Create an intent to go to the login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val next1: Button = findViewById(R.id.signupbtn)
        next1.setOnClickListener {
            // Create an intent to go to the signup screen
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }


    }

}