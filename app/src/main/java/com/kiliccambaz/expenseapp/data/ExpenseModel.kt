package com.kiliccambaz.expenseapp.data

import com.google.firebase.Timestamp

data class ExpenseModel(
    var amount: Double = 0.0,
    var date: String = "",
    var description: String = "",
    var expenseType: String = "",
    var userId: String = "-Ndj8cP7h0zzHXNRT1g2",
    var currencyType: String = "",
    var statusId: Int = 1,
    var rejectedReason: String = "",
    var expenseId: String = ""
)