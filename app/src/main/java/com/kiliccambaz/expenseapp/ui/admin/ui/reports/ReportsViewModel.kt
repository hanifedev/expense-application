package com.kiliccambaz.expenseapp.ui.admin.ui.reports

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
import com.kiliccambaz.expenseapp.data.UserModel
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ReportsViewModel : ViewModel() {

    private val _userExpenseData = MutableLiveData<Map<String, Float>>()
    val userExpenseData: LiveData<Map<String, Float>> = _userExpenseData

    private val _expenseTypeData = MutableLiveData<Map<String, Float>>()
    val expenseTypeData: LiveData<Map<String, Float>> = _expenseTypeData

    private val _dailyExpense = MutableLiveData<Map<String, Float>>()
    val dailyExpense: LiveData<Map<String, Float>> = _dailyExpense

    init {
        fetchExpenseListFromDatabase()
    }

    private fun fetchExpenseListFromDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val databaseReference = Firebase.database.reference.child("expenses")

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val expenseList = mutableListOf<ExpenseModel>()
                        val expensesTypeMap = mutableMapOf<String, Float>()
                        val dailyExpenses = mutableMapOf<String, Float>()

                        for (expenseSnapshot in dataSnapshot.children) {
                            val expenseId = expenseSnapshot.key
                            val expense = expenseSnapshot.getValue(ExpenseModel::class.java)
                            expense?.let {
                                if (expenseId != null) {
                                    expense.expenseId = expenseId
                                }
                                expenseList.add(it)
                                if (expensesTypeMap.containsKey(expense.expenseType)) {
                                    expensesTypeMap[expense.expenseType] = (expensesTypeMap[expense.expenseType]!! + expense.amount).toFloat()
                                } else {
                                    expensesTypeMap[expense.expenseType] = expense.amount.toFloat()
                                }
                                val date = expense.date.substring(0,10)
                                if (dailyExpenses.containsKey(date)) {
                                    dailyExpenses[date] = (dailyExpenses[date]!! + expense.amount).toFloat()
                                } else {
                                    dailyExpenses[date] = expense.amount.toFloat()
                                }
                            }
                        }
                        _dailyExpense.postValue(dailyExpenses)
                        _expenseTypeData.postValue(expensesTypeMap)

                        val usersMap = mutableMapOf<String, String>()

                        val userRef = Firebase.database.reference.child("users")
                        userRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                for(userSnapshot in dataSnapshot.children) {
                                    val user = userSnapshot.getValue(UserModel::class.java)
                                    user?.let { userModel ->
                                        val userId = userSnapshot.key
                                        userId?.let {
                                            usersMap[userId] = userModel.username
                                        }
                                    }
                                }

                                val userExpensesMap = groupExpensesByUser(expenseList, usersMap)
                                _userExpenseData.postValue(userExpensesMap)
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                ErrorUtils.addErrorToDatabase(databaseError.toException(), "")
                                FirebaseCrashlytics.getInstance().recordException(databaseError.toException())
                            }
                        })

                    } else {

                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    val errorMessage = "Firebase Database error: ${databaseError.message}"
                    ErrorUtils.addErrorToDatabase(java.lang.Exception(errorMessage), "")
                    FirebaseCrashlytics.getInstance().recordException(Exception(errorMessage))
                }
            })
        }
    }

    private fun groupExpensesByUser(
        expensesList: List<ExpenseModel>,
        usersMap: Map<String, String>
    ): Map<String, Float> {
        val userExpensesMap = mutableMapOf<String, Float>()

        for (expense in expensesList) {
            val userId = expense.userId
            val amount = expense.amount.toFloat()

            if (userId != null && usersMap.containsKey(userId)) {
                val userEmail = usersMap[userId]!!

                if (userExpensesMap.containsKey(userEmail)) {
                    val existingValue = userExpensesMap[userEmail] ?: 0f
                    userExpensesMap[userEmail] = existingValue + amount
                } else {
                    userExpensesMap[userEmail] = amount
                }
            }
        }

        return userExpensesMap
    }


}