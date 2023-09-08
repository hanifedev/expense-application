package com.kiliccambaz.expenseapp.ui.employee.expenses

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.kiliccambaz.expenseapp.BR
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.databinding.ExpenseListBinding

class ExpenseListAdapter constructor(private val expenseClickListener: ExpenseAdapterClickListener) : RecyclerView.Adapter<ExpenseListAdapter.ExpenseListViewHolder>() {

    class ExpenseListViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ExpenseListBinding.inflate(layoutInflater, parent,false)
        return ExpenseListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseListViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.bind(expense, expenseClickListener, position)
        when (expense.expenseType) {
            "Gas" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.gas)
            "Food" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.food)
            "Taxi" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.taxi)
            "Accommodation" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.otel)
            else -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.expenses)
        }

        when (expense.statusId) {
            1 -> holder.itemView.findViewById<TextView>(R.id.tvStatus).text = "Waiting"
            2 -> holder.itemView.findViewById<TextView>(R.id.tvStatus).text = "Approved"
            3 -> holder.itemView.findViewById<TextView>(R.id.tvStatus).text = "Change Request"
            4 -> holder.itemView.findViewById<TextView>(R.id.tvStatus).text = "Rejected"
            5 -> holder.itemView.findViewById<TextView>(R.id.tvStatus).text = "Paid"
        }
    }

    override fun getItemCount() = expenseList.size

}