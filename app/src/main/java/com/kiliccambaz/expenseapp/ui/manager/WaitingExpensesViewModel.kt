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
import com.kiliccambaz.expenseapp.utils.DateTimeUtils
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WaitingExpensesViewModel : ViewModel() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val _expenseList = MutableLiveData<List<ExpenseModel>>()
    val expenseList : LiveData<List<ExpenseModel>> = _expenseList

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
                    .equalTo("-Ndj8KEUEI5KmBhOqnJr")

                usersQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(usersDataSnapshot: DataSnapshot) {
                        if (usersDataSnapshot.exists()) {
                            val managerUserIds = mutableListOf<String>()

                            for (userSnapshot in usersDataSnapshot.children) {
                                managerUserIds.add(userSnapshot.key!!)
                            }

                            val expensesQuery = databaseReference.child("expenses")
                                .orderByChild("statusId")
                                .equalTo(1.0)

                            expensesQuery.addListenerForSingleValueEvent(object : ValueEventListener {
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
        val expensesRef: DatabaseReference = database.getReference("expenses")

        try {
            val updateData = hashMapOf<String, Any>("statusId" to expenseModel.statusId, "rejectedReason" to expenseModel.rejectedReason, "amount" to expenseModel.amount, "currencyType" to expenseModel.currencyType, "date" to expenseModel.date, "description" to expenseModel.description, "expenseType" to expenseModel.expenseType, "userId" to expenseModel.userId)

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

}