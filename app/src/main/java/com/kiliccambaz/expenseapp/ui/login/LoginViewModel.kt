package com.kiliccambaz.expenseapp.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.data.UserModel
import com.kiliccambaz.expenseapp.utils.ErrorUtils
import com.kiliccambaz.expenseapp.utils.HashUtils
import com.kiliccambaz.expenseapp.utils.UserManager
import java.security.MessageDigest
import java.security.SecureRandom

class LoginViewModel : ViewModel() {

    fun signInWithEmailAndPassword(email: String, password: String, onLoginComplete: (Result<Int>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val usersRef = Firebase.database.getReference("users")
                val emailOrUsername = if(email.contains("@")) {
                    "email"
                } else {
                    "username"
                }
                usersRef.orderByChild(emailOrUsername).equalTo(email).addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (userSnapshot in dataSnapshot.children) {
                                val user = userSnapshot.getValue(UserModel::class.java)
                                val hashedPassword = HashUtils.hashPassword(password)
                                if (user != null && user.password == hashedPassword) {
                                    val userRole = user.role
                                    if (userRole != null) {
                                        val userId = userSnapshot.key
                                        if (userId != null) {
                                            UserManager.setUserId(userId)
                                        }
                                        onLoginComplete(Result.Success(userRole))
                                        return
                                    } else {
                                        onLoginComplete(Result.Error("User role could not be determined"))
                                    }
                                }
                            }
                        }
                        onLoginComplete(Result.Error("Email/username or password is incorrect"))
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        ErrorUtils.addErrorToDatabase(databaseError.toException(), "")
                        FirebaseCrashlytics.getInstance().recordException(databaseError.toException())
                        onLoginComplete(Result.Error("Login failed"))
                    }
                })
            } catch (e: Exception) {
                ErrorUtils.addErrorToDatabase(e, "")
                FirebaseCrashlytics.getInstance().recordException(e)
                onLoginComplete(Result.Error("Login failed"))
            }
        }
    }


}