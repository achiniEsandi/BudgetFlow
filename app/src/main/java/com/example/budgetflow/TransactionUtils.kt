// TransactionUtils.kt
package com.example.budgetflow

import android.content.Context
import com.example.budgetflow.TransactionManager.getAllTransactions
import com.google.gson.Gson
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
    }

    /** Read the JSON backup file and save it back into SharedPreferences */
    fun restoreFromBackup(context: Context) {
        val file = File(context.filesDir, BACKUP_FILENAME)
        if (!file.exists()) return

        val json = file.readText()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_JSON, json)
            .apply()
    }
}
