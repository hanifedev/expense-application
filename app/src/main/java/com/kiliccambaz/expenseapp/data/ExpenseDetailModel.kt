package com.kiliccambaz.expenseapp.data

import android.os.Parcelable
import com.kiliccambaz.expenseapp.utils.DateTimeUtils
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExpenseDetailModel(var expenseDate: String = "", var expenseType: String = "", var amount: Double = 0.0, var expenseDetailId: String = "", var date: String = DateTimeUtils.getCurrentDateTimeAsString())  :
    Parcelable {
    fun toMap(): Map<String, Any?> {
        val map = HashMap<String, Any?>()
        map["expenseDate"] = expenseDate
        map["expenseType"] = expenseType
        map["amount"] = amount
        map["expenseDetailId"] = expenseDetailId
        map["date"] = date
        return map
    }
}