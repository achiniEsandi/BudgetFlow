// ProfileActivity.kt
package com.example.budgetflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.budgetflow.TransactionManager.getAllTransactions
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnChangePassword: Button
    private lateinit var btnExportData: Button
    private lateinit var btnExportJson: Button
    private lateinit var btnRestoreData: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImageView  = findViewById(R.id.profileImageView)
        tvUsername        = findViewById(R.id.tvUsername)
        tvEmail           = findViewById(R.id.tvEmail)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnExportData     = findViewById(R.id.btnExportData)
        btnExportJson     = findViewById(R.id.btnExportJson)
        btnRestoreData    = findViewById(R.id.btnRestoreData)

        loadUserData()
        profileImageView.setImageResource(R.drawable.ic_profile)

        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        // 1) Export as a human-readable text summary
        btnExportData.setOnClickListener {
            exportTransactionData()
        }

        // 2) Export raw JSON backup
        btnExportJson.setOnClickListener {
            TransactionUtils.backupToInternalStorage(this)
            Toast.makeText(this, "Transactions backed up (JSON)", Toast.LENGTH_SHORT).show()
        }

        // 3) Restore from JSON backup
        btnRestoreData.setOnClickListener {
            TransactionUtils.restoreFromBackup(this)
            Toast.makeText(this, "Transactions restored from backup", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        val sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        tvUsername.text = sp.getString("username", "—")
        tvEmail.text    = sp.getString("email",    "—")
    }

    private fun exportTransactionData() {
        val list = getAllTransactions(this)
        val textData = buildString {
            append("Transaction Summary\n\n")
            list.forEach { tx ->
                append("ID ${tx.id}:\n")
                append("  • Amount: ${tx.amount}\n")
                append("  • Desc:   ${tx.notes}\n\n")
            }
        }

        val textFile = File(filesDir, "transaction_backup.txt")
        textFile.writeText(textData)

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
        startActivity(Intent.createChooser(shareIntent, "Share Transaction Summary"))
    }
}
