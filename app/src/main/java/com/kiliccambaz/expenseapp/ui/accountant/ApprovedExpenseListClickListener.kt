package com.kiliccambaz.expenseapp.ui.accountant

import com.kiliccambaz.expenseapp.data.ExpenseUIModel

interface ApprovedExpenseListClickListener {

    fun onPayButtonClick(expenseModel: ExpenseUIModel)

}