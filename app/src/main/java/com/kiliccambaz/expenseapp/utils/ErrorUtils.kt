package com.kiliccambaz.expenseapp.utils

import com.google.android.recaptcha.RecaptchaErrorCode
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kiliccambaz.expenseapp.data.ErrorCodes
import com.kiliccambaz.expenseapp.data.ErrorModel
import java.lang.Exception

object ErrorUtils {
    fun addErrorToDatabase(e: Exception, userId: String) {
        val errorModel = when (e) {
            is NullPointerException -> {
                ErrorModel(ErrorCodes.NULL_POINTER,"Null değer algılandı.", userId, "NullPointerException", Timestamp.now())
            }
            is IllegalArgumentException -> {
                ErrorModel(ErrorCodes.INVALID_ARGUMENT, "Geçersiz argüman algılandı.", userId, "IllegalArgumentException", Timestamp.now())
            }
            is FirebaseNetworkException -> {
                ErrorModel(ErrorCodes.NETWORK_ERROR, "Firebase ağ hatası.", userId, "FirebaseNetworkException", Timestamp.now())
            }
            // Diğer özel hata türleri için ek koşullar ekleyebilirsiniz.
            else -> {
                ErrorModel(ErrorCodes.UNKNOWN_ERROR,"Diğer Hata Türü", userId,"Bilinmeyen hata türü algılandı.", Timestamp.now())
            }
        }

        val databaseReference = Firebase.database.getReference("errors")
        val newErrorKey = databaseReference.push().key

        if (newErrorKey != null) {
            val errorReference = databaseReference.child(newErrorKey)
            errorReference.setValue(errorModel)
                .addOnFailureListener { e ->
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
        }
    }

}