package com.kiliccambaz.expenseapp.ui.manager

import com.kiliccambaz.expenseapp.data.ExpenseUIModel

interface WaitingExpenseAdapterClickListener {
    fun onApproveButtonClick(expenseModel: ExpenseUIModel)
    fun onRejectButtonClick(expenseModel: ExpenseUIModel)

    fun onShowDetailClick(expenseModel: ExpenseUIModel)
}