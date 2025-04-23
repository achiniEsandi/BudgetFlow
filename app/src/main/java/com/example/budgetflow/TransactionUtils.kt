package com.example.budgetflow

import android.content.Context
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object TransactionUtils {
    private const val FILE_NAME_JSON = "transactions_backup.json"

    // Backup transactions to internal storage in JSON format
    fun backupToInternalStorage(context: Context, transactions: List<Transaction>) {
        val jsonArray = JSONArray()
        transactions.forEach {
            val jsonObject = JSONObject()
            jsonObject.put("id", it.id)
            jsonObject.put("type", it.type)
            jsonObject.put("amount", it.amount)
            jsonObject.put("category", it.category)
            jsonObject.put("date", it.date)
            jsonObject.put("notes", it.notes)
            jsonArray.put(jsonObject)
        }

        val jsonFile = File(context.filesDir, FILE_NAME_JSON)
        jsonFile.writeText(jsonArray.toString())
    }

    // Restore transactions from JSON backup file
    fun restoreFromBackup(context: Context) {
        val jsonFile = File(context.filesDir, FILE_NAME_JSON)
        if (jsonFile.exists()) {
            val jsonData = jsonFile.readText()
            val jsonArray = JSONArray(jsonData)
            val restoredTransactions = mutableListOf<Transaction>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val transaction = Transaction(
                    jsonObject.getLong("id"),
                    jsonObject.getString("type"),
                    jsonObject.getDouble("amount"),
                    jsonObject.getString("category"),
                    jsonObject.getString("date"),
                    jsonObject.getString("notes")
                )
                restoredTransactions.add(transaction)
            }

            // TODO: Save to SharedPreferences if needed
            Toast.makeText(context, "Data restored successfully!", Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(context, "No backup found", Toast.LENGTH_SHORT).show()
        }
    }


    // Delete the backup file
    fun deleteBackup(context: Context) {
        val file = File(context.filesDir, FILE_NAME_JSON)
        if (file.exists()) {
            file.delete()
        }
    }
}
