package com.kiliccambaz.expenseapp.ui.admin.ui.history.historydetail

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.databinding.FragmentHistoryBinding
import com.kiliccambaz.expenseapp.databinding.FragmentHistoryDetailBinding
import com.kiliccambaz.expenseapp.ui.admin.ui.expenses.ExpensesAdapter
import com.kiliccambaz.expenseapp.ui.admin.ui.history.HistoryViewModel
import com.kiliccambaz.expenseapp.ui.employee.addexpense.AddExpenseFragmentArgs

class HistoryDetailFragment : Fragment() {

    private var _binding: FragmentHistoryDetailBinding? = null
    private lateinit var historyDetailViewModel: HistoryDetailViewModel
    private lateinit var historyDetailAdapter : HistoryDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryDetailBinding.inflate(inflater, container, false)
        historyDetailViewModel =
            ViewModelProvider(this)[HistoryDetailViewModel::class.java]

        _binding!!.toolbarHistory.toolbarTitle.text = "History Detail List"

        val args: HistoryDetailFragmentArgs by navArgs()
        val expenseModel = args.expenseUIModel

        expenseModel?.let {
            historyDetailViewModel.getExpenseHistoryForExpenseId(expenseModel.expenseId)
        }

        historyDetailAdapter = HistoryDetailAdapter(requireContext())
        _binding!!.rvHistoryDetail.layoutManager = LinearLayoutManager(requireContext())
        _binding!!.rvHistoryDetail.adapter = historyDetailAdapter

        historyDetailViewModel.historyList.observe(viewLifecycleOwner) { historyList ->
            historyList?.let {
                historyDetailAdapter.updateList(historyList)
            }
        }

        historyDetailViewModel.filteredList.observe(viewLifecycleOwner) { filteredList ->
            historyDetailAdapter.updateList(filteredList)
        }


        _binding!!.toolbarHistory.filterIcon.setOnClickListener {
            showFilterPopup()
        }

        return _binding!!.root
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

        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            historyDetailViewModel.getExpensesFromStatus(selectedStatusTypes)
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

}