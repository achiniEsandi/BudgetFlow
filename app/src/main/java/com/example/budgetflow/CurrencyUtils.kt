package com.example.budgetflow

import android.content.Context
import java.text.NumberFormat

object CurrencyUtils {
    const val PREFS_NAME = "budget_prefs"
    const val KEY_CURRENCY = "currency"
    private const val DEFAULT_CURRENCY = "Rs."

    /**
     * Retrieves the selected currency symbol from SharedPreferences.
     */
    fun getSelectedCurrency(context: Context): String {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
    }

    /**
     * Formats the given amount with the selected currency symbol, using two decimal places.
     * Uses NumberFormat for locale-aware formatting.
     */
    fun formatAmount(context: Context, amount: Double): String {
        val currency = getSelectedCurrency(context)
        val numberFormat = NumberFormat.getNumberInstance().apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "$currency${numberFormat.format(amount)}"
    }
}

