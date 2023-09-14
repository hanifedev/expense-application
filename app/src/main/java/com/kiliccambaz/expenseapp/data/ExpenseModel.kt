package com.kiliccambaz.expenseapp.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.auth.User
import kotlinx.parcelize.Parcelize

@Parcelize
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
) : Parcelable {
    fun toMap(): Map<String, Any?> {
        val map = HashMap<String, Any?>()
        map["amount"] = amount
        map["date"] = date
        map["description"] = description
        map["expenseType"] = expenseType
        map["userId"] = userId
        map["currencyType"] = currencyType
        map["statusId"] = statusId
        map["rejectedReason"] = rejectedReason
        map["expenseId"] = expenseId
        return map
    }
}