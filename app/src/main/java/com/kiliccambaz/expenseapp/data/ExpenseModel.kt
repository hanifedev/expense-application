package com.kiliccambaz.expenseapp.data

import com.google.firebase.Timestamp

data class ExpenseModel(
    var amount: Double = 0.0,
    var date: String = "",
    var description: String = "",
    var expenseType: String = "",
    var userId: String = "",
    var currencyType: String = ""
)