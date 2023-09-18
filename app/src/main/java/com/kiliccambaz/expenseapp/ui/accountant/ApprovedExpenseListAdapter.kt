package com.kiliccambaz.expenseapp.ui.accountant

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import com.kiliccambaz.expenseapp.BR
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseUIModel
import com.kiliccambaz.expenseapp.databinding.ApprovedExpenseListBinding


class ApprovedExpenseListAdapter constructor(private val approvedExpenseListClickListener: ApprovedExpenseListClickListener): RecyclerView.Adapter<ApprovedExpenseListAdapter.ApprovedExpenseListViewHolder>() {

    class ApprovedExpenseListViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expenseModel: ExpenseUIModel?, clickListener: ApprovedExpenseListClickListener, position: Int) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApprovedExpenseListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ApprovedExpenseListBinding.inflate(layoutInflater, parent,false)
        return ApprovedExpenseListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ApprovedExpenseListViewHolder, position: Int) {
        val expense = expenseList?.get(position)
        holder.bind(expense, approvedExpenseListClickListener, position)
        if (expense != null) {
            when (expense.expenseType) {
                "Gas" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.gas)
                "Food" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.food)
                "Taxi" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.taxi)
                "Accommodation" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(
                    R.drawable.otel)
                else -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.expenses)
            }
        }

        when (expense?.statusId) {
            1 -> holder.itemView.findViewById<TextView>(R.id.tvStatus).text = "Waiting"
            2 -> holder.itemView.findViewById<TextView>(R.id.tvStatus).text = "Approved"
            3 -> holder.itemView.findViewById<TextView>(R.id.tvStatus).text = "Change Request"
            4 -> holder.itemView.findViewById<TextView>(R.id.tvStatus).text = "Rejected"
            5 -> holder.itemView.findViewById<TextView>(R.id.tvStatus).text = "Paid"
        }
    }

    override fun getItemCount() = expenseList?.size ?: 0

}