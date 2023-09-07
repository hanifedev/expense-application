package com.kiliccambaz.expenseapp.ui.employee.expenses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.kiliccambaz.expenseapp.BR
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.databinding.ExpenseListBinding

class ExpenseListAdapter constructor(private val expenseClickListener: ExpenseAdapterClickListener) : RecyclerView.Adapter<ExpenseListAdapter.CountingViewHolder>() {

    class CountingViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expenseModel: ExpenseModel, clickListener: ExpenseAdapterClickListener, position: Int) {
            binding.setVariable(BR.expenseModel, expenseModel)
            binding.setVariable(BR.clickListener, clickListener)
            binding.setVariable(BR.position, position)
        }
    }

    private var expenseList: List<ExpenseModel> = arrayListOf()

    fun updateList(expenseList: List<ExpenseModel>) {
        this.expenseList = expenseList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountingViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ExpenseListBinding.inflate(layoutInflater, parent,false)
        return CountingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountingViewHolder, position: Int) {
        val epc = expenseList[position]
        holder.bind(epc, expenseClickListener, position)
    }

    override fun getItemCount() = expenseList.size

}