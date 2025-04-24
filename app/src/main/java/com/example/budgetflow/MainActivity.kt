package com.example.budgetflow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val next: ImageView = findViewById(R.id.imageView2)
        next.setOnClickListener {
            // Create an intent to go to the launch screen
            val intent = Intent(this, launch2::class.java)
            startActivity(intent)
        }

        // Navigate to Onboard1 after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, launch2::class.java))
            finish()
        }, 2000) }

}