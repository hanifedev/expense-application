package com.kiliccambaz.expenseapp.data

data class ExpenseHistoryUIModel(var user: String = "",
                                 var date: String = "",
                                 var amount: Double = 0.0,
                                 var description: String = "",
                                 var expenseType: String = "",
                                 var currencyType: String = "",
                                 var statusId: Int = 1,
                                 var rejectedReason: String = "",
                                 var expenseId: String = "")