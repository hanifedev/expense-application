package com.kiliccambaz.expenseapp.ui.register

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.kiliccambaz.expenseapp.data.Result

class RegisterViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    fun registerWithEmailAndPassword(
        email: String,
        password: String,
        onRegisterComplete: (Result<FirebaseUser>) -> Unit
    ) {
        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.let {
                            val userMap = hashMapOf(
                                "uid" to user.uid,
                                "email" to email,
                                "role" to "Manager"
                            )

                            firestore.collection("users").document(user.uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    onRegisterComplete(Result.Success(user))
                                }
                                .addOnFailureListener { e ->
                                    onRegisterComplete(Result.Error("Firestore kaydetme hatası: ${e.message}"))
                                }
                        }
                    } else {
                        val errorMessage = task.exception?.message ?: "Kayıt işlemi başarısız oldu."
                        onRegisterComplete(Result.Error(errorMessage))
                    }
                }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            onRegisterComplete(Result.Error("Kayıt işlemi başarısız oldu."))
        }
    }
}