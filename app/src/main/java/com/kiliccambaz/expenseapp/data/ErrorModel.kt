package com.kiliccambaz.expenseapp.data

import com.google.firebase.Timestamp

data class ErrorModel(
    val errorCode: Int,
    val errorMessage: String?,
    val userId: String?,
    val errorType: String?,
    val timestamp: Timestamp
)