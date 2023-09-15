package com.kiliccambaz.expenseapp.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.kiliccambaz.expenseapp.data.ExpenseHistoryUIModel
import com.kiliccambaz.expenseapp.data.ExpenseModel
import java.text.DecimalFormat

object BindingAdaptersUtil {
    @BindingAdapter("formattedCurrency")
    @JvmStatic
    fun setFormattedCurrency(textView: TextView, expenseModel: ExpenseModel) {

        val currencySymbol = when (expenseModel.currencyType) {
            "TL" -> "₺"
            "USD" -> "$"
            "EUR" -> "€"
            "PKR" -> "₨"
            "INR" -> "₹"
            else -> ""
        }
        val formattedAmount = DecimalFormat("#,###.##").format(expenseModel.amount)
        textView.text = "$currencySymbol $formattedAmount"
    }

    @BindingAdapter("formattedCurrency")
    @JvmStatic
    fun setFormattedCurrency(textView: TextView, expenseModel: ExpenseHistoryUIModel) {

        val currencySymbol = when (expenseModel.currencyType) {
            "TL" -> "₺"
            "USD" -> "$"
            "EUR" -> "€"
            "PKR" -> "₨"
            "INR" -> "₹"
            else -> ""
        }
        val formattedAmount = DecimalFormat("#,###.##").format(expenseModel.amount)
        textView.text = "$currencySymbol $formattedAmount"
    }
}
