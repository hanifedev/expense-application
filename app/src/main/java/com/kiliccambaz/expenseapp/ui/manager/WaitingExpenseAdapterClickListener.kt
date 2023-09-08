package com.kiliccambaz.expenseapp.ui.manager

import com.kiliccambaz.expenseapp.data.ExpenseModel

interface WaitingExpenseAdapterClickListener {
    fun onApproveButtonClick(expenseModel: ExpenseModel)
    fun onRejectButtonClick(expenseModel: ExpenseModel)
}