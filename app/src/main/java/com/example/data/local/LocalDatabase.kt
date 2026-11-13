package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.R
import com.example.model.*
import java.net.URLDecoder
import java.net.URLEncoder

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val country: String,
    val emailOrPhone: String,
    val passwordHash: String,
    val salt: String,
    val ageTierName: String,
    val vipTierName: String,
    val roleName: String,
    val preferredQualityName: String,
    val actionDownloadsThisMonth: Int,
    val seriesDownloadsThisMonth: Int,
    val favoritesMovieIdsJson: String,
    val downloadedMovieIdsJson: String,
    val isEmailVerified: Boolean,
    val djStageName: String?,
    val hasDashboardAccess: Boolean,
    val vipActivatedAt: Long?,
    val vipExpiresAt: Long?
)

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: String,
    val title: String,
    val categoryName: String,
    val ageRating: String,
    val descriptionSw: String,
    val descriptionFr: String,
    val posterKey: String?,
    val posterUrl: String,
    val djName: String?,
    val videoUrl: String,
    val isDriveLink: Boolean,
    val isVipOnly: Boolean,
    val releaseYear: String,
    val durationMinutes: Int,
    val rating: Double,
    val viewsCount: Int,
    val isSeries: Boolean,
    val episodesJson: String,
    val sortOrder: Long
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val movieId: String,
    val userName: String,
    val userCountry: String,
    val vipTierName: String,
    val commentText: String,
    val timestamp: String,
    val rating: Int,
    val sortOrder: Long
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val titleSw: String,
    val titleFr: String,
    val messageSw: String,
    val messageFr: String,
    val timeAgo: String,
    val isRead: Boolean,
    val sortOrder: Long
)

@Entity(tableName = "download_tasks")
data class DownloadTaskEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val movieId: String,
    val movieTitle: String,
    val posterKey: String?,
    val categoryName: String,
    val progress: Float,
    val percentage: Int,
    val downloadedMb: Float,
    val totalMb: Float,
    val speed: String,
    val statusName: String
)

@Entity(tableName = "app_state")
data class AppStateEntity(
    @PrimaryKey val stateKey: String = "main_session",
    val loggedInUserId: String? = null,
    val languageCode: String = "sw",
    val hasCompletedOnboarding: Boolean = false,
    val lastBottomNavTab: String = "home",
    val isSeeded: Boolean = false
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<UserEntity>)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
}

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY sortOrder DESC")
    suspend fun getAllMovies(): List<MovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMovie(movie: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(movies: List<MovieEntity>)

    @Query("DELETE FROM movies WHERE id = :movieId")
    suspend fun deleteMovieById(movieId: String)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments ORDER BY sortOrder DESC")
    suspend fun getAllComments(): List<CommentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertComment(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(comments: List<CommentEntity>)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY sortOrder DESC")
    suspend fun getAllNotifications(): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(notifications: List<NotificationEntity>)
}

@Dao
interface DownloadTaskDao {
    @Query("SELECT * FROM download_tasks WHERE userId = :userId")
    suspend fun getDownloadsForUser(userId: String): List<DownloadTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDownload(task: DownloadTaskEntity)

    @Query("DELETE FROM download_tasks WHERE id = :taskId AND userId = :userId")
    suspend fun deleteDownload(taskId: String, userId: String)
}

@Dao
interface AppStateDao {
    @Query("SELECT * FROM app_state WHERE stateKey = 'main_session' LIMIT 1")
    suspend fun getSession(): AppStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(state: AppStateEntity)
}

@Database(
    entities = [
        UserEntity::class,
        MovieEntity::class,
        CommentEntity::class,
        NotificationEntity::class,
        DownloadTaskEntity::class,
        AppStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MovieSwDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun movieDao(): MovieDao
    abstract fun commentDao(): CommentDao
    abstract fun notificationDao(): NotificationDao
    abstract fun downloadTaskDao(): DownloadTaskDao
    abstract fun appStateDao(): AppStateDao

    companion object {
        @Volatile
        private var INSTANCE: MovieSwDatabase? = null

        fun getInstance(context: Context): MovieSwDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MovieSwDatabase::class.java,
                    "moviesw_persistent_db"
                ).fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

object EntityMappers {
    private fun enc(value: String): String = URLEncoder.encode(value, "UTF-8")
    private fun dec(value: String): String = URLDecoder.decode(value, "UTF-8")

    private fun parseVipTier(name: String): VipTier = when (name) {
        "VIP_PRO", "VIP_PRO_MAX" -> VipTier.VIP_PRO
        else -> VipTier.BURE
    }

    fun encodeStringList(items: List<String>): String {
        if (items.isEmpty()) return ""
        return items.joinToString(",") { enc(it) }
    }

    fun decodeStringList(encoded: String): List<String> {
        if (encoded.isBlank()) return emptyList()
        return try {
            encoded.split(",").filter { it.isNotBlank() }.map { dec(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun encodeEpisodes(episodes: List<Episode>): String {
        if (episodes.isEmpty()) return ""
        return episodes.joinToString("\n") { ep ->
            listOf(
                ep.episodeNumber.toString(),
                enc(ep.titleSw),
                enc(ep.titleFr),
                ep.durationMinutes.toString(),
                enc(ep.driveLink)
            ).joinToString("|")
        }
    }

    fun decodeEpisodes(encoded: String): List<Episode> {
        if (encoded.isBlank()) return emptyList()
        return try {
            encoded.split("\n").filter { it.isNotBlank() }.mapIndexedNotNull { idx, row ->
                val parts = row.split("|")
                if (parts.size < 5) null else Episode(
                    episodeNumber = parts[0].toIntOrNull() ?: (idx + 1),
                    titleSw = dec(parts[1]),
                    titleFr = dec(parts[2]),
                    durationMinutes = parts[3].toIntOrNull() ?: 45,
                    driveLink = dec(parts[4])
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun posterResToKey(resId: Int?): String? = when (resId) {
        R.drawable.img_onboarding_1 -> "img_onboarding_1"
        R.drawable.img_onboarding_2 -> "img_onboarding_2"
        R.drawable.img_onboarding_3 -> "img_onboarding_3"
        else -> null
    }

    fun keyToPosterRes(key: String?): Int? = when (key) {
        "img_onboarding_1" -> R.drawable.img_onboarding_1
        "img_onboarding_2" -> R.drawable.img_onboarding_2
        "img_onboarding_3" -> R.drawable.img_onboarding_3
        else -> null
    }

    fun UserAccount.toEntity(): UserEntity = UserEntity(
        id = id,
        name = name,
        country = country,
        emailOrPhone = emailOrPhone,
        passwordHash = passwordHash,
        salt = salt,
        ageTierName = ageTier.name,
        vipTierName = vipTier.name,
        roleName = role.name,
        preferredQualityName = preferredQuality.name,
        actionDownloadsThisMonth = actionDownloadsThisMonth,
        seriesDownloadsThisMonth = seriesDownloadsThisMonth,
        favoritesMovieIdsJson = encodeStringList(favoritesMovieIds),
        downloadedMovieIdsJson = encodeStringList(downloadedMovieIds),
        isEmailVerified = isEmailVerified,
        djStageName = djStageName,
        hasDashboardAccess = hasDashboardAccess,
        vipActivatedAt = vipActivatedAt,
        vipExpiresAt = vipExpiresAt
    )

    fun UserEntity.toDomain(): UserAccount = UserAccount(
        id = id,
        name = name,
        country = country,
        emailOrPhone = emailOrPhone,
        passwordHash = passwordHash,
        salt = salt,
        ageTier = runCatching { AgeTier.valueOf(ageTierName) }.getOrDefault(AgeTier.AGE_18_PLUS),
        vipTier = parseVipTier(vipTierName),
        role = runCatching { UserRole.valueOf(roleName) }.getOrDefault(UserRole.VIEWER),
        preferredQuality = runCatching { VideoQuality.valueOf(preferredQualityName) }.getOrDefault(VideoQuality.P360),
        actionDownloadsThisMonth = actionDownloadsThisMonth,
        seriesDownloadsThisMonth = seriesDownloadsThisMonth,
        favoritesMovieIds = decodeStringList(favoritesMovieIdsJson),
        downloadedMovieIds = decodeStringList(downloadedMovieIdsJson),
        isEmailVerified = isEmailVerified,
        djStageName = djStageName,
        hasDashboardAccess = hasDashboardAccess,
        vipActivatedAt = vipActivatedAt,
        vipExpiresAt = vipExpiresAt
    )

    fun MovieItem.toEntity(sortOrder: Long): MovieEntity = MovieEntity(
        id = id,
        title = title,
        categoryName = category.name,
        ageRating = ageRating,
        descriptionSw = descriptionSw,
        descriptionFr = descriptionFr,
        posterKey = posterResToKey(posterRes),
        posterUrl = posterUrl,
        djName = djName,
        videoUrl = videoUrl,
        isDriveLink = isDriveLink,
        isVipOnly = isVipOnly,
        releaseYear = releaseYear,
        durationMinutes = durationMinutes,
        rating = rating,
        viewsCount = viewsCount,
        isSeries = isSeries,
        episodesJson = encodeEpisodes(episodes),
        sortOrder = sortOrder
    )

    fun MovieEntity.toDomain(): MovieItem = MovieItem(
        id = id,
        title = title,
        category = runCatching { MovieCategory.valueOf(categoryName) }.getOrDefault(MovieCategory.ACTION),
        ageRating = ageRating,
        descriptionSw = descriptionSw,
        descriptionFr = descriptionFr,
        posterRes = keyToPosterRes(posterKey),
        posterUrl = posterUrl,
        djName = djName,
        videoUrl = videoUrl,
        isDriveLink = isDriveLink,
        isVipOnly = isVipOnly,
        releaseYear = releaseYear,
        durationMinutes = durationMinutes,
        rating = rating,
        viewsCount = viewsCount,
        isSeries = isSeries,
        episodes = decodeEpisodes(episodesJson)
    )

    fun CommentItem.toEntity(sortOrder: Long): CommentEntity = CommentEntity(
        id = id,
        movieId = movieId,
        userName = userName,
        userCountry = userCountry,
        vipTierName = vipTier.name,
        commentText = commentText,
        timestamp = timestamp,
        rating = rating,
        sortOrder = sortOrder
    )

    fun CommentEntity.toDomain(): CommentItem = CommentItem(
        id = id,
        movieId = movieId,
        userName = userName,
        userCountry = userCountry,
        vipTier = parseVipTier(vipTierName),
        commentText = commentText,
        timestamp = timestamp,
        rating = rating
    )

    fun NotificationItem.toEntity(sortOrder: Long): NotificationEntity = NotificationEntity(
        id = id,
        titleSw = titleSw,
        titleFr = titleFr,
        messageSw = messageSw,
        messageFr = messageFr,
        timeAgo = timeAgo,
        isRead = isRead,
        sortOrder = sortOrder
    )

    fun NotificationEntity.toDomain(): NotificationItem = NotificationItem(
        id = id,
        titleSw = titleSw,
        titleFr = titleFr,
        messageSw = messageSw,
        messageFr = messageFr,
        timeAgo = timeAgo,
        isRead = isRead
    )

    fun DownloadTask.toEntity(userId: String): DownloadTaskEntity = DownloadTaskEntity(
        id = id,
        userId = userId,
        movieId = movieId,
        movieTitle = movieTitle,
        posterKey = posterResToKey(moviePosterRes),
        categoryName = category.name,
        progress = progress,
        percentage = percentage,
        downloadedMb = downloadedMb,
        totalMb = totalMb,
        speed = speed,
        statusName = status.name
    )

    fun DownloadTaskEntity.toDomain(): DownloadTask = DownloadTask(
        id = id,
        movieId = movieId,
        movieTitle = movieTitle,
        moviePosterRes = keyToPosterRes(posterKey),
        category = runCatching { MovieCategory.valueOf(categoryName) }.getOrDefault(MovieCategory.ACTION),
        progress = progress,
        percentage = percentage,
        downloadedMb = downloadedMb,
        totalMb = totalMb,
        speed = speed,
        status = runCatching { DownloadStatus.valueOf(statusName) }.getOrDefault(DownloadStatus.COMPLETED)
    )
}
