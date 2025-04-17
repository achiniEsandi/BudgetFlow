package com.example.budgetflow


import android.content.Context
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

object StorageHelper {
    private const val PREF_NAME = "budget_prefs"
    private const val TRANSACTION_KEY = "transactions"

    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(transactions)
        prefs.edit().putString(TRANSACTION_KEY, json).apply()
    }

    fun loadTransactions(context: Context): MutableList<Transaction> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(TRANSACTION_KEY, null)
        return if (json != null) {
            val type = object : com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken<MutableList<Transaction>>() {}.type
            Gson().fromJson(json, type)
        } else mutableListOf()
    }

    fun addTransaction(context: Context, transaction: Transaction) {
        val list = loadTransactions(context)
        list.add(transaction)
        saveTransactions(context, list)
    }

    fun updateTransaction(context: Context, updatedTransaction: Transaction) {
        val list = loadTransactions(context)
        val index = list.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            list[index] = updatedTransaction
            saveTransactions(context, list)
        }
    }

    fun deleteTransaction(context: Context, transactionId: Long) {
        val list = loadTransactions(context)
        val updatedList = list.filter { it.id != transactionId }
        saveTransactions(context, updatedList)
    }
}