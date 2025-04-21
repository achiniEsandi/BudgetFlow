package com.example.budgetflow

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TransactionManager {
    private const val PREFS_NAME = "transaction_prefs"
    private const val KEY_TRANSACTIONS = "transactions"
    private const val KEY_TOTAL_BALANCE = "total_balance"
    private const val KEY_TOTAL_EXPENSE = "total_expense"

    private val gson = Gson()

    private fun getPreferences(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun getTransactionsJson(context: Context): String? {
        return getPreferences(context).getString(KEY_TRANSACTIONS, null)
    }

    private fun saveTransactionsJson(context: Context, transactionsJson: String) {
        getPreferences(context).edit().putString(KEY_TRANSACTIONS, transactionsJson).apply()
        Log.d("TransactionManager", "Saved transactions to SharedPreferences.")
    }

    fun getAllTransactions(context: Context): MutableList<Transaction> {
        val json = getTransactionsJson(context)
        return if (!json.isNullOrEmpty()) {
            try {
                // Explicitly provide the type token to resolve the type inference issue
                val type = object : TypeToken<MutableList<Transaction>>() {}.type
                gson.fromJson<MutableList<Transaction>>(json, type).also {
                    Log.d("TransactionManager", "Loaded ${it.size} transactions.")
                }
            } catch (e: Exception) {
                Log.e("TransactionManager", "Failed to parse transactions JSON: $e")
                mutableListOf()
            }
        } else {
            Log.d("TransactionManager", "No transactions found.")
            mutableListOf()
        }
    }

    fun addTransaction(context: Context, transaction: Transaction) {
        val transactions = getAllTransactions(context)
        transactions.add(transaction)
        saveTransactions(context, transactions)
        Log.d("TransactionManager", "Added transaction: $transaction")
        updateTotals(context)
    }

    fun updateTransaction(context: Context, updatedTransaction: Transaction) {
        val transactions = getAllTransactions(context)
        val index = transactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            transactions[index] = updatedTransaction
            saveTransactions(context, transactions)
            Log.d("TransactionManager", "Updated transaction: $updatedTransaction")
            updateTotals(context)
        } else {
            Log.w("TransactionManager", "Transaction with ID ${updatedTransaction.id} not found.")
        }
    }

    fun deleteTransaction(context: Context, transactionId: Long) {
        val transactions = getAllTransactions(context)
        val updatedTransactions = transactions.filter { it.id != transactionId }
        saveTransactions(context, updatedTransactions)
        Log.d("TransactionManager", "Deleted transaction with ID: $transactionId")
        updateTotals(context)
    }

    fun getTransactionById(context: Context, id: Long): Transaction? {
        return getAllTransactions(context).find { it.id == id }?.also {
            Log.d("TransactionManager", "Fetched transaction by ID $id: $it")
        }
    }

    private fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val transactionsJson = gson.toJson(transactions)
        saveTransactionsJson(context, transactionsJson)
    }

    private fun updateTotals(context: Context) {
        val transactions = getAllTransactions(context)

        // Ensure that the transaction type matches the exact case stored in your model
        val totalIncome = transactions.filter { it.type.equals("income", ignoreCase = true) }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type.equals("expense", ignoreCase = true) }.sumOf { it.amount }
        val totalBalance = totalIncome - totalExpense

        // Log the totals before saving to SharedPreferences
        Log.d("TransactionManager", "Before saving - Total Balance: $totalBalance, Total Expense: $totalExpense")

        // Save to SharedPreferences
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_TOTAL_BALANCE, totalBalance.toFloat())
            .putFloat(KEY_TOTAL_EXPENSE, totalExpense.toFloat())
            .apply()

        Log.d("TransactionManager", "Updated Totals -> Income: $totalIncome, Expense: $totalExpense, Balance: $totalBalance")
    }

    fun getTotalBalance(context: Context): Float {
        val balance = getPreferences(context).getFloat(KEY_TOTAL_BALANCE, 0f)
        Log.d("TransactionManager", "Fetched Total Balance: $balance")
        return balance
    }

    fun getTotalExpense(context: Context): Float {
        val expense = getPreferences(context).getFloat(KEY_TOTAL_EXPENSE, 0f)
        Log.d("TransactionManager", "Fetched Total Expense: $expense")
        return expense
    }


    fun getMonthlyExpenses(context: Context): Double {
        val transactions = getAllTransactions(context)
        val now = java.util.Calendar.getInstance()
        val currentMonth = now.get(java.util.Calendar.MONTH)
        val currentYear = now.get(java.util.Calendar.YEAR)

        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        return transactions.filter {
            try {
                val date = formatter.parse(it.date)
                val cal = java.util.Calendar.getInstance().apply { time = date!! }
                cal.get(java.util.Calendar.MONTH) == currentMonth &&
                        cal.get(java.util.Calendar.YEAR) == currentYear &&
                        it.type.equals("expense", ignoreCase = true)
            } catch (e: Exception) {
                false
            }
        }.sumOf { it.amount }
    }

    fun getMonthlyIncome(context: Context): Double {
        val transactions = getAllTransactions(context)
        val now = java.util.Calendar.getInstance()
        val currentMonth = now.get(java.util.Calendar.MONTH)
        val currentYear = now.get(java.util.Calendar.YEAR)

        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        return transactions.filter {
            try {
                val date = formatter.parse(it.date)
                val cal = java.util.Calendar.getInstance().apply { time = date!! }
                cal.get(java.util.Calendar.MONTH) == currentMonth &&
                        cal.get(java.util.Calendar.YEAR) == currentYear &&
                        it.type.equals("income", ignoreCase = true)
            } catch (e: Exception) {
                false
            }
        }.sumOf { it.amount }
    }






}
