package com.example.data

import android.content.Context
import com.example.R
import com.example.data.local.AppStateEntity
import com.example.data.local.EntityMappers.toDomain
import com.example.data.local.EntityMappers.toEntity
import com.example.data.local.MovieSwDatabase
import com.example.model.*
import com.example.util.AppUpdateManager
import com.example.util.RemoteUpdateInfo
import com.example.util.SecurityUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

object MovieRepository {

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    private var database: MovieSwDatabase? = null

    private val _isStorageReady = MutableStateFlow(false)
    val isStorageReady: StateFlow<Boolean> = _isStorageReady.asStateFlow()

    private val _savedSessionState = MutableStateFlow(AppStateEntity())
    val savedSessionState: StateFlow<AppStateEntity> = _savedSessionState.asStateFlow()

    private val _persistedDownloads = MutableStateFlow<Map<String, DownloadTask>>(emptyMap())
    val persistedDownloads: StateFlow<Map<String, DownloadTask>> = _persistedDownloads.asStateFlow()

    @Volatile
    private var appContext: Context? = null

    private val _publishedAppUpdate = MutableStateFlow<RemoteUpdateInfo?>(null)
    val publishedAppUpdate: StateFlow<RemoteUpdateInfo?> = _publishedAppUpdate.asStateFlow()

    // Salt and hash for default admin and demo user
    private val adminSalt = SecurityUtil.generateSalt()
    private val adminHash = SecurityUtil.hashPassword("boBO200@", adminSalt)

    private val demoSalt = SecurityUtil.generateSalt()
    private val demoHash = SecurityUtil.hashPassword("viewer1234", demoSalt)

    private val _users = MutableStateFlow<List<UserAccount>>(
        listOf(
            UserAccount(
                id = "admin_01",
                name = "Yesu Kishilo (Admin Movie SW)",
                country = "DRC",
                emailOrPhone = "yesukishilo08@gmail.com",
                passwordHash = adminHash,
                salt = adminSalt,
                ageTier = AgeTier.AGE_18_PLUS,
                vipTier = VipTier.VIP_PRO,
                role = UserRole.ADMIN,
                preferredQuality = VideoQuality.P8K,
                isEmailVerified = true
            ),
            UserAccount(
                id = "user_01",
                name = "Juma Ally",
                country = "Tanzania",
                emailOrPhone = "viewer@moviesw.com",
                passwordHash = demoHash,
                salt = demoSalt,
                ageTier = AgeTier.AGE_18_PLUS,
                vipTier = VipTier.BURE,
                role = UserRole.VIEWER,
                preferredQuality = VideoQuality.P360,
                favoritesMovieIds = listOf("mov_01", "mov_03"),
                downloadedMovieIds = emptyList(),
                isEmailVerified = true
            ),
            UserAccount(
                id = "user_02",
                name = "Sarah Mwangi",
                country = "Kenya",
                emailOrPhone = "sarah.mwangi@gmail.com",
                passwordHash = demoHash,
                salt = demoSalt,
                ageTier = AgeTier.AGE_10_PLUS,
                vipTier = VipTier.VIP_PRO,
                role = UserRole.VIEWER,
                preferredQuality = VideoQuality.P1080,
                actionDownloadsThisMonth = 3,
                seriesDownloadsThisMonth = 1,
                favoritesMovieIds = listOf("mov_02", "mov_04"),
                downloadedMovieIds = listOf("mov_02"),
                isEmailVerified = true,
                vipActivatedAt = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000),
                vipExpiresAt = System.currentTimeMillis() + (28L * 24 * 60 * 60 * 1000)
            ),
            UserAccount(
                id = "user_03",
                name = "Jean-Luc Kabila",
                country = "DRC (Kongo)",
                emailOrPhone = "+243991234567",
                passwordHash = demoHash,
                salt = demoSalt,
                ageTier = AgeTier.AGE_18_PLUS,
                vipTier = VipTier.VIP_PRO,
                role = UserRole.VIEWER,
                preferredQuality = VideoQuality.P8K,
                actionDownloadsThisMonth = 14,
                seriesDownloadsThisMonth = 6,
                favoritesMovieIds = listOf("mov_01", "mov_05"),
                downloadedMovieIds = listOf("mov_01", "mov_05"),
                isEmailVerified = false,
                vipActivatedAt = System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000),
                vipExpiresAt = System.currentTimeMillis() + (29L * 24 * 60 * 60 * 1000)
            ),
            UserAccount(
                id = "user_04",
                name = "Amina Uwase",
                country = "Rwanda",
                emailOrPhone = "amina@gmail.com",
                passwordHash = demoHash,
                salt = demoSalt,
                ageTier = AgeTier.AGE_10_PLUS,
                vipTier = VipTier.BURE,
                role = UserRole.VIEWER,
                preferredQuality = VideoQuality.P144,
                isEmailVerified = true
            ),
            // DJ Accounts created by Admin
            UserAccount(
                id = "dj_01",
                name = "DJ Afro (Tafsiri Pro)",
                country = "Kenya",
                emailOrPhone = "djafro@moviesw.com",
                passwordHash = demoHash,
                salt = demoSalt,
                ageTier = AgeTier.AGE_18_PLUS,
                vipTier = VipTier.VIP_PRO,
                role = UserRole.DJ,
                preferredQuality = VideoQuality.P1080,
                isEmailVerified = true,
                djStageName = "DJ Afro",
                hasDashboardAccess = true,
                vipActivatedAt = System.currentTimeMillis(),
                vipExpiresAt = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
            ),
            UserAccount(
                id = "dj_02",
                name = "DJ Murphy",
                country = "Tanzania",
                emailOrPhone = "djmurphy@moviesw.com",
                passwordHash = demoHash,
                salt = demoSalt,
                ageTier = AgeTier.AGE_18_PLUS,
                vipTier = VipTier.VIP_PRO,
                role = UserRole.DJ,
                preferredQuality = VideoQuality.P1080,
                isEmailVerified = true,
                djStageName = "DJ Murphy",
                hasDashboardAccess = true,
                vipActivatedAt = System.currentTimeMillis(),
                vipExpiresAt = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
            ),
            UserAccount(
                id = "dj_03",
                name = "DJ Black",
                country = "DRC",
                emailOrPhone = "djblack@moviesw.com",
                passwordHash = demoHash,
                salt = demoSalt,
                ageTier = AgeTier.AGE_18_PLUS,
                vipTier = VipTier.BURE,
                role = UserRole.DJ,
                preferredQuality = VideoQuality.P360,
                isEmailVerified = true,
                djStageName = "DJ Black",
                hasDashboardAccess = false // Demonstrates access pending admin approval!
            )
        )
    )
    val users: StateFlow<List<UserAccount>> = _users.asStateFlow()

    private val _movies = MutableStateFlow<List<MovieItem>>(
        listOf(
            MovieItem(
                id = "mov_01",
                title = "Kisasi Cha Kung Fu",
                category = MovieCategory.ACTION,
                ageRating = "18+",
                descriptionSw = "Hadithi ya mpiganaji anayeapa kulipiza kisasi cha hekalu lake la Shaolin lililoteketezwa. Mapigano makali na ucheshi kutoka kwa DJ Afro.",
                descriptionFr = "L'histoire épique d'un maître d'arts martiaux vengeant la destruction de son temple Shaolin. Combats grandioses commentés avec passion.",
                posterRes = R.drawable.img_onboarding_1,
                djName = "DJ Afro",
                videoUrl = "https://drive.google.com/file/d/sample-kungfu-kisasi/view",
                isDriveLink = true,
                isVipOnly = false,
                releaseYear = "2024",
                durationMinutes = 118,
                rating = 4.9,
                viewsCount = 38500
            ),
            MovieItem(
                id = "mov_02",
                title = "Upendo Mjini Seoul",
                category = MovieCategory.KOREAN_DRAMA,
                ageRating = "10+",
                descriptionSw = "Tamthilia ya kimapenzi kuhusu binti wa kitanzania anayepata ufadhili wa masomo Korea na kukutana na mrithi wa kampuni kubwa ya muziki.",
                descriptionFr = "Une comédie romantique touchante entre une étudiante étrangère boursière et le jeune héritier d'une maison de disques à Séoul.",
                posterRes = R.drawable.img_onboarding_2,
                djName = "DJ Murphy",
                videoUrl = "https://drive.google.com/file/d/sample-upendo-seoul/view",
                isDriveLink = true,
                isVipOnly = false,
                releaseYear = "2024",
                durationMinutes = 60,
                rating = 4.8,
                viewsCount = 29400,
                isSeries = true,
                episodes = listOf(
                    Episode(1, "Mwanzo wa Safari", "Le Début du Voyage", 58, "https://drive.google.com/ep1"),
                    Episode(2, "Mkutano wa Kwanza", "Première Rencontre", 62, "https://drive.google.com/ep2"),
                    Episode(3, "Siri za Ikulu ya Muziki", "Secrets de la Cité", 55, "https://drive.google.com/ep3")
                )
            ),
            MovieItem(
                id = "mov_03",
                title = "Mfalme Wa Wuxia & Mapanga 7",
                category = MovieCategory.CHINESE_DRAMA,
                ageRating = "10+",
                descriptionSw = "Filamu ya Kichina ya wuxia inayohusisha mapanga saba ya kichawi yanayolinda amani ya nasaba ya kale. DJ Black anashuka na mistari mikali.",
                descriptionFr = "Une saga d'arts martiaux wuxia légendaire autour des sept épées mystiques protégeant l'Empire millénaire.",
                posterRes = R.drawable.img_onboarding_2,
                djName = "DJ Black",
                videoUrl = "https://drive.google.com/file/d/sample-wuxia-7mapanga/view",
                isDriveLink = true,
                isVipOnly = false,
                releaseYear = "2023",
                durationMinutes = 126,
                rating = 4.7,
                viewsCount = 21900
            ),
            MovieItem(
                id = "mov_04",
                title = "Kivuli Cha Mapenzi",
                category = MovieCategory.LOVE_STORY,
                ageRating = "10+",
                descriptionSw = "Hadithi ya dhati ya mapenzi kati ya vijana wawili wanaokabiliana na vikwazo vya koo zao jijini Mombasa na Dar es Salaam.",
                descriptionFr = "Une émouvante histoire d'amour contemporaine affrontant les traditions familiales le long de la côte swahilie.",
                posterRes = R.drawable.img_onboarding_1,
                djName = "DJ Smith",
                videoUrl = "https://drive.google.com/file/d/sample-kivuli-mapenzi/view",
                isDriveLink = true,
                isVipOnly = false,
                releaseYear = "2024",
                durationMinutes = 104,
                rating = 4.6,
                viewsCount = 18700
            ),
            MovieItem(
                id = "mov_05",
                title = "Komando Wa Giza (VIP Exclusive)",
                category = MovieCategory.ACTION,
                ageRating = "18+",
                descriptionSw = "Kikosi maalum cha makomando kinapenya ndani ya ngome ya maadui wa kimataifa. Filamu maalum iliyopakiwa moja kwa moja kutoka kwenye faili la Admin.",
                descriptionFr = "Mission commando ultra-secrète en territoire hostile. Édition spéciale issue des archives exclusives de l'administrateur.",
                posterRes = R.drawable.img_onboarding_3,
                djName = "DJ Vasco",
                videoUrl = "https://drive.google.com/file/d/admin-file-komando-giza/view",
                isDriveLink = false,
                isVipOnly = true, // Locked for Bure users!
                releaseYear = "2024",
                durationMinutes = 135,
                rating = 4.95,
                viewsCount = 45200
            ),
            MovieItem(
                id = "mov_06",
                title = "Vita Vya Mawe Meusi (VIP Series)",
                category = MovieCategory.DRAMA,
                ageRating = "18+",
                descriptionSw = "Mfululizo wa vita vya kijasusi na biashara ya siri ya madini ya thamani. Mkusanyiko wa kipekee kwa wateja wa VIP tu.",
                descriptionFr = "Série d'espionnage et de trafic clandestin de minerais rares. Collection ultra-haute définition réservée aux abonnés VIP.",
                posterRes = R.drawable.img_onboarding_3,
                djName = "DJ Afro",
                videoUrl = "https://drive.google.com/file/d/admin-file-mawe-meusi/view",
                isDriveLink = false,
                isVipOnly = true, // Locked for Bure users!
                releaseYear = "2024",
                durationMinutes = 55,
                rating = 4.88,
                viewsCount = 33800,
                isSeries = true,
                episodes = listOf(
                    Episode(1, "Operesheni Kongo", "Opération Congo", 54, "https://drive.google.com/ep1"),
                    Episode(2, "Msaliti Ndani ya Kambi", "Le Traître", 58, "https://drive.google.com/ep2")
                )
            ),
            MovieItem(
                id = "mov_07",
                title = "Daktari Wa Kijiji (K-Drama)",
                category = MovieCategory.DRAMA,
                ageRating = "10+",
                descriptionSw = "Daktari bingwa wa upasuaji anakataa mamilioni ya jiji na kuamua kuokoa maisha vijijini. Tafsiri iliyojaa hisia kali.",
                descriptionFr = "Un chirurgien émérite quitte le confort de la capitale pour transformer un hôpital rural oublié.",
                posterRes = R.drawable.img_onboarding_2,
                djName = "DJ Murphy",
                videoUrl = "https://drive.google.com/file/d/sample-daktari-kijiji/view",
                isDriveLink = true,
                isVipOnly = false,
                releaseYear = "2023",
                durationMinutes = 65,
                rating = 4.9,
                viewsCount = 31200
            ),
            MovieItem(
                id = "mov_08",
                title = "Siri Ya Ikulu Ya Beijing",
                category = MovieCategory.CHINESE_DRAMA,
                ageRating = "18+",
                descriptionSw = "Mipango ya chinichini na fitina za kifalme kati ya mawaziri na makamanda wa kijeshi wa karne ya 16.",
                descriptionFr = "Complots palatiaux, intrigues politiques et duels d'honneur au cœur de la Cité Interdite du 16ème siècle.",
                posterRes = R.drawable.img_onboarding_2,
                djName = "DJ Black",
                videoUrl = "https://drive.google.com/file/d/sample-ikulu-beijing/view",
                isDriveLink = true,
                isVipOnly = false,
                releaseYear = "2024",
                durationMinutes = 112,
                rating = 4.75,
                viewsCount = 20500
            )
        )
    )
    val movies: StateFlow<List<MovieItem>> = _movies.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentItem>>(
        listOf(
            CommentItem("c1", "mov_01", "Rashidi Simba", "Tanzania", VipTier.VIP_PRO, "DJ Afro ametisha sana kwenye hii filamu! Nimecheka mwanzo mwisho.", "Masaa 2 yaliyopita", 5),
            CommentItem("c2", "mov_01", "Baraka Ken", "Kenya", VipTier.BURE, "Mapigano ya Shaolin yanapendeza sana, tafsiri safi kabisa.", "Masaa 5 yaliyopita", 5),
            CommentItem("c3", "mov_02", "Chantal M.", "DRC", VipTier.VIP_PRO, "Cette série coréenne est incroyable ! Très belle qualité d'image en VIP.", "Jana", 5),
            CommentItem("c4", "mov_05", "Moses Otieno", "Kenya", VipTier.VIP_PRO, "Faili la Admin lipo vizuri sana, ubora wa 1080p unalipa fedha zangu za VIP.", "Siku 2 zilizopita", 5)
        )
    )
    val comments: StateFlow<List<CommentItem>> = _comments.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(
        listOf(
            NotificationItem(
                id = "notif_1",
                titleSw = "Filamu Mpya ya DJ Afro Imepakiwa!",
                titleFr = "Nouveau film de DJ Afro disponible !",
                messageSw = "Kisasi Cha Kung Fu kimeongezwa kwenye mkusanyiko wa Action. Tazama sasa ufurahie sauti ya DJ Afro.",
                messageFr = "Le chef d'œuvre d'arts martiaux est désormais en ligne avec le doublage swahili.",
                timeAgo = "Dakika 20 zilizopita"
            ),
            NotificationItem(
                id = "notif_2",
                titleSw = "Ofa Maalum ya VIP Pro",
                titleFr = "Offre Spéciale VIP Pro",
                messageSw = "Fungua filamu zote za VIP, ubora wa 8K na upakuaji bila kikomo kwa $3.99 tu kwa mwezi mzima.",
                messageFr = "Débloquez les téléchargements illimités et la haute définition pour 3.99 $ / mois.",
                timeAgo = "Masaa 3 yaliyopita"
            ),
            NotificationItem(
                id = "notif_3",
                titleSw = "Mitandao Yetu Rasmi ya Kijamii",
                titleFr = "Nos Réseaux Sociaux Officiels",
                messageSw = "Jiunge na group letu la WhatsApp na kurasa za Instagram & Facebook kupata matoleo mapya kila siku.",
                messageFr = "Rejoignez notre communauté sur WhatsApp, Instagram, Facebook et YouTube.",
                timeAgo = "Jana"
            )
        )
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _socialLinks = MutableStateFlow<List<SocialLink>>(
        listOf(
            SocialLink("soc_fb", "Facebook", "facebook", "https://facebook.com/moviesw.official", "@MovieSW.Official"),
            SocialLink("soc_wa", "WhatsApp", "whatsapp", "https://wa.me/255700000000", "+255 700 000 000"),
            SocialLink("soc_ig", "Instagram", "instagram", "https://instagram.com/moviesw_tz", "@moviesw_tz"),
            SocialLink("soc_yt", "YouTube", "youtube", "https://youtube.com/@movieswofficial", "Movie SW TV")
        )
    )
    val socialLinks: StateFlow<List<SocialLink>> = _socialLinks.asStateFlow()

    val countryStats = listOf(
        CountryStat("Tanzania", 46, 5240),
        CountryStat("Kenya", 28, 3180),
        CountryStat("DR Congo", 15, 1710),
        CountryStat("Rwanda", 7, 790),
        CountryStat("Burundi & Wengine", 4, 450)
    )

    // Persistence Initialization (called on app start)
    fun initialize(context: Context) {
        if (database != null) return
        synchronized(this) {
            if (database != null) return
            appContext = context.applicationContext
            val prefs = context.applicationContext.getSharedPreferences("moviesw_admin_update_prefs", Context.MODE_PRIVATE)
            prefs.getString("published_update_json", null)?.let { raw ->
                _publishedAppUpdate.value = AppUpdateManager.decodeStoredUpdateJson(raw)
            }
            val db = MovieSwDatabase.getInstance(context)
            database = db
            ioScope.launch {
                val existingSession = db.appStateDao().getSession()
                if (existingSession == null || !existingSession.isSeeded) {
                    // Seed defaults on very first launch
                    db.userDao().upsertAll(_users.value.map { it.toEntity() })
                    val baseTime = System.currentTimeMillis()
                    db.movieDao().upsertAll(_movies.value.mapIndexed { idx, m ->
                        m.toEntity(sortOrder = baseTime - idx)
                    })
                    db.commentDao().upsertAll(_comments.value.mapIndexed { idx, c ->
                        c.toEntity(sortOrder = baseTime - idx)
                    })
                    db.notificationDao().upsertAll(_notifications.value.mapIndexed { idx, n ->
                        n.toEntity(sortOrder = baseTime - idx)
                    })
                    val initialSession = AppStateEntity(
                        stateKey = "main_session",
                        loggedInUserId = null,
                        languageCode = "sw",
                        hasCompletedOnboarding = false,
                        lastBottomNavTab = "home",
                        isSeeded = true
                    )
                    db.appStateDao().saveSession(initialSession)
                    _savedSessionState.value = initialSession
                } else {
                    // Restore saved state from Room
                    val loadedUsers = db.userDao().getAllUsers().map { it.toDomain() }
                    if (loadedUsers.isNotEmpty()) {
                        _users.value = loadedUsers
                    }
                    val loadedMovies = db.movieDao().getAllMovies().map { it.toDomain() }
                    _movies.value = loadedMovies
                    val loadedComments = db.commentDao().getAllComments().map { it.toDomain() }
                    _comments.value = loadedComments
                    val loadedNotifications = db.notificationDao().getAllNotifications().map { it.toDomain() }
                    _notifications.value = loadedNotifications
                    _savedSessionState.value = existingSession

                    existingSession.loggedInUserId?.let { uid ->
                        val tasks = db.downloadTaskDao().getDownloadsForUser(uid).map { it.toDomain() }
                        _persistedDownloads.value = tasks.associateBy { it.id }
                    }
                }
                checkAndExpireVipSubscriptions()
                _isStorageReady.value = true
            }
        }
    }

    fun saveSession(
        loggedInUserId: String? = _savedSessionState.value.loggedInUserId,
        languageCode: String = _savedSessionState.value.languageCode,
        hasCompletedOnboarding: Boolean = _savedSessionState.value.hasCompletedOnboarding,
        lastBottomNavTab: String = _savedSessionState.value.lastBottomNavTab
    ) {
        val updated = _savedSessionState.value.copy(
            loggedInUserId = loggedInUserId,
            languageCode = languageCode,
            hasCompletedOnboarding = hasCompletedOnboarding,
            lastBottomNavTab = lastBottomNavTab,
            isSeeded = true
        )
        _savedSessionState.value = updated
        val db = database ?: return
        ioScope.launch {
            db.appStateDao().saveSession(updated)
            if (loggedInUserId != null) {
                val tasks = db.downloadTaskDao().getDownloadsForUser(loggedInUserId).map { it.toDomain() }
                _persistedDownloads.value = tasks.associateBy { it.id }
            } else {
                _persistedDownloads.value = emptyMap()
            }
        }
    }

    fun persistDownloadTask(userId: String, task: DownloadTask) {
        _persistedDownloads.value = _persistedDownloads.value + (task.id to task)
        val db = database ?: return
        ioScope.launch {
            db.downloadTaskDao().upsertDownload(task.toEntity(userId))
        }
    }

    fun removePersistedDownloadTask(userId: String, taskId: String) {
        _persistedDownloads.value = _persistedDownloads.value - taskId
        val db = database ?: return
        ioScope.launch {
            db.downloadTaskDao().deleteDownload(taskId, userId)
        }
    }

    private fun persistUser(user: UserAccount) {
        val db = database ?: return
        ioScope.launch { db.userDao().upsertUser(user.toEntity()) }
    }

    // User Operations
    fun registerUser(
        name: String,
        country: String,
        emailOrPhone: String,
        passwordPlain: String,
        ageTier: AgeTier,
        isEmailVerified: Boolean = false
    ): UserAccount {
        val salt = SecurityUtil.generateSalt()
        val hash = SecurityUtil.hashPassword(passwordPlain, salt)
        val newUser = UserAccount(
            id = UUID.randomUUID().toString(),
            name = name,
            country = country,
            emailOrPhone = emailOrPhone,
            passwordHash = hash,
            salt = salt,
            ageTier = ageTier,
            vipTier = VipTier.BURE,
            role = UserRole.VIEWER,
            preferredQuality = VideoQuality.P360,
            isEmailVerified = isEmailVerified
        )
        _users.value = _users.value + newUser
        persistUser(newUser)
        return newUser
    }

    fun verifyUserEmail(userId: String): Boolean {
        var updated = false
        _users.value = _users.value.map {
            if (it.id == userId) {
                updated = true
                val copy = it.copy(isEmailVerified = true)
                persistUser(copy)
                copy
            } else it
        }
        return updated
    }

    fun authenticateUser(identifier: String, passwordPlain: String): UserAccount? {
        val user = _users.value.find {
            it.emailOrPhone.equals(identifier.trim(), ignoreCase = true)
        } ?: return null

        val isValid = SecurityUtil.verifyPassword(passwordPlain, user.salt, user.passwordHash)
        return if (isValid) user else null
    }

    fun findUserByEmail(email: String): UserAccount? {
        return _users.value.find { it.emailOrPhone.equals(email.trim(), ignoreCase = true) }
    }

    fun resetPassword(email: String, newPasswordPlain: String): Boolean {
        val user = findUserByEmail(email) ?: return false
        val newSalt = SecurityUtil.generateSalt()
        val newHash = SecurityUtil.hashPassword(newPasswordPlain, newSalt)
        _users.value = _users.value.map {
            if (it.id == user.id) {
                val copy = it.copy(passwordHash = newHash, salt = newSalt)
                persistUser(copy)
                copy
            } else it
        }
        return true
    }

    fun updateUserVip(userId: String, newTier: VipTier) {
        val now = System.currentTimeMillis()
        val expiry = if (newTier == VipTier.BURE) null else now + (30L * 24 * 60 * 60 * 1000) // 1 month = 30 days
        _users.value = _users.value.map {
            if (it.id == userId) {
                val copy = it.copy(
                    vipTier = newTier,
                    vipActivatedAt = if (newTier == VipTier.BURE) null else now,
                    vipExpiresAt = expiry
                )
                persistUser(copy)
                copy
            } else it
        }
    }

    fun checkAndExpireVipSubscriptions(): List<UserAccount> {
        val now = System.currentTimeMillis()
        val expired = mutableListOf<UserAccount>()
        _users.value = _users.value.map { user ->
            if (user.role != UserRole.ADMIN && user.vipTier != VipTier.BURE && user.vipExpiresAt != null && now >= user.vipExpiresAt) {
                expired.add(user)
                val copy = user.copy(
                    vipTier = VipTier.BURE,
                    vipActivatedAt = null,
                    vipExpiresAt = null
                )
                persistUser(copy)
                copy
            } else user
        }
        return expired
    }

    // Admin DJ Account Operations
    fun createDjAccount(
        name: String,
        email: String,
        passwordPlain: String,
        djStageName: String,
        country: String,
        grantAccess: Boolean
    ): UserAccount {
        val salt = SecurityUtil.generateSalt()
        val hash = SecurityUtil.hashPassword(passwordPlain, salt)
        val now = System.currentTimeMillis()
        val newDj = UserAccount(
            id = "dj_" + UUID.randomUUID().toString().take(8),
            name = name,
            country = country,
            emailOrPhone = email.trim(),
            passwordHash = hash,
            salt = salt,
            ageTier = AgeTier.AGE_18_PLUS,
            vipTier = VipTier.VIP_PRO,
            role = UserRole.DJ,
            preferredQuality = VideoQuality.P1080,
            isEmailVerified = true,
            djStageName = djStageName.trim(),
            hasDashboardAccess = grantAccess,
            vipActivatedAt = now,
            vipExpiresAt = now + (30L * 24 * 60 * 60 * 1000)
        )
        _users.value = _users.value + newDj
        persistUser(newDj)
        return newDj
    }

    fun toggleDjDashboardAccess(userId: String, hasAccess: Boolean): Boolean {
        var found = false
        _users.value = _users.value.map {
            if (it.id == userId && it.role == UserRole.DJ) {
                found = true
                val copy = it.copy(hasDashboardAccess = hasAccess)
                persistUser(copy)
                copy
            } else it
        }
        return found
    }

    fun updateUserQuality(userId: String, quality: VideoQuality) {
        _users.value = _users.value.map {
            if (it.id == userId) {
                val copy = it.copy(preferredQuality = quality)
                persistUser(copy)
                copy
            } else it
        }
    }

    fun deleteUser(userId: String) {
        _users.value = _users.value.filter { it.id != userId }
        val db = database ?: return
        ioScope.launch { db.userDao().deleteUserById(userId) }
    }

    fun deleteMovie(movieId: String) {
        _movies.value = _movies.value.filter { it.id != movieId }
        val db = database ?: return
        ioScope.launch { db.movieDao().deleteMovieById(movieId) }
    }

    fun toggleFavorite(userId: String, movieId: String) {
        _users.value = _users.value.map { user ->
            if (user.id == userId) {
                val current = user.favoritesMovieIds
                val updated = if (current.contains(movieId)) {
                    current - movieId
                } else {
                    current + movieId
                }
                val copy = user.copy(favoritesMovieIds = updated)
                persistUser(copy)
                copy
            } else user
        }
    }

    fun downloadMovie(userId: String, movie: MovieItem): Boolean {
        val user = _users.value.find { it.id == userId } ?: return false
        if (!user.isVipActive()) {
            return false
        }
        val isAction = movie.category == MovieCategory.ACTION
        val isSeries = movie.isSeries

        val updatedAction = if (isAction) user.actionDownloadsThisMonth + 1 else user.actionDownloadsThisMonth
        val updatedSeries = if (isSeries) user.seriesDownloadsThisMonth + 1 else user.seriesDownloadsThisMonth
        val updatedDownloads = if (!user.downloadedMovieIds.contains(movie.id)) {
            user.downloadedMovieIds + movie.id
        } else user.downloadedMovieIds

        _users.value = _users.value.map {
            if (it.id == userId) {
                val copy = it.copy(
                    actionDownloadsThisMonth = updatedAction,
                    seriesDownloadsThisMonth = updatedSeries,
                    downloadedMovieIds = updatedDownloads
                )
                persistUser(copy)
                copy
            } else it
        }
        return true
    }

    // Admin / DJ Operations
    fun addMovie(
        title: String,
        category: MovieCategory,
        ageRating: String,
        descSw: String,
        descFr: String,
        djName: String?,
        videoUrl: String,
        isDriveLink: Boolean,
        isVipOnly: Boolean,
        isSeries: Boolean,
        episodesList: List<Episode>,
        posterUrl: String = ""
    ): MovieItem {
        val movie = MovieItem(
            id = "mov_" + System.currentTimeMillis(),
            title = title,
            category = category,
            ageRating = ageRating,
            descriptionSw = descSw,
            descriptionFr = descFr,
            posterRes = if (category == MovieCategory.ACTION) R.drawable.img_onboarding_1 else if (category == MovieCategory.KOREAN_DRAMA || category == MovieCategory.CHINESE_DRAMA) R.drawable.img_onboarding_2 else R.drawable.img_onboarding_3,
            posterUrl = posterUrl,
            djName = djName.takeIf { !it.isNullOrBlank() },
            videoUrl = videoUrl,
            isDriveLink = isDriveLink,
            isVipOnly = isVipOnly,
            releaseYear = "2024",
            durationMinutes = if (isSeries) 50 else 115,
            rating = 4.8,
            viewsCount = 100,
            isSeries = isSeries,
            episodes = episodesList
        )
        _movies.value = listOf(movie) + _movies.value
        val db = database
        if (db != null) {
            ioScope.launch {
                db.movieDao().upsertMovie(movie.toEntity(sortOrder = System.currentTimeMillis()))
            }
        }
        return movie
    }

    fun addComment(movieId: String, userName: String, userCountry: String, vipTier: VipTier, text: String, rating: Int = 5) {
        val comment = CommentItem(
            id = "comm_" + System.currentTimeMillis(),
            movieId = movieId,
            userName = userName,
            userCountry = userCountry,
            vipTier = vipTier,
            commentText = text,
            timestamp = "Sasa hivi",
            rating = rating
        )
        _comments.value = listOf(comment) + _comments.value
        val db = database
        if (db != null) {
            ioScope.launch {
                db.commentDao().upsertComment(comment.toEntity(sortOrder = System.currentTimeMillis()))
            }
        }
    }

    fun broadcastNotification(titleSw: String, titleFr: String, msgSw: String, msgFr: String) {
        val notif = NotificationItem(
            id = "notif_" + System.currentTimeMillis(),
            titleSw = titleSw,
            titleFr = titleFr,
            messageSw = msgSw,
            messageFr = msgFr,
            timeAgo = "Sasa hivi"
        )
        _notifications.value = listOf(notif) + _notifications.value
        val db = database
        if (db != null) {
            ioScope.launch {
                db.notificationDao().upsertNotification(notif.toEntity(sortOrder = System.currentTimeMillis()))
            }
        }
    }

    fun publishAppUpdate(info: RemoteUpdateInfo) {
        _publishedAppUpdate.value = info
        val ctx = appContext ?: return
        val prefs = ctx.getSharedPreferences("moviesw_admin_update_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("published_update_json", AppUpdateManager.encodeUpdateJson(info)).apply()
    }

    fun clearPublishedAppUpdate() {
        _publishedAppUpdate.value = null
        val ctx = appContext ?: return
        val prefs = ctx.getSharedPreferences("moviesw_admin_update_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("published_update_json").apply()
    }
}
