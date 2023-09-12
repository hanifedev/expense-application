package com.kiliccambaz.expenseapp.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.auth.User

data class ExpenseModel(
    var amount: Double = 0.0,
    var date: String = "",
    var description: String = "",
    var expenseType: String = "",
    var userId: String = "",
    var currencyType: String = "",
    var statusId: Int = 1,
    var rejectedReason: String = "",
    var expenseId: String = ""
)