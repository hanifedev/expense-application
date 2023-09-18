package com.kiliccambaz.expenseapp.ui.admin.ui.history

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.kiliccambaz.expenseapp.BR
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseUIModel
import com.kiliccambaz.expenseapp.databinding.HistoryListBinding
import com.kiliccambaz.expenseapp.ui.employee.expenses.ExpenseAdapterClickListener

class HistoryAdapter constructor(private val context: Context, private val showUser: Boolean, private val historyAdapterClickListener: HistoryAdapterClickListener) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expenseModel: ExpenseUIModel?, position: Int, clickListener: HistoryAdapterClickListener) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = HistoryListBinding.inflate(layoutInflater, parent,false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val expense = expenseList?.get(position)
        holder.bind(expense, position, historyAdapterClickListener)
        when (expense?.expenseType) {
            "Gas" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.gas)
            "Food" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.food)
            "Taxi" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.taxi)
            "Accommodation" -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(
                R.drawable.otel)
            else -> holder.itemView.findViewById<ImageView>(R.id.ivExpenseType).setImageResource(R.drawable.expenses)
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

        val userCard = holder.itemView.findViewById<CardView>(R.id.cardView2)
        if(showUser) {
            userCard.visibility = View.VISIBLE
        } else {
            userCard.visibility = View.GONE
        }
    }

    override fun getItemCount() = expenseList?.size ?: 0

}