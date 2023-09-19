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
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
import com.kiliccambaz.expenseapp.data.ExpenseDetailModel
import com.kiliccambaz.expenseapp.data.ExpenseMainModel
import com.kiliccambaz.expenseapp.data.UserModel
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger


class ReportsViewModel : ViewModel() {

    private val _userExpenseData = MutableLiveData<Map<String, Float>>()
    val userExpenseData: LiveData<Map<String, Float>> = _userExpenseData

    private val _expenseTypeData = MutableLiveData<Map<String, Float>>()
    val expenseTypeData: LiveData<Map<String, Float>> = _expenseTypeData

    private val _dailyExpense = MutableLiveData<Map<String, Float>>()
    val dailyExpense: LiveData<Map<String, Float>> = _dailyExpense

    private val _monthlyExpense = MutableLiveData<MutableMap<String, HashMap<String, Float>>>()
    val monthExpense: LiveData<MutableMap<String, HashMap<String, Float>>> = _monthlyExpense

    init {
        fetchExpenseListFromDatabase()
        calculateMonthlyExpenses()
    }

    private fun calculateMonthlyExpenses() {
        val expensesRef = Firebase.database.getReference("expenses")
        val usersRef = Firebase.database.getReference("users")

        val monthlyExpensesMap = mutableMapOf<String, HashMap<String, Float>>()

        // Kullanıcı bilgilerini almak için bir ValueEventListener kullanın
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(usersDataSnapshot: DataSnapshot) {
                expensesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (expenseSnapshot in dataSnapshot.children) {
                            val expense = expenseSnapshot.getValue(ExpenseMainModel::class.java)
                            expense?.let {
                                val expenseDetail = expenseSnapshot.child("expenseDetail")

                                for (monthSnapshot in expenseDetail.children) {
                                    val amount = monthSnapshot.child("amount").getValue(Float::class.java)

                                    // Date alanından ay bilgisini alın
                                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    val date = dateFormat.parse(expense.date)
                                    val calendar = Calendar.getInstance()
                                    calendar.time = date
                                    val month =
                                        SimpleDateFormat("MM", Locale.getDefault()).format(calendar.time)

                                    if (amount != null && expense.userId != null && month != null) {
                                        // Kullanıcıyı bulun ve kullanıcı adını alın
                                        val userSnapshot = usersDataSnapshot.child(expense.userId)
                                        val username = userSnapshot.child("username").getValue(String::class.java)

                                        if (username != null) {
                                            // Kullanıcıya ait aylık masraf verilerini saklayın
                                            val existingMonthlyExpenses =
                                                monthlyExpensesMap.getOrPut(username) { HashMap() }
                                            existingMonthlyExpenses[month] =
                                                existingMonthlyExpenses.getOrDefault(month, 0f) + amount
                                        }
                                    }
                                }
                            }
                        }
                        _monthlyExpense.postValue(monthlyExpensesMap)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        ErrorUtils.addErrorToDatabase(java.lang.Exception(databaseError.toException()), UserManager.getUserId())
                        FirebaseCrashlytics.getInstance().recordException(Exception(databaseError.toException()))
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                ErrorUtils.addErrorToDatabase(java.lang.Exception(databaseError.toException()), UserManager.getUserId())
                FirebaseCrashlytics.getInstance().recordException(Exception(databaseError.toException()))
            }
        })
    }


    private fun fetchExpenseListFromDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val databaseReference = Firebase.database.reference.child("expenses")

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val expenseList = mutableListOf<ExpenseMainModel>()
                        val dailyExpenses = mutableMapOf<String, Float>()

                        for (expenseSnapshot in dataSnapshot.children) {
                            val expenseId = expenseSnapshot.key
                            val expense = expenseSnapshot.getValue(ExpenseMainModel::class.java)
                            expense?.let {
                                if (expenseId != null) {
                                    expense.expenseId = expenseId
                                }
                                expenseList.add(it)

                                val expenseDetail = expenseSnapshot.child("expenseDetail")
                                for (detailSnapshot in expenseDetail.children) {
                                    val detail = detailSnapshot.getValue(ExpenseDetailModel::class.java)
                                    if (detail != null && expense.date != null) {
                                        val dateString = expense.date.substring(0, 10) // Sadece tarih kısmını al
                                        //günlük masraflar için oluşturulan map
                                        if (dailyExpenses.containsKey(dateString)) {
                                            dailyExpenses[dateString] = dailyExpenses[dateString]!! + detail.amount.toFloat()
                                        } else {
                                            dailyExpenses[dateString] = detail.amount.toFloat()
                                        }
                                    }
                                }
                            }
                        }
                        _dailyExpense.postValue(dailyExpenses)

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

                                groupExpensesByUser(expenseList, usersMap)
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                ErrorUtils.addErrorToDatabase(databaseError.toException(), UserManager.getUserId())
                                FirebaseCrashlytics.getInstance().recordException(databaseError.toException())
                            }
                        })

                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    val errorMessage = "Firebase Database error: ${databaseError.message}"
                    ErrorUtils.addErrorToDatabase(java.lang.Exception(errorMessage), UserManager.getUserId())
                    FirebaseCrashlytics.getInstance().recordException(Exception(errorMessage))
                }
            })
        }
    }

    private fun groupExpensesByUser(
        expensesList: List<ExpenseMainModel>,
        usersMap: Map<String, String>
    ) {
            val userExpensesMap = mutableMapOf<String, Float>()
            val expensesRef = Firebase.database.getReference("expenses")

            for (expense in expensesList) {
                val expenseId = expense.expenseId
                val userId = expense.userId

                expensesRef.child(expenseId).child("expenseDetail").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        try {
                            for (detailSnapshot in dataSnapshot.children) {
                                val amount = detailSnapshot.child("amount").getValue(Float::class.java)

                                if (userId != null && usersMap.containsKey(userId) && amount != null) {
                                    val userEmail = usersMap[userId]!!

                                    if (userExpensesMap.containsKey(userEmail)) {
                                        val existingValue = userExpensesMap[userEmail] ?: 0f
                                        userExpensesMap[userEmail] = existingValue + amount
                                    } else {
                                        userExpensesMap[userEmail] = amount
                                    }
                                }
                            }
                            _userExpenseData.postValue(userExpensesMap)

                        } catch (e: Exception) {
                            ErrorUtils.addErrorToDatabase(java.lang.Exception(e.message), UserManager.getUserId())
                            FirebaseCrashlytics.getInstance().recordException(Exception(e.message))
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        ErrorUtils.addErrorToDatabase(databaseError.toException(), UserManager.getUserId())
                        FirebaseCrashlytics.getInstance().recordException(databaseError.toException())
                    }
                })
            }
    }

}