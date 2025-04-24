// TransactionUtils.kt
package com.example.budgetflow

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.budgetflow.TransactionManager.getAllTransactions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object TransactionUtils {
    private const val PREFS_NAME       = "transaction_prefs"
    private const val KEY_JSON         = "transactions_json"
    private const val BACKUP_FILENAME  = "transactions_backup.json"

    /** Write current list into an on-device JSON file */
    fun backupToInternalStorage(context: Context) {
        val list = getAllTransactions(context)
        val json = Gson().toJson(list)
        context.openFileOutput(BACKUP_FILENAME, Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
        Log.d("TransactionUtils", "Backup saved to internal storage")
    }

    /** Read the JSON backup file and save it back into SharedPreferences */
    fun restoreFromBackup(context: Context) {
        val file = File(context.filesDir, BACKUP_FILENAME)
        if (!file.exists()) {
            Log.e("TransactionUtils", "Backup file not found")
            return
        }

        val json = file.readText()
        if (json.isBlank() || json == "[]") {
            Log.e("TransactionUtils", "Backup file is empty or invalid")
            return
        }

        try {
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            Gson().fromJson<List<Map<String, Any>>>(json, type) // Validate JSON
        } catch (e: Exception) {
            Log.e("TransactionUtils", "Invalid JSON in backup: ${e.message}")
            return
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_JSON, json)
            .apply()
        Log.d("TransactionUtils", "Backup restored from internal storage")
    }

    /** Export backup JSON file to external URI (e.g., user selected document or drive) */
    fun exportToUri(context: Context, uri: Uri): Boolean {
        return try {
            val file = File(context.filesDir, BACKUP_FILENAME)
            if (!file.exists()) {
                Log.w("TransactionUtils", "No backup file to export")
                return false
            }

            val json = file.readText()
            if (json.isBlank() || json == "[]") {
                Log.w("TransactionUtils", "Backup file is empty")
                return false
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
                outputStream.flush()
            }
            Log.d("TransactionUtils", "Backup exported to URI: $uri")
            true
        } catch (e: Exception) {
            Log.e("TransactionUtils", "Export failed: ${e.message}", e)
            false
        }
    }

    /** Export data as a plain text summary for sharing */
    fun exportAsText(context: Context): String {
        val transactions = getAllTransactions(context)
        if (transactions.isEmpty()) return "No transactions to export."

        val builder = StringBuilder()
        builder.append("Transaction Summary:\n\n")
        for (tx in transactions) {
            builder.append("ID: ${tx.id}\n")
            builder.append("Amount: ${tx.amount}\n")
            builder.append("Category: ${tx.category}\n")
            builder.append("Date: ${tx.date}\n")
            builder.append("Type: ${tx.type}\n")
            builder.append("Note: ${tx.notes}\n")
            builder.append("\n")
        }

        return builder.toString()
    }
}
