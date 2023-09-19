package com.kiliccambaz.expenseapp.ui.admin.ui.expenses

import com.kiliccambaz.expenseapp.data.ExpenseUIModel

interface ExpensesAdapterClickListener {

    fun onShowDetailClick(expenseUIModel: ExpenseUIModel)
}