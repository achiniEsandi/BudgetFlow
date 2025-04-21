package com.example.budgetflow

import android.content.Context
import android.content.SharedPreferences

object BudgetUtils {
    private const val PREF_NAME = "BudgetPrefs"
    private const val KEY_BUDGET = "monthly_budget"

    fun saveBudget(context: Context, budget: Double) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_BUDGET, budget.toFloat()).apply()
    }

    fun getBudget(context: Context): Double {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_BUDGET, 0f).toDouble()
    }
}
