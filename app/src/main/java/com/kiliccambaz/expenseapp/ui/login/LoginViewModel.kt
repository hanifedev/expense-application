package com.kiliccambaz.expenseapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.kiliccambaz.expenseapp.data.Result

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun signInWithEmailAndPassword(email: String, password: String, onLoginComplete: (Result<String>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid
                if (userId != null) {
                    val userDocument = firestore.collection("users").document(userId).get().await()
                    val userRole = userDocument.getString("role")
                    if (userRole != null) {
                        onLoginComplete(Result.Success(userRole))
                    } else {
                        onLoginComplete(Result.Error("Kullanıcı rolü belirlenemedi"))
                    }
                } else {
                    onLoginComplete(Result.Error("Kullanıcı bulunamadı"))
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                onLoginComplete(Result.Error("Giriş işlemi başarısız oldu"))
            }
        }
    }
}