package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.CloudMovieRepository
import com.example.model.CloudMovie
import com.example.model.CommentItem
import com.example.model.UserProfile
import com.example.model.VipTier
import com.example.util.AppNotificationHelper
import com.example.util.AppUpdateManager
import com.example.util.AppUpdateUiState
import com.example.util.FirestoreErrorHandler
import com.example.util.NetworkMonitor
import com.example.util.RemoteUpdateInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.CancellationException

data class MovieUploadUiState(
    val isUploading: Boolean = false,
    val progress: Float = 0f,
    val percentage: Int = 0,
    val statusText: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class CloudMovieViewModel(
    application: Application,
    private val uid: String,
    private val email: String,
    private val displayName: String,
    private val isEmailVerified: Boolean,
    private val repository: CloudMovieRepository = CloudMovieRepository(application)
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("moviesw_user_prefs_$uid", Context.MODE_PRIVATE)

    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    val currentProfile: StateFlow<UserProfile?> = _currentProfile.asStateFlow()

    private val _isProfileLoading = MutableStateFlow(true)
    val isProfileLoading: StateFlow<Boolean> = _isProfileLoading.asStateFlow()

    private val _profileError = MutableStateFlow<String?>(null)
    val profileError: StateFlow<String?> = _profileError.asStateFlow()

    private val _moviesError = MutableStateFlow<String?>(null)
    val moviesError: StateFlow<String?> = _moviesError.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()

    private val _bottomNavTab = MutableStateFlow("home")
    val bottomNavTab: StateFlow<String> = _bottomNavTab.asStateFlow()

    private val _selectedMovie = MutableStateFlow<CloudMovie?>(null)
    val selectedMovie: StateFlow<CloudMovie?> = _selectedMovie.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _favoriteIds = MutableStateFlow(loadFavorites())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentItem>>(emptyList())
    val comments: StateFlow<List<CommentItem>> = _comments.asStateFlow()

    private val _showNotifications = MutableStateFlow(false)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _uploadState = MutableStateFlow(MovieUploadUiState())
    val uploadState: StateFlow<MovieUploadUiState> = _uploadState.asStateFlow()

    private val _appUpdateState = MutableStateFlow<AppUpdateUiState>(AppUpdateUiState.Idle)
    val appUpdateState: StateFlow<AppUpdateUiState> = _appUpdateState.asStateFlow()

    val movies: StateFlow<List<CloudMovie>> = repository.observeMovies()
        .catch { e ->
            if (e is CancellationException) throw e
            val fsEx = FirestoreErrorHandler.unwrapFirestoreException(e)
            _moviesError.value = fsEx.message ?: "Unable to load movies."
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val filteredMovies: StateFlow<List<CloudMovie>> = combine(movies, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter { movie ->
                movie.title.contains(query, ignoreCase = true) ||
                    movie.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private var usersCollectorStarted = false

    init {
        initializeAndObserveProfile()
    }

    private fun initializeAndObserveProfile() {
        viewModelScope.launch {
            _isProfileLoading.value = true
            _profileError.value = null
            try {
                val ensured = repository.ensureUserProfile(
                    uid = uid,
                    email = email,
                    displayName = displayName,
                    appVersionCode = BuildConfig.VERSION_CODE,
                    isEmailVerified = isEmailVerified
                )
                _currentProfile.value = ensured
                _isProfileLoading.value = false
                if (ensured.isAdmin) {
                    startUsersObserverIfNeeded()
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val fsEx = FirestoreErrorHandler.unwrapFirestoreException(e)
                _profileError.value = fsEx.message ?: "Failed to load user profile."
                _isProfileLoading.value = false
            }

            repository.observeUserProfile(uid)
                .catch { e ->
                    if (e is CancellationException) throw e
                    val fsEx = FirestoreErrorHandler.unwrapFirestoreException(e)
                    _profileError.value = fsEx.message ?: "Unable to sync profile."
                }
                .collect { profile ->
                    if (profile != null) {
                        _currentProfile.value = profile
                        _isProfileLoading.value = false
                        if (profile.isAdmin) {
                            startUsersObserverIfNeeded()
                        } else {
                            if (_bottomNavTab.value == "admin") {
                                _bottomNavTab.value = "home"
                            }
                        }
                    }
                }
        }
    }

    private fun startUsersObserverIfNeeded() {
        if (usersCollectorStarted) return
        usersCollectorStarted = true
        viewModelScope.launch {
            repository.observeAllUsers()
                .catch { e ->
                    if (e is CancellationException) throw e
                    val fsEx = FirestoreErrorHandler.unwrapFirestoreException(e)
                    _snackbarMessage.value = "Unable to load users: ${fsEx.message}"
                }
                .collect { list ->
                    _allUsers.value = list
                }
        }
    }

    fun selectTab(tab: String) {
        if (tab == "admin" && _currentProfile.value?.isAdmin != true) return
        _bottomNavTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openMovie(movie: CloudMovie) {
        _selectedMovie.value = movie
    }

    fun closePlayer() {
        _selectedMovie.value = null
    }

    fun showNotifications(show: Boolean) {
        _showNotifications.value = show
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun clearUploadFeedback() {
        _uploadState.value = _uploadState.value.copy(errorMessage = null, successMessage = null)
    }

    fun toggleFavorite(movieId: String) {
        val current = _favoriteIds.value.toMutableSet()
        val added = if (current.contains(movieId)) {
            current.remove(movieId)
            false
        } else {
            current.add(movieId)
            true
        }
        _favoriteIds.value = current
        prefs.edit().putStringSet("favorites", current).apply()
        _snackbarMessage.value = if (added) "Saved to your library." else "Removed from Saved."
    }

    fun postComment(movieId: String, text: String) {
        if (text.isBlank()) return
        val name = _currentProfile.value?.displayName?.ifBlank { email } ?: "Viewer"
        val item = CommentItem(
            id = "c_${System.currentTimeMillis()}",
            movieId = movieId,
            userName = name,
            userCountry = "",
            vipTier = VipTier.BURE,
            commentText = text.trim(),
            timestamp = "Just now",
            rating = 5
        )
        _comments.value = listOf(item) + _comments.value
    }

    fun uploadMovie(
        context: Context,
        videoUri: Uri,
        fileName: String,
        fileSizeBytes: Long,
        durationSeconds: Int,
        title: String,
        description: String
    ) {
        if (_currentProfile.value?.isAdmin != true) {
            _uploadState.value = MovieUploadUiState(errorMessage = "Only admins can upload movies.")
            return
        }
        if (title.isBlank()) {
            _uploadState.value = MovieUploadUiState(errorMessage = "Please enter a movie title.")
            return
        }
        if (_uploadState.value.isUploading) return

        viewModelScope.launch {
            _uploadState.value = MovieUploadUiState(
                isUploading = true,
                progress = 0f,
                percentage = 0,
                statusText = "Starting upload..."
            )
            try {
                val movieId = "mov_${UUID.randomUUID().toString().take(12)}"
                repository.uploadMovieWithProgress(
                    context = context,
                    movieId = movieId,
                    videoUri = videoUri,
                    title = title.trim(),
                    description = description.trim(),
                    fileName = fileName,
                    fileSizeBytes = fileSizeBytes,
                    durationSeconds = durationSeconds,
                    uploadedBy = uid
                ) { fraction, pct, transferred, total ->
                    val mbDone = transferred / (1024f * 1024f)
                    val mbTotal = (total / (1024f * 1024f)).coerceAtLeast(mbDone)
                    _uploadState.value = MovieUploadUiState(
                        isUploading = true,
                        progress = fraction,
                        percentage = pct,
                        statusText = "Uploading %.1f / %.1f MB (%d%%)".format(mbDone, mbTotal, pct)
                    )
                }

                _uploadState.value = MovieUploadUiState(
                    isUploading = false,
                    progress = 1f,
                    percentage = 100,
                    statusText = "Movie saved",
                    successMessage = "Movie saved"
                )
                _snackbarMessage.value = "Movie saved"
                AppNotificationHelper.showSystemNotification(
                    context,
                    "New movie added",
                    "${title.trim()} is now ready to watch in Movie SW."
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val fsEx = FirestoreErrorHandler.unwrapFirestoreException(e)
                val msg = fsEx.message ?: e.message ?: "Upload failed. Please check your connection and Firebase setup."
                _uploadState.value = MovieUploadUiState(
                    isUploading = false,
                    errorMessage = msg
                )
                _snackbarMessage.value = msg
            }
        }
    }

    fun deleteMovie(movie: CloudMovie) {
        if (_currentProfile.value?.isAdmin != true) return
        viewModelScope.launch {
            try {
                repository.deleteMovie(movie)
                _snackbarMessage.value = "Movie deleted."
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val fsEx = FirestoreErrorHandler.unwrapFirestoreException(e)
                _snackbarMessage.value = "Delete failed: ${fsEx.message}"
            }
        }
    }

    fun toggleUserRole(targetUser: UserProfile) {
        if (_currentProfile.value?.isAdmin != true) return
        if (targetUser.uid == uid) {
            _snackbarMessage.value = "You cannot remove your own admin role."
            return
        }
        val nextRole = if (targetUser.isAdmin) "user" else "admin"
        viewModelScope.launch {
            try {
                repository.updateUserRole(targetUser.uid, nextRole)
                _snackbarMessage.value = "${targetUser.email} role changed to $nextRole."
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val fsEx = FirestoreErrorHandler.unwrapFirestoreException(e)
                _snackbarMessage.value = "Role update failed: ${fsEx.message}"
            }
        }
    }

    fun toggleUserLock(targetUser: UserProfile) {
        if (_currentProfile.value?.isAdmin != true) return
        if (targetUser.uid == uid) {
            _snackbarMessage.value = "You cannot lock your own account."
            return
        }
        val nextLocked = !targetUser.isLocked
        viewModelScope.launch {
            try {
                repository.setUserLocked(targetUser.uid, nextLocked)
                _snackbarMessage.value = if (nextLocked) {
                    "${targetUser.email} has been locked."
                } else {
                    "${targetUser.email} has been unlocked."
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val fsEx = FirestoreErrorHandler.unwrapFirestoreException(e)
                _snackbarMessage.value = "Lock update failed: ${fsEx.message}"
            }
        }
    }

    fun lockOutdatedUsers(minVersionCode: Int) {
        if (_currentProfile.value?.isAdmin != true) return
        viewModelScope.launch {
            try {
                val count = repository.lockOutdatedUsers(minVersionCode)
                _snackbarMessage.value = if (count > 0) {
                    "Locked $count account(s) on older app versions."
                } else {
                    "No older-version accounts to lock."
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val fsEx = FirestoreErrorHandler.unwrapFirestoreException(e)
                _snackbarMessage.value = "Action failed: ${fsEx.message}"
            }
        }
    }

    fun checkForAppUpdates(context: Context) {
        val current = _appUpdateState.value
        if (current is AppUpdateUiState.Checking || current is AppUpdateUiState.Downloading) return

        viewModelScope.launch {
            _appUpdateState.value = AppUpdateUiState.Checking
            if (!NetworkMonitor.isInternetAvailable(context)) {
                _appUpdateState.value = AppUpdateUiState.Error(AppUpdateManager.ERROR_CONNECTION_MSG)
                return@launch
            }
            val remoteResult = runCatching { AppUpdateManager.fetchUpdateInfo() }
            val info = remoteResult.getOrNull()
            if (info == null) {
                _appUpdateState.value = AppUpdateUiState.Error(AppUpdateManager.ERROR_CONNECTION_MSG)
            } else if (AppUpdateManager.isUpdateAvailable(info.versionCode, BuildConfig.VERSION_CODE)) {
                _appUpdateState.value = AppUpdateUiState.UpdateAvailable(info)
            } else {
                _appUpdateState.value = AppUpdateUiState.UpToDate
            }
        }
    }

    fun startAppUpdateDownload(context: Context, info: RemoteUpdateInfo) {
        if (_appUpdateState.value is AppUpdateUiState.Downloading) return
        viewModelScope.launch {
            _appUpdateState.value = AppUpdateUiState.Downloading(info, 0f, 0)
            try {
                val apkFile = AppUpdateManager.downloadApk(context, info.apkUrl) { progress, pct ->
                    _appUpdateState.value = AppUpdateUiState.Downloading(info, progress, pct)
                }
                _appUpdateState.value = AppUpdateUiState.ReadyToInstall(info, apkFile)
                AppUpdateManager.launchApkInstaller(context, apkFile)
            } catch (_: Exception) {
                _appUpdateState.value = AppUpdateUiState.Error(
                    message = AppUpdateManager.ERROR_CONNECTION_MSG,
                    retryInfo = info
                )
            }
        }
    }

    private fun loadFavorites(): Set<String> {
        return prefs.getStringSet("favorites", emptySet())?.toSet() ?: emptySet()
    }

    class Factory(
        private val application: Application,
        private val uid: String,
        private val email: String,
        private val displayName: String,
        private val isEmailVerified: Boolean
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CloudMovieViewModel(
                application = application,
                uid = uid,
                email = email,
                displayName = displayName,
                isEmailVerified = isEmailVerified
            ) as T
        }
    }
}
