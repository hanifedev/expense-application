package com.kiliccambaz.expenseapp.ui.employee.addexpense

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
import com.kiliccambaz.expenseapp.utils.DateTimeUtils
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddExpenseViewModel : ViewModel() {

    private val _addExpenseResponse = MutableLiveData<Result<Boolean>>()
    val addExpenseResponse : LiveData<Result<Boolean>> = _addExpenseResponse

    private val _updateExpenseResponse = MutableLiveData<Result<Boolean>>()
    val updateExpenseResponse : LiveData<Result<Boolean>> = _updateExpenseResponse

    private val _expenseList = MutableLiveData<List<ExpenseUIModel>>()
    val expenseList : LiveData<List<ExpenseUIModel>> = _expenseList

    private val _hideDescription = MutableLiveData<Boolean>()
    val hideDescription : LiveData<Boolean> = _hideDescription


    private val mainExpense = MutableLiveData<ExpenseMainModel>()

    fun setMainExpense(expenseModel: ExpenseMainModel) {
        mainExpense.value = expenseModel
        getExpenseDetailList()
    }

    fun saveExpenseDetail(expense: ExpenseDetailModel, expenseDetailId: String) {
         viewModelScope.launch(Dispatchers.IO) {
             try {
                 if(!expenseDetailId.isNullOrEmpty()) {
                     expense.expenseDetailId = expenseDetailId
                     updateExpense(expense)
                 } else {
                     mainExpense.value?.let { mainExpenseValue ->
                         val expensesRef = Firebase.database.getReference("expenses").child(mainExpenseValue.expenseId)

                         val documentKey = expensesRef.child("expenseDetail").push().key

                         if (documentKey != null) {
                             expense.expenseDetailId = documentKey
                             expensesRef.child("expenseDetail").child(documentKey).setValue(expense)
                                 .addOnSuccessListener {
                                     _addExpenseResponse.postValue(Result.Success(true))
                                     getExpenseDetailList()
                                 }
                                 .addOnFailureListener { e ->
                                     FirebaseCrashlytics.getInstance().recordException(e)
                                     _addExpenseResponse.postValue(Result.Error("Firestore kaydetme hatası: ${e.message}"))
                                 }
                         } else {
                             _addExpenseResponse.postValue(Result.Error("Belge kimliği oluşturulamadı."))
                         }
                     }
                 }
             } catch (e: Exception) {
                 _addExpenseResponse.postValue(Result.Error("İşlem başarısız oldu: ${e.message}"))
                 ErrorUtils.addErrorToDatabase(e, "")
                 FirebaseCrashlytics.getInstance().recordException(e)
             }
         }
     }

    private fun updateExpense(expense: ExpenseDetailModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("expenses")
                val expenseUpdates = HashMap<String, Any>()
                mainExpense.value?.let {
                    expenseUpdates["/${mainExpense.value!!.expenseId}/expenseDetail/${expense.expenseDetailId}"] = expense.toMap()

                    databaseReference.updateChildren(expenseUpdates)
                        .addOnSuccessListener {
                            _updateExpenseResponse.postValue(Result.Success(true))
                            if(mainExpense.value!!.statusId == 3) {
                                mainExpense.value?.let {
                                    changeStatus(mainExpense.value!!.expenseId)
                                }
                            } else {
                                getExpenseDetailList()
                            }
                        }
                        .addOnFailureListener { error ->
                            FirebaseCrashlytics.getInstance().recordException(error)
                            ErrorUtils.addErrorToDatabase(error, UserManager.getUserId())
                            _updateExpenseResponse.postValue(Result.Error("Firestore error: ${error.message}"))
                        }
                }


            } catch (ex: java.lang.Exception) {
                _addExpenseResponse.postValue(Result.Error("İşlem başarısız oldu: ${ex.message}"))
                ErrorUtils.addErrorToDatabase(ex, UserManager.getUserId())
                FirebaseCrashlytics.getInstance().recordException(ex)
            }
        }
    }

    private fun changeStatus(expenseId : String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val database = FirebaseDatabase.getInstance()
                val reference: DatabaseReference = database.getReference("expenses").child(expenseId)

                val updateMap = mutableMapOf<String, Any>()
                updateMap["statusId"] = 1

                reference.updateChildren(updateMap) { databaseError, _ ->
                    if (databaseError == null) {
                        mainExpense.value!!.statusId = 1
                        _hideDescription.value = true
                        saveExpenseHistory()
                        getExpenseDetailList()
                    }
                }

            } catch (ex: java.lang.Exception) {
                _addExpenseResponse.postValue(Result.Error("İşlem başarısız oldu: ${ex.message}"))
                ErrorUtils.addErrorToDatabase(ex, UserManager.getUserId())
                FirebaseCrashlytics.getInstance().recordException(ex)
            }
        }
    }

    private fun saveExpenseHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val expenseMap = HashMap<String, Any>()
            expenseMap["date"] = DateTimeUtils.getCurrentDateTimeAsString()
            expenseMap["expenseId"] = mainExpense.value!!.expenseId
            expenseMap["status"] = mainExpense.value!!.statusId
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
                                            _expenseList.postValue(expenseList)
                                        }
                                    }
                                }
                            }

                            _expenseList.postValue(expenseList.sortedByDescending { DateTimeUtils.parseDate(it.date) })
                        } else {
                            _expenseList.postValue(emptyList())
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        val errorMessage = "Firebase Database error: ${databaseError.message}"
                        ErrorUtils.addErrorToDatabase(java.lang.Exception(errorMessage), "")
                        FirebaseCrashlytics.getInstance().recordException(Exception(errorMessage))
                        _expenseList.postValue(emptyList())
                    }
                })
            }
        }
    }

    fun createMainExpense(expenseModel: ExpenseMainModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val expensesRef = Firebase.database.getReference("expenses")
                if(expenseModel.expenseId.isNullOrEmpty()) {
                    val documentKey = expensesRef.push().key

                    if (documentKey != null) {
                        expenseModel.expenseId = documentKey
                        expensesRef.child(documentKey).setValue(expenseModel)
                            .addOnSuccessListener {
                                mainExpense.postValue(expenseModel)
                                _addExpenseResponse.postValue(Result.Success(true))
                            }
                            .addOnFailureListener { e ->
                                FirebaseCrashlytics.getInstance().recordException(e)
                                _addExpenseResponse.postValue(Result.Error("Firestore kaydetme hatası: ${e.message}"))
                            }
                        } else {
                            _addExpenseResponse.postValue(Result.Error("Belge kimliği oluşturulamadı."))
                        }
                    }
            } catch (e: Exception) {
                _addExpenseResponse.postValue(Result.Error("İşlem başarısız oldu: ${e.message}"))
                ErrorUtils.addErrorToDatabase(e, "")
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

}