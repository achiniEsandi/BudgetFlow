package com.example.budgetflow

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.reflect.Type

object TransactionManager {

    private const val PREFS_NAME = "TransactionPrefs"
    private const val TRANSACTION_LIST_KEY = "transaction_list"
    private const val FILE_NAME = "transactions_data"

    // Get the transaction list from SharedPreferences or create an empty list
    private fun getTransactions(context: Context): MutableList<Transaction> {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(TRANSACTION_LIST_KEY, null)
        val type: Type = object : TypeToken<MutableList<Transaction>>() {}.type
        return if (json.isNullOrEmpty()) {
            mutableListOf()
        } else {
            Gson().fromJson(json, type)
        }
    }

    // Save the list of transactions to SharedPreferences
    private fun saveTransactions(context: Context, transactions: MutableList<Transaction>) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(transactions)
        sharedPreferences.edit { putString(TRANSACTION_LIST_KEY, json) }
    }

    // Add a new transaction
    fun addTransaction(context: Context, transaction: Transaction) {
        val transactions = getTransactions(context)
        transactions.add(transaction)
        saveTransactions(context, transactions)
        Log.d("TransactionManager", "Transaction added: $transaction")
    }

    // Update an existing transaction by ID
    fun updateTransaction(context: Context, updatedTransaction: Transaction) {
        val transactions = getTransactions(context)
        val index = transactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            transactions[index] = updatedTransaction
            saveTransactions(context, transactions)
            Log.d("TransactionManager", "Transaction updated: $updatedTransaction")
        } else {
            Log.d("TransactionManager", "Transaction not found for update")
        }
    }

    // Delete a transaction by ID
    fun deleteTransaction(context: Context, transactionId: Long) {
        val transactions = getTransactions(context)
        val index = transactions.indexOfFirst { it.id == transactionId }
        if (index != -1) {
            transactions.removeAt(index)
            saveTransactions(context, transactions)
            Log.d("TransactionManager", "Transaction deleted with ID: $transactionId")
        } else {
            Log.d("TransactionManager", "Transaction not found for deletion")
        }
    }

    // Get a transaction by ID
    fun getTransactionById(context: Context, transactionId: Long): Transaction? {
        val transactions = getTransactions(context)
        return transactions.find { it.id == transactionId }
    }

    // Optionally, you could save transactions to a file for persistence beyond SharedPreferences
    private fun saveToFile(context: Context, transactions: MutableList<Transaction>) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            FileOutputStream(file).use { fos ->
                ObjectOutputStream(fos).use { oos ->
                    oos.writeObject(transactions)
                }
            }
        } catch (e: Exception) {
            Log.e("TransactionManager", "Error saving transactions to file", e)
        }
    }

    // Optionally, load transactions from a file
    private fun loadFromFile(context: Context): MutableList<Transaction> {
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) return mutableListOf()

            FileInputStream(file).use { fis ->
                ObjectInputStream(fis).use { ois ->
                    return ois.readObject() as? MutableList<Transaction> ?: mutableListOf()
                }
            }
        } catch (e: Exception) {
            Log.e("TransactionManager", "Error loading transactions from file", e)
            return mutableListOf()
        }
    }
}
