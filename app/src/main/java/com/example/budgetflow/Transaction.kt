package com.example.budgetflow

data class Transaction(
    var id: Long,
    var type: String, // "Income" or "Expense"
    var amount: Double,
    var category: String,
    var date: String,
    var notes: String
)
