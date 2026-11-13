package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Language
import com.example.model.UserRole
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopBar
import com.example.ui.components.NotificationsDialog
import com.example.ui.components.OfflineGateScreen
import com.example.ui.components.VipUpgradeDialog
import com.example.ui.screens.*
import com.example.ui.theme.MovieDarkBg
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NetworkMonitor
import com.example.viewmodel.AuthMode
import com.example.viewmodel.MovieViewModel
import com.example.viewmodel.ScreenState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.data.MovieRepository.initialize(applicationContext)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MovieSwApp()
            }
        }
    }
}

@Composable
fun MovieSwApp(
    viewModel: MovieViewModel = viewModel()
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val authMode by viewModel.authMode.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val bottomNavTab by viewModel.bottomNavTab.collectAsStateWithLifecycle()
    val selectedMovie by viewModel.selectedMovie.collectAsStateWithLifecycle()
    val filteredMovies by viewModel.filteredMovies.collectAsStateWithLifecycle()
    val allMovies by viewModel.movies.collectAsStateWithLifecycle()
    val availableDjs by viewModel.availableDjs.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedDj by viewModel.selectedDj.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showVipModal by viewModel.showVipModal.collectAsStateWithLifecycle()
    val showNotificationDialog by viewModel.showNotificationDialog.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val socialLinks by viewModel.socialLinks.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val sentOtpCode by viewModel.sentOtpCode.collectAsStateWithLifecycle()
    val otpTargetEmail by viewModel.otpTargetEmail.collectAsStateWithLifecycle()
    val sentVerifyEmailOtp by viewModel.sentVerifyEmailOtp.collectAsStateWithLifecycle()
    val activeDownloads by viewModel.activeDownloads.collectAsStateWithLifecycle()
    val adminUploadState by viewModel.adminUploadState.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val isCatalogLoading by viewModel.isCatalogLoading.collectAsStateWithLifecycle()
    val isActionLoading by viewModel.isActionLoading.collectAsStateWithLifecycle()
    val appUpdateState by viewModel.appUpdateState.collectAsStateWithLifecycle()
    val publishedAppUpdate by viewModel.publishedAppUpdate.collectAsStateWithLifecycle()

    var networkRetryTick by remember { mutableIntStateOf(0) }
    val observedOnline by NetworkMonitor.observeIsOnline(context)
        .collectAsStateWithLifecycle(initialValue = NetworkMonitor.isInternetAvailable(context))
    var isOnline by remember { mutableStateOf(NetworkMonitor.isInternetAvailable(context)) }
    LaunchedEffect(observedOnline, networkRetryTick) {
        isOnline = observedOnline || NetworkMonitor.isInternetAvailable(context)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Modal dialogs
    if (showVipModal) {
        VipUpgradeDialog(
            language = language,
            currentUser = currentUser,
            onDismiss = { viewModel.showVipModal(false) },
            onSelectPlan = { tier -> viewModel.upgradeVip(tier) }
        )
    }

    if (showNotificationDialog) {
        NotificationsDialog(
            language = language,
            notifications = notifications,
            onDismiss = { viewModel.showNotifications(false) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MovieDarkBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (currentScreen == ScreenState.MAIN_VIEWER) {
                AppTopBar(
                    language = language,
                    currentUser = currentUser,
                    unreadNotificationsCount = notifications.size,
                    onLanguageToggle = { viewModel.toggleLanguage() },
                    onNotificationsClick = { viewModel.showNotifications(true) },
                    onVipClick = { viewModel.showVipModal(true) },
                    onAdminClick = { viewModel.navigateTo(ScreenState.ADMIN_DASHBOARD) }
                )
            }
        },
        bottomBar = {
            if (currentScreen == ScreenState.MAIN_VIEWER) {
                AppBottomNav(
                    selectedTab = bottomNavTab,
                    language = language,
                    onTabSelected = { viewModel.setBottomNavTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MovieDarkBg)
        ) {
            when (currentScreen) {
                ScreenState.SPLASH -> {
                    SplashScreen(
                        language = language,
                        onFinish = { viewModel.onSplashFinished() }
                    )
                }

                ScreenState.ONBOARDING -> {
                    OnboardingScreen(
                        language = language,
                        onLanguageToggle = { viewModel.toggleLanguage() },
                        onFinish = { viewModel.completeOnboarding() }
                    )
                }

                ScreenState.BIENVENUE -> {
                    BienvenueScreen(
                        language = language,
                        onLanguageToggle = { viewModel.toggleLanguage() },
                        onSignUpClick = { viewModel.setAuthMode(AuthMode.SIGN_UP) },
                        onSignInClick = { viewModel.setAuthMode(AuthMode.SIGN_IN) }
                    )
                }

                ScreenState.AUTH -> {
                    BackHandler {
                        viewModel.navigateTo(ScreenState.BIENVENUE)
                    }
                    AuthScreen(
                        language = language,
                        authMode = authMode,
                        errorMessage = authError,
                        sentOtpCode = sentOtpCode,
                        otpTargetEmail = otpTargetEmail,
                        sentVerifyEmailOtp = sentVerifyEmailOtp,
                        isLoading = isAuthLoading,
                        onSwitchMode = { viewModel.setAuthMode(it) },
                        onBack = { viewModel.navigateTo(ScreenState.BIENVENUE) },
                        onLogin = { id, pass -> viewModel.login(id, pass) },
                        onRegister = { name, country, email, pass, ageTier, isVerified ->
                            viewModel.register(name, country, email, pass, ageTier, isVerified)
                        },
                        onSendOtp = { viewModel.sendPasswordResetOtp(it) },
                        onResetPassword = { email, code, pass ->
                            viewModel.verifyOtpAndResetPassword(email, code, pass)
                        },
                        onSendVerifyEmailOtp = { viewModel.sendEmailVerificationOtp(it) }
                    )
                }

                ScreenState.MAIN_VIEWER -> {
                    when (bottomNavTab) {
                        "home" -> HomeScreen(
                            language = language,
                            currentUser = currentUser,
                            movies = filteredMovies,
                            availableDjs = availableDjs,
                            selectedCategory = selectedCategory,
                            selectedDj = selectedDj,
                            isLoading = isCatalogLoading,
                            onRefresh = { viewModel.refreshCatalog() },
                            onSelectCategory = { viewModel.setCategory(it) },
                            onSelectDj = { viewModel.setDj(it) },
                            onMovieClick = { viewModel.openMovie(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onVipUpgradeClick = { viewModel.showVipModal(true) }
                        )

                        "explore" -> ExploreScreen(
                            language = language,
                            currentUser = currentUser,
                            movies = filteredMovies,
                            availableDjs = availableDjs,
                            selectedCategory = selectedCategory,
                            selectedDj = selectedDj,
                            searchQuery = searchQuery,
                            isLoading = isCatalogLoading,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onSelectCategory = { viewModel.setCategory(it) },
                            onSelectDj = { viewModel.setDj(it) },
                            onMovieClick = { viewModel.openMovie(it) }
                        )

                        "album" -> AlbumScreen(
                            language = language,
                            currentUser = currentUser,
                            allMovies = allMovies,
                            activeDownloads = activeDownloads,
                            isLoading = isCatalogLoading,
                            onMovieClick = { viewModel.openMovie(it) },
                            onRemoveFavorite = { viewModel.toggleFavorite(it) },
                            onVipUpgradeClick = { viewModel.showVipModal(true) }
                        )

                        "account" -> AccountScreen(
                            language = language,
                            currentUser = currentUser,
                            socialLinks = socialLinks,
                            sentVerifyEmailOtp = sentVerifyEmailOtp,
                            isLoading = isActionLoading,
                            appUpdateState = appUpdateState,
                            onLanguageToggle = { viewModel.toggleLanguage() },
                            onSelectQuality = { viewModel.updateQuality(it) },
                            onVipUpgradeClick = { viewModel.showVipModal(true) },
                            onAdminClick = { viewModel.navigateTo(ScreenState.ADMIN_DASHBOARD) },
                            onLogout = { viewModel.logout() },
                            onSendEmailVerification = { viewModel.sendEmailVerificationOtp(it) },
                            onVerifyEmail = { viewModel.verifyCurrentUserEmail(it) },
                            onCheckForUpdates = { viewModel.checkForAppUpdates(context) },
                            onDownloadAndInstallUpdate = { info -> viewModel.startAppUpdateDownload(context, info) },
                            onLaunchApkInstall = { file -> com.example.util.AppUpdateManager.launchApkInstaller(context, file) }
                        )
                    }
                }

                ScreenState.VIDEO_PLAYER -> {
                    selectedMovie?.let { movie ->
                        BackHandler {
                            viewModel.closePlayer()
                        }
                        VideoPlayerScreen(
                            movie = movie,
                            language = language,
                            currentUser = currentUser,
                            comments = comments,
                            activeDownloads = activeDownloads,
                            onBack = { viewModel.closePlayer() },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onDownload = { viewModel.downloadMovie(it) },
                            onPostComment = { text, rating -> viewModel.addComment(movie.id, text, rating) },
                            onVipUpgradeClick = { viewModel.showVipModal(true) }
                        )
                    }
                }

                ScreenState.ADMIN_DASHBOARD -> {
                    // SECURITY CHECK: Mutumiyaji wa kawaida aruusiwi ku weka movie atakama awe na vip pro max ila admin pekeyake!
                    if (currentUser?.role != UserRole.ADMIN) {
                        LaunchedEffect(Unit) {
                            viewModel.showSnackbar(
                                if (language == Language.SW) "Ruhusa imekataliwa: Mtumiaji wa kawaida haruhusiwi kuingia hapa!" 
                                else "Accès refusé : Seul l'administrateur peut accéder ici !"
                            )
                            viewModel.navigateTo(if (currentUser != null) ScreenState.MAIN_VIEWER else ScreenState.BIENVENUE)
                        }
                    } else {
                        BackHandler {
                            viewModel.navigateTo(ScreenState.MAIN_VIEWER)
                        }
                        AdminDashboardScreen(
                            language = language,
                            users = users,
                            movies = allMovies,
                            socialLinks = socialLinks,
                            adminUploadState = adminUploadState,
                            publishedAppUpdate = publishedAppUpdate,
                            isActionLoading = isActionLoading,
                            onPublishApkUpdate = { uri, fileName, sizeMb, directUrl, vCode, vName, notes ->
                                viewModel.adminPublishApkUpdate(
                                    context = context,
                                    apkUri = uri,
                                    fileName = fileName,
                                    expectedSizeMb = sizeMb,
                                    directApkUrl = directUrl,
                                    versionCode = vCode,
                                    versionName = vName,
                                    releaseNotes = notes
                                )
                            },
                            onClearPublishedApkUpdate = { viewModel.adminClearPublishedApkUpdate() },
                            onAddMovie = { title, cat, age, descSw, descFr, dj, url, isDrive, isVip, isSeries, epCount, sendNotif ->
                                viewModel.adminAddMovie(
                                    title = title,
                                    category = cat,
                                    ageRating = age,
                                    descSw = descSw,
                                    descFr = descFr,
                                    djName = dj,
                                    videoUrl = url,
                                    isDriveLink = isDrive,
                                    isVipOnly = isVip,
                                    isSeries = isSeries,
                                    episodesCount = epCount,
                                    sendNotification = sendNotif,
                                    context = context
                                )
                            },
                            onStartUploadMovie = { title, cat, age, descSw, descFr, dj, file, isVip, isSeries, epCount, sendNotif ->
                                viewModel.adminStartUploadMovie(
                                    title = title,
                                    category = cat,
                                    ageRating = age,
                                    descSw = descSw,
                                    descFr = descFr,
                                    djName = dj,
                                    localFileName = file,
                                    isVipOnly = isVip,
                                    isSeries = isSeries,
                                    episodesCount = epCount,
                                    sendNotification = sendNotif,
                                    context = context
                                )
                            },
                            onImportPickedVideo = { uri, fileName, sizeMb, posterUri, title, cat, age, descSw, descFr, dj, isVip, isSeries, epCount, sendNotif ->
                                viewModel.adminImportPickedVideo(
                                    context = context,
                                    videoUri = uri,
                                    fileName = fileName,
                                    expectedSizeMb = sizeMb,
                                    posterUri = posterUri,
                                    title = title,
                                    category = cat,
                                    ageRating = age,
                                    descSw = descSw,
                                    descFr = descFr,
                                    djName = dj,
                                    isVipOnly = isVip,
                                    isSeries = isSeries,
                                    episodesCount = epCount,
                                    sendNotification = sendNotif
                                )
                            },
                            onDeleteMovie = { movieId -> viewModel.adminDeleteMovie(movieId) },
                            onChangeUserVip = { userId, tier -> viewModel.adminChangeUserVip(userId, tier) },
                            onDeleteUser = { userId -> viewModel.adminDeleteUser(userId) },
                            onBroadcastNotif = { tSw, tFr, mSw, mFr -> viewModel.adminBroadcast(tSw, tFr, mSw, mFr, context) },
                            onCreateDjAccount = { name, email, pass, stage, country, access ->
                                viewModel.createDjAccount(name, email, pass, stage, country, access)
                            },
                            onToggleDjAccess = { userId, hasAccess -> viewModel.toggleDjAccess(userId, hasAccess) },
                            onCancelUserVip = { userId -> viewModel.cancelUserVip(userId) },
                            onRenewUserVip = { userId, tier -> viewModel.renewUserVip(userId, tier) },
                            onLanguageToggle = { viewModel.toggleLanguage() },
                            onExitAdmin = { viewModel.navigateTo(ScreenState.MAIN_VIEWER) }
                        )
                    }
                }

                ScreenState.DJ_DASHBOARD -> {
                    val user = currentUser
                    if (user?.role != UserRole.DJ || !user.hasDashboardAccess) {
                        LaunchedEffect(user) {
                            viewModel.showSnackbar(
                                if (language == Language.SW)
                                    "Access imefungwa: Wasiliana na Admin kupata idhini ya DJ Dashboard."
                                else
                                    "Accès refusé : Demandez l'accès à l'administrateur."
                            )
                            viewModel.navigateTo(if (user != null) ScreenState.MAIN_VIEWER else ScreenState.BIENVENUE)
                        }
                    } else {
                        DjDashboardScreen(
                            language = language,
                            currentDj = user,
                            allMovies = allMovies,
                            uploadState = adminUploadState,
                            onLanguageToggle = { viewModel.toggleLanguage() },
                            onAddMovie = { title, cat, age, descSw, descFr, dj, url, isDrive, isVip, isSeries, epCount ->
                                viewModel.adminAddMovie(
                                    title = title,
                                    category = cat,
                                    ageRating = age,
                                    descSw = descSw,
                                    descFr = descFr,
                                    djName = dj,
                                    videoUrl = url,
                                    isDriveLink = isDrive,
                                    isVipOnly = isVip,
                                    isSeries = isSeries,
                                    episodesCount = epCount,
                                    sendNotification = true,
                                    context = context
                                )
                            },
                            onStartUploadMovie = { title, cat, age, descSw, descFr, dj, file, isVip, isSeries, epCount ->
                                viewModel.adminStartUploadMovie(
                                    title = title,
                                    category = cat,
                                    ageRating = age,
                                    descSw = descSw,
                                    descFr = descFr,
                                    djName = dj,
                                    localFileName = file,
                                    isVipOnly = isVip,
                                    isSeries = isSeries,
                                    episodesCount = epCount,
                                    sendNotification = true,
                                    context = context
                                )
                            },
                            onImportPickedVideo = { uri, fileName, sizeMb, posterUri, title, cat, age, descSw, descFr, dj, isVip, isSeries, epCount, sendNotif ->
                                viewModel.adminImportPickedVideo(
                                    context = context,
                                    videoUri = uri,
                                    fileName = fileName,
                                    expectedSizeMb = sizeMb,
                                    posterUri = posterUri,
                                    title = title,
                                    category = cat,
                                    ageRating = age,
                                    descSw = descSw,
                                    descFr = descFr,
                                    djName = dj,
                                    isVipOnly = isVip,
                                    isSeries = isSeries,
                                    episodesCount = epCount,
                                    sendNotification = sendNotif
                                )
                            },
                            onDeleteMovie = { movieId -> viewModel.adminDeleteMovie(movieId) },
                            onLogout = { viewModel.logout() }
                        )
                    }
                }
            }
        }
    }

    // Strict online enforcement: block offline app usage once Splash completes
    if (!isOnline && currentScreen != ScreenState.SPLASH) {
        OfflineGateScreen(
            language = language,
            onRetry = {
                isOnline = NetworkMonitor.isInternetAvailable(context)
                networkRetryTick++
            }
        )
    }
}
