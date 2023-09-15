package com.kiliccambaz.expenseapp.ui.admin.ui.expenses

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.databinding.FragmentExpensesBinding
import com.kiliccambaz.expenseapp.ui.admin.ui.history.HistoryAdapter
import com.kiliccambaz.expenseapp.ui.employee.expenses.ExpenseAdapterClickListener
import com.kiliccambaz.expenseapp.ui.employee.expenses.ExpenseListAdapter
import kotlin.math.exp

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private lateinit var expensesViewModel: ExpensesViewModel
    private lateinit var expenseListAdapter : HistoryAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        expensesViewModel =
            ViewModelProvider(this)[ExpensesViewModel::class.java]

        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.toolbarExpensesTitle.text = "Expense List"
        expenseListAdapter = HistoryAdapter( requireContext())
        binding.rvExpenseList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenseList.adapter = expenseListAdapter

        expensesViewModel.expenseList.observe(viewLifecycleOwner) { expenseList ->
            expenseListAdapter.updateList(expenseList)
        }

        expensesViewModel.filteredList.observe(viewLifecycleOwner) { filteredList ->
            expenseListAdapter.updateList(filteredList)
        }

        binding.expensesFilterIcon.setOnClickListener {
            showFilterPopup()
        }

        return root
    }

    private fun showFilterPopup() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.filtering_options)

        val view = layoutInflater.inflate(R.layout.filter_status_options, null)
        builder.setView(view)

        val checkBoxes = listOf(
            Pair(view.findViewById<CheckBox>(R.id.checkBoxWaiting), 1),
            Pair(view.findViewById(R.id.checkBoxApproved), 2),
            Pair(view.findViewById(R.id.checkBoxChangeStatus), 3),
            Pair(view.findViewById(R.id.checkBoxRejected), 4),
            Pair(view.findViewById(R.id.checkBoxPaid), 5)
        )

        val selectedStatusTypes = mutableListOf<Int>()
        checkBoxes.forEach { (checkBox, statusType) ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedStatusTypes.add(statusType)
                } else {
                    selectedStatusTypes.remove(statusType)
                }
            }
        }

        builder.setPositiveButton(R.string.filter) { dialog, _ ->
            expensesViewModel.getExpensesFromStatus(selectedStatusTypes)
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}