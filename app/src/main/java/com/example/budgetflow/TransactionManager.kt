package com.example.budgetflow

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TransactionManager {
    private const val PREFS_NAME = "transaction_prefs"
    private const val KEY_TRANSACTIONS = "transactions"

    private val gson = Gson()

    // This function fetches all transactions from SharedPreferences and returns a list of transactions.
    fun getAllTransactions(context: Context): MutableList<Transaction> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TRANSACTIONS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    // Adds a new transaction to the list and saves it.
    fun addTransaction(context: Context, transaction: Transaction) {
        val transactions = getAllTransactions(context)
        transactions.add(transaction)
        saveTransactions(context, transactions)
    }

    // Updates an existing transaction in the list and saves the updated list.
    fun updateTransaction(context: Context, updatedTransaction: Transaction) {
        val transactions = getAllTransactions(context)
        val index = transactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            transactions[index] = updatedTransaction
            saveTransactions(context, transactions)
        }
    }

    // Deletes a transaction by its ID.
    fun deleteTransaction(context: Context, transactionId: Long) {
        val transactions = getAllTransactions(context) // Fetch all transactions

        // Log before deletion
        Log.d("TransactionManager", "Transactions before deletion: $transactions")

        // Remove the transaction with the matching ID
        val updatedTransactions = transactions.filter { it.id != transactionId }

        // Log after deletion
        Log.d("TransactionManager", "Transactions after deletion: $updatedTransactions")

        // Save the updated list of transactions with the correct key
        saveTransactions(context, updatedTransactions)
    }

    // Function to fetch a transaction by its ID.
    fun getTransactionById(context: Context, id: Long): Transaction? {
        return getAllTransactions(context).find { it.id == id }
    }

    // Saves the updated list of transactions to SharedPreferences.
    private fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Convert the updated list of transactions to JSON
        val json = gson.toJson(transactions)

        // Log the saved JSON to ensure the correct data is being saved
        Log.d("TransactionManager", "Saving transactions: $json")

        // Use the consistent key for saving the transactions
        editor.putString(KEY_TRANSACTIONS, json) // Ensure using the same key as the rest of the code
        editor.apply()
    }
}
