package com.kiliccambaz.expenseapp.ui.accountant

import android.content.Context
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


class ApprovedExpenseListAdapter constructor(private val context: Context, private val approvedExpenseListClickListener: ApprovedExpenseListClickListener): RecyclerView.Adapter<ApprovedExpenseListAdapter.ApprovedExpenseListViewHolder>() {

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

        val status = holder.itemView.findViewById<TextView>(R.id.tvStatus)
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