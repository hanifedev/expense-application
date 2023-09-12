package com.kiliccambaz.expenseapp.ui.admin.ui.expenses

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.databinding.FragmentExpensesBinding

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private lateinit var expensesViewModel: ExpensesViewModel

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
        binding.toolbarTitle.text = "Expense List"

        expensesViewModel.expenseList.observe(viewLifecycleOwner) { expenseList ->

        }

        binding.filterIcon.setOnClickListener {
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
            expensesViewModel.getExpensesFromStatus(selectedStatusTypes) { expenseList ->

            }
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
        expensesViewModel.fetchExpenseListFromDatabase()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}