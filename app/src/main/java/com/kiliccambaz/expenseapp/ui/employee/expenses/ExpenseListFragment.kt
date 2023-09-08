package com.kiliccambaz.expenseapp.ui.employee.expenses

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        expenseListViewModel = ViewModelProvider(this)[ExpenseListViewModel::class.java]

        expenseListAdapter = ExpenseListAdapter( this)
        binding!!.rvExpenseList.layoutManager = LinearLayoutManager(requireContext())
        binding!!.rvExpenseList.adapter = expenseListAdapter

        expenseListViewModel.expenseList.observe(viewLifecycleOwner) { expenseList ->
            expenseList?.let {
                expenseListAdapter.updateList(expenseList)
            }
        }

        binding!!.fabAddExpense.setOnClickListener {
            val action = ExpenseListFragmentDirections.actionExpenseListFragmentToAddExpenseFragment()
            findNavController().navigate(action)
        }
        return binding!!.root
    }

    override fun onRecyclerViewItemClick(model: ExpenseModel, position: Int) {

    }

    override fun onResume() {
        super.onResume()
        expenseListViewModel.fetchExpenseListFromDatabase()
    }

}