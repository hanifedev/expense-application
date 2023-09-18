package com.kiliccambaz.expenseapp.ui.admin.ui.expenses

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
import com.kiliccambaz.expenseapp.data.ExpenseUIModel
import com.kiliccambaz.expenseapp.data.ExpenseMainModel
import com.kiliccambaz.expenseapp.data.UserModel
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpensesViewModel : ViewModel() {

    private val _expenseList = MutableLiveData<List<ExpenseUIModel>>()
    val expenseList : LiveData<List<ExpenseUIModel>> = _expenseList

    private val _filteredList = MutableLiveData<List<ExpenseUIModel>>()
    val filteredList : LiveData<List<ExpenseUIModel>?> = _filteredList

    init {
        fetchExpenseListFromDatabase { list ->
            _expenseList.value = list
        }
    }


    private fun fetchExpenseListFromDatabase(onExpenseListFetched: (List<ExpenseUIModel>)  -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val databaseReference = Firebase.database.reference.child("expenses")
            val histories = arrayListOf<ExpenseUIModel>()

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {

                        for (expenseSnapshot in dataSnapshot.children) {
                            val expenseId = expenseSnapshot.key
                            val expense = expenseSnapshot.getValue(ExpenseMainModel::class.java)
                            expense?.let {
                                if (expenseId != null) {
                                    expense.expenseId = expenseId
                                }
                                setUsername(expense.userId) { username ->
                                    val expenseHistoryUIModel = ExpenseUIModel()
                                    expenseHistoryUIModel.currencyType = expense.currencyType
                                    expenseHistoryUIModel.description = expense.description
                                    expenseHistoryUIModel.rejectedReason = expense.rejectedReason
                                    expenseHistoryUIModel.statusId = expense.statusId
                                    expenseHistoryUIModel.date = expense.date
                                    expenseHistoryUIModel.expenseId = expense.expenseId
                                    expenseHistoryUIModel.user = username
                                    histories.add(expenseHistoryUIModel)
                                    onExpenseListFetched(histories)
                                }
                            }
                        }

                        onExpenseListFetched(histories)

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

    private fun setUsername(userId: String, callback: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userRef = Firebase.database.getReference("users").child(userId)
                userRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val user = dataSnapshot.getValue(UserModel::class.java)
                        user?.let { userModel ->
                            callback(userModel.username)
                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        ErrorUtils.addErrorToDatabase(databaseError.toException(), UserManager.getUserId())
                        FirebaseCrashlytics.getInstance().recordException(databaseError.toException())
                    }
                })
            } catch (ex: java.lang.Exception) {
                ErrorUtils.addErrorToDatabase(ex, UserManager.getUserId())
                FirebaseCrashlytics.getInstance().recordException(ex)
            }
        }
    }

    fun getExpensesFromStatus(statusTypes: List<Int>) {
        val filteredExpenses = _expenseList.value?.filter { statusTypes.contains(it.statusId) }
        if(filteredExpenses?.isEmpty() == true) {
            _filteredList.value = emptyList()
        } else {
            _filteredList.value = filteredExpenses!!
        }
    }



}