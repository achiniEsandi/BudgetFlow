package com.example.budgetflow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var signupbtn: Button
    private lateinit var loginbtn: Button
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        signupbtn = findViewById(R.id.signupbtn)
        loginbtn = findViewById(R.id.loginbtn)
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        signupbtn.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Check for empty fields
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simple password validation (minimum length of 6)
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save the user data to SharedPreferences
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("username", username)
            editor.putString("email", email)
            editor.putString("password", password)
            editor.apply()

            Toast.makeText(this, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        loginbtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()  // Go back to LoginActivity
        }
    }
}
