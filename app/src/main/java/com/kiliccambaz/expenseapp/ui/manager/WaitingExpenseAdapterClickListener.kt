package com.kiliccambaz.expenseapp.ui.manager

import com.kiliccambaz.expenseapp.data.ExpenseHistoryUIModel
import com.kiliccambaz.expenseapp.data.ExpenseModel

interface WaitingExpenseAdapterClickListener {
    fun onApproveButtonClick(expenseModel: ExpenseHistoryUIModel)
    fun onRejectButtonClick(expenseModel: ExpenseHistoryUIModel)
}