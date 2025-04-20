package com.example.budgetflow

import android.content.Context
import java.io.File

object TransactionUtils {
    private const val FILE_NAME = "transactions_backup.txt"

    // Backup transactions to internal storage in a readable format
    fun backupToInternalStorage(context: Context, transactions: List<Transaction>) {
        val file = File(context.filesDir, FILE_NAME)
        file.bufferedWriter().use { writer ->
            transactions.forEach {
                // Ensure ID is valid (in case some old transactions still have -1)
                val validId = if (it.id <= 0) System.currentTimeMillis() else it.id
                writer.write("$validId,${it.type},${it.amount},${it.category},${it.date},${it.notes}")
                writer.newLine()
            }
        }
    }

    // Deletes the backup file from internal storage
    fun deleteBackup(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            file.delete()
        }
    }
}
