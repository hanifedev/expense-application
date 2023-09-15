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
        username: String,
        email: String,
        password: String,
        onRegisterComplete: (Result<String>) -> Unit
    ) {
        try {
            val usersReference = Firebase.database.reference.child("users")

            val user = UserModel(
                username = username,
                email = email,
                password = HashUtils.hashPassword(password),
                role = 1
            )

            usersReference.push().setValue(user) { databaseError, _ ->
                if (databaseError == null) {
                    onRegisterComplete(Result.Success("Register successfully"))
                } else {
                    onRegisterComplete(Result.Error("Firebase save error: ${databaseError.message}"))
                }
            }
        } catch (e: Exception) {
            ErrorUtils.addErrorToDatabase(e, "")
            FirebaseCrashlytics.getInstance().recordException(e)
            onRegisterComplete(Result.Error("Register failed"))
        }
    }


}