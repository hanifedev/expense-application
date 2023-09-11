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
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpenseListViewModel : ViewModel() {

    private val _expenseList = MutableLiveData<List<ExpenseModel>>()
    val expenseList : LiveData<List<ExpenseModel>> = _expenseList

    fun fetchExpenseListFromDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
        val databaseReference = Firebase.database.reference.child("expenses")
            val query = databaseReference.orderByChild("userId").equalTo("-Ndj8cP7h0zzHXNRT1g2")

            query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val expenseList = mutableListOf<ExpenseModel>()

                    for (expenseSnapshot in dataSnapshot.children) {
                        val expenseId = expenseSnapshot.key
                        val expense = expenseSnapshot.getValue(ExpenseModel::class.java)
                        expense?.let {
                            if (expenseId != null) {
                                expense.expenseId = expenseId
                            }
                            expenseList.add(it)
                        }
                    }

                    _expenseList.value = expenseList
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

}