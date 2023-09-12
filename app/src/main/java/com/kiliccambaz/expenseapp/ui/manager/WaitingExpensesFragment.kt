package com.kiliccambaz.expenseapp.ui.manager

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseModel
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
            expenseList?.let {
                waitingExpensesAdapter.updateList(expenseList)
            }
        }

        binding!!.toolbar.filterIcon.setOnClickListener {
            showFilterPopup()
        }

        return binding!!.root
    }

    override fun onApproveButtonClick(expenseModel: ExpenseModel) {
        expenseModel.statusId = 2
        waitingViewModel.updateExpense(expenseModel)
    }

    override fun onRejectButtonClick(expenseModel: ExpenseModel) {
        showDescriptionDialog(expenseModel)
    }

    private fun showDescriptionDialog(expenseModel: ExpenseModel) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Değişiklik İsteği")

        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.custom_description_dialog, null)
        val editTextDescription = dialogView.findViewById<TextInputEditText>(R.id.txtDescription)

        builder.setView(dialogView)
        builder.setPositiveButton("Tamam") { _, _ ->
            val description = editTextDescription.text.toString()
            expenseModel.rejectedReason = description
            expenseModel.statusId = 4
            waitingViewModel.updateExpense(expenseModel)
        }
        builder.setNegativeButton("Vazgeç") { _, _ -> }

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

        builder.setPositiveButton("Tamam") { dialog, _ ->
            waitingViewModel.getExpensesByTypes(selectedExpenseTypes) { expenseList ->
                waitingExpensesAdapter.updateList(expenseList)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("İptal") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }


}