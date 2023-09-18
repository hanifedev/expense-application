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
import com.kiliccambaz.expenseapp.data.ExpenseDetailModel
import com.kiliccambaz.expenseapp.data.ExpenseMainModel
import com.kiliccambaz.expenseapp.data.ExpenseUIModel
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.data.UserModel
import com.kiliccambaz.expenseapp.utils.DateTimeUtils
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WaitingExpensesViewModel : ViewModel() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val _expenseList = MutableLiveData<List<ExpenseUIModel>>()
    val expenseList : LiveData<List<ExpenseUIModel>?> = _expenseList

    private val _filteredList = MutableLiveData<List<ExpenseUIModel>>()
    val filteredList : LiveData<List<ExpenseUIModel>?> = _filteredList

    private val _updateResponse = MutableLiveData<Result<Boolean>>()
    val updateResponse : LiveData<Result<Boolean>> = _updateResponse

    private val _expenseDetailList = MutableLiveData<List<ExpenseUIModel>>()
    val expenseDetailList : LiveData<List<ExpenseUIModel>> = _expenseDetailList


    private val mainExpense = MutableLiveData<ExpenseUIModel>()

    init {
        fetchWaitingExpenseList { list ->
            _expenseList.value = list
        }
    }

    private fun fetchWaitingExpenseList(onExpenseListFetched: (List<ExpenseUIModel>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val databaseReference = Firebase.database.reference

                val usersQuery = databaseReference.child("users")
                    .orderByChild("managerId")
                    .equalTo(UserManager.getUserId())

                usersQuery.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(usersDataSnapshot: DataSnapshot) {


                        val expenses = arrayListOf<ExpenseUIModel>()
                        if (usersDataSnapshot.exists()) {
                            val managerUserIds = mutableListOf<String>()

                            for (userSnapshot in usersDataSnapshot.children) {
                                managerUserIds.add(userSnapshot.key!!)
                            }

                            val expensesQuery = databaseReference.child("expenses")

                            expensesQuery.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(expensesDataSnapshot: DataSnapshot) {

                                    for (expenseSnapshot in expensesDataSnapshot.children) {
                                        val expense =
                                            expenseSnapshot.getValue(ExpenseMainModel::class.java)
                                        if (managerUserIds.contains(expense?.userId)) {
                                            expense?.let {
                                                if (expense.statusId == 1) {
                                                    setUsername(expense.userId) { username ->
                                                        val historyUIModel = ExpenseUIModel()
                                                        historyUIModel.expenseId = expense.expenseId
                                                        historyUIModel.date = expense.date
                                                        historyUIModel.statusId = expense.statusId
                                                        historyUIModel.description = expense.description
                                                        historyUIModel.rejectedReason =
                                                            expense.rejectedReason
                                                        historyUIModel.currencyType =
                                                            expense.currencyType
                                                        historyUIModel.userId = expense.userId
                                                        historyUIModel.user = username
                                                        expenses.add(historyUIModel)
                                                        onExpenseListFetched(expenses)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    onExpenseListFetched(expenses)

                                }

                                override fun onCancelled(expensesDatabaseError: DatabaseError) {
                                    val errorMessage =
                                        "Firebase Realtime Database error: ${expensesDatabaseError.message}"
                                    ErrorUtils.addErrorToDatabase(
                                        java.lang.Exception(errorMessage),
                                        ""
                                    )
                                    FirebaseCrashlytics.getInstance()
                                        .recordException(java.lang.Exception(errorMessage))
                                    onExpenseListFetched(emptyList())
                                }
                            })
                        } else {
                            onExpenseListFetched(emptyList())
                        }
                    }

                    override fun onCancelled(usersDatabaseError: DatabaseError) {
                        val errorMessage =
                            "Firebase Realtime Database error: ${usersDatabaseError.message}"
                        ErrorUtils.addErrorToDatabase(java.lang.Exception(errorMessage), "")
                        FirebaseCrashlytics.getInstance()
                            .recordException(java.lang.Exception(errorMessage))
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

    fun updateExpense(expenseModel: ExpenseUIModel) {
        viewModelScope.launch(Dispatchers.IO) {
        val expensesRef: DatabaseReference = database.getReference("expenses")

        try {
            val expenseModel = ExpenseMainModel(expenseModel.date, expenseModel.description, expenseModel.userId, expenseModel.currencyType, expenseModel.statusId, expenseModel.rejectedReason, expenseModel.expenseId)
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

    private fun saveExpenseHistory(expense: ExpenseMainModel) {
        viewModelScope.launch(Dispatchers.IO) {
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

    fun getExpensesByTypes(expenseTypes: List<String>) {
        val filteredExpenses = _expenseList.value?.filter { expenseTypes.contains(it.expenseType) }
        if(filteredExpenses?.isEmpty() == true) {
            _filteredList.value = emptyList()
        } else {
            _filteredList.value = filteredExpenses!!
        }
    }

    fun setMainExpense(expenseModel: ExpenseUIModel) {
        mainExpense.value = expenseModel
        getExpenseDetailList()
    }

    private fun getExpenseDetailList() {
        viewModelScope.launch(Dispatchers.IO) {
            mainExpense.value?.let {
                val databaseReference = Firebase.database.reference.child("expenses").child(
                    mainExpense.value!!.expenseId
                ).child("expenseDetail")

                databaseReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val expenseList = mutableListOf<ExpenseUIModel>()

                            for (expenseSnapshot in dataSnapshot.children) {
                                val expenseDetailId = expenseSnapshot.key
                                val expense = expenseSnapshot.getValue(ExpenseDetailModel::class.java)
                                expense?.let {
                                    if (expenseDetailId != null) {
                                        expense.expenseDetailId = expenseDetailId
                                        val expenseUIModel = ExpenseUIModel()
                                        mainExpense.value?.let {
                                            expenseUIModel.expenseId = mainExpense.value!!.expenseId
                                            expenseUIModel.currencyType = mainExpense.value!!.currencyType
                                            expenseUIModel.expenseDate = expense.expenseDate
                                            expenseUIModel.date = DateTimeUtils.getCurrentDateTimeAsString()
                                            expenseUIModel.amount = expense.amount
                                            expenseUIModel.expenseType = expense.expenseType
                                            expenseUIModel.statusId = mainExpense.value!!.statusId
                                            expenseUIModel.rejectedReason = mainExpense.value!!.rejectedReason
                                            expenseUIModel.description = mainExpense.value!!.description
                                            expenseUIModel.userId = mainExpense.value!!.userId
                                            expenseUIModel.expenseDetailId = expenseDetailId
                                            expenseList.add(expenseUIModel)
                                            _expenseDetailList.postValue(expenseList)
                                        }
                                    }
                                }
                            }

                            _expenseDetailList.postValue(expenseList.sortedByDescending { DateTimeUtils.parseDate(it.date) })
                        } else {
                            _expenseDetailList.postValue(emptyList())
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        val errorMessage = "Firebase Database error: ${databaseError.message}"
                        ErrorUtils.addErrorToDatabase(java.lang.Exception(errorMessage), "")
                        FirebaseCrashlytics.getInstance().recordException(Exception(errorMessage))
                        _expenseDetailList.postValue(emptyList())
                    }
                })
            }
        }
    }


}