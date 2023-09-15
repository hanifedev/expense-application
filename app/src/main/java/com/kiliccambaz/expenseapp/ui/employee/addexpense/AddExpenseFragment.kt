package com.kiliccambaz.expenseapp.ui.employee.addexpense

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.AttributeSet
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
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.databinding.FragmentAddExpenseBinding
import com.kiliccambaz.expenseapp.utils.CustomExpenseView
import com.kiliccambaz.expenseapp.utils.DateTimeUtils
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.lang.Exception
import java.text.DecimalFormat
import kotlin.math.exp

class AddExpenseFragment : Fragment(), CustomExpenseView.AmountChangeListener {

    private lateinit var addExpenseViewModel: AddExpenseViewModel
    private var binding: FragmentAddExpenseBinding? = null
    private val expenseList = arrayListOf<ExpenseModel>()
    private var currencySymbol = ""
    private val customExpenseViews = mutableListOf<CustomExpenseView>()
    private var firstAmount = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddExpenseBinding.inflate(layoutInflater)
        addExpenseViewModel = ViewModelProvider(this)[AddExpenseViewModel::class.java]

        val args: AddExpenseFragmentArgs by navArgs()
        val expenseModel = args.expenseModel

        expenseModel?.let {
            binding!!.txtAmount.text = expenseModel.amount.toString().toEditable()
            binding!!.txtDescription.text = expenseModel.description.toEditable()
            binding!!.autoCompleteExpenseType.text = expenseModel.expenseType.toEditable()
            binding!!.autoCompleteCurrencyType.text = expenseModel.currencyType.toEditable()
        }

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
                try {
                    val hasText = s?.isNotEmpty() == true
                    firstAmount = s.toString().toDouble()

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
                            binding!!.txtAmountInputLayout.error = getString(R.string.amount_price_validation)
                        }
                    } else {
                        binding!!.cardTotal.visibility = View.INVISIBLE
                    }
                } catch (ex: Exception) {
                    ErrorUtils.addErrorToDatabase(ex, UserManager.getUserId())
                    FirebaseCrashlytics.getInstance().recordException(ex)
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


        binding!!.btnSaveExpense.setOnClickListener {
            val expenseType = binding!!.autoCompleteExpenseType.text
            if(expenseType.isEmpty()) {
                binding!!.txtInputLayoutExpenseType.error = getString(R.string.expense_type_validation)
            } else {
                binding!!.txtInputLayoutExpenseType.error = null
                val currencyType = binding!!.autoCompleteCurrencyType.text
                if(currencyType.isEmpty()) {
                    binding!!.txtInputLayoutCurrency.error = getString(R.string.currency_type_validation)
                } else {
                    binding!!.txtInputLayoutCurrency.error = null
                    val amount = binding!!.txtAmount.text.toString()
                    if (amount.isEmpty()) {
                        binding!!.txtAmountInputLayout.error = getString(R.string.amount_validation)
                    } else if(amount.toDouble() > 5000) {
                        binding!!.txtAmountInputLayout.error = getString(R.string.amount_price_validation)
                    }
                    else {
                        binding!!.txtAmountInputLayout.error = null
                        val description = binding!!.txtDescription.text.toString()
                        if(description.isEmpty()) {
                            binding!!.txtDescriptionInputLayout.error = getString(R.string.description_validation)
                        } else {
                            binding!!.txtDescriptionInputLayout.error = null
                            expenseList.add(ExpenseModel(expenseId = expenseModel?.expenseId ?: "", amount = amount.toDouble(), date  = DateTimeUtils.getCurrentDateTimeAsString(), description = description, expenseType = expenseType.toString(), userId = UserManager.getUserId() ?: "", currencyType =  currencyType.toString()))
                            if(customExpenseViews.isEmpty()) {
                                addExpenseViewModel.saveExpenses(expenseList)
                            } else {
                                var allInputsValid = true

                                for (customExpenseView in customExpenseViews) {
                                    if (!customExpenseView.isValid()) {
                                        allInputsValid = false
                                    }
                                }

                                if (allInputsValid) {
                                    for (customExpenseView in customExpenseViews) {
                                        val amount = customExpenseView.getAmount()
                                        val description = customExpenseView.getDescription()
                                        val expenseType = customExpenseView.getExpenseType()
                                        expenseList.add(ExpenseModel(amount = amount.toDouble(), date  = DateTimeUtils.getCurrentDateTimeAsString(), description = description, expenseType = expenseType, userId = UserManager.getUserId() ?: "", currencyType =  currencyType.toString()))
                                    }
                                    addExpenseViewModel.saveExpenses(expenseList)
                                }
                            }
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
                            Toast.makeText(context, getString(R.string.expense_type_added_successfully), Toast.LENGTH_LONG).show()
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
        val totalAmount = customExpenseViews.sumOf { it.getAmount().toDouble() } + firstAmount
        val decimalFormat = DecimalFormat("#,###.###")
        val formattedTotalAmount = decimalFormat.format(totalAmount)
        val totalAmountWithCurrency = "$currencySymbol $formattedTotalAmount"
        binding!!.totalAmountTextview.text = totalAmountWithCurrency

    }

    private fun addExpenseInput() {
        val customExpenseView = CustomExpenseView(requireContext(), this)
        binding!!.linearLayout.addView(customExpenseView)
        customExpenseView.setAmountTextWatcher()
        customExpenseView.setDescriptionTextWatcher()
        customExpenseView.setExpenseTypeTextWatcher()
        customExpenseViews.add(customExpenseView)
    }

    override fun onAmountChanged(newAmount: Double) {
        updateTotalAmount(newAmount)
    }

    private fun updateTotalAmount(newAmount: Double) {
        convertDecimalFormat(newAmount)
    }

    fun String.toEditable(): Editable {
        return SpannableStringBuilder(this)
    }

}