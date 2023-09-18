package com.kiliccambaz.expenseapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExpenseUIModel(var user: String = "",
                                 var date: String = "",
                                 var expenseDate: String = "",
                                 var amount: Double = 0.0,
                                 var description: String = "",
                                 var expenseType: String = "",
                                 var currencyType: String = "",
                                 var statusId: Int = 1,
                                 var rejectedReason: String = "",
                                 var expenseId: String = "",
                                 var expenseDetailId: String = "",
                                 var userId: String = "") : Parcelable