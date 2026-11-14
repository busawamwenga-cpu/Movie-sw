package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.MovieRepository
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopBar
import com.example.ui.components.NotificationsDialog
import com.example.ui.components.OfflineGateScreen
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.util.GoogleAuthHelper
import com.example.util.NetworkMonitor
import com.example.viewmodel.CloudMovieViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MovieRepository.initialize(applicationContext)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MovieSwApp(activity = this)
            }
        }
    }
}

private fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    trySend(currentUser)
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun MovieSwApp(activity: ComponentActivity) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val firebaseUser by remember { Firebase.auth.authStateFlow() }
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    var isSigningIn by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var autoSignInAttempted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null && !autoSignInAttempted) {
            autoSignInAttempted = true
            isSigningIn = true
            val result = GoogleAuthHelper.attemptAutoSignIn(activity)
            isSigningIn = false
            result.onFailure { err ->
                if (err !is GoogleAuthHelper.SignInCancelledException) {
                    // Silent auto-sign-in failure simply leaves the Login screen ready
                }
            }
        }
    }

    var networkRetryTick by remember { mutableIntStateOf(0) }
    val observedOnline by NetworkMonitor.observeIsOnline(context)
        .collectAsStateWithLifecycle(initialValue = NetworkMonitor.isInternetAvailable(context))
    var isOnline by remember { mutableStateOf(NetworkMonitor.isInternetAvailable(context)) }
    LaunchedEffect(observedOnline, networkRetryTick) {
        isOnline = observedOnline || NetworkMonitor.isInternetAvailable(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg)
    ) {
        val signedInUser = firebaseUser
        if (signedInUser == null) {
            LoginScreen(
                isLoading = isSigningIn,
                errorMessage = loginError,
                onGoogleSignInClick = {
                    if (!isSigningIn) {
                        scope.launch {
                            isSigningIn = true
                            loginError = null
                            val result = GoogleAuthHelper.signInWithGoogle(activity)
                            isSigningIn = false
                            result.onFailure { err ->
                                if (err !is GoogleAuthHelper.SignInCancelledException) {
                                    loginError = err.message ?: "Sign-in failed. Please try again."
                                }
                            }
                        }
                    }
                }
            )
        } else {
            key(signedInUser.uid) {
                val app = context.applicationContext as android.app.Application
                val cloudViewModel: CloudMovieViewModel = viewModel(
                    key = "cloud_${signedInUser.uid}",
                    factory = CloudMovieViewModel.Factory(
                        application = app,
                        uid = signedInUser.uid,
                        email = signedInUser.email ?: "",
                        displayName = signedInUser.displayName ?: "",
                        isEmailVerified = signedInUser.isEmailVerified
                    )
                )
                AuthenticatedAppContent(
                    viewModel = cloudViewModel,
                    onLogout = {
                        scope.launch {
                            GoogleAuthHelper.signOut(context)
                        }
                    }
                )
            }
        }

        if (!isOnline) {
            OfflineGateScreen(
                onRetry = {
                    isOnline = NetworkMonitor.isInternetAvailable(context)
                    networkRetryTick++
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedAppContent(
    viewModel: CloudMovieViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by viewModel.currentProfile.collectAsStateWithLifecycle()
    val isProfileLoading by viewModel.isProfileLoading.collectAsStateWithLifecycle()
    val profileError by viewModel.profileError.collectAsStateWithLifecycle()
    val moviesError by viewModel.moviesError.collectAsStateWithLifecycle()
    val movies by viewModel.movies.collectAsStateWithLifecycle()
    val filteredMovies by viewModel.filteredMovies.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val bottomNavTab by viewModel.bottomNavTab.collectAsStateWithLifecycle()
    val selectedMovie by viewModel.selectedMovie.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val showNotifications by viewModel.showNotifications.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()
    val appUpdateState by viewModel.appUpdateState.collectAsStateWithLifecycle()

    val isAdmin = userProfile?.isAdmin == true
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    if (isProfileLoading && userProfile == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MovieDarkBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MovieRedPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Loading your account...",
                    color = MovieTextSecondary,
                    fontSize = 14.sp
                )
            }
        }
        return
    }

    if (profileError != null && userProfile == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MovieDarkBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MovieSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = profileError ?: "Unable to load profile.",
                        color = MovieRedPrimary,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary)
                    ) {
                        Text("Log out", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    // Account Lock Enforcement
    if (userProfile?.isLocked == true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MovieDarkBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MovieSurface,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieRedPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MovieRedPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Account Locked",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MovieTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your account has been locked by an administrator. Please update the app or contact support.",
                        fontSize = 13.sp,
                        color = MovieTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { viewModel.checkForAppUpdates(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = MovieGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check for updates", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log out", color = MovieTextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    // Full-screen Media3 Video Player
    val activeMovie = selectedMovie
    if (activeMovie != null) {
        BackHandler {
            viewModel.closePlayer()
        }
        VideoPlayerScreen(
            movie = activeMovie,
            isFavorite = favoriteIds.contains(activeMovie.id),
            comments = comments.filter { it.movieId == activeMovie.id },
            onBack = { viewModel.closePlayer() },
            onToggleFavorite = { viewModel.toggleFavorite(activeMovie.id) },
            onPostComment = { text -> viewModel.postComment(activeMovie.id, text) }
        )
        return
    }

    if (showNotifications) {
        NotificationsDialog(
            recentMovies = movies,
            onDismiss = { viewModel.showNotifications(false) },
            onMovieClick = { viewModel.openMovie(it) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MovieDarkBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopBar(
                isAdmin = isAdmin,
                unreadNotificationsCount = movies.size.coerceAtMost(9),
                onNotificationsClick = { viewModel.showNotifications(true) },
                onAdminClick = {
                    if (isAdmin) viewModel.selectTab("admin")
                }
            )
        },
        bottomBar = {
            AppBottomNav(
                selectedTab = bottomNavTab,
                isAdmin = isAdmin,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MovieDarkBg)
        ) {
            when (bottomNavTab) {
                "home" -> HomeScreen(
                    movies = movies,
                    favoriteIds = favoriteIds,
                    errorMessage = moviesError,
                    onMovieClick = { viewModel.openMovie(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it.id) }
                )

                "explore" -> ExploreScreen(
                    movies = filteredMovies,
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onMovieClick = { viewModel.openMovie(it) }
                )

                "album" -> AlbumScreen(
                    allMovies = movies,
                    favoriteIds = favoriteIds,
                    onMovieClick = { viewModel.openMovie(it) },
                    onRemoveFavorite = { viewModel.toggleFavorite(it.id) }
                )

                "account" -> AccountScreen(
                    userProfile = userProfile,
                    appUpdateState = appUpdateState,
                    onCheckForUpdates = { viewModel.checkForAppUpdates(context) },
                    onDownloadAndInstallUpdate = { info -> viewModel.startAppUpdateDownload(context, info) },
                    onLaunchApkInstall = { file -> com.example.util.AppUpdateManager.launchApkInstaller(context, file) },
                    onLogout = onLogout
                )

                "admin" -> {
                    if (isAdmin) {
                        AdminDashboardScreen(
                            currentAdminUid = userProfile?.uid ?: "",
                            movies = movies,
                            users = allUsers,
                            uploadState = uploadState,
                            onUploadMovie = { uri, fileName, sizeBytes, durationSec, title, desc ->
                                viewModel.uploadMovie(
                                    context = context,
                                    videoUri = uri,
                                    fileName = fileName,
                                    fileSizeBytes = sizeBytes,
                                    durationSeconds = durationSec,
                                    title = title,
                                    description = desc
                                )
                            },
                            onClearUploadFeedback = { viewModel.clearUploadFeedback() },
                            onDeleteMovie = { viewModel.deleteMovie(it) },
                            onToggleUserRole = { viewModel.toggleUserRole(it) },
                            onToggleUserLock = { viewModel.toggleUserLock(it) },
                            onLockOutdatedUsers = { viewModel.lockOutdatedUsers(it) }
                        )
                    } else {
                        LaunchedEffect(Unit) { viewModel.selectTab("home") }
                    }
                }
            }
        }
    }
}
