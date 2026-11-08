package com.example.model

enum class Language(val code: String, val displayName: String) {
    SW("sw", "Kiswahili"),
    FR("fr", "Français")
}

enum class UserRole {
    VIEWER,
    ADMIN,
    DJ
}

enum class AgeTier(val label: String, val minAge: Int) {
    AGE_10_PLUS("10+", 10),
    AGE_18_PLUS("18+", 18)
}

enum class VipTier(
    val id: String,
    val titleSw: String,
    val titleFr: String,
    val priceSw: String,
    val priceFr: String,
    val maxQuality: VideoQuality,
    val actionDownloadsLimit: Int,
    val seriesDownloadsLimit: Int
) {
    BURE(
        id = "bure",
        titleSw = "Bure (Free)",
        titleFr = "Gratuit",
        priceSw = "0 TSh / mwezi",
        priceFr = "Gratuit",
        maxQuality = VideoQuality.P360,
        actionDownloadsLimit = 0,
        seriesDownloadsLimit = 0
    ),
    VIP_PRO(
        id = "vip_pro",
        titleSw = "VIP Pro",
        titleFr = "VIP Pro",
        priceSw = "2.99 $ / mwezi",
        priceFr = "2.99 $ / mois",
        maxQuality = VideoQuality.P1080,
        actionDownloadsLimit = 10,
        seriesDownloadsLimit = 5
    ),
    VIP_PRO_MAX(
        id = "vip_pro_max",
        titleSw = "VIP Pro Max",
        titleFr = "VIP Pro Max",
        priceSw = "5.99 $ / mwezi",
        priceFr = "5.99 $ / mois",
        maxQuality = VideoQuality.P8K,
        actionDownloadsLimit = 9999, // unlimited
        seriesDownloadsLimit = 9999
    )
}

enum class VideoQuality(val label: String, val requiresVip: Boolean) {
    P144("144p", false),
    P360("360p", false),
    P750("750p", true),
    P1080("1080p Full HD", true),
    P8K("8K Ultra HD", true)
}

enum class MovieCategory(val id: String, val nameSw: String, val nameFr: String) {
    ALL("all", "Zote", "Tout"),
    DRAMA("drama", "Drama", "Drame"),
    ACTION("action", "Action", "Action"),
    LOVE_STORY("love_story", "Love Story", "Romance"),
    KOREAN_DRAMA("korean", "Drama za Korea", "Drames Coréens"),
    CHINESE_DRAMA("chinese", "Filamu za China", "Drames Chinois"),
    DJ_DUBBED("dj_dubbed", "Zilizotafsiriwa na Ma-DJ", "Doublées par les DJs")
}

data class Episode(
    val episodeNumber: Int,
    val titleSw: String,
    val titleFr: String,
    val durationMinutes: Int,
    val driveLink: String
)

/**
 * Core movie data structure storing title, description, thumbnail URL, and language (Swahili or French).
 */
data class Movie(
    val id: String = "",
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val language: Language = Language.SW, // Swahili (Language.SW) or French (Language.FR)
    val category: MovieCategory = MovieCategory.ACTION,
    val ageRating: String = "10+", // "10+" or "18+"
    val djName: String? = null,
    val videoUrl: String = "",
    val isDriveLink: Boolean = true,
    val isVipOnly: Boolean = false,
    val releaseYear: String = "2024",
    val durationMinutes: Int = 115,
    val rating: Double = 4.8,
    val viewsCount: Int = 0,
    val isSeries: Boolean = false,
    val episodes: List<Episode> = emptyList()
) {
    fun toMovieItem(): MovieItem = MovieItem(
        id = id.ifBlank { "mov_" + System.currentTimeMillis() },
        title = title,
        category = category,
        ageRating = ageRating,
        descriptionSw = if (language == Language.SW) description else "",
        descriptionFr = if (language == Language.FR) description else "",
        posterUrl = thumbnailUrl,
        djName = djName,
        videoUrl = videoUrl,
        isDriveLink = isDriveLink,
        isVipOnly = isVipOnly,
        releaseYear = releaseYear,
        durationMinutes = durationMinutes,
        rating = rating,
        viewsCount = viewsCount,
        isSeries = isSeries,
        episodes = episodes
    )
}

data class MovieItem(
    val id: String,
    val title: String,
    val category: MovieCategory,
    val ageRating: String, // "10+" or "18+"
    val descriptionSw: String,
    val descriptionFr: String,
    val posterRes: Int? = null,
    val posterUrl: String = "",
    val djName: String? = null, // e.g. "DJ Afro", "DJ Murphy", "DJ Black", "DJ Vasco"
    val videoUrl: String, // Google Drive stream URL or web link
    val isDriveLink: Boolean = true,
    val isVipOnly: Boolean = false, // Uploaded from admin file - locked for Bure
    val releaseYear: String = "2024",
    val durationMinutes: Int = 115,
    val rating: Double = 4.8,
    val viewsCount: Int = 1240,
    val isSeries: Boolean = false,
    val episodes: List<Episode> = emptyList()
) {
    fun toMovie(lang: Language = Language.SW): Movie = Movie(
        id = id,
        title = title,
        description = if (lang == Language.SW) descriptionSw else descriptionFr,
        thumbnailUrl = posterUrl,
        language = lang,
        category = category,
        ageRating = ageRating,
        djName = djName,
        videoUrl = videoUrl,
        isDriveLink = isDriveLink,
        isVipOnly = isVipOnly,
        releaseYear = releaseYear,
        durationMinutes = durationMinutes,
        rating = rating,
        viewsCount = viewsCount,
        isSeries = isSeries,
        episodes = episodes
    )
}

data class CommentItem(
    val id: String,
    val movieId: String,
    val userName: String,
    val userCountry: String,
    val vipTier: VipTier,
    val commentText: String,
    val timestamp: String,
    val rating: Int = 5
)

data class NotificationItem(
    val id: String,
    val titleSw: String,
    val titleFr: String,
    val messageSw: String,
    val messageFr: String,
    val timeAgo: String,
    val isRead: Boolean = false
)

data class UserAccount(
    val id: String,
    val name: String,
    val country: String,
    val emailOrPhone: String,
    val passwordHash: String,
    val salt: String,
    val ageTier: AgeTier,
    val vipTier: VipTier,
    val role: UserRole = UserRole.VIEWER,
    val preferredQuality: VideoQuality = VideoQuality.P360,
    val actionDownloadsThisMonth: Int = 0,
    val seriesDownloadsThisMonth: Int = 0,
    val favoritesMovieIds: List<String> = emptyList(),
    val downloadedMovieIds: List<String> = emptyList(),
    val isEmailVerified: Boolean = false,
    val djStageName: String? = null,
    val hasDashboardAccess: Boolean = false,
    val vipActivatedAt: Long? = null,
    val vipExpiresAt: Long? = null
) {
    fun isVipActive(): Boolean {
        if (role == UserRole.ADMIN) return true
        if (vipTier == VipTier.BURE) return false
        val exp = vipExpiresAt ?: return false
        return System.currentTimeMillis() < exp
    }

    fun daysRemaining(): Int {
        val exp = vipExpiresAt ?: return 0
        val diff = exp - System.currentTimeMillis()
        return if (diff > 0) ((diff / (1000L * 60 * 60 * 24)) + 1).toInt() else 0
    }
}

data class SocialLink(
    val id: String,
    val name: String,
    val iconName: String,
    val url: String,
    val handle: String
)

data class CountryStat(
    val country: String,
    val percentage: Int,
    val userCount: Int
)

enum class DownloadStatus {
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

data class DownloadTask(
    val id: String,
    val movieId: String,
    val movieTitle: String,
    val moviePosterRes: Int?,
    val category: MovieCategory,
    val progress: Float = 0f,
    val percentage: Int = 0,
    val downloadedMb: Float = 0f,
    val totalMb: Float = 680f,
    val speed: String = "3.8 MB/s",
    val status: DownloadStatus = DownloadStatus.DOWNLOADING
)

data class AdminUploadState(
    val isUploading: Boolean = false,
    val progress: Float = 0f,
    val percentage: Int = 0,
    val mbUploaded: Float = 0f,
    val totalMb: Float = 950f,
    val speed: String = "5.2 MB/s",
    val fileName: String = "",
    val stageText: String = ""
)

data class PasswordResetRequest(
    val email: String,
    val otpCode: String,
    val createdAt: Long = System.currentTimeMillis()
)
