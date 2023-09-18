package com.kiliccambaz.expenseapp.utils

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.ExpenseDetailModel
import com.kiliccambaz.expenseapp.data.ExpenseUIModel
import com.kiliccambaz.expenseapp.ui.employee.addexpense.AddExpenseViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class CustomExpenseDialog(context: Context, addExpenseViewModel: AddExpenseViewModel) {
    private val dialogView: View = LayoutInflater.from(context).inflate(R.layout.layout_expense_entry, null)
    private var alertDialog: AlertDialog? = null
    private val dateInputLayout: TextInputLayout = dialogView.findViewById(R.id.txtDateInputLayout)
    private val expenseTypeInputLayout: TextInputLayout = dialogView.findViewById(R.id.txtInputLayoutExpenseType)
    private val amountInputLayout: TextInputLayout = dialogView.findViewById(R.id.txtAmountInputLayout)

    private val dateEditText: TextInputEditText = dialogView.findViewById(R.id.txtDate)
    private val expenseTypeEditText: AutoCompleteTextView = dialogView.findViewById(R.id.autoCompleteExpenseType)
    private val amountEditText: TextInputEditText = dialogView.findViewById(R.id.txtAmount)

    private var expenseDetailId = ""

    init {
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.save_button_text), null)
            .setNegativeButton(context.getString(R.string.cancel)) { _, _ ->
                clearValidations()
                alertDialog?.dismiss()
            }
            .create()

        alertDialog?.setOnShowListener {
            val saveButton: Button = alertDialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val isValid = validateInput(context)
                if (isValid) {
                    addExpenseViewModel.saveExpenseDetail(ExpenseDetailModel(expenseDate = dateEditText.text.toString(), expenseType = expenseTypeEditText.text.toString(), amount = amountEditText.text.toString().toDouble()), expenseDetailId)
                    alertDialog?.dismiss()
                    clearValidations()
                }
            }
        }

        val expenseTypes = context.resources.getStringArray(R.array.expense_types)
        val arrayAdapterCurrency = ArrayAdapter(context, R.layout.dropdown_item, expenseTypes)
        expenseTypeEditText.setAdapter(arrayAdapterCurrency)

        dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                    dateEditText.setText(selectedDate)
                }, year, month, dayOfMonth)

            datePickerDialog.show()
        }
    }


    fun show() {
        alertDialog?.show()
    }

    private fun clearValidations() {
        dateEditText.text?.clear()
        expenseTypeEditText.text.clear()
        amountEditText.text?.clear()
        dateInputLayout.error = null
        expenseTypeInputLayout.error = null
        amountInputLayout.error = null
    }

    private fun isValidDate(date: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateFormat.isLenient = false
        return try {
            dateFormat.parse(date)
            true
        } catch (e: ParseException) {
            false
        }
    }

    private fun validateInput(context: Context): Boolean {
        val date = dateEditText.text.toString()
        val expenseType = expenseTypeEditText.text.toString()
        val amount = amountEditText.text.toString()

        var isValid = true

        if (date.isEmpty()) {
            dateInputLayout.error = context.getString(R.string.date_validation)
            isValid = false
        } else if(!isValidDate(date)) {
            dateInputLayout.error = context.getString(R.string.date_format_validation)
            isValid = false
        } else {
            dateInputLayout.error = null
        }

        if (expenseType.isEmpty()) {
            expenseTypeInputLayout.error = context.getString(R.string.expense_type_validation)
            isValid = false
        } else {
            expenseTypeInputLayout.error = null
        }

        if (amount.isEmpty()) {
            amountInputLayout.error = context.getString(R.string.amount_validation)
            isValid = false
        } else if(amount.toDouble() > 5000) {
            amountInputLayout.error = context.getString(R.string.amount_price_validation)
            isValid = false
        } else {
            amountInputLayout.error = null
        }

        return isValid
    }

    fun setModel(model: ExpenseUIModel) {
        expenseDetailId = model.expenseDetailId
        dateEditText.text = model.date.toEditable()
        amountEditText.text = model.amount.toString().toEditable()
        expenseTypeEditText.text = model.expenseType.toEditable()
    }

    fun String.toEditable(): Editable {
        return SpannableStringBuilder(this)
    }
}
