package com.kiliccambaz.expenseapp.data

data class UserModel(
    val email: String = "",
    val managerId: String = "",
    val password: String = "",
    val role: Int = 0
)