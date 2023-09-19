package com.kiliccambaz.expenseapp.ui.accountant

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseUIModel
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.databinding.FragmentApprovedExpenseListBinding

class ApprovedExpenseListFragment : Fragment(), ApprovedExpenseListClickListener {

    private lateinit var approvedExpenseListAdapter: ApprovedExpenseListAdapter
    private lateinit var approvedExpenseListViewModel: ApprovedExpenseListViewModel
    private var _binding: FragmentApprovedExpenseListBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApprovedExpenseListBinding.inflate(layoutInflater)
        binding.include.toolbarTitle.text = "Approved Expense List"
        approvedExpenseListViewModel = ViewModelProvider(this)[ApprovedExpenseListViewModel::class.java]
        approvedExpenseListAdapter = ApprovedExpenseListAdapter(requireContext(), this)
        binding.rvApprovedExpenseList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvApprovedExpenseList.adapter = approvedExpenseListAdapter

        approvedExpenseListViewModel.expenseList.observe(viewLifecycleOwner) { expenseList ->
            approvedExpenseListAdapter.updateList(expenseList)
        }

        approvedExpenseListViewModel.filteredList.observe(viewLifecycleOwner) { filteredList ->
            approvedExpenseListAdapter.updateList(filteredList)

        }

        approvedExpenseListViewModel.updateResponse.observe(viewLifecycleOwner) { response ->
            when(response) {
                is Result.Success -> {
                    if(response.data) {
                        Toast.makeText(context, getString(R.string.payment_complated), Toast.LENGTH_LONG).show()
                    }
                }

                else -> {}
            }
        }

        binding.include.filterIcon.setOnClickListener {
            showFilterPopup()
        }

        return binding.root
    }

    override fun onPayButtonClick(expenseModel: ExpenseUIModel) {
        expenseModel.statusId = 5
        approvedExpenseListViewModel.updateExpense(expenseModel)
    }

    private fun showFilterPopup() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.filtering_options)

        val view = layoutInflater.inflate(R.layout.filter_type_options, null)
        builder.setView(view)

        val checkBoxes = listOf(
            Pair(view.findViewById(R.id.checkBoxTaxi), "Taxi"),
            Pair(view.findViewById(R.id.checkBoxFood), "Food"),
            Pair(view.findViewById(R.id.checkBoxGas), "Gas"),
            Pair(view.findViewById(R.id.checkBoxAccommodation), "Accommodation"),
            Pair(view.findViewById<CheckBox>(R.id.checkBoxOther), "Other")
        )

        val selectedExpenseTypes = mutableListOf<String>()

        checkBoxes.forEach { (checkBox, expenseType) ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedExpenseTypes.add(expenseType)
                } else {
                    selectedExpenseTypes.remove(expenseType)
                }
            }
        }

        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            approvedExpenseListViewModel.getExpensesByTypes(selectedExpenseTypes)
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

}