package com.kiliccambaz.expenseapp.ui.employee.expenses

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.databinding.FragmentExpenseListBinding
import com.kiliccambaz.expenseapp.databinding.FragmentLoginBinding

class ExpenseListFragment : Fragment(), ExpenseAdapterClickListener {

    private lateinit var expenseListViewModel: ExpenseListViewModel
    private var binding: FragmentExpenseListBinding? = null
    private lateinit var expenseListAdapter : ExpenseListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExpenseListBinding.inflate(layoutInflater)
        binding!!.toolbarTitle.text = "Expense List"
        expenseListViewModel = ViewModelProvider(this)[ExpenseListViewModel::class.java]
        expenseListAdapter = ExpenseListAdapter( requireContext(), this)
        binding!!.rvExpenseList.layoutManager = LinearLayoutManager(requireContext())
        binding!!.rvExpenseList.adapter = expenseListAdapter

        expenseListViewModel.expenseList.observe(viewLifecycleOwner) { expenseList ->
            expenseList?.let {
                expenseListAdapter.updateList(expenseList)
            }
        }

        expenseListViewModel.filteredList.observe(viewLifecycleOwner) { filteredList ->
            filteredList?.let {
                expenseListAdapter.updateList(filteredList)
            }
        }

        binding!!.fabAddExpense.setOnClickListener {
            val action = ExpenseListFragmentDirections.actionExpenseListFragmentToAddExpenseFragment(null)
            findNavController().navigate(action)
        }

        binding!!.filterIcon.setOnClickListener {
            showFilterPopup()
        }

        return binding!!.root
    }

    override fun onRecyclerViewItemClick(model: ExpenseModel, position: Int) {
        if(model.statusId == 1) {
            val action = ExpenseListFragmentDirections.actionExpenseListFragmentToAddExpenseFragment(model)
            findNavController().navigate(action)
        } else if(model.statusId == 3) {
            showDescriptionDetailDialog(model)
        }
    }

    private fun showDescriptionDetailDialog(model: ExpenseModel) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_description_detail, null)
        val descriptionTextView = dialogView.findViewById<TextView>(R.id.tvDescription)

        descriptionTextView.text = model.rejectedReason

        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Description Detail")
            .setPositiveButton(R.string.update_expense) { dialog, _ ->
                dialog.dismiss()
                val action = ExpenseListFragmentDirections.actionExpenseListFragmentToAddExpenseFragment(model)
                findNavController().navigate(action)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
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
            expenseListViewModel.getExpensesByTypes(selectedExpenseTypes)
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        expenseListViewModel.fetchExpenseListFromDatabase()
    }

}