package com.example.budgetflow

import android.content.Context
import java.io.File

object TransactionUtils {
    private const val FILE_NAME = "transactions_backup.txt"

    fun backupToInternalStorage(context: Context, transactions: List<Transaction>) {
        val file = File(context.filesDir, FILE_NAME)
        file.bufferedWriter().use { writer ->
            transactions.forEach {
                writer.write("${it.id},${it.type},${it.amount},${it.category},${it.date},${it.notes}")
                writer.newLine()
            }
        }
    }

    fun deleteBackup(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            file.delete()
        }
    }
}
