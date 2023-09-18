package com.kiliccambaz.expenseapp.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.kiliccambaz.expenseapp.data.ExpenseUIModel
import java.text.DecimalFormat

object BindingAdaptersUtil {

    @BindingAdapter("formattedCurrency")
    @JvmStatic
    fun setFormattedCurrency(textView: TextView, expenseModel: ExpenseUIModel) {

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
