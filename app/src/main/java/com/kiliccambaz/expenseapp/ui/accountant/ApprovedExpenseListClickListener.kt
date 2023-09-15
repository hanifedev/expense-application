package com.kiliccambaz.expenseapp.ui.accountant

import com.kiliccambaz.expenseapp.data.ExpenseHistoryUIModel
import com.kiliccambaz.expenseapp.data.ExpenseModel

interface ApprovedExpenseListClickListener {

    fun onPayButtonClick(expenseModel: ExpenseHistoryUIModel)

}