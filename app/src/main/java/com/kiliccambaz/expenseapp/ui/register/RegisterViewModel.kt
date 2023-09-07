package com.kiliccambaz.expenseapp.ui.register

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kiliccambaz.expenseapp.data.ErrorModel
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.data.UserModel
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.HashUtils

class RegisterViewModel : ViewModel() {

    fun registerWithEmailAndPassword(
        email: String,
        password: String,
        onRegisterComplete: (Result<String>) -> Unit
    ) {
        try {
            val usersReference = Firebase.database.reference.child("users")

            val user = UserModel(
                email = email,
                password = HashUtils.hashPassword(password),
                role = 1,
                managerId = "-Ndj8KEUEI5KmBhOqnJr"
            )

            usersReference.push().setValue(user) { databaseError, _ ->
                if (databaseError == null) {
                    onRegisterComplete(Result.Success("Kayıt başarılı"))
                } else {
                    onRegisterComplete(Result.Error("Firebase kaydetme hatası: ${databaseError.message}"))
                }
            }
        } catch (e: Exception) {
            ErrorUtils.addErrorToDatabase(e, "")
            FirebaseCrashlytics.getInstance().recordException(e)
            onRegisterComplete(Result.Error("Kayıt işlemi başarısız oldu."))
        }
    }


}