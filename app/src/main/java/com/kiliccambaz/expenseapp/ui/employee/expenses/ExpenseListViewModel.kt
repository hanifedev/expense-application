package com.kiliccambaz.expenseapp.ui.employee.expenses

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
import com.kiliccambaz.expenseapp.data.ExpenseMainModel
import com.kiliccambaz.expenseapp.utils.DateTimeUtils
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpenseListViewModel : ViewModel() {

    private val _expenseList = MutableLiveData<List<ExpenseMainModel>>()
    val expenseList : LiveData<List<ExpenseMainModel>> = _expenseList

    private val _filteredList = MutableLiveData<List<ExpenseMainModel>>()
    val filteredList : LiveData<List<ExpenseMainModel>?> = _filteredList

    fun fetchExpenseListFromDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val databaseReference = Firebase.database.reference.child("expenses")
            val query = databaseReference.orderByChild("userId").equalTo(UserManager.getUserId())

            query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val expenseList = mutableListOf<ExpenseMainModel>()

                    for (expenseSnapshot in dataSnapshot.children) {
                        val expenseId = expenseSnapshot.key
                        val expense = expenseSnapshot.getValue(ExpenseMainModel::class.java)
                        expense?.let {
                            if (expenseId != null) {
                                expense.expenseId = expenseId
                            }
                            expenseList.add(it)
                        }
                    }

                    _expenseList.value = expenseList.sortedByDescending { DateTimeUtils.parseDate(it.date) }
                } else {
                    _expenseList.value = emptyList()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                val errorMessage = "Firebase Database error: ${databaseError.message}"
                ErrorUtils.addErrorToDatabase(java.lang.Exception(errorMessage), "")
                FirebaseCrashlytics.getInstance().recordException(Exception(errorMessage))
                _expenseList.value = emptyList()
            }
        })
        }
    }
    fun getExpensesFromStatus(selectedStatusTypes: MutableList<Int>) {
         val filteredExpenses = _expenseList.value?.filter { selectedStatusTypes.contains(it.statusId) }
        if(filteredExpenses?.isEmpty() == true) {
            _filteredList.value = emptyList()
        } else {
            _filteredList.value = filteredExpenses!!
        }
    }

}