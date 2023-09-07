package com.kiliccambaz.expenseapp.ui.employee.addexpense

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.Timestamp
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.databinding.FragmentAddExpenseBinding
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.ValidationHelper
import java.lang.Exception

class AddExpenseFragment : Fragment() {

    private lateinit var addExpenseViewModel: AddExpenseViewModel
    private var binding: FragmentAddExpenseBinding? = null
    private val expenseList = arrayListOf<ExpenseModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddExpenseBinding.inflate(layoutInflater)
        addExpenseViewModel = ViewModelProvider(this).get(AddExpenseViewModel::class.java)
        binding!!.btnAddExpense.setOnClickListener {
            val amount = binding!!.txtAmount.text.toString()
            if (amount.isEmpty()) {
                binding!!.txtAmountInputLayout.error = "Tutar alanı boş bırakılamaz"
            } else if(!ValidationHelper.isNumeric(amount)) {
                binding!!.txtAmountInputLayout.error = "Miktar alanı numeric olmalıdır"
            } else {
                binding!!.txtAmountInputLayout.error = null
                val description = binding!!.txtDescription.text.toString()
                if(description.isEmpty()) {
                    binding!!.txtDescriptionInputLayout.error = "Açıklama alanı boş bırakılamaz"
                } else {
                    binding!!.txtDescriptionInputLayout.error = null
                    expenseList.add(ExpenseModel(amount.toDouble(), Timestamp.now().toString(), description, "Benzin", "1"))
                    addExpenseViewModel.saveExpenses(expenseList)
                }
            }
        }
        return binding!!.root
    }

}