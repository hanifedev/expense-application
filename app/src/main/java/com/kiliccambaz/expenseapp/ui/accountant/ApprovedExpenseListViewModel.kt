package com.kiliccambaz.expenseapp.ui.accountant

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
import com.kiliccambaz.expenseapp.data.ExpenseHistoryUIModel
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.data.UserModel
import com.kiliccambaz.expenseapp.utils.DateTimeUtils
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApprovedExpenseListViewModel: ViewModel() {

    private val _expenseList = MutableLiveData<List<ExpenseHistoryUIModel>>()
    val expenseList : LiveData<List<ExpenseHistoryUIModel>> = _expenseList

    private val _filteredList = MutableLiveData<List<ExpenseHistoryUIModel>>()
    val filteredList : LiveData<List<ExpenseHistoryUIModel>?> = _filteredList

    init {
        fetchWaitingExpenseList { list ->
            _expenseList.value = list
        }
    }

    private fun fetchWaitingExpenseList(onExpenseListFetched: (List<ExpenseHistoryUIModel>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val databaseReference = Firebase.database.reference

                            val expensesQuery = databaseReference.child("expenses")
                                .orderByChild("statusId")
                                .equalTo(2.0)

                            expensesQuery.addValueEventListener(object :
                                ValueEventListener {
                                override fun onDataChange(expensesDataSnapshot: DataSnapshot) {
                                    val expenseList = mutableListOf<ExpenseHistoryUIModel>()

                                    for (expenseSnapshot in expensesDataSnapshot.children) {
                                        val expense = expenseSnapshot.getValue(ExpenseModel::class.java)
                                        expense?.let {
                                            setUsername(expense.userId) { username ->
                                                val expenseHistoryUIModel = ExpenseHistoryUIModel()
                                                expenseHistoryUIModel.expenseType = expense.expenseType
                                                expenseHistoryUIModel.amount = expense.amount
                                                expenseHistoryUIModel.currencyType = expense.currencyType
                                                expenseHistoryUIModel.description = expense.description
                                                expenseHistoryUIModel.rejectedReason = expense.rejectedReason
                                                expenseHistoryUIModel.statusId = expense.statusId
                                                expenseHistoryUIModel.date = expense.date
                                                expenseHistoryUIModel.expenseId = expense.expenseId
                                                expenseHistoryUIModel.userId = expense.userId
                                                expenseHistoryUIModel.user = username
                                                expenseList.add(expenseHistoryUIModel)
                                                onExpenseListFetched(expenseList)
                                            }
                                        }
                                    }

                                    onExpenseListFetched(expenseList)
                                }

                                override fun onCancelled(expensesDatabaseError: DatabaseError) {
                                    val errorMessage = "Firebase Realtime Database error: ${expensesDatabaseError.message}"
                                    ErrorUtils.addErrorToDatabase(java.lang.Exception(errorMessage), "")
                                    FirebaseCrashlytics.getInstance().recordException(java.lang.Exception(errorMessage))
                                    onExpenseListFetched(emptyList())
                                }
                            })
            } catch (ex: java.lang.Exception) {
                ErrorUtils.addErrorToDatabase(ex, ex.message.toString())
                FirebaseCrashlytics.getInstance().recordException(ex)
            }
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

    fun updateExpense(expenseHistoryModel: ExpenseHistoryUIModel) {
        val expensesRef: DatabaseReference = Firebase.database.getReference("expenses")

        try {
            val expenseModel = ExpenseModel(expenseHistoryModel.amount, expenseHistoryModel.date, expenseHistoryModel.description, expenseHistoryModel.expenseType, expenseHistoryModel.userId, expenseHistoryModel.currencyType, expenseHistoryModel.statusId, expenseHistoryModel.rejectedReason, expenseHistoryModel.expenseId)
            val updateData = expenseModel.toMap()

            expensesRef.child(expenseModel.expenseId).updateChildren(updateData)
                .addOnSuccessListener {
                    fetchWaitingExpenseList { list ->
                        _expenseList.value = list
                    }
                    saveExpenseHistory(expenseModel)
                }
                .addOnFailureListener { e ->
                    ErrorUtils.addErrorToDatabase(e, e.message.toString())
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
        } catch (e: Exception) {
            ErrorUtils.addErrorToDatabase(e, e.message.toString())
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun saveExpenseHistory(expense: ExpenseModel) {
        val expenseMap = HashMap<String, Any>()
        expenseMap["date"] = DateTimeUtils.getCurrentDateTimeAsString()
        expenseMap["expenseId"] = expense.expenseId
        expenseMap["status"] = expense.statusId
        val expenseRef: DatabaseReference = Firebase.database.getReference("expenseHistory")
        val documentKey = expenseRef.push().key
        if (documentKey != null) {
            expenseRef.child(documentKey).setValue(expenseMap)
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    ErrorUtils.addErrorToDatabase(e, e.message.toString())
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
        }
    }

    fun getExpensesByTypes(expenseTypes: List<String>) {
        val filteredExpenses = _expenseList.value?.filter { expenseTypes.contains(it.expenseType) }
        if(filteredExpenses?.isEmpty() == true) {
            _filteredList.value = emptyList()
        } else {
            _filteredList.value = filteredExpenses!!
        }
    }

}