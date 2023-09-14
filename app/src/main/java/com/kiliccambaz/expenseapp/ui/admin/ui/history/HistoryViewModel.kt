package com.kiliccambaz.expenseapp.ui.admin.ui.history

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
import com.kiliccambaz.expenseapp.data.ExpenseHistory
import com.kiliccambaz.expenseapp.data.ExpenseHistoryUIModel
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.data.UserModel
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class HistoryViewModel : ViewModel() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val histories = arrayListOf<ExpenseHistoryUIModel>()

        private val _historyList = MutableLiveData<List<ExpenseHistoryUIModel>>()
    val historyList : LiveData<List<ExpenseHistoryUIModel>> = _historyList

    init {
        getExpenseHistoryList()
    }

    private fun getExpenseHistoryList() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val expensesRef = database.getReference("expenseHistory")

                expensesRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (childSnapshot in dataSnapshot.children) {
                            val expense = childSnapshot.getValue(ExpenseHistory::class.java)
                            if (expense != null) {
                                val expenseHistoryModel = ExpenseHistoryUIModel()
                                expenseHistoryModel.date = expense.date
                                expenseHistoryModel.statusId = expense.status
                                getExpenseDetail(expense.expenseId, expenseHistoryModel)
                            }
                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        databaseError.toException().printStackTrace()
                        ErrorUtils.addErrorToDatabase(databaseError.toException(), "")
                        FirebaseCrashlytics.getInstance().recordException(databaseError.toException())
                    }
                })

            }catch (ex: Exception) {
                ErrorUtils.addErrorToDatabase(ex, "")
                FirebaseCrashlytics.getInstance().recordException(ex)
            }
        }
    }

    private fun getExpenseDetail(expenseId: String, expenseHistoryUIModel: ExpenseHistoryUIModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val expensesRef = database.getReference("expenses").child(expenseId)

                expensesRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val expense = dataSnapshot.getValue(ExpenseModel::class.java)
                        if (expense != null) {
                            expenseHistoryUIModel.expenseType = expense.expenseType
                            expenseHistoryUIModel.amount = expense.amount
                            expenseHistoryUIModel.currencyType = expense.currencyType
                            expenseHistoryUIModel.description = expense.description
                            expenseHistoryUIModel.rejectedReason = expense.rejectedReason
                            expenseHistoryUIModel.user = getUserEmail(expense.userId)
                            histories.add(expenseHistoryUIModel)
                        }


                        _historyList.postValue(histories)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        databaseError.toException().printStackTrace()
                        ErrorUtils.addErrorToDatabase(databaseError.toException(), "")
                        FirebaseCrashlytics.getInstance().recordException(databaseError.toException())
                    }
                })
            } catch (ex: Exception) {
                ErrorUtils.addErrorToDatabase(ex, "")
                FirebaseCrashlytics.getInstance().recordException(ex)
            }
        }
    }

    private fun getUserEmail(userId: String): String {
        var userEmail = ""
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userRef = database.getReference("users").child(userId)
                userRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val user = dataSnapshot.getValue(UserModel::class.java)
                        user?.let { userModel ->
                            userEmail = userModel.email
                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        ErrorUtils.addErrorToDatabase(databaseError.toException(), "")
                        FirebaseCrashlytics.getInstance().recordException(databaseError.toException())
                    }
                })
            } catch (ex: Exception) {
                ErrorUtils.addErrorToDatabase(ex, "")
                FirebaseCrashlytics.getInstance().recordException(ex)
            }
        }
        return userEmail
    }

    fun getExpensesByStatus(statusTypes: List<Int>) {
        histories.clear()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val expensesRef = database.getReference("expenseHistory")

                expensesRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (childSnapshot in dataSnapshot.children) {
                            val expense = childSnapshot.getValue(ExpenseHistory::class.java)
                            if (expense != null && expense.status in statusTypes) {
                                val expenseHistoryModel = ExpenseHistoryUIModel()
                                expenseHistoryModel.date = expense.date
                                expenseHistoryModel.statusId = expense.status
                                getExpenseDetail(expense.expenseId, expenseHistoryModel)
                            }
                        }

                        if(histories.isEmpty()) {
                            _historyList.postValue(emptyList())
                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        databaseError.toException().printStackTrace()
                        ErrorUtils.addErrorToDatabase(databaseError.toException(), "")
                        FirebaseCrashlytics.getInstance().recordException(databaseError.toException())
                    }
                })

            }catch (ex: Exception) {
                ErrorUtils.addErrorToDatabase(ex, "")
                FirebaseCrashlytics.getInstance().recordException(ex)
            }
        }
    }

}