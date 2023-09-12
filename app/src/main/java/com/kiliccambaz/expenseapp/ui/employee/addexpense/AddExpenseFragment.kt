package com.kiliccambaz.expenseapp.ui.employee.addexpense

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.databinding.FragmentAddExpenseBinding
import com.kiliccambaz.expenseapp.utils.DateTimeUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.text.DecimalFormat

class AddExpenseFragment : Fragment() {

    private lateinit var addExpenseViewModel: AddExpenseViewModel
    private var binding: FragmentAddExpenseBinding? = null
    private val expenseList = arrayListOf<ExpenseModel>()
    private var currencySymbol = ""

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

        binding!!.autoCompleteCurrencyType.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currencySymbol = when (s.toString()) {
                    "TL" -> "₺"
                    "USD" -> "$"
                    "EUR" -> "€"
                    "PKR" -> "₨"
                    "INR" -> "₹"
                    else -> ""
                }
                val totalAmount = binding!!.txtAmount.text
                if (totalAmount != null) {
                    if(totalAmount.isNotEmpty())  {
                        convertDecimalFormat(totalAmount.toString().toDouble())

                    }
                }
            }
        })

        binding!!.txtAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hasText = s?.isNotEmpty() == true

                if (hasText) {
                    if(s.toString().toDouble() < 5000) {
                    binding!!.txtAmountInputLayout.error = null

                    binding!!.cardTotal.visibility = View.VISIBLE
                    val totalAmount = s.toString()
                    val selectedCurrency = binding!!.autoCompleteCurrencyType.text.toString()

                    currencySymbol = when (selectedCurrency) {
                        "TL" -> "₺"
                        "USD" -> "$"
                        "EUR" -> "€"
                        "PKR" -> "₨"
                        "INR" -> "₹"
                        else -> ""
                    }

                    convertDecimalFormat(totalAmount.toDouble())
                    } else {
                        binding!!.txtAmountInputLayout.error = "Amount must be greater than 5000"
                    }
                } else {
                    binding!!.cardTotal.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


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
                    } else if(amount.toDouble() > 5000) {
                        binding!!.txtAmountInputLayout.error = "Amount must be greater than 5000"
                    }
                    else {
                        binding!!.txtAmountInputLayout.error = null
                        val description = binding!!.txtDescription.text.toString()
                        if(description.isEmpty()) {
                            binding!!.txtDescriptionInputLayout.error = "Açıklama alanı boş bırakılamaz"
                        } else {
                            binding!!.txtDescriptionInputLayout.error = null
                            expenseList.add(ExpenseModel(amount = amount.toDouble(), date  = DateTimeUtils.getCurrentDateTimeAsString(), description = description, expenseType = expenseType.toString(), userId = UserManager.getUserId() ?: "", currencyType =  currencyType.toString()))
                            binding!!.totalAmountTextview.text = expenseList.sumOf { it.amount }.toString()
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

    private fun convertDecimalFormat(amount: Double) {
        val decimalFormat = DecimalFormat("#,###.###")
        val formattedTotalAmount = decimalFormat.format(amount)

        val totalAmountWithCurrency = "$currencySymbol $formattedTotalAmount"
        binding!!.totalAmountTextview.text = totalAmountWithCurrency

    }

    private fun addExpenseInput() {
        val layoutResId = R.layout.layout_expense_entry

        val inflater = LayoutInflater.from(context)
        val inflatedLayout = inflater.inflate(layoutResId, null)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        

        binding!!.linearLayout.addView(inflatedLayout, layoutParams)
    }

}