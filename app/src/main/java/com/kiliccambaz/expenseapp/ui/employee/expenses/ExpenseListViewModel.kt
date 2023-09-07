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
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpenseListViewModel : ViewModel() {

    private val _expenseList = MutableLiveData<List<ExpenseModel>>()
    val expenseList : LiveData<List<ExpenseModel>> = _expenseList

    init {
        fetchExpenseListFromDatabase { list ->
            _expenseList.value = list
        }
    }

    private fun fetchExpenseListFromDatabase(onExpenseListFetched: (List<ExpenseModel>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
        val databaseReference = Firebase.database.reference.child("expenses")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val expenseList = mutableListOf<ExpenseModel>()

                    for (expenseSnapshot in dataSnapshot.children) {
                        val expense = expenseSnapshot.getValue(ExpenseModel::class.java)
                        expense?.let { expenseList.add(it) }
                    }

                    onExpenseListFetched(expenseList)
                } else {
                    onExpenseListFetched(emptyList())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                val errorMessage = "Firebase Database error: ${databaseError.message}"
                ErrorUtils.addErrorToDatabase(java.lang.Exception(errorMessage), "")
                FirebaseCrashlytics.getInstance().recordException(Exception(errorMessage))
                onExpenseListFetched(emptyList())
            }
        })
        }
    }

}