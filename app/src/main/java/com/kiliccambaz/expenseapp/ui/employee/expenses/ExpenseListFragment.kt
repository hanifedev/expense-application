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
import com.kiliccambaz.expenseapp.data.ExpenseMainModel
import com.kiliccambaz.expenseapp.databinding.FragmentExpenseListBinding

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

    override fun onRecyclerViewItemClick(model: ExpenseMainModel, position: Int) {
        if(model.statusId == 1 || model.statusId == 3) {
            val action = ExpenseListFragmentDirections.actionExpenseListFragmentToAddExpenseFragment(model)
            findNavController().navigate(action)
        }
    }

    private fun showDescriptionDetailDialog(model: ExpenseMainModel) {
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
            expenseListViewModel.getExpensesFromStatus(selectedStatusTypes)
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
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