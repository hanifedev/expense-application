package com.kiliccambaz.expenseapp.ui.employee.addexpense

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseMainModel
import com.kiliccambaz.expenseapp.data.ExpenseUIModel
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.databinding.FragmentAddExpenseBinding
import com.kiliccambaz.expenseapp.ui.admin.ui.history.HistoryAdapter
import com.kiliccambaz.expenseapp.ui.admin.ui.history.HistoryAdapterClickListener
import com.kiliccambaz.expenseapp.utils.CustomExpenseDialog
import com.kiliccambaz.expenseapp.utils.DateTimeUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.exp

class AddExpenseFragment : Fragment(), HistoryAdapterClickListener {

    private lateinit var addExpenseViewModel: AddExpenseViewModel
    private lateinit var historyAdapter: HistoryAdapter
    private var binding: FragmentAddExpenseBinding? = null
    private var currencySymbol = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddExpenseBinding.inflate(layoutInflater)
        addExpenseViewModel = ViewModelProvider(this)[AddExpenseViewModel::class.java]

        val args: AddExpenseFragmentArgs by navArgs()
        val expenseModel = args.expenseModel

        expenseModel?.let {
            addExpenseViewModel.setMainExpense(it)
            binding!!.clAddExpense.visibility = View.GONE
            binding!!.tvExpenseDescription.visibility = View.VISIBLE
            binding!!.tvExpenseDescription.text = it.description
            binding!!.fabAddExpense.visibility = View.VISIBLE
            setCurrencySembol(it.currencyType)
            if(expenseModel.statusId == 3) {
                binding!!.cardRejectedStatus.visibility = View.VISIBLE
                binding!!.txtRejectedDescription.text = expenseModel.rejectedReason
            }
        }

        historyAdapter = HistoryAdapter(requireContext(), false, this)
        binding!!.rvExpenseDetail.layoutManager = LinearLayoutManager(requireContext())
        binding!!.rvExpenseDetail.adapter = historyAdapter



        val currencyTypes = resources.getStringArray(R.array.currency_types)
        val arrayAdapterCurrency = ArrayAdapter(requireContext(), R.layout.dropdown_item, currencyTypes)
        binding!!.autoCompleteCurrencyType.setAdapter(arrayAdapterCurrency)


        binding!!.autoCompleteCurrencyType.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                binding!!.txtInputLayoutCurrency.error = null
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setCurrencySembol(s.toString())
            }
        })

        binding!!.txtDescription.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                binding!!.txtDescriptionInputLayout.error = null
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setCurrencySembol(s.toString())
            }
        })

        binding!!.btnSaveExpense.setOnClickListener {
            val currencyType = binding!!.autoCompleteCurrencyType.text.toString()
            val description = binding!!.txtDescription.text.toString()
            val isValid = checkMainExpenseValidation(currencyType, description)
            if(isValid) {
                binding!!.txtDescription.clearFocus()
                addExpenseViewModel.createMainExpense(ExpenseMainModel(DateTimeUtils.getCurrentDateTimeAsString(), description, UserManager.getUserId(), currencyType, 1, ""))
                val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding!!.txtDescription.windowToken, 0)
            }
        }

        binding!!.fabAddExpense.setOnClickListener {
            val customDialog = CustomExpenseDialog(requireContext(), addExpenseViewModel)
            customDialog.show()
        }

        addExpenseViewModel.expenseList.observe(viewLifecycleOwner) { expenseList ->
            if(expenseList.isNotEmpty()) {
                historyAdapter.updateList(expenseList)
                convertDecimalFormat(expenseList)
            }
        }

        addExpenseViewModel.addExpenseResponse.observe(viewLifecycleOwner) { result ->
            result?.let {
                when(result) {
                    is Result.Success -> {
                        GlobalScope.launch(Dispatchers.Main) {
                            Toast.makeText(context, getString(R.string.expense_added_successfully), Toast.LENGTH_LONG).show()
                            binding!!.fabAddExpense.visibility = View.VISIBLE
                            binding!!.btnSaveExpense.isEnabled = false
                            //findNavController().popBackStack()
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

        addExpenseViewModel.updateExpenseResponse.observe(viewLifecycleOwner) { result ->
            result?.let {
                when(result) {
                    is Result.Success -> {
                        GlobalScope.launch(Dispatchers.Main) {
                            Toast.makeText(context, getString(R.string.expense_updated_successfully), Toast.LENGTH_LONG).show()
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

        addExpenseViewModel.hideDescription.observe(viewLifecycleOwner) { result ->
            if(result) {
                binding!!.cardRejectedStatus.visibility = View.GONE
            }
        }

        return binding!!.root
    }

    private fun setCurrencySembol(currencyType: String) {
        currencySymbol = when (currencyType.toString()) {
            "TL" -> "₺"
            "USD" -> "$"
            "EUR" -> "€"
            "PKR" -> "₨"
            "INR" -> "₹"
            else -> ""
        }
    }

    private fun checkMainExpenseValidation(currencyType: String, description: String): Boolean {
        if(currencyType.isNullOrEmpty()) {
            binding!!.txtInputLayoutCurrency.error = getString(R.string.currency_type_validation)
            return false
        } else {
            binding!!.txtDescriptionInputLayout.error = null
        }

        if(description.isNullOrEmpty()) {
            binding!!.txtDescriptionInputLayout.error = getString(R.string.description_validation)
            return false
        } else {
            binding!!.txtDescriptionInputLayout.error = null
        }

        return true
    }


    private fun convertDecimalFormat(expenseDetailList: List<ExpenseUIModel>) {
        val totalAmount = expenseDetailList.sumOf { it.amount }
        val decimalFormat = DecimalFormat("#,###.###")
        val formattedTotalAmount = decimalFormat.format(totalAmount)
        val totalAmountWithCurrency = "$currencySymbol $formattedTotalAmount"
        binding!!.totalAmountTextview.text = totalAmountWithCurrency
        binding!!.cardTotal.visibility = View.VISIBLE
    }

    override fun onRecyclerViewItemClick(model: ExpenseUIModel, position: Int) {
        val customExpenseDialog = CustomExpenseDialog(requireContext(), addExpenseViewModel)
        customExpenseDialog.setModel(model)
        customExpenseDialog.show()
    }

}