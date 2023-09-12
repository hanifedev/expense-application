package com.kiliccambaz.expenseapp.ui.admin.ui.history

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.databinding.FragmentHistoryBinding
import com.kiliccambaz.expenseapp.ui.employee.expenses.ExpenseListAdapter

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var historyAdapter : HistoryAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        historyViewModel =
            ViewModelProvider(this)[HistoryViewModel::class.java]
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.toolbar.toolbarTitle.text = "Expense History List"
        historyAdapter = HistoryAdapter()
        binding.rvHistoryList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistoryList.adapter = historyAdapter


        historyViewModel.historyList.observe(viewLifecycleOwner) { historyList ->
            historyList?.let {
                historyAdapter.updateList(historyList)
            }
        }

        binding.toolbar.filterIcon.setOnClickListener {
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

        builder.setPositiveButton("Tamam") { dialog, _ ->
            historyViewModel.getExpensesByStatus(selectedStatusTypes)
            dialog.dismiss()
        }
        builder.setNegativeButton("İptal") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        historyViewModel.getExpenseHistoryList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}