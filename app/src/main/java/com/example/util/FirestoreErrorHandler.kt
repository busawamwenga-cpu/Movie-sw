package com.example.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import org.json.JSONObject
import java.util.concurrent.CancellationException

enum class OperationType(val label: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write")
}

object FirestoreErrorHandler {
    private const val TAG = "FirestoreSecurityError"

    fun handleFirestoreError(
        error: Throwable,
        operationType: OperationType,
        path: String?
    ): Nothing {
        if (error is CancellationException) throw error

        val auth = runCatching { FirebaseAuth.getInstance() }.getOrNull()
        val currentUser = auth?.currentUser

        val authInfo = JSONObject().apply {
            put("userId", currentUser?.uid ?: JSONObject.NULL)
            put("email", currentUser?.email ?: JSONObject.NULL)
            put("emailVerified", currentUser?.isEmailVerified ?: false)
            put("isAnonymous", currentUser?.isAnonymous ?: false)
            put(
                "providerInfo",
                currentUser?.providerData?.map {
                    JSONObject().apply {
                        put("providerId", it.providerId)
                        put("displayName", it.displayName ?: JSONObject.NULL)
                        put("email", it.email ?: JSONObject.NULL)
                    }
                } ?: emptyList<JSONObject>()
            )
        }

        val errPayload = JSONObject().apply {
            put("error", error.message ?: error.toString())
            put("operationType", operationType.label)
            put("path", path ?: JSONObject.NULL)
            put("authInfo", authInfo)
        }

        Log.e(TAG, "Firestore Error: $errPayload", error)
        throw IllegalStateException(errPayload.toString(), error)
    }

    fun unwrapFirestoreException(error: Throwable): Exception {
        var current: Throwable? = error
        while (current != null) {
            if (current is FirebaseFirestoreException) return current
            current = current.cause
        }
        return error as? Exception ?: RuntimeException(error)
    }
}
