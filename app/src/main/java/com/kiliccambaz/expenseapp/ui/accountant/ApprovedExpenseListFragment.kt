package com.kiliccambaz.expenseapp.ui.accountant

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kiliccambaz.expenseapp.data.ExpenseModel
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


        approvedExpenseListViewModel = ViewModelProvider(this)[ApprovedExpenseListViewModel::class.java]
        approvedExpenseListAdapter = ApprovedExpenseListAdapter(this)
        binding.rvApprovedExpenseList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvApprovedExpenseList.adapter = approvedExpenseListAdapter

        approvedExpenseListViewModel.expenseList.observe(viewLifecycleOwner) { expenseList ->
            expenseList?.let {
                approvedExpenseListAdapter.updateList(expenseList)
            }
        }

        return binding.root
    }

    override fun onPayButtonClick(expenseModel: ExpenseModel) {
        expenseModel.statusId = 5
        approvedExpenseListViewModel.updateExpense(expenseModel)
    }

}