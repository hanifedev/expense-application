package com.kiliccambaz.expenseapp.ui.employee.addexpense

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddExpenseViewModel : ViewModel() {

    private val _addExpenseResponse = MutableLiveData<Result<Boolean>>()
    val addExpenseResponse : LiveData<Result<Boolean>> = _addExpenseResponse

     fun saveExpenses(expenses: List<ExpenseModel>) {
         viewModelScope.launch(Dispatchers.IO) {
             try {
                 val expensesRef = Firebase.database.getReference("expenses")

                 for (expense in expenses) {
                     if(expense.expenseId.isNullOrEmpty()) {
                         val documentKey = expensesRef.push().key

                         if (documentKey != null) {
                             expense.expenseId = documentKey
                             expensesRef.child(documentKey).setValue(expense)
                                 .addOnSuccessListener {
                                     _addExpenseResponse.postValue(Result.Success(true))
                                 }
                                 .addOnFailureListener { e ->
                                     FirebaseCrashlytics.getInstance().recordException(e)
                                     _addExpenseResponse.postValue(Result.Error("Firestore kaydetme hatası: ${e.message}"))
                                 }
                         } else {
                             _addExpenseResponse.postValue(Result.Error("Belge kimliği oluşturulamadı."))
                         }
                     } else {
                         updateExpense(expense)
                     }
                 }
             } catch (e: Exception) {
                 _addExpenseResponse.postValue(Result.Error("İşlem başarısız oldu: ${e.message}"))
                 ErrorUtils.addErrorToDatabase(e, "")
                 FirebaseCrashlytics.getInstance().recordException(e)
             }
         }
     }

    private fun updateExpense(expense: ExpenseModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("expenses")
                val expenseUpdates = HashMap<String, Any>()
                expenseUpdates["/${expense.expenseId}"] = expense.toMap()

                databaseReference.updateChildren(expenseUpdates)
                    .addOnSuccessListener {
                        _addExpenseResponse.postValue(Result.Success(true))
                    }
                    .addOnFailureListener { error ->
                        FirebaseCrashlytics.getInstance().recordException(error)
                        ErrorUtils.addErrorToDatabase(error, UserManager.getUserId())
                        _addExpenseResponse.postValue(Result.Error("Firestore kaydetme hatası: ${error.message}"))
                    }

            } catch (ex: java.lang.Exception) {
                _addExpenseResponse.postValue(Result.Error("İşlem başarısız oldu: ${ex.message}"))
                ErrorUtils.addErrorToDatabase(ex, UserManager.getUserId())
                FirebaseCrashlytics.getInstance().recordException(ex)
            }
        }
    }

}