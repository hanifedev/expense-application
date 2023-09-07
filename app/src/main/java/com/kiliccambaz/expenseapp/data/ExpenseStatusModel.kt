package com.kiliccambaz.expenseapp.data

data class ExpenseStatusModel(
    val approved: Boolean,
    val changeRequested: Boolean,
    val changeRequestedDescription: String,
    val paid: Boolean
)