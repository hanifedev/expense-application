package com.kiliccambaz.expenseapp.utils

import java.text.SimpleDateFormat
import java.util.Calendar

object DateTimeUtils {
    fun getCurrentDateTimeAsString(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDateTimeString = dateFormat.format(calendar.time)
        return currentDateTimeString
    }
}