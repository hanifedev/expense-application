package com.kiliccambaz.expenseapp.ui.manager.expensedetails

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kiliccambaz.expenseapp.data.ExpenseUIModel
import com.kiliccambaz.expenseapp.databinding.FragmentExpenseDetailBinding
import com.kiliccambaz.expenseapp.ui.admin.ui.history.HistoryAdapter
import com.kiliccambaz.expenseapp.ui.admin.ui.history.HistoryAdapterClickListener
import com.kiliccambaz.expenseapp.ui.manager.WaitingExpensesViewModel
import java.text.DecimalFormat


class ExpenseDetailFragment : Fragment(), HistoryAdapterClickListener {

    private var currencySymbol = ""
    private var binding: FragmentExpenseDetailBinding? = null
    private lateinit var expenseViewModel: WaitingExpensesViewModel
    private lateinit var historyAdapter: HistoryAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExpenseDetailBinding.inflate(layoutInflater)
        expenseViewModel = ViewModelProvider(this)[WaitingExpensesViewModel::class.java]

        val args: ExpenseDetailFragmentArgs by navArgs()
        val expenseModel = args.expenseUIModel

        expenseModel?.let {
            expenseViewModel.setMainExpense(it)
            binding!!.tvExpenseDescription.visibility = View.VISIBLE
            binding!!.tvExpenseDescription.text = it.description
            setCurrencySembol(it.currencyType)
            if(expenseModel.statusId == 3) {
                binding!!.cardRejectedStatus.visibility = View.VISIBLE
                binding!!.txtRejectedDescription.text = expenseModel.rejectedReason
            } else {
                binding!!.cardRejectedStatus.visibility = View.GONE
            }
        }

        historyAdapter = HistoryAdapter(requireContext(), false, this)
        binding!!.rvExpenseDetail.layoutManager = LinearLayoutManager(requireContext())
        binding!!.rvExpenseDetail.adapter = historyAdapter

        expenseViewModel.expenseDetailList.observe(viewLifecycleOwner) { expenseList ->
            if(expenseList.isNotEmpty()) {
                historyAdapter.updateList(expenseList)
                convertDecimalFormat(expenseList)
            }
        }

        return binding!!.root
    }

    private fun convertDecimalFormat(expenseDetailList: List<ExpenseUIModel>) {
        val totalAmount = expenseDetailList.sumOf { it.amount }
        val decimalFormat = DecimalFormat("#,###.###")
        val formattedTotalAmount = decimalFormat.format(totalAmount)
        val totalAmountWithCurrency = "$currencySymbol $formattedTotalAmount"
        binding!!.totalAmountTextview.text = totalAmountWithCurrency
    }


    private fun setCurrencySembol(currencyType: String) {
        currencySymbol = when (currencyType) {
            "TL" -> "₺"
            "USD" -> "$"
            "EUR" -> "€"
            "PKR" -> "₨"
            "INR" -> "₹"
            else -> ""
        }
    }

    override fun onRecyclerViewItemClick(model: ExpenseUIModel, position: Int) {

    }
}