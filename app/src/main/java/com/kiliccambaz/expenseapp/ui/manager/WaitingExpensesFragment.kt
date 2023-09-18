package com.kiliccambaz.expenseapp.ui.manager

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseDetailModel
import com.kiliccambaz.expenseapp.data.ExpenseUIModel
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.databinding.FragmentWaitingExpensesBinding
import com.kiliccambaz.expenseapp.ui.employee.expenses.ExpenseListFragmentDirections

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

    override fun onApproveButtonClick(expenseModel: ExpenseUIModel) {
        expenseModel.statusId = 2
        waitingViewModel.updateExpense(expenseModel)
    }

    override fun onRejectButtonClick(expenseModel: ExpenseUIModel) {
        showDescriptionDialog(expenseModel)
    }

    override fun onShowDetailClick(expenseModel: ExpenseUIModel) {
        val action = WaitingExpensesFragmentDirections.actionWaitingExpensesFragmentToExpenseDetailFragment(expenseModel)
        findNavController().navigate(action)
    }

    private fun showDescriptionDialog(expenseModel: ExpenseUIModel) {
        var alertDialog: AlertDialog? = null
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.custom_description_dialog, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(requireContext().getString(R.string.save_button_text), null)
            .setNegativeButton(requireContext().getString(R.string.cancel)) { _, _ -> alertDialog?.dismiss() }.create()

        val editTextDescription = dialogView.findViewById<TextInputEditText>(R.id.txtDescription)

        alertDialog?.setOnShowListener {
            val saveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val description = editTextDescription.text.toString()
                val txtDescriptionInputLayout = dialogView.findViewById<TextInputLayout>(R.id.txtDescriptionInputLayout)
                if(description.isNullOrEmpty()) {
                    txtDescriptionInputLayout.error = requireContext().getString(R.string.description_validation)
                } else {
                    txtDescriptionInputLayout.error = null
                    expenseModel.rejectedReason = description
                    expenseModel.statusId = 3
                    waitingViewModel.updateExpense(expenseModel)
                    alertDialog.dismiss()
                }
            }
        }

        alertDialog.show()
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