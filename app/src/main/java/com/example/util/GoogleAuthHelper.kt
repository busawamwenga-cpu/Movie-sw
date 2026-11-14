package com.example.util

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object GoogleAuthHelper {
    private const val TAG = "GoogleAuthHelper"

    class SignInCancelledException : Exception("Sign-in cancelled.")

    suspend fun attemptAutoSignIn(activity: ComponentActivity): Result<FirebaseUser> {
        return try {
            val webClientId = activity.getString(R.string.default_web_client_id)
            val option = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()
            performCredentialRequest(activity, option)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(activity: ComponentActivity): Result<FirebaseUser> {
        return try {
            val webClientId = activity.getString(R.string.default_web_client_id)
            val option = GetSignInWithGoogleOption.Builder(webClientId).build()
            performCredentialRequest(activity, option)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun performCredentialRequest(
        activity: ComponentActivity,
        option: androidx.credentials.CredentialOption
    ): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(activity)
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = Firebase.auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user
                    ?: return Result.failure(IllegalStateException("Sign-in failed. No user returned."))
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Unsupported credential type received."))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(SignInCancelledException())
        } catch (e: NoCredentialException) {
            Result.failure(
                IllegalStateException("No Google account found on this device. Please add a Google account in Settings.")
            )
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager sign-in error", e)
            Result.failure(
                IllegalStateException("Sign-in failed. Please check your internet connection and try again.")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Google sign-in error", e)
            Result.failure(
                IllegalStateException(e.message ?: "Sign-in failed. Please try again.")
            )
        }
    }

    suspend fun signOut(context: Context) {
        withContext(NonCancellable) {
            runCatching {
                CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
            }
            Firebase.auth.signOut()
        }
    }
}
