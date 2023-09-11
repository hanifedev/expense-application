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
        builder.setTitle("Filtreleme")

        // Filtre seçeneklerini içeren bir görünümü yükleyin
        val view = layoutInflater.inflate(R.layout.filter_status_options, null)
        builder.setView(view)

        val checkBoxPending = view.findViewById<CheckBox>(R.id.checkBoxPending)
        val checkBoxApproved = view.findViewById<CheckBox>(R.id.checkBoxApproved)
        val checkBoxPaid = view.findViewById<CheckBox>(R.id.checkBoxPaid)

        checkBoxPending.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

            }
        }

        checkBoxApproved.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

            }
        }

        checkBoxPaid.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

            }
        }

        builder.setPositiveButton("Tamam") { dialog, _ ->
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