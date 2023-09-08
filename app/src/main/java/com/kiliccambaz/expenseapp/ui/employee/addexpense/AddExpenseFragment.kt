package com.kiliccambaz.expenseapp.ui.employee.addexpense

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.databinding.FragmentAddExpenseBinding
import com.kiliccambaz.expenseapp.utils.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class AddExpenseFragment : Fragment() {

    private lateinit var addExpenseViewModel: AddExpenseViewModel
    private var binding: FragmentAddExpenseBinding? = null
    private val expenseList = arrayListOf<ExpenseModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddExpenseBinding.inflate(layoutInflater)
        addExpenseViewModel = ViewModelProvider(this)[AddExpenseViewModel::class.java]

        val expenseTypes = resources.getStringArray(R.array.expense_types)
        val arrayAdapterExpense = ArrayAdapter(requireContext(), R.layout.dropdown_item, expenseTypes)
        binding!!.autoCompleteExpenseType.setAdapter(arrayAdapterExpense)

        val currencyTypes = resources.getStringArray(R.array.currency_types)
        val arrayAdapterCurrency = ArrayAdapter(requireContext(), R.layout.dropdown_item, currencyTypes)
        binding!!.autoCompleteCurrencyType.setAdapter(arrayAdapterCurrency)


        binding!!.btnSaveExpense.setOnClickListener {
            val expenseType = binding!!.autoCompleteExpenseType.text
            if(expenseType.isEmpty()) {
                binding!!.txtInputLayoutExpenseType.error = "Masraf tipi boş bırakılamaz"
            } else {
                binding!!.txtInputLayoutExpenseType.error = null
                val currencyType = binding!!.autoCompleteCurrencyType.text
                if(currencyType.isEmpty()) {
                    binding!!.txtInputLayoutCurrency.error = "Para birimi boş bırakılamaz"
                } else {
                    binding!!.txtInputLayoutCurrency.error = null
                    val amount = binding!!.txtAmount.text.toString()
                    if (amount.isEmpty()) {
                        binding!!.txtAmountInputLayout.error = "Tutar alanı boş bırakılamaz"
                    } else {
                        binding!!.txtAmountInputLayout.error = null
                        val description = binding!!.txtDescription.text.toString()
                        if(description.isEmpty()) {
                            binding!!.txtDescriptionInputLayout.error = "Açıklama alanı boş bırakılamaz"
                        } else {
                            binding!!.txtDescriptionInputLayout.error = null
                            expenseList.add(ExpenseModel(amount = amount.toDouble(), date  = DateTimeUtils.getCurrentDateTimeAsString(), description = description, expenseType = expenseType.toString(), userId = "-Ndj8cP7h0zzHXNRT1g2", currencyType =  currencyType.toString()))
                            addExpenseViewModel.saveExpenses(expenseList)
                        }
                    }
                }
            }
        }

        binding!!.btnAddExpense.setOnClickListener {
            addExpenseInput()
        }

        addExpenseViewModel.addExpenseResponse.observe(viewLifecycleOwner) { result ->
            result?.let {
                when(result) {
                    is Result.Success -> {
                        GlobalScope.launch(Dispatchers.Main) {
                            Toast.makeText(context, "Masraf başarıyla eklendi", Toast.LENGTH_LONG).show()
                            findNavController().popBackStack()
                        }
                    }
                    is Result.Error -> {
                        GlobalScope.launch(Dispatchers.Main) {
                            val errorMessage = result.message
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        return binding!!.root
    }

    private fun addExpenseInput() {
        val newDescriptionInputLayout = TextInputLayout(requireContext())
        val newAmountInputLayout = TextInputLayout(requireContext())
        val newExpenseTypeInputLayout = TextInputLayout(requireContext())

        newDescriptionInputLayout.id = View.generateViewId()
        newAmountInputLayout.id = View.generateViewId()
        newExpenseTypeInputLayout.id = View.generateViewId()

        newDescriptionInputLayout.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        newAmountInputLayout.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        newExpenseTypeInputLayout.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        newDescriptionInputLayout.hint = getString(R.string.description)
        newAmountInputLayout.hint = getString(R.string.amount)
        newExpenseTypeInputLayout.hint = getString(R.string.expense_type)

        binding!!.container.addView(newDescriptionInputLayout)
        binding!!.container.addView(newAmountInputLayout)
        binding!!.container.addView(newExpenseTypeInputLayout)

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding!!.container)

        constraintSet.connect(
            newDescriptionInputLayout.id,
            ConstraintSet.TOP,
            binding!!.btnAddExpense.id,
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(R.dimen.margin_16dp)
        )

        constraintSet.connect(newDescriptionInputLayout.id, ConstraintSet.START, newDescriptionInputLayout.id, ConstraintSet.START)
        constraintSet.connect(newDescriptionInputLayout.id, ConstraintSet.END, newDescriptionInputLayout.id, ConstraintSet.END)


        constraintSet.connect(
            newAmountInputLayout.id,
            ConstraintSet.TOP,
            newDescriptionInputLayout.id,
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(R.dimen.margin_16dp)
        )

        constraintSet.connect(
            newExpenseTypeInputLayout.id,
            ConstraintSet.TOP,
            newAmountInputLayout.id,
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(R.dimen.margin_16dp)
        )

        constraintSet.applyTo(binding!!.container)
    }

}