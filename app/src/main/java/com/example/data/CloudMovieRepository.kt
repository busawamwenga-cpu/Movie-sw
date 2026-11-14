package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.R
import com.example.model.CloudMovie
import com.example.model.UserProfile
import com.example.util.FirestoreErrorHandler
import com.example.util.OperationType
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.CancellationException

class CloudMovieRepository(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage? = null
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        storage = defaultStorage()
    )

    companion object {
        const val BOOTSTRAP_ADMIN_EMAIL = "busawamwenga@gmail.com"

        private fun defaultStorage(): FirebaseStorage? {
            return try {
                Firebase.storage
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun ensureUserProfile(
        uid: String,
        email: String,
        displayName: String,
        appVersionCode: Int,
        isEmailVerified: Boolean = false
    ): UserProfile {
        val path = "users/$uid"
        return try {
            val docRef = db.collection("users").document(uid)
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val updates = mutableMapOf<String, Any>(
                    "appVersionCode" to appVersionCode,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (displayName.isNotBlank()) {
                    updates["displayName"] = displayName
                }
                docRef.update(updates).await()
                val existing = snapshot.toObject(UserProfile::class.java)
                existing?.copy(
                    appVersionCode = appVersionCode,
                    displayName = displayName.ifBlank { existing.displayName },
                    isLocked = snapshot.getBoolean("isLocked") ?: existing.isLocked
                ) ?: UserProfile(
                    id = uid,
                    uid = uid,
                    email = email,
                    displayName = displayName,
                    role = "user",
                    appVersionCode = appVersionCode,
                    isLocked = false
                )
            } else {
                val initialRole = if (
                    email.equals(BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true) && isEmailVerified
                ) {
                    "admin"
                } else {
                    "user"
                }
                val newUser = UserProfile(
                    id = uid,
                    uid = uid,
                    email = email,
                    displayName = displayName.ifBlank { email.substringBefore("@").ifBlank { "Movie User" } },
                    role = initialRole,
                    appVersionCode = appVersionCode,
                    isLocked = false
                )
                docRef.set(newUser.toCreateMap()).await()
                newUser
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, path)
        }
    }

    fun observeUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val path = "users/$uid"
        val registration = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    try {
                        FirestoreErrorHandler.handleFirestoreError(error, OperationType.GET, path)
                    } catch (wrapped: Exception) {
                        close(wrapped)
                    }
                    return@addSnapshotListener
                }
                val profile = snapshot?.takeIf { it.exists() }?.let {
                    it.toObject(UserProfile::class.java)?.copy(
                        isLocked = it.getBoolean("isLocked") ?: false,
                        createdAt = it.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                        updatedAt = it.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    )
                }
                trySend(profile)
            }
        awaitClose { registration.remove() }
    }

    fun observeAllUsers(): Flow<List<UserProfile>> = callbackFlow {
        val path = "users"
        val registration = db.collection("users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    try {
                        FirestoreErrorHandler.handleFirestoreError(error, OperationType.LIST, path)
                    } catch (wrapped: Exception) {
                        close(wrapped)
                    }
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(UserProfile::class.java)?.copy(
                        isLocked = doc.getBoolean("isLocked") ?: false,
                        createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                        updatedAt = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    suspend fun updateUserRole(targetUid: String, newRole: String) {
        val sanitizedRole = if (newRole.equals("admin", ignoreCase = true)) "admin" else "user"
        val path = "users/$targetUid"
        try {
            db.collection("users").document(targetUid).update(
                mapOf(
                    "role" to sanitizedRole,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    suspend fun setUserLocked(targetUid: String, isLocked: Boolean) {
        val path = "users/$targetUid"
        try {
            db.collection("users").document(targetUid).update(
                mapOf(
                    "isLocked" to isLocked,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    suspend fun lockOutdatedUsers(minVersionCode: Int): Int {
        val path = "users"
        return try {
            val snap = db.collection("users").get().await()
            val batch = db.batch()
            var count = 0
            for (doc in snap.documents) {
                val user = doc.toObject(UserProfile::class.java) ?: continue
                if (!user.isAdmin && user.appVersionCode < minVersionCode && !user.isLocked) {
                    batch.update(
                        doc.reference,
                        mapOf(
                            "isLocked" to true,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    count++
                }
            }
            if (count > 0) {
                batch.commit().await()
            }
            count
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    fun observeMovies(): Flow<List<CloudMovie>> = callbackFlow {
        val path = "movies"
        val registration = db.collection("movies")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    try {
                        FirestoreErrorHandler.handleFirestoreError(error, OperationType.LIST, path)
                    } catch (wrapped: Exception) {
                        close(wrapped)
                    }
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CloudMovie::class.java)?.copy(
                        createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                        updatedAt = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveMovieMetadata(
        movieId: String,
        title: String,
        description: String,
        videoUrl: String,
        storagePath: String,
        thumbnailUrl: String,
        fileName: String,
        fileSizeBytes: Long,
        durationSeconds: Int,
        uploadedBy: String,
        category: String = "action",
        isVipOnly: Boolean = false
    ): CloudMovie {
        val path = "movies/$movieId"
        return try {
            val movie = CloudMovie(
                id = movieId,
                title = title.trim(),
                description = description.trim(),
                videoUrl = videoUrl,
                storagePath = storagePath,
                thumbnailUrl = thumbnailUrl,
                fileName = fileName,
                fileSizeBytes = fileSizeBytes,
                durationSeconds = durationSeconds,
                category = category,
                isVipOnly = isVipOnly,
                uploadedBy = uploadedBy
            )
            db.collection("movies").document(movieId).set(movie.toCreateMap()).await()
            movie
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, path)
        }
    }

    suspend fun uploadMovieWithProgress(
        context: Context,
        movieId: String,
        videoUri: Uri,
        title: String,
        description: String,
        fileName: String,
        fileSizeBytes: Long,
        durationSeconds: Int,
        uploadedBy: String,
        category: String = "action",
        isVipOnly: Boolean = false,
        onProgress: (fraction: Float, percent: Int, transferredBytes: Long, totalBytes: Long) -> Unit
    ): CloudMovie {
        val activeStorage = storage
            ?: throw IllegalStateException("Firebase Storage is not configured. Check your Firebase project setup.")

        val storagePath = "movies/$movieId.mp4"
        val videoRef = activeStorage.reference.child(storagePath)

        val uploadTask = videoRef.putFile(videoUri)
        uploadTask.addOnProgressListener { snapshot ->
            val total = snapshot.totalByteCount.coerceAtLeast(fileSizeBytes).coerceAtLeast(1L)
            val transferred = snapshot.bytesTransferred.coerceAtLeast(0L)
            val fraction = (transferred.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            val pct = (fraction * 100f).toInt().coerceIn(0, 100)
            onProgress(fraction, pct, transferred, total)
        }
        uploadTask.await()

        val videoDownloadUrl = videoRef.downloadUrl.await().toString()
        val thumbUrl = uploadVideoThumbnailBestEffort(context, activeStorage, movieId, videoUri)

        return saveMovieMetadata(
            movieId = movieId,
            title = title,
            description = description,
            videoUrl = videoDownloadUrl,
            storagePath = storagePath,
            thumbnailUrl = thumbUrl,
            fileName = fileName,
            fileSizeBytes = fileSizeBytes,
            durationSeconds = durationSeconds,
            uploadedBy = uploadedBy,
            category = category,
            isVipOnly = isVipOnly
        )
    }

    private suspend fun uploadVideoThumbnailBestEffort(
        context: Context,
        storage: FirebaseStorage,
        movieId: String,
        videoUri: Uri
    ): String = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
            val frame = retriever.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: return@withContext ""
            val baos = ByteArrayOutputStream()
            frame.compress(Bitmap.CompressFormat.JPEG, 78, baos)
            frame.recycle()
            val bytes = baos.toByteArray()
            if (bytes.isEmpty()) return@withContext ""
            val thumbRef = storage.reference.child("movies/${movieId}_thumb.jpg")
            thumbRef.putBytes(bytes).await()
            thumbRef.downloadUrl.await().toString()
        } catch (_: Exception) {
            ""
        } finally {
            runCatching { retriever.release() }
        }
    }

    suspend fun deleteMovie(movie: CloudMovie) {
        val path = "movies/${movie.id}"
        try {
            db.collection("movies").document(movie.id).delete().await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.DELETE, path)
        }

        val activeStorage = storage ?: return
        if (movie.storagePath.isNotBlank()) {
            try {
                activeStorage.reference.child(movie.storagePath).delete().await()
            } catch (e: StorageException) {
                if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                    // Best-effort storage cleanup after Firestore document removal
                }
            } catch (_: Exception) {
            }
            val thumbPath = movie.storagePath.removeSuffix(".mp4") + "_thumb.jpg"
            runCatching { activeStorage.reference.child(thumbPath).delete().await() }
        }
    }
}
