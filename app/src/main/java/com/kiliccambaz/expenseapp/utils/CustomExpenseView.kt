package com.kiliccambaz.expenseapp.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kiliccambaz.expenseapp.R
import java.lang.Exception

class CustomExpenseView(context: Context, private val listener: AmountChangeListener) : LinearLayout(context) {
    init {
        LayoutInflater.from(context).inflate(R.layout.layout_expense_entry, this, true)
        val expenseTypes = resources.getStringArray(R.array.expense_types)
        val arrayAdapterExpense = ArrayAdapter(context, R.layout.dropdown_item, expenseTypes)

        val autoCompleteExpenseType = findViewById<AutoCompleteTextView>(R.id.autoCompleteExpenseType)
        autoCompleteExpenseType.setAdapter(arrayAdapterExpense)
    }

    fun getAmount(): String {
        val amount = findViewById<TextInputEditText>(R.id.txtAmount)
        return amount.text.toString()
    }

    fun getDescription(): String {
        val description = findViewById<TextInputEditText>(R.id.txtDescription)
        return description.text.toString()
    }

    fun getExpenseType(): String {
        val expenseType = findViewById<AutoCompleteTextView>(R.id.autoCompleteExpenseType)
        return expenseType.text.toString()
    }

    fun setAmountTextWatcher() {
        val amount = findViewById<TextInputEditText>(R.id.txtAmount)
        amount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val amountTextInputLayout = findViewById<TextInputLayout>(R.id.txtAmountInputLayout)
                    val amount = s.toString().toDoubleOrNull() ?: 0.0
                    if(amount > 5000) {
                        amountTextInputLayout.error = context.getString(R.string.amount_price_validation)
                    } else {
                        amountTextInputLayout.error = null
                        listener.onAmountChanged(amount)
                    }
                } catch (ex: Exception) {
                    ErrorUtils.addErrorToDatabase(ex, UserManager.getUserId())
                    FirebaseCrashlytics.getInstance().recordException(ex)
                }

            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    fun setDescriptionTextWatcher() {
        val description = findViewById<TextInputEditText>(R.id.txtDescription)
        description.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val descriptionInputLayout = findViewById<TextInputLayout>(R.id.txtDescriptionInputLayout)
                descriptionInputLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    fun setExpenseTypeTextWatcher() {
        val expenseType = findViewById<AutoCompleteTextView>(R.id.autoCompleteExpenseType)
        expenseType.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val expenseTypeInputLayout = findViewById<TextInputLayout>(R.id.txtInputLayoutExpenseType)
                expenseTypeInputLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }


    fun isValid(): Boolean {
        val amountEditText = findViewById<TextInputEditText>(R.id.txtAmount)
        val descriptionEditText = findViewById<TextInputEditText>(R.id.txtDescription)
        val expenseTypeEditText = findViewById<AutoCompleteTextView>(R.id.autoCompleteExpenseType)

        val amount = amountEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val expenseType = expenseTypeEditText.text.toString()

        var isValid = true

        if (amount.isEmpty()) {
            val amountTextInputLayout = findViewById<TextInputLayout>(R.id.txtAmountInputLayout)
            amountTextInputLayout.error = "Amount cannot be empty"
            isValid = false
        }

        if (description.isEmpty()) {
            val descriptionTextInputLayout = findViewById<TextInputLayout>(R.id.txtDescriptionInputLayout)
            descriptionTextInputLayout.error = context.getString(R.string.description_validation)
            isValid = false
        }

        if (expenseType.isEmpty()) {
            val expenseTypeTextInputLayout = findViewById<TextInputLayout>(R.id.txtInputLayoutExpenseType)
            expenseTypeTextInputLayout.error = "Expense type cannot be empty"
            isValid = false
        }

        return isValid
    }

    interface AmountChangeListener {
        fun onAmountChanged(newAmount: Double)
    }
}
