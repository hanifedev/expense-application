package com.kiliccambaz.expenseapp.ui.manager

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.kiliccambaz.expenseapp.BR
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseUIModel
import com.kiliccambaz.expenseapp.databinding.WaitingExpenseListBinding

class WaitingExpensesAdapter(private val clickListener: WaitingExpenseAdapterClickListener) : RecyclerView.Adapter<WaitingExpensesAdapter.WaitingExpenseViewHolder>() {

    class WaitingExpenseViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expenseModel: ExpenseUIModel?, clickListener: WaitingExpenseAdapterClickListener, position: Int) {
            binding.setVariable(BR.clickListener, clickListener)
            binding.setVariable(BR.expenseModel, expenseModel)
            binding.setVariable(BR.position, position)
        }
    }

    private var expenseList: List<ExpenseUIModel>? = arrayListOf()

    fun updateList(expenseList: List<ExpenseUIModel>?) {
        this.expenseList = expenseList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaitingExpenseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = WaitingExpenseListBinding.inflate(layoutInflater, parent,false)
        return WaitingExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WaitingExpenseViewHolder, position: Int) {
        val expense = expenseList?.get(position)
        holder.bind(expense, clickListener, position)
        when (expense?.expenseType) {
            "Gas" -> holder.itemView.findViewById<ImageView>(R.id.iv_expense_type).setImageResource(R.drawable.gas)
            "Food" -> holder.itemView.findViewById<ImageView>(R.id.iv_expense_type).setImageResource(R.drawable.food)
            "Taxi" -> holder.itemView.findViewById<ImageView>(R.id.iv_expense_type).setImageResource(R.drawable.taxi)
            "Accommodation" -> holder.itemView.findViewById<ImageView>(R.id.iv_expense_type).setImageResource(
                R.drawable.otel)
            else -> holder.itemView.findViewById<ImageView>(R.id.iv_expense_type).setImageResource(R.drawable.expenses)
        }
    }

    override fun getItemCount() = expenseList?.size ?: 0

}