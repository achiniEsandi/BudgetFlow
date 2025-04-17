import android.content.Context
import android.content.SharedPreferences
import com.example.budgetflow.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TransactionUtils {

    private const val PREFS_NAME = "transactions_prefs"
    private const val KEY_TRANSACTIONS = "transactions_key"

    // Add a new transaction
    fun addTransaction(context: Context, transaction: Transaction) {
        val sharedPrefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val transactionsJson = sharedPrefs.getString(KEY_TRANSACTIONS, null)
        val transactionsList: MutableList<Transaction> = if (transactionsJson != null) {
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            Gson().fromJson(transactionsJson, type)
        } else {
            mutableListOf()
        }
        transactionsList.add(transaction)
        saveTransactions(context, transactionsList)
    }

    // Update an existing transaction
    fun updateTransaction(context: Context, transactionId: Long, updatedTransaction: Transaction) {
        val sharedPrefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val transactionsJson = sharedPrefs.getString(KEY_TRANSACTIONS, null)
        val transactionsList: MutableList<Transaction> = if (transactionsJson != null) {
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            Gson().fromJson(transactionsJson, type)
        } else {
            mutableListOf()
        }

        val index = transactionsList.indexOfFirst { it.id == transactionId }
        if (index != -1) {
            transactionsList[index] = updatedTransaction
            saveTransactions(context, transactionsList)
        }
    }

    // Delete a transaction
    fun deleteTransaction(context: Context, transactionId: Long) {
        val sharedPrefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val transactionsJson = sharedPrefs.getString(KEY_TRANSACTIONS, null)
        val transactionsList: MutableList<Transaction> = if (transactionsJson != null) {
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            Gson().fromJson(transactionsJson, type)
        } else {
            mutableListOf()
        }

        val index = transactionsList.indexOfFirst { it.id == transactionId }
        if (index != -1) {
            transactionsList.removeAt(index)
            saveTransactions(context, transactionsList)
        }
    }

    // Helper function to save transactions to SharedPreferences
    private fun saveTransactions(context: Context, transactionsList: MutableList<Transaction>) {
        val sharedPrefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val transactionsJson = Gson().toJson(transactionsList)
        editor.putString(KEY_TRANSACTIONS, transactionsJson)
        editor.apply()
    }

    // Retrieve all transactions
    fun getTransactions(context: Context): List<Transaction> {
        val sharedPrefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val transactionsJson = sharedPrefs.getString(KEY_TRANSACTIONS, null)
        return if (transactionsJson != null) {
            val type = object : TypeToken<List<Transaction>>() {}.type
            Gson().fromJson(transactionsJson, type)
        } else {
            emptyList()
        }
    }
}
