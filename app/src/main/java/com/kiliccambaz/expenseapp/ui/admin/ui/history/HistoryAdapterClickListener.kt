package com.kiliccambaz.expenseapp.ui.admin.ui.history

import com.kiliccambaz.expenseapp.data.ExpenseUIModel

interface HistoryAdapterClickListener {

    fun onRecyclerViewItemClick(model : ExpenseUIModel, position : Int)

}