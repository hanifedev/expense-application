package com.kiliccambaz.expenseapp.ui.employee.addexpense

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kiliccambaz.expenseapp.data.ExpenseModel
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.utils.ErrorUtils
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
                     val documentKey = expensesRef.push().key

                     if (documentKey != null) {
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
                 }
             } catch (e: Exception) {
                 _addExpenseResponse.postValue(Result.Error("İşlem başarısız oldu: ${e.message}"))
                 ErrorUtils.addErrorToDatabase(e, "")
                 FirebaseCrashlytics.getInstance().recordException(e)
             }
         }
     }

}