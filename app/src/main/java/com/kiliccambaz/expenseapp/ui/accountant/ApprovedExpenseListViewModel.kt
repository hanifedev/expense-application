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
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.utils.DateTimeUtils
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApprovedExpenseListViewModel: ViewModel() {

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

                            val expensesQuery = databaseReference.child("expenses")
                                .orderByChild("statusId")
                                .equalTo(2.0)

                            expensesQuery.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(expensesDataSnapshot: DataSnapshot) {
                                    val expenseList = mutableListOf<ExpenseModel>()

                                    for (expenseSnapshot in expensesDataSnapshot.children) {
                                        val expense = expenseSnapshot.getValue(ExpenseModel::class.java)
                                        expense?.let { expenseList.add(it) }
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

    fun updateExpense(expenseModel: ExpenseModel) {
        val expensesRef: DatabaseReference = Firebase.database.getReference("expenses")

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

    fun getExpensesByTypes(expenseTypes: List<String>, onExpensesFetched: (List<ExpenseModel>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                            val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("expenses")
                            val query = databaseReference.orderByChild("expenseType")

                            query.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val expenses = mutableListOf<ExpenseModel>()

                                    for (expenseSnapshot in dataSnapshot.children) {
                                        val expense = expenseSnapshot.getValue(ExpenseModel::class.java)
                                        expense?.let {
                                            if (it.expenseType in expenseTypes && expense.statusId == 2) {
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