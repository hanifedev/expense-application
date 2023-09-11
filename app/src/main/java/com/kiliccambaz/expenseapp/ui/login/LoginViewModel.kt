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
import java.security.MessageDigest
import java.security.SecureRandom

class LoginViewModel : ViewModel() {

    fun signInWithEmailAndPassword(email: String, password: String, onLoginComplete: (Result<Int>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val usersRef = Firebase.database.getReference("users")

                usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
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

                                        onLoginComplete(Result.Success(userRole))
                                        return
                                    } else {
                                        onLoginComplete(Result.Error("Kullanıcı rolü belirlenemedi."))
                                    }
                                }
                            }
                        }
                        onLoginComplete(Result.Error("Kullanıcı bulunamadı."))
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        ErrorUtils.addErrorToDatabase(databaseError.toException(), "")
                        FirebaseCrashlytics.getInstance().recordException(databaseError.toException())
                        onLoginComplete(Result.Error("Giriş işlemi başarısız oldu"))
                    }
                })
            } catch (e: Exception) {
                ErrorUtils.addErrorToDatabase(e, "")
                FirebaseCrashlytics.getInstance().recordException(e)
                onLoginComplete(Result.Error("Giriş işlemi başarısız oldu"))
            }
        }
    }


}