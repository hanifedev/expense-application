package com.kiliccambaz.expenseapp.ui.manager

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
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.utils.DateTimeUtils
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WaitingExpensesViewModel : ViewModel() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val _expenseList = MutableLiveData<List<ExpenseModel>>()
    val expenseList : LiveData<List<ExpenseModel>> = _expenseList

    private val _updateResponse = MutableLiveData<Result<Boolean>>()
    val updateResponse : LiveData<Result<Boolean>> = _updateResponse

    init {
        fetchWaitingExpenseList { list ->
            _expenseList.value = list
        }
    }

    private fun fetchWaitingExpenseList(onExpenseListFetched: (List<ExpenseModel>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val databaseReference = Firebase.database.reference

                val usersQuery = databaseReference.child("users")
                    .orderByChild("managerId")
                    .equalTo(UserManager.getUserId())

                usersQuery.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(usersDataSnapshot: DataSnapshot) {
                        if (usersDataSnapshot.exists()) {
                            val managerUserIds = mutableListOf<String>()

                            for (userSnapshot in usersDataSnapshot.children) {
                                managerUserIds.add(userSnapshot.key!!)
                            }

                            val expensesQuery = databaseReference.child("expenses")
                                .orderByChild("statusId")
                                .equalTo(1.0)

                            expensesQuery.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(expensesDataSnapshot: DataSnapshot) {
                                    val expenseList = mutableListOf<ExpenseModel>()

                                    for (expenseSnapshot in expensesDataSnapshot.children) {
                                        val expense = expenseSnapshot.getValue(ExpenseModel::class.java)
                                        if (managerUserIds.contains(expense?.userId)) {
                                            expense?.let { expenseList.add(it) }
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
                        } else {
                            onExpenseListFetched(emptyList())
                        }
                    }

                    override fun onCancelled(usersDatabaseError: DatabaseError) {
                        val errorMessage = "Firebase Realtime Database error: ${usersDatabaseError.message}"
                        ErrorUtils.addErrorToDatabase(java.lang.Exception(errorMessage), "")
                        FirebaseCrashlytics.getInstance().recordException(java.lang.Exception(errorMessage))
                        onExpenseListFetched(emptyList())
                    }
                })
            } catch (e: Exception) {
                val errorMessage = "Firebase Realtime Database error: ${e.message}"
                ErrorUtils.addErrorToDatabase(e, errorMessage)
                FirebaseCrashlytics.getInstance().recordException(e)
                onExpenseListFetched(emptyList())
            }
        }
    }

    fun updateExpense(expenseModel: ExpenseModel) {
        viewModelScope.launch(Dispatchers.IO) {
        val expensesRef: DatabaseReference = database.getReference("expenses")

        try {
            val updateData = expenseModel.toMap()

            expensesRef.child(expenseModel.expenseId).updateChildren(updateData)
                .addOnSuccessListener {
                    _updateResponse.postValue(Result.Success(true))
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
    }

    private fun saveExpenseHistory(expense: ExpenseModel) {
        val expenseMap = HashMap<String, Any>()
        expenseMap["date"] = DateTimeUtils.getCurrentDateTimeAsString()
        expenseMap["expenseId"] = expense.expenseId
        expenseMap["status"] = expense.statusId
        val expenseRef: DatabaseReference = database.getReference("expenseHistory")
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

    fun getExpensesByTypes(expenseTypes: List<String>, onExpensesFetched: (List<ExpenseModel>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val databaseReference = Firebase.database.reference

                val usersQuery = databaseReference.child("users")
                    .orderByChild("managerId")
                    .equalTo(UserManager.getUserId())

                usersQuery.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(usersDataSnapshot: DataSnapshot) {
                        if (usersDataSnapshot.exists()) {
                            val managerUserIds = mutableListOf<String>()

                            for (userSnapshot in usersDataSnapshot.children) {
                                managerUserIds.add(userSnapshot.key!!)
                            }

                            val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("expenses")
                            val query = databaseReference.orderByChild("expenseType")

                            query.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val expenses = mutableListOf<ExpenseModel>()

                                    for (expenseSnapshot in dataSnapshot.children) {
                                        val expense = expenseSnapshot.getValue(ExpenseModel::class.java)
                                        expense?.let {
                                            if (it.expenseType in expenseTypes && managerUserIds.contains(expense?.userId)) {
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
                        } else {
                            onExpensesFetched(emptyList())
                        }
                    }

                    override fun onCancelled(usersDatabaseError: DatabaseError) {
                        val errorMessage = "Firebase Realtime Database error: ${usersDatabaseError.message}"
                        ErrorUtils.addErrorToDatabase(java.lang.Exception(errorMessage), UserManager.getUserId())
                        FirebaseCrashlytics.getInstance().recordException(java.lang.Exception(errorMessage))
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