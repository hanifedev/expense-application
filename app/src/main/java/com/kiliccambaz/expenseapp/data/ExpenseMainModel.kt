package com.kiliccambaz.expenseapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExpenseMainModel(
    var date: String = "",
    var description: String = "",
    var userId: String = "",
    var currencyType: String = "",
    var statusId: Int = 1,
    var rejectedReason: String = "",
    var expenseId: String = ""
) : Parcelable {
    fun toMap(): Map<String, Any?> {
        val map = HashMap<String, Any?>()
        map["date"] = date
        map["description"] = description
        map["userId"] = userId
        map["currencyType"] = currencyType
        map["statusId"] = statusId
        map["rejectedReason"] = rejectedReason
        map["expenseId"] = expenseId
        return map
    }
}