package com.kiliccambaz.expenseapp.ui.admin.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kiliccambaz.expenseapp.data.ExpenseHistory
import com.kiliccambaz.expenseapp.data.ExpenseUIModel
import com.kiliccambaz.expenseapp.data.ExpenseMainModel
import com.kiliccambaz.expenseapp.data.UserModel
import com.kiliccambaz.expenseapp.utils.DateTimeUtils
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class HistoryViewModel : ViewModel() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val histories = arrayListOf<ExpenseUIModel>()

        private val _historyList = MutableLiveData<List<ExpenseUIModel>>()
    val historyList : LiveData<List<ExpenseUIModel>> = _historyList

    private val _filteredList = MutableLiveData<List<ExpenseUIModel>>()
    val filteredList : LiveData<List<ExpenseUIModel>?> = _filteredList

    init {
        getExpenseList { list ->
            _historyList.value = list.sortedByDescending { DateTimeUtils.parseDate(it.date) }
        }
    }

    private fun getExpenseList(onExpenseListFetched: (List<ExpenseUIModel>)  -> Unit) {
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
                                    expenseHistoryUIModel.userId = expense.userId
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
            } catch (ex: Exception) {
                ErrorUtils.addErrorToDatabase(ex, UserManager.getUserId())
                FirebaseCrashlytics.getInstance().recordException(ex)
            }
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