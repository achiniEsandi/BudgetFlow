package com.example.budgetflow

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnChangePassword: Button
    private lateinit var btnExportData: Button
    private lateinit var btnRestoreData: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImageView = findViewById(R.id.profileImageView)
        tvUsername = findViewById(R.id.tvUsername)
        tvEmail = findViewById(R.id.tvEmail)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnExportData = findViewById(R.id.btnExportData)
        btnRestoreData = findViewById(R.id.btnRestoreData)

        loadUserData()

        profileImageView.setImageResource(R.drawable.ic_profile)

        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        btnExportData.setOnClickListener {
            exportTransactionData()
        }

        btnRestoreData.setOnClickListener {
            restoreDataFromBackup()
        }
    }

    private fun loadUserData() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "No username available")
        val email = sharedPref.getString("email", "No email available")

        tvUsername.text = username
        tvEmail.text = email
    }

    private fun exportTransactionData() {
        val jsonData = getTransactionData()
        val textData = getTransactionSummary(jsonData)

        val textFile = File(filesDir, "transaction_summary.txt")

        try {
            FileOutputStream(textFile).use { it.write(textData.toByteArray()) }

            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                textFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Open or Share Text File"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getTransactionSummary(jsonData: String): String {
        return buildString {
            append("Transaction Summary\n\n")
            val lines = jsonData.split("},")
            for ((index, line) in lines.withIndex()) {
                val id = Regex("\"id\":\\s*(\\d+)").find(line)?.groupValues?.get(1) ?: "N/A"
                val amount = Regex("\"amount\":\\s*(\\d+(\\.\\d+)?)").find(line)?.groupValues?.get(1) ?: "N/A"
                val desc = Regex("\"description\":\\s*\"([^\"]+)\"").find(line)?.groupValues?.get(1) ?: "N/A"
                append("Transaction $id:\n")
                append(" - Amount: $amount\n")
                append(" - Description: $desc\n\n")
            }
        }
    }

    private fun getTransactionData(): String {
        val sharedPref = getSharedPreferences("Transactions", Context.MODE_PRIVATE)
        return sharedPref.getString("transaction_data", "[]") ?: "[]"
    }


    private fun restoreDataFromBackup() {
        val jsonFile = File(filesDir, "transaction_data.json")
        if (jsonFile.exists()) {
            val fileContent = jsonFile.readBytes()
            restoreTransactions(fileContent)
        } else {
            Toast.makeText(this, "No backup found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restoreTransactions(fileContent: ByteArray) {
        val transactions = String(fileContent)
        Toast.makeText(this, "Data restored: $transactions", Toast.LENGTH_SHORT).show()
    }
}
