package com.example.budgetflow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {

    // Declare the views
    private lateinit var loginbtn: Button
    private lateinit var signupbtn: Button
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layout
        enableEdgeToEdge()

        // Set the content view
        setContentView(R.layout.activity_login)

        // Apply window insets to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets // Return insets to allow further processing
        }

        // Initialize the views using findViewById
        loginbtn = findViewById(R.id.loginbtn)
        signupbtn = findViewById(R.id.signupbtn)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        val loginBtn = findViewById<Button>(R.id.loginbtn)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        loginBtn.setOnClickListener {
            val emailInput = emailEditText.text.toString()
            val passwordInput = passwordEditText.text.toString()

            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val savedEmail = sharedPref.getString("email", null)
            val savedPassword = sharedPref.getString("password", null)

            if (emailInput == savedEmail && passwordInput == savedPassword) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Home::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
            }

        }


        // Handle sign-up button click to navigate to the SignUpActivity
        signupbtn.setOnClickListener {
            // Navigate to the sign-up screen
            startActivity(Intent(this, SignupActivity::class.java))
            finish()  // Close this activity and go to the sign-up screen
        }
    }
}
