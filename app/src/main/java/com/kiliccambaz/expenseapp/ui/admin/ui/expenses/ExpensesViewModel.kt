package com.kiliccambaz.expenseapp.ui.admin.ui.expenses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kiliccambaz.expenseapp.data.ExpenseHistory
import com.kiliccambaz.expenseapp.data.ExpenseHistoryUIModel
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpensesViewModel : ViewModel() {

    private val _expenseList = MutableLiveData<List<ExpenseModel>>()
    val expenseList : LiveData<List<ExpenseModel>> = _expenseList

    fun fetchExpenseListFromDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val databaseReference = Firebase.database.reference.child("expenses")

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
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

    fun getExpensesFromStatus(statusTypes: List<Int>, onExpensesFetched: (List<ExpenseModel>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("expenses")
                val query = databaseReference .orderByChild("userId")
                    .equalTo("${UserManager.getUserId()}")

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val expenses = mutableListOf<ExpenseModel>()

                        for (expenseSnapshot in dataSnapshot.children) {
                            val expense = expenseSnapshot.getValue(ExpenseModel::class.java)
                            expense?.let {
                                if (it.statusId in statusTypes) {
                                    expenses.add(it)
                                }
                            }
                        }

                        onExpensesFetched(expenses)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        databaseError.toException().printStackTrace()
                        ErrorUtils.addErrorToDatabase(databaseError.toException(), databaseError.toException().message.toString())
                        FirebaseCrashlytics.getInstance().recordException(databaseError.toException())
                        onExpensesFetched(emptyList())
                    }
                })
            } catch (ex: java.lang.Exception) {
                ErrorUtils.addErrorToDatabase(ex, ex.message.toString())
                FirebaseCrashlytics.getInstance().recordException(ex)
            }
        }
    }

}