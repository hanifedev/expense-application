package com.kiliccambaz.expenseapp.ui.employee.expenses

import com.kiliccambaz.expenseapp.data.ExpenseModel

interface ExpenseAdapterClickListener {

    fun onRecyclerViewItemClick(model : ExpenseModel, position : Int)

}