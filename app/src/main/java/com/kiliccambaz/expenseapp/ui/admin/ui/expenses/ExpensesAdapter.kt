package com.kiliccambaz.expenseapp.ui.admin.ui.expenses

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.kiliccambaz.expenseapp.BR
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseUIModel
import com.kiliccambaz.expenseapp.databinding.ExpenseListBinding
import com.kiliccambaz.expenseapp.databinding.ExpensesListBinding

class ExpensesAdapter constructor(private val context: Context, private val expensesAdapterClickListener: ExpensesAdapterClickListener) : RecyclerView.Adapter<ExpensesAdapter.ExpensesViewHolder>() {

    class ExpensesViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expenseModel: ExpenseUIModel?, position: Int, clickListener: ExpensesAdapterClickListener) {
            binding.setVariable(BR.expenseModel, expenseModel)
            binding.setVariable(BR.clickListener, clickListener)
            binding.setVariable(BR.position, position)
        }
    }

    private var expenseList: List<ExpenseUIModel>? = arrayListOf()

    fun updateList(expenseList: List<ExpenseUIModel>?) {
        this.expenseList = expenseList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ExpensesListBinding.inflate(layoutInflater, parent,false)
        return ExpensesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpensesViewHolder, position: Int) {
        val expense = expenseList?.get(position)
        holder.bind(expense, position, expensesAdapterClickListener)

        val status = holder.itemView.findViewById<TextView>(R.id.tv_status)
        when (expense?.statusId) {
            1 -> {
                status.setTextColor(context.getColor(com.google.android.material.R.color.material_blue_grey_800))
                status.text = "Waiting"
            }
            2 -> {
                status.setTextColor(context.getColor(androidx.appcompat.R.color.material_deep_teal_500))
                status.text = "Approved"
            }
            3 -> {
                status.setTextColor(context.getColor(R.color.red))
                status.text = "Change Request"
            }
            4 -> {
                status.setTextColor(context.getColor(androidx.appcompat.R.color.material_blue_grey_800))
                status.text = "Rejected"
            }
            5 -> {
                status.setTextColor(context.getColor(R.color.green))
                status.text = "Paid"
            }
        }
    }

    override fun getItemCount() = expenseList?.size ?: 0

}