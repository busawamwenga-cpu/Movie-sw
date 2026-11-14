package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserProfile(
    @DocumentId val id: String = "",
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "user", // "user" or "admin"
    val appVersionCode: Int = 1,
    @get:PropertyName("isLocked")
    val isLocked: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val isAdmin: Boolean
        get() = role.equals("admin", ignoreCase = true)

    fun toCreateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "role" to role,
            "appVersionCode" to appVersionCode,
            "isLocked" to isLocked,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return map
    }
}

data class CloudMovie(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val storagePath: String = "",
    val thumbnailUrl: String = "",
    val fileName: String = "",
    val fileSizeBytes: Long = 0L,
    val durationSeconds: Int = 0,
    val category: String = "action",
    @get:PropertyName("isVipOnly")
    val isVipOnly: Boolean = false,
    val uploadedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        return mapOf(
            "title" to title,
            "description" to description,
            "videoUrl" to videoUrl,
            "storagePath" to storagePath,
            "thumbnailUrl" to thumbnailUrl,
            "fileName" to fileName,
            "fileSizeBytes" to fileSizeBytes,
            "durationSeconds" to durationSeconds,
            "category" to category,
            "isVipOnly" to isVipOnly,
            "uploadedBy" to uploadedBy,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    fun formattedDate(): String {
        val date: Date = createdAt?.toDate() ?: return "Just now"
        return SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date)
    }

    fun formattedDuration(): String {
        if (durationSeconds <= 0) return "--:--"
        val mins = durationSeconds / 60
        val secs = durationSeconds % 60
        return if (mins >= 60) {
            val hrs = mins / 60
            val remMins = mins % 60
            "%d:%02d:%02d".format(hrs, remMins, secs)
        } else {
            "%02d:%02d".format(mins, secs)
        }
    }

    fun formattedSize(): String {
        if (fileSizeBytes <= 0L) return "Unknown size"
        val mb = fileSizeBytes / (1024f * 1024f)
        return if (mb >= 1024f) {
            "%.2f GB".format(Locale.US, mb / 1024f)
        } else {
            "%.1f MB".format(Locale.US, mb)
        }
    }

    fun toMovieItem(): MovieItem {
        val mappedCategory = MovieCategory.values().firstOrNull {
            it.id.equals(category, ignoreCase = true)
        } ?: MovieCategory.ACTION
        val mins = (durationSeconds / 60).coerceAtLeast(1)
        return MovieItem(
            id = id,
            title = title,
            category = mappedCategory,
            ageRating = "10+",
            descriptionSw = description,
            descriptionFr = description,
            posterRes = null,
            posterUrl = thumbnailUrl,
            djName = null,
            videoUrl = videoUrl,
            isDriveLink = false,
            isVipOnly = isVipOnly,
            releaseYear = formattedDate(),
            durationMinutes = mins,
            rating = 4.9,
            viewsCount = 0,
            isSeries = false,
            episodes = emptyList()
        )
    }
}
