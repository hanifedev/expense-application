package com.kiliccambaz.expenseapp.ui.employee.expenses

import com.kiliccambaz.expenseapp.data.ExpenseMainModel

interface ExpenseAdapterClickListener {

    fun onRecyclerViewItemClick(model : ExpenseMainModel, position : Int)

}