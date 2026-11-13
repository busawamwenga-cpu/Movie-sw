package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MovieRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class ScreenState {
    SPLASH,
    ONBOARDING,
    BIENVENUE,
    AUTH,
    MAIN_VIEWER,
    VIDEO_PLAYER,
    ADMIN_DASHBOARD,
    DJ_DASHBOARD
}

enum class AuthMode {
    SIGN_IN,
    SIGN_UP,
    ADMIN_LOGIN,
    FORGOT_PASSWORD
}

class MovieViewModel : ViewModel() {

    private val _language = MutableStateFlow(Language.SW)
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _currentScreen = MutableStateFlow(ScreenState.SPLASH)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _authMode = MutableStateFlow(AuthMode.SIGN_IN)
    val authMode: StateFlow<AuthMode> = _authMode.asStateFlow()

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _bottomNavTab = MutableStateFlow("home") // "home", "explore", "album", "account"
    val bottomNavTab: StateFlow<String> = _bottomNavTab.asStateFlow()

    private val _selectedMovie = MutableStateFlow<MovieItem?>(null)
    val selectedMovie: StateFlow<MovieItem?> = _selectedMovie.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MovieCategory.ALL)
    val selectedCategory: StateFlow<MovieCategory> = _selectedCategory.asStateFlow()

    private val _selectedDj = MutableStateFlow<String?>(null)
    val selectedDj: StateFlow<String?> = _selectedDj.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showVipModal = MutableStateFlow(false)
    val showVipModal: StateFlow<Boolean> = _showVipModal.asStateFlow()

    private val _showNotificationDialog = MutableStateFlow(false)
    val showNotificationDialog: StateFlow<Boolean> = _showNotificationDialog.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Gmail OTP & Reset Password States
    private val _sentOtpCode = MutableStateFlow<String?>(null)
    val sentOtpCode: StateFlow<String?> = _sentOtpCode.asStateFlow()

    private val _otpTargetEmail = MutableStateFlow<String?>(null)
    val otpTargetEmail: StateFlow<String?> = _otpTargetEmail.asStateFlow()

    // Email Verification & Anti-Hijack States
    private val _sentVerifyEmailOtp = MutableStateFlow<String?>(null)
    val sentVerifyEmailOtp: StateFlow<String?> = _sentVerifyEmailOtp.asStateFlow()

    private val _verifyEmailTarget = MutableStateFlow<String?>(null)
    val verifyEmailTarget: StateFlow<String?> = _verifyEmailTarget.asStateFlow()

    // Vidmate-Style Download Manager State
    private val _activeDownloads = MutableStateFlow<Map<String, DownloadTask>>(emptyMap())
    val activeDownloads: StateFlow<Map<String, DownloadTask>> = _activeDownloads.asStateFlow()

    // Admin Upload Progress State
    private val _adminUploadState = MutableStateFlow(AdminUploadState())
    val adminUploadState: StateFlow<AdminUploadState> = _adminUploadState.asStateFlow()

    val movies = MovieRepository.movies
    val users = MovieRepository.users
    val comments = MovieRepository.comments
    val notifications = MovieRepository.notifications
    val socialLinks = MovieRepository.socialLinks

    // Filtered movies based on user's age tier, search, category, DJ
    val filteredMovies: StateFlow<List<MovieItem>> = combine(
        movies,
        _currentUser,
        _selectedCategory,
        _selectedDj,
        _searchQuery
    ) { list, user, cat, dj, query ->
        list.filter { movie ->
            // Age restrictions: hide 18+ content if user is 10+
            val passesAge = if (user != null && user.ageTier == AgeTier.AGE_10_PLUS) {
                movie.ageRating != "18+"
            } else true

            // Category filter
            val passesCat = if (cat == MovieCategory.ALL) {
                true
            } else if (cat == MovieCategory.DJ_DUBBED) {
                !movie.djName.isNullOrBlank()
            } else {
                movie.category == cat
            }

            // DJ filter
            val passesDj = if (dj.isNullOrBlank()) true else movie.djName == dj

            // Search query
            val passesSearch = if (query.isBlank()) true else {
                movie.title.contains(query, ignoreCase = true) ||
                        movie.descriptionSw.contains(query, ignoreCase = true) ||
                        movie.descriptionFr.contains(query, ignoreCase = true) ||
                        (movie.djName != null && movie.djName.contains(query, ignoreCase = true))
            }

            passesAge && passesCat && passesDj && passesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // All available unique DJs
    val availableDjs: StateFlow<List<String>> = movies.map { list ->
        list.mapNotNull { it.djName }.distinct()
    }.stateIn(viewModelScope, SharingStarted.Lazily, listOf("DJ Afro", "DJ Murphy", "DJ Black", "DJ Vasco", "DJ Smith"))

    fun toggleLanguage() {
        val next = if (_language.value == Language.SW) Language.FR else Language.SW
        _language.value = next
        MovieRepository.saveSession(languageCode = next.code)
    }

    fun setLanguage(lang: Language) {
        _language.value = lang
        MovieRepository.saveSession(languageCode = lang.code)
    }

    fun navigateTo(screen: ScreenState) {
        _currentScreen.value = screen
    }

    fun setBottomNavTab(tab: String) {
        _bottomNavTab.value = tab
        MovieRepository.saveSession(lastBottomNavTab = tab)
    }

    fun onSplashFinished() {
        viewModelScope.launch {
            withTimeoutOrNull(1500) {
                MovieRepository.isStorageReady.first { it }
            }
            MovieRepository.checkAndExpireVipSubscriptions()
            val session = MovieRepository.savedSessionState.value
            _language.value = if (session.languageCode == Language.FR.code) Language.FR else Language.SW
            _bottomNavTab.value = session.lastBottomNavTab.ifBlank { "home" }

            val restoredUser = session.loggedInUserId?.let { uid ->
                MovieRepository.users.value.find { it.id == uid }
            }
            if (restoredUser != null) {
                if (restoredUser.role == UserRole.DJ && !restoredUser.hasDashboardAccess) {
                    MovieRepository.saveSession(loggedInUserId = null)
                    _currentUser.value = null
                    _currentScreen.value = ScreenState.BIENVENUE
                } else {
                    _currentUser.value = restoredUser
                    _activeDownloads.value = MovieRepository.persistedDownloads.value
                    _currentScreen.value = when (restoredUser.role) {
                        UserRole.ADMIN -> ScreenState.ADMIN_DASHBOARD
                        UserRole.DJ -> ScreenState.DJ_DASHBOARD
                        UserRole.VIEWER -> ScreenState.MAIN_VIEWER
                    }
                }
            } else {
                _currentScreen.value = if (session.hasCompletedOnboarding) {
                    ScreenState.BIENVENUE
                } else {
                    ScreenState.ONBOARDING
                }
            }
        }
    }

    fun completeOnboarding() {
        MovieRepository.saveSession(hasCompletedOnboarding = true)
        _currentScreen.value = ScreenState.BIENVENUE
    }

    fun setAuthMode(mode: AuthMode) {
        _authMode.value = mode
        _authError.value = null
        _currentScreen.value = ScreenState.AUTH
    }

    fun setCategory(cat: MovieCategory) {
        _selectedCategory.value = cat
    }

    fun setDj(dj: String?) {
        _selectedDj.value = dj
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openMovie(movie: MovieItem) {
        _selectedMovie.value = movie
        _currentScreen.value = ScreenState.VIDEO_PLAYER
    }

    fun closePlayer() {
        _selectedMovie.value = null
        _currentScreen.value = ScreenState.MAIN_VIEWER
    }

    fun showVipModal(show: Boolean) {
        _showVipModal.value = show
    }

    fun showNotifications(show: Boolean) {
        _showNotificationDialog.value = show
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    init {
        MovieRepository.checkAndExpireVipSubscriptions()
        viewModelScope.launch {
            MovieRepository.users.collect { list ->
                val current = _currentUser.value
                if (current != null) {
                    val updated = list.find { it.id == current.id }
                    if (updated != null && updated != current) {
                        _currentUser.value = updated
                    }
                }
            }
        }
        viewModelScope.launch {
            MovieRepository.persistedDownloads.collect { persisted ->
                if (_currentUser.value != null && persisted.isNotEmpty()) {
                    val merged = persisted.toMutableMap()
                    _activeDownloads.value.forEach { (k, v) -> merged[k] = v }
                    _activeDownloads.value = merged
                }
            }
        }
    }

    fun login(identifier: String, pass: String) {
        if (identifier.isBlank() || pass.isBlank()) {
            _authError.value = if (_language.value == Language.SW) "Tafadhali jaza nafasi zote!" else "Veuillez remplir tous les champs !"
            return
        }
        MovieRepository.checkAndExpireVipSubscriptions()
        val user = MovieRepository.authenticateUser(identifier, pass)
        if (user != null) {
            if (user.role == UserRole.DJ && !user.hasDashboardAccess) {
                _authError.value = if (_language.value == Language.SW)
                    "Huna idhini ya kuingia kwenye Dashboard! Wasiliana na Admin (Yesu Kishilo) ili akupe access."
                else
                    "Accès refusé au Dashboard ! L'administrateur (Yesu Kishilo) doit vous accorder l'accès."
                return
            }
            _currentUser.value = user
            _authError.value = null
            MovieRepository.saveSession(loggedInUserId = user.id, hasCompletedOnboarding = true)
            when (user.role) {
                UserRole.ADMIN -> _currentScreen.value = ScreenState.ADMIN_DASHBOARD
                UserRole.DJ -> _currentScreen.value = ScreenState.DJ_DASHBOARD
                UserRole.VIEWER -> _currentScreen.value = ScreenState.MAIN_VIEWER
            }
        } else {
            _authError.value = if (_language.value == Language.SW)
                "Taarifa za kuingia si sahihi! Angalia barua pepe na nywila yako."
            else
                "Identifiants incorrects ! Vérifiez votre adresse ou mot de passe."
        }
    }

    fun register(
        name: String,
        country: String,
        emailOrPhone: String,
        pass: String,
        ageTier: AgeTier,
        isEmailVerified: Boolean = false
    ) {
        if (name.isBlank() || country.isBlank() || emailOrPhone.isBlank()) {
            _authError.value = if (_language.value == Language.SW) "Tafadhali jaza taarifa zako zote." else "Veuillez remplir toutes vos informations."
            return
        }
        if (pass.length < 8) {
            _authError.value = if (_language.value == Language.SW)
                "Nywila lazima iwe na herufi 8 au zaidi!"
            else
                "Le mot de passe doit comporter au moins 8 caractères !"
            return
        }
        val newUser = MovieRepository.registerUser(name, country, emailOrPhone, pass, ageTier, isEmailVerified)
        _currentUser.value = newUser
        _authError.value = null
        MovieRepository.saveSession(loggedInUserId = newUser.id, hasCompletedOnboarding = true)
        _currentScreen.value = ScreenState.MAIN_VIEWER
    }

    fun sendEmailVerificationOtp(email: String): String? {
        val clean = email.trim()
        if (clean.isBlank() || !clean.contains("@")) {
            _authError.value = if (_language.value == Language.SW)
                "Weka anwani sahihi ya Gmail kuthibitisha."
            else
                "Entrez une adresse Gmail valide pour la vérification."
            return null
        }
        val code = (100000..999999).random().toString()
        _sentVerifyEmailOtp.value = code
        _verifyEmailTarget.value = clean
        _authError.value = null
        _snackbarMessage.value = if (_language.value == Language.SW)
            "Code ya uhakiki [$code] imetumwa kwa $clean kuzuia ku-jack!"
        else
            "Code de vérification [$code] envoyé à $clean contre le piratage !"
        return code
    }

    fun verifyCurrentUserEmail(code: String): Boolean {
        val user = _currentUser.value ?: return false
        if (code.trim() != _sentVerifyEmailOtp.value) {
            _authError.value = if (_language.value == Language.SW)
                "Code uliyoweka si sahihi! Hakiki tena."
            else
                "Code de vérification incorrect !"
            return false
        }
        MovieRepository.verifyUserEmail(user.id)
        _currentUser.value = MovieRepository.users.value.find { it.id == user.id }
        _sentVerifyEmailOtp.value = null
        _verifyEmailTarget.value = null
        _authError.value = null
        _snackbarMessage.value = if (_language.value == Language.SW)
            "Barua pepe imethibitishwa kikamilifu! Akaunti yako sasa inalindwa dhidi ya udukuzi."
        else
            "Email vérifié avec succès ! Votre compte est protégé contre le piratage."
        return true
    }

    fun logout() {
        _currentUser.value = null
        _activeDownloads.value = emptyMap()
        MovieRepository.saveSession(loggedInUserId = null)
        _currentScreen.value = ScreenState.BIENVENUE
    }

    fun toggleFavorite(movie: MovieItem) {
        val user = _currentUser.value ?: return
        MovieRepository.toggleFavorite(user.id, movie.id)
        // Refresh currentUser
        _currentUser.value = MovieRepository.users.value.find { it.id == user.id }
        val isNowFav = _currentUser.value?.favoritesMovieIds?.contains(movie.id) == true
        _snackbarMessage.value = if (_language.value == Language.SW) {
            if (isNowFav) "Imehifadhiwa kwenye Albamu yako!" else "Imeondolewa kwenye Albamu."
        } else {
            if (isNowFav) "Ajouté à votre Album !" else "Retiré de l'Album."
        }
    }

    // Password Reset via Gmail with Verification Code (OTP)
    fun sendPasswordResetOtp(email: String): Boolean {
        if (email.isBlank() || !email.contains("@")) {
            _authError.value = if (_language.value == Language.SW)
                "Tafadhali weka barua pepe sahihi ya Gmail."
            else
                "Veuillez entrer une adresse Gmail valide."
            return false
        }
        val code = (100000..999999).random().toString()
        _sentOtpCode.value = code
        _otpTargetEmail.value = email.trim()
        _authError.value = null

        _snackbarMessage.value = if (_language.value == Language.SW)
            "Code ya uhakiki [$code] imetumwa kwenye $email!"
        else
            "Code de vérification [$code] envoyé à $email !"
        return true
    }

    fun verifyOtpAndResetPassword(email: String, inputCode: String, newPasswordPlain: String): Boolean {
        if (inputCode.trim() != _sentOtpCode.value) {
            _authError.value = if (_language.value == Language.SW)
                "Code uliyoweka si sahihi! Angalia tena code uliyotumiwa."
            else
                "Code de vérification incorrect !"
            return false
        }
        if (newPasswordPlain.length < 8) {
            _authError.value = if (_language.value == Language.SW)
                "Nywila mpya lazima iwe na herufi 8 au zaidi."
            else
                "Le mot de passe doit comporter au moins 8 caractères."
            return false
        }

        val success = MovieRepository.resetPassword(email, newPasswordPlain)
        if (success) {
            _sentOtpCode.value = null
            _otpTargetEmail.value = null
            _authError.value = null
            _authMode.value = AuthMode.SIGN_IN
            _snackbarMessage.value = if (_language.value == Language.SW)
                "Nywila mpya imewekwa kikamilifu! Ingia sasa."
            else
                "Mot de passe réinitialisé avec succès ! Connectez-vous."
            return true
        } else {
            _authError.value = if (_language.value == Language.SW)
                "Akaunti yenye barua pepe hiyo haijapatikana."
            else
                "Compte introuvable avec cette adresse."
            return false
        }
    }

    // Vidmate-Style Download Manager with Realtime % Progress
    fun downloadMovie(movie: MovieItem) {
        MovieRepository.checkAndExpireVipSubscriptions()
        val user = _currentUser.value ?: return
        if (!user.isVipActive()) {
            _showVipModal.value = true
            return
        }

        val taskId = "dl_" + movie.id
        val totalSizeMb = if (movie.isSeries) 450f else 850f
        val initialTask = DownloadTask(
            id = taskId,
            movieId = movie.id,
            movieTitle = movie.title,
            moviePosterRes = movie.posterRes,
            category = movie.category,
            progress = 0.05f,
            percentage = 5,
            downloadedMb = totalSizeMb * 0.05f,
            totalMb = totalSizeMb,
            speed = "3.8 MB/s",
            status = DownloadStatus.DOWNLOADING
        )

        _activeDownloads.value = _activeDownloads.value + (taskId to initialTask)
        MovieRepository.persistDownloadTask(user.id, initialTask)
        _snackbarMessage.value = if (_language.value == Language.SW)
            "Upakuaji umeanza: ${movie.title} (kama Vidmate)"
        else
            "Téléchargement démarré : ${movie.title}"

        // Launch background simulated download progression
        viewModelScope.launch {
            for (step in 1..20) {
                kotlinx.coroutines.delay(350)
                val currentTask = _activeDownloads.value[taskId] ?: break
                if (currentTask.status == DownloadStatus.PAUSED) {
                    continue
                }
                val newProgress = (step / 20f).coerceAtMost(1.0f)
                val newPct = (newProgress * 100).toInt()
                val newDownloadedMb = totalSizeMb * newProgress
                val randomSpeed = "%.1f MB/s".format(3.2 + (0..15).random() / 10.0)

                val updatedTask = currentTask.copy(
                    progress = newProgress,
                    percentage = newPct,
                    downloadedMb = newDownloadedMb,
                    speed = randomSpeed,
                    status = if (newPct >= 100) DownloadStatus.COMPLETED else DownloadStatus.DOWNLOADING
                )
                _activeDownloads.value = _activeDownloads.value + (taskId to updatedTask)
                if (newPct >= 100) {
                    MovieRepository.persistDownloadTask(user.id, updatedTask)
                }
            }

            // Completed: add to downloadedMovieIds in repository
            MovieRepository.downloadMovie(user.id, movie)
            _currentUser.value = MovieRepository.users.value.find { it.id == user.id }
            _snackbarMessage.value = if (_language.value == Language.SW)
                "Vidmate: '${movie.title}' imehifadhiwa kikamilifu kwenye faili la simu yako (100%)!"
            else
                "Vidmate : '${movie.title}' téléchargé à 100% sur votre appareil !"
        }
    }

    fun pauseDownload(taskId: String) {
        val task = _activeDownloads.value[taskId] ?: return
        val paused = task.copy(status = DownloadStatus.PAUSED)
        _activeDownloads.value = _activeDownloads.value + (taskId to paused)
        _currentUser.value?.let { MovieRepository.persistDownloadTask(it.id, paused) }
    }

    fun resumeDownload(taskId: String) {
        val task = _activeDownloads.value[taskId] ?: return
        val resumed = task.copy(status = DownloadStatus.DOWNLOADING)
        _activeDownloads.value = _activeDownloads.value + (taskId to resumed)
        _currentUser.value?.let { MovieRepository.persistDownloadTask(it.id, resumed) }
    }

    fun cancelDownload(taskId: String) {
        _activeDownloads.value = _activeDownloads.value - taskId
        _currentUser.value?.let { MovieRepository.removePersistedDownloadTask(it.id, taskId) }
    }

    private fun canPublishMovies(user: UserAccount?): Boolean {
        if (user == null) return false
        return user.role == UserRole.ADMIN || (user.role == UserRole.DJ && user.hasDashboardAccess)
    }

    // Admin & Authorized DJ Video File Upload with Live % Progress
    fun adminStartUploadMovie(
        title: String,
        category: MovieCategory,
        ageRating: String,
        descSw: String,
        descFr: String,
        djName: String?,
        localFileName: String,
        isVipOnly: Boolean,
        isSeries: Boolean,
        episodesCount: Int
    ) {
        val publisher = _currentUser.value
        if (!canPublishMovies(publisher)) {
            _snackbarMessage.value = if (_language.value == Language.SW)
                "Ruhusa imekataliwa: Mtumiaji wa kawaida haruhusiwi kuweka filamu!"
            else
                "Accès refusé : Vous n'avez pas l'autorisation de téléverser des films !"
            return
        }
        val effectiveDjName = if (publisher?.role == UserRole.DJ) {
            publisher.djStageName ?: publisher.name
        } else djName

        val totalMb = if (isSeries) 520f else 980f
        _adminUploadState.value = AdminUploadState(
            isUploading = true,
            progress = 0.05f,
            percentage = 5,
            mbUploaded = totalMb * 0.05f,
            totalMb = totalMb,
            speed = "5.4 MB/s",
            fileName = localFileName,
            stageText = if (_language.value == Language.SW) "Inasoma faili la simu: $localFileName" else "Lecture du fichier local : $localFileName"
        )

        viewModelScope.launch {
            for (step in 1..25) {
                kotlinx.coroutines.delay(220)
                val newProgress = (step / 25f).coerceAtMost(1.0f)
                val newPct = (newProgress * 100).toInt()
                val mb = totalMb * newProgress
                val speed = "%.1f MB/s".format(4.8 + (0..16).random() / 10.0)

                val stage = when {
                    newPct < 35 -> if (_language.value == Language.SW) "Inasoma na kubana video: $localFileName" else "Lecture et compression de la vidéo"
                    newPct < 85 -> if (_language.value == Language.SW) "Inapakiwa ndani ya hifadhi ya APK ($newPct%)..." else "Téléversement dans l'APK ($newPct%)..."
                    else -> if (_language.value == Language.SW) "Inathibitisha na kusajili filamu kwenye orodha..." else "Finalisation et enregistrement..."
                }

                _adminUploadState.value = AdminUploadState(
                    isUploading = true,
                    progress = newProgress,
                    percentage = newPct,
                    mbUploaded = mb,
                    totalMb = totalMb,
                    speed = speed,
                    fileName = localFileName,
                    stageText = stage
                )
            }

            // Finished: add movie to repository
            adminAddMovie(
                title = title,
                category = category,
                ageRating = ageRating,
                descSw = descSw,
                descFr = descFr,
                djName = effectiveDjName,
                videoUrl = "content://media/external/video/moviesw/" + localFileName.replace(" ", "_"),
                isDriveLink = false,
                isVipOnly = isVipOnly,
                isSeries = isSeries,
                episodesCount = episodesCount
            )

            _adminUploadState.value = AdminUploadState(
                isUploading = false,
                progress = 1.0f,
                percentage = 100,
                stageText = if (_language.value == Language.SW) "Imepakiwa kwa 100% kwenye APK!" else "Téléversé à 100% dans l'APK !"
            )

            _snackbarMessage.value = if (_language.value == Language.SW)
                "Filamu '$title' imepakiwa kwa 100% kutoka kwenye faili la simu hadi kwenye APK!"
            else
                "Film '$title' importé à 100% depuis le fichier local dans l'APK !"
        }
    }

    fun updateQuality(quality: VideoQuality) {
        val user = _currentUser.value ?: return
        if (quality.requiresVip && !user.isVipActive()) {
            _showVipModal.value = true
            return
        }
        MovieRepository.updateUserQuality(user.id, quality)
        _currentUser.value = MovieRepository.users.value.find { it.id == user.id }
        _snackbarMessage.value = if (_language.value == Language.SW)
            "Ubora wa video umewekwa kuwa ${quality.label}"
        else
            "Qualité vidéo réglée sur ${quality.label}"
    }

    fun upgradeVip(tier: VipTier) {
        val user = _currentUser.value ?: return
        if (tier == VipTier.BURE) {
            _showVipModal.value = false
            return
        }
        if (user.isVipActive()) {
            _snackbarMessage.value = if (_language.value == Language.SW)
                "Tayari unayo abonnement ya VIP ya mwezi 1 (zimebaki siku ${user.daysRemaining()}). Kitufe kimefungwa hadi mwezi umalizike."
            else
                "Abonnement VIP actif (${user.daysRemaining()} jours restants). Le bouton VIP reviendra à l'expiration."
            _showVipModal.value = false
            return
        }
        MovieRepository.updateUserVip(user.id, tier)
        _currentUser.value = MovieRepository.users.value.find { it.id == user.id }
        _showVipModal.value = false
        _snackbarMessage.value = if (_language.value == Language.SW)
            "Hongera! Akaunti yako imeboreshwa kuwa ${tier.titleSw} kwa mwezi 1 (siku 30). Kitufe cha kujiunga kimeondolewa sasa."
        else
            "Félicitations ! Abonnement ${tier.titleFr} activé pour 1 mois (30 jours)."
    }

    fun createDjAccount(
        name: String,
        email: String,
        pass: String,
        djStageName: String,
        country: String,
        grantAccess: Boolean
    ) {
        val user = _currentUser.value
        if (user?.role != UserRole.ADMIN) return
        if (name.isBlank() || email.isBlank() || pass.length < 6 || djStageName.isBlank()) {
            _snackbarMessage.value = if (_language.value == Language.SW)
                "Tafadhali jaza taarifa zote za DJ (nywila angalau herufi 6)."
            else
                "Veuillez remplir toutes les informations du DJ (mot de passe 6+ car.)."
            return
        }
        if (MovieRepository.findUserByEmail(email) != null) {
            _snackbarMessage.value = if (_language.value == Language.SW)
                "Barua pepe hii tayari imesajiliwa!"
            else
                "Cet email est déjà utilisé !"
            return
        }
        val newDj = MovieRepository.createDjAccount(name, email, pass, djStageName, country, grantAccess)
        _snackbarMessage.value = if (_language.value == Language.SW)
            "Akaunti ya DJ [${newDj.djStageName}] imeundwa kikamilifu! Access ya Dashboard: ${if (grantAccess) "Imewashwa" else "Imefungwa"}."
        else
            "Compte DJ [${newDj.djStageName}] créé avec succès !"
    }

    fun toggleDjAccess(userId: String, hasAccess: Boolean) {
        val user = _currentUser.value
        if (user?.role != UserRole.ADMIN) return
        val ok = MovieRepository.toggleDjDashboardAccess(userId, hasAccess)
        if (ok) {
            _snackbarMessage.value = if (_language.value == Language.SW)
                if (hasAccess) "Access ya Dashboard imefunguliwa kwa DJ!" else "Access ya Dashboard imefungwa!"
            else
                if (hasAccess) "Accès au tableau de bord activé pour le DJ !" else "Accès désactivé !"
        }
    }

    fun cancelUserVip(userId: String) {
        val user = _currentUser.value
        if (user?.role != UserRole.ADMIN) return
        MovieRepository.updateUserVip(userId, VipTier.BURE)
        _snackbarMessage.value = if (_language.value == Language.SW)
            "Abonnement ya VIP imeondolewa. Mteja amerejeshwa kuwa Bure."
        else
            "Abonnement VIP révoqué. Statut remis à Gratuit."
    }

    fun renewUserVip(userId: String, tier: VipTier) {
        val user = _currentUser.value
        if (user?.role != UserRole.ADMIN) return
        MovieRepository.updateUserVip(userId, tier)
        _snackbarMessage.value = if (_language.value == Language.SW)
            "VIP imeongezwa mwezi 1 (siku 30)!"
        else
            "VIP prolongé pour 1 mois (30 jours) !"
    }

    fun addComment(movieId: String, text: String, rating: Int = 5) {
        val user = _currentUser.value ?: return
        if (text.isBlank()) return
        MovieRepository.addComment(movieId, user.name, user.country, user.vipTier, text, rating)
    }

    // Admin & Authorized DJ Operations
    fun adminAddMovie(
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
        episodesCount: Int
    ) {
        val publisher = _currentUser.value
        if (!canPublishMovies(publisher)) {
            _snackbarMessage.value = if (_language.value == Language.SW)
                "Ruhusa imekataliwa: Mtumiaji wa kawaida haruhusiwi kuweka filamu!"
            else
                "Accès refusé : Seul l'administrateur ou un DJ autorisé peut publier des films !"
            return
        }
        val effectiveDjName = if (publisher?.role == UserRole.DJ) {
            publisher.djStageName ?: publisher.name
        } else djName

        val episodesList = if (isSeries) {
            (1..episodesCount).map { epNum ->
                Episode(epNum, "Kipindi cha $epNum", "Épisode $epNum", 50, videoUrl)
            }
        } else emptyList()

        MovieRepository.addMovie(
            title, category, ageRating, descSw, descFr, effectiveDjName, videoUrl, isDriveLink, isVipOnly, isSeries, episodesList
        )
        _snackbarMessage.value = if (_language.value == Language.SW)
            "Filamu '$title' imechapishwa kikamilifu!"
        else
            "Le film '$title' a été publié avec succès !"
    }

    fun adminDeleteMovie(movieId: String) {
        val requester = _currentUser.value
        val movie = movies.value.find { it.id == movieId }
        val isAdmin = requester?.role == UserRole.ADMIN
        val isAuthorizedDjOwner = requester?.role == UserRole.DJ &&
            requester.hasDashboardAccess &&
            movie?.djName.equals(requester.djStageName ?: requester.name, ignoreCase = true)

        if (!isAdmin && !isAuthorizedDjOwner) {
            _snackbarMessage.value = if (_language.value == Language.SW)
                "Ruhusa imekataliwa: Mtumiaji wa kawaida haruhusiwi kufuta filamu!"
            else
                "Accès refusé : Vous n'êtes pas autorisé à supprimer ce film !"
            return
        }
        MovieRepository.deleteMovie(movieId)
        _snackbarMessage.value = if (_language.value == Language.SW)
            "Filamu '${movie?.title ?: ""}' imefutwa kikamilifu kwenye app!"
        else
            "Film '${movie?.title ?: ""}' supprimé avec succès de l'application !"
    }

    fun adminChangeUserVip(userId: String, tier: VipTier) {
        if (_currentUser.value?.role != UserRole.ADMIN) {
            _snackbarMessage.value = if (_language.value == Language.SW)
                "Ruhusa ya Admin inahitajika!"
            else
                "Accès réservé à l'administrateur !"
            return
        }
        MovieRepository.updateUserVip(userId, tier)
        _snackbarMessage.value = if (_language.value == Language.SW)
            "Kiwango cha mteja kimebadilishwa kuwa ${tier.titleSw}"
        else
            "Statut client mis à jour vers ${tier.titleFr}"
    }

    fun adminDeleteUser(userId: String) {
        if (_currentUser.value?.role != UserRole.ADMIN) {
            _snackbarMessage.value = if (_language.value == Language.SW)
                "Ruhusa ya Admin inahitajika!"
            else
                "Accès réservé à l'administrateur !"
            return
        }
        MovieRepository.deleteUser(userId)
        _snackbarMessage.value = if (_language.value == Language.SW)
            "Mteja amefutwa kwenye mfumo."
        else
            "Client supprimé du système."
    }

    fun adminBroadcast(titleSw: String, titleFr: String, msgSw: String, msgFr: String) {
        if (_currentUser.value?.role != UserRole.ADMIN) return
        if (titleSw.isBlank() || msgSw.isBlank()) return
        MovieRepository.broadcastNotification(titleSw, titleFr, msgSw, msgFr)
        _snackbarMessage.value = if (_language.value == Language.SW)
            "Arifa imetumwa kwa wateja wote!"
        else
            "Notification envoyée à tous les spectateurs !"
    }
}
