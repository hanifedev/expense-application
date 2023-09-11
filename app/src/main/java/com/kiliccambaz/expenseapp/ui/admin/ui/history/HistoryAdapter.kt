package com.kiliccambaz.expenseapp.ui.admin.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.kiliccambaz.expenseapp.BR
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseHistoryUIModel
import com.kiliccambaz.expenseapp.databinding.HistoryListBinding

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expenseModel: ExpenseHistoryUIModel, position: Int) {
            binding.setVariable(BR.expenseModel, expenseModel)
            binding.setVariable(BR.position, position)
        }
    }

    private var expenseList: List<ExpenseHistoryUIModel> = arrayListOf()

    fun updateList(expenseList: List<ExpenseHistoryUIModel>) {
        this.expenseList = expenseList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = HistoryListBinding.inflate(layoutInflater, parent,false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.bind(expense, position)
        when (expense.expenseType) {
            "Gas" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.gas)
            "Food" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.food)
            "Taxi" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.taxi)
            "Accommodation" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(
                R.drawable.otel)
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