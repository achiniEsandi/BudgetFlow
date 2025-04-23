package com.example.budgetflow

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var etOldPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var btnSavePassword: Button

    private val PREFS_NAME = "budget_prefs"
    private val PASSWORD_KEY = "password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        etOldPassword = findViewById(R.id.etOldPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        btnSavePassword = findViewById(R.id.btnSavePassword)

        btnSavePassword.setOnClickListener {
            val oldPassword = etOldPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()

            if (oldPassword.isBlank() || newPassword.isBlank()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentPassword = sharedPref.getString(PASSWORD_KEY, "")

            if (oldPassword == currentPassword) {
                sharedPref.edit().putString(PASSWORD_KEY, newPassword).apply()
                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Incorrect old password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
