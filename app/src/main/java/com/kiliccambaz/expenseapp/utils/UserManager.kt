package com.kiliccambaz.expenseapp.utils

object UserManager {
    private var userId: String? = null

    fun getUserId(): String? {
        return userId
    }

    fun setUserId(id: String) {
        userId = id
    }
}