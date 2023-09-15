package com.kiliccambaz.expenseapp.ui.manager

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
import com.google.android.material.textfield.TextInputEditText
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseHistoryUIModel
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.databinding.FragmentWaitingExpensesBinding

class WaitingExpensesFragment : Fragment(), WaitingExpenseAdapterClickListener {

    private lateinit var waitingViewModel: WaitingExpensesViewModel
    private var binding: FragmentWaitingExpensesBinding? = null
    private lateinit var waitingExpensesAdapter : WaitingExpensesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWaitingExpensesBinding.inflate(layoutInflater)
        binding!!.toolbar.toolbarTitle.text = "Waiting Expense List"
        waitingViewModel = ViewModelProvider(this)[WaitingExpensesViewModel::class.java]
        waitingExpensesAdapter = WaitingExpensesAdapter(this)
        binding!!.rvWaitingExpenseList.layoutManager = LinearLayoutManager(requireContext())
        binding!!.rvWaitingExpenseList.adapter = waitingExpensesAdapter

        waitingViewModel.expenseList.observe(viewLifecycleOwner) { expenseList ->
            waitingExpensesAdapter.updateList(expenseList)
        }

        waitingViewModel.filteredList.observe(viewLifecycleOwner) { filteredList ->
            waitingExpensesAdapter.updateList(filteredList)
        }

        binding!!.toolbar.filterIcon.setOnClickListener {
            showFilterPopup()
        }

        waitingViewModel.updateResponse.observe(viewLifecycleOwner) { response ->
            when(response) {
                is Result.Success -> {
                    if (response.data) {
                        Toast.makeText(context, R.string.transaction_successfully, Toast.LENGTH_LONG).show()
                    }
                }

                else -> {}
            }

        }

        return binding!!.root
    }

    override fun onApproveButtonClick(expenseModel: ExpenseHistoryUIModel) {
        expenseModel.statusId = 2
        waitingViewModel.updateExpense(expenseModel)
    }

    override fun onRejectButtonClick(expenseModel: ExpenseHistoryUIModel) {
        showDescriptionDialog(expenseModel)
    }

    private fun showDescriptionDialog(expenseModel: ExpenseHistoryUIModel) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.change_request))

        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.custom_description_dialog, null)
        val editTextDescription = dialogView.findViewById<TextInputEditText>(R.id.txtDescription)

        builder.setView(dialogView)
        builder.setPositiveButton(getString(R.string.save_button_text)) { _, _ ->
            val description = editTextDescription.text.toString()
            expenseModel.rejectedReason = description
            expenseModel.statusId = 3
            waitingViewModel.updateExpense(expenseModel)
        }
        builder.setNegativeButton(getString(R.string.cancel)) { _, _ -> }

        val dialog = builder.create()
        dialog.show()
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

        builder.setPositiveButton(getString(R.string.save_button_text)) { dialog, _ ->
            waitingViewModel.getExpensesByTypes(selectedExpenseTypes)
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

}