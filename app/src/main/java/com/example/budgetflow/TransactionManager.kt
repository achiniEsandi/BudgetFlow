package com.example.budgetflow

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

object TransactionManager {
    private const val PREFS_NAME = "transaction_prefs"
    private const val KEY_TRANSACTIONS = "transactions"
    private const val KEY_TOTAL_BALANCE = "total_balance"
    private const val KEY_TOTAL_EXPENSE = "total_expense"
    private const val KEY_TOTAL_INCOME = "total_income"

    private val gson = Gson()
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun getPreferences(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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

        val totalIncome = transactions.filter { it.type.equals("income", ignoreCase = true) }
            .sumOf { it.amount }
        val totalExpense = transactions.filter { it.type.equals("expense", ignoreCase = true) }
            .sumOf { it.amount }
        val totalBalance = totalIncome - totalExpense

        Log.d("TransactionManager", "Calculated Totals -> Income: $totalIncome, Expense: $totalExpense, Balance: $totalBalance")

        val prefs = getPreferences(context)
        prefs.edit().apply {
            putFloat(KEY_TOTAL_BALANCE, totalBalance.toFloat())
            putFloat(KEY_TOTAL_EXPENSE, totalExpense.toFloat())
            putFloat(KEY_TOTAL_INCOME, totalIncome.toFloat())
        }.apply()

        Log.d("TransactionManager", "Saved Totals -> Balance: ${prefs.getFloat(KEY_TOTAL_BALANCE, 0f)}, Expense: ${prefs.getFloat(KEY_TOTAL_EXPENSE, 0f)}, Income: ${prefs.getFloat(KEY_TOTAL_INCOME, 0f)}")
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

    fun getTotalIncome(context: Context): Float {
        val income = getPreferences(context).getFloat(KEY_TOTAL_INCOME, 0f)
        Log.d("TransactionManager", "Fetched Total Income: $income")
        return income
    }

    fun getMonthlyExpenses(context: Context): Double {
        val transactions = getAllTransactions(context)
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        return transactions.filter {
            try {
                val date = dateFormatter.parse(it.date)
                val cal = Calendar.getInstance().apply { time = date!! }
                cal.get(Calendar.MONTH) == currentMonth &&
                        cal.get(Calendar.YEAR) == currentYear &&
                        it.type.equals("expense", ignoreCase = true)
            } catch (e: Exception) {
                false
            }
        }.sumOf { it.amount }
    }

    fun getMonthlyIncome(context: Context): Double {
        val transactions = getAllTransactions(context)
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        return transactions.filter {
            try {
                val date = dateFormatter.parse(it.date)
                val cal = Calendar.getInstance().apply { time = date!! }
                cal.get(Calendar.MONTH) == currentMonth &&
                        cal.get(Calendar.YEAR) == currentYear &&
                        it.type.equals("income", ignoreCase = true)
            } catch (e: Exception) {
                false
            }
        }.sumOf { it.amount }
    }

    fun getTransactions(context: Context): List<Transaction> {
        val prefs = context.getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val json = prefs.getString("data", "[]") ?: "[]"
        val type = object : TypeToken<List<Transaction>>() {}.type
        return Gson().fromJson(json, type)
    }
}
