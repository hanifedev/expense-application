package com.kiliccambaz.expenseapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun signInWithEmailAndPassword(email: String, password: String, onLoginComplete: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid

                if (userId != null) {
                    val userDocument = firestore.collection("users").document(userId).get().await()
                    val userRole = userDocument.getString("role")

                    if (userRole != null) {
                        onLoginComplete(userRole)
                    } else {
                        onLoginComplete(null)
                    }
                } else {
                    onLoginComplete(null)
                }
            } catch (e: Exception) {
                // Giriş başarısız olduğunda hata mesajı gösterebilirsiniz
                onLoginComplete(null)
            }
        }
    }


}