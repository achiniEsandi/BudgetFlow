package com.example.budgetflow

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TransactionManager {
    private const val PREFS_NAME = "transaction_prefs"
    private const val KEY_TRANSACTIONS = "transactions"

    private val gson = Gson()

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

    fun addTransaction(context: Context, transaction: Transaction) {
        val transactions = getAllTransactions(context)
        transactions.add(transaction)
        saveTransactions(context, transactions)
    }

    fun updateTransaction(context: Context, updatedTransaction: Transaction) {
        val transactions = getAllTransactions(context)
        val index = transactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            transactions[index] = updatedTransaction
            saveTransactions(context, transactions)
        }
    }

    fun deleteTransaction(context: Context, transactionId: Long) {
        val transactions = getAllTransactions(context)
        val updatedList = transactions.filter { it.id != transactionId }.toMutableList()
        saveTransactions(context, updatedList)
    }

    fun getTransactionById(context: Context, id: Long): Transaction? {
        return getAllTransactions(context).find { it.id == id }
    }

    private fun saveTransactions(context: Context, transactions: MutableList<Transaction>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = gson.toJson(transactions)
        editor.putString(KEY_TRANSACTIONS, json)
        editor.apply()
    }
}
