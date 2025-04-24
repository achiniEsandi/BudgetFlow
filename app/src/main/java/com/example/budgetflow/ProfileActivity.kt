// ProfileActivity.kt
package com.example.budgetflow

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.budgetflow.TransactionManager.getAllTransactions
import com.example.budgetflow.TransactionUtils
import java.io.File

class ProfileActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_RESTORE_JSON = 200
    }

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

        // 1) Export as human-readable text summary
        btnExportData.setOnClickListener {
            val textData = TransactionUtils.exportAsText(this)
            val textFile = File(filesDir, "transaction_summary.txt")
            textFile.writeText(textData)

            val uri = FileProvider.getUriForFile(
                this,
                "$packageName.provider",
                textFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Transaction Summary"))
        }

        // 2) Export raw JSON backup and share
        btnExportJson.setOnClickListener {
            TransactionUtils.backupToInternalStorage(this)
            val jsonFile = File(filesDir, "transactions_backup.json")
            if (jsonFile.exists()) {
                val uri: Uri = FileProvider.getUriForFile(
                    this,
                    "$packageName.provider",
                    jsonFile
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Transaction JSON"))
            } else {
                Toast.makeText(this, "No JSON backup found", Toast.LENGTH_SHORT).show()
            }
        }

        // 3) Restore from JSON via file chooser
        btnRestoreData.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            startActivityForResult(intent, REQUEST_CODE_RESTORE_JSON)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_RESTORE_JSON && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                // Persist permission and restore
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                try {
                    contentResolver.openInputStream(uri)?.use { input ->
                        val json = input.bufferedReader().readText()
                        // Save into SharedPreferences
                        getSharedPreferences("transaction_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .putString("transactions_json", json)
                            .apply()
                    }
                    Toast.makeText(this, "Transactions restored from selected file", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadUserData() {
        val sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        tvUsername.text = sp.getString("username", "—")
        tvEmail.text    = sp.getString("email",    "—")
    }
}
