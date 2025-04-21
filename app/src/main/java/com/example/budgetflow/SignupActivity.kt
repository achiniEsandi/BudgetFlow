package com.example.budgetflow

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignupActivity : AppCompatActivity() {

    // Declare the views
    private lateinit var signupbtn: Button
    private lateinit var loginbtn: Button
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layout
        enableEdgeToEdge()

        // Set the content view
        setContentView(R.layout.activity_signup)

        // Apply window insets to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets // Return insets to allow further processing
        }

        // Initialize the views using findViewById
        signupbtn = findViewById(R.id.signupbtn)
        loginbtn = findViewById(R.id.loginbtn)
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        // Handle sign-up button click
        signupbtn.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Check for empty fields
            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                // Code for signing up the user (this could be an API call or saving to SharedPreferences)
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("email", email)
                editor.putString("password", password)
                editor.apply()

                Toast.makeText(this, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                // You can navigate to the login page after successful sign up
                // For example: startActivity(Intent(this, LoginActivity::class.java))
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle login button click to navigate to the LoginActivity
        loginbtn.setOnClickListener {
            // Navigate to the login screen
            // For example: startActivity(Intent(this, LoginActivity::class.java))
            finish()  // Close this activity and go back to the previous one
        }
    }
}
