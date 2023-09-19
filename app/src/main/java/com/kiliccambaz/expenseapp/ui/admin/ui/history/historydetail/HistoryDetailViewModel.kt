package com.kiliccambaz.expenseapp.ui.admin.ui.history.historydetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kiliccambaz.expenseapp.data.ExpenseHistory
import com.kiliccambaz.expenseapp.data.ExpenseHistoryUIModel
import com.kiliccambaz.expenseapp.data.ExpenseMainModel
import com.kiliccambaz.expenseapp.utils.DateTimeUtils
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HistoryDetailViewModel : ViewModel() {

    private val histories = arrayListOf<ExpenseHistoryUIModel>()

    private val _historyList = MutableLiveData<List<ExpenseHistoryUIModel>>()
    val historyList : LiveData<List<ExpenseHistoryUIModel>> = _historyList

    private val _filteredList = MutableLiveData<List<ExpenseHistoryUIModel>>()
    val filteredList : LiveData<List<ExpenseHistoryUIModel>?> = _filteredList

    fun getExpenseHistoryForExpenseId(expenseIdToFind: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val expenseHistoryRef = Firebase.database.getReference("expenseHistory")

                expenseHistoryRef.orderByChild("expenseId").equalTo(expenseIdToFind)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val histories = mutableListOf<ExpenseHistoryUIModel>()

                            for (historySnapshot in dataSnapshot.children) {
                                val history = historySnapshot.getValue(ExpenseHistory::class.java)
                                history?.let {
                                    val expenseDetail = ExpenseHistoryUIModel()
                                    expenseDetail.date = history.date
                                    viewModelScope.launch {
                                        expenseDetail.description = getExpenseDescription(expenseIdToFind) ?: ""
                                    }

                                    expenseDetail.statusId = history.status
                                    histories.add(expenseDetail)
                                }
                            }

                            _historyList.postValue(histories.sortedByDescending { DateTimeUtils.parseDate(it.date) })
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            ErrorUtils.addErrorToDatabase(databaseError.toException(), UserManager.getUserId())
                            FirebaseCrashlytics.getInstance().recordException(databaseError.toException())
                        }
                    })
            } catch (ex: Exception) {
                ErrorUtils.addErrorToDatabase(ex, UserManager.getUserId())
                FirebaseCrashlytics.getInstance().recordException(ex)
            }
        }
    }

    private suspend fun getExpenseDescription(expenseId: String): String? {
        return suspendCoroutine { continuation ->
            val expensesRef = Firebase.database.getReference("expenses")
            expensesRef.child(expenseId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val expense = dataSnapshot.getValue(ExpenseMainModel::class.java)
                    val description = expense?.description
                    continuation.resume(description)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    continuation.resume(null)
                }
            })
        }
    }
    fun getExpensesFromStatus(statusTypes: List<Int>) {
        val filteredExpenses = _historyList.value?.filter { statusTypes.contains(it.statusId) }
        if(filteredExpenses?.isEmpty() == true) {
            _filteredList.value = emptyList()
        } else {
            _filteredList.value = filteredExpenses!!
        }
    }
}