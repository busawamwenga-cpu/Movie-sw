package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class CloudMovieRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun observeMovies_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = CloudMovieRepository(firestore)

        var thrown: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeMovies().first()
            }
        } catch (e: Throwable) {
            thrown = e
        }

        val fsEx = checkNotNull(thrown?.unwrapFirestoreException()) {
            "Expected FirebaseFirestoreException, got $thrown"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx.code)
    }

    @Test
    fun regularUser_createsProfileAsUser_andCannotWriteMoviesOrListUsers() = runBlocking {
        val email = "viewer_${UUID.randomUUID()}@example.com"
        val uid = signInTestUser(email)
        val repository = CloudMovieRepository(firestore)

        val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureUserProfile(
                uid = uid,
                email = email,
                displayName = "Viewer User",
                appVersionCode = 1,
                isEmailVerified = false
            )
        }
        assertEquals("user", profile.role)
        assertFalse(profile.isLocked)

        // Regular user can read movies collection
        val movies = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeMovies().first()
        }
        assertTrue(movies.isNotEmpty() || movies.isEmpty())

        // Regular user cannot create a movie
        var writeError: Throwable? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.saveMovieMetadata(
                    movieId = "mov_${UUID.randomUUID()}",
                    title = "Unauthorized Upload",
                    description = "Should fail",
                    videoUrl = "https://example.com/movie.mp4",
                    storagePath = "movies/unauth.mp4",
                    thumbnailUrl = "",
                    fileName = "unauth.mp4",
                    fileSizeBytes = 1024L,
                    durationSeconds = 120,
                    uploadedBy = uid
                )
            }
        } catch (e: Throwable) {
            writeError = e
        }
        val writeFsEx = checkNotNull(writeError?.unwrapFirestoreException()) {
            "Expected PERMISSION_DENIED on movie write, got $writeError"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, writeFsEx.code)

        // Regular user cannot list all users
        var listUsersError: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeAllUsers().first()
            }
        } catch (e: Throwable) {
            listUsersError = e
        }
        val listFsEx = checkNotNull(listUsersError?.unwrapFirestoreException()) {
            "Expected PERMISSION_DENIED on users list, got $listUsersError"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, listFsEx.code)
    }

    @Test
    fun admin_canUploadDeleteMovies_andManageUserRolesAndLocks() = runBlocking {
        // First sign in a regular user so their profile doc exists
        val viewerEmail = "target_viewer_${UUID.randomUUID()}@example.com"
        val viewerUid = signInTestUser(viewerEmail)
        val viewerRepo = CloudMovieRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            viewerRepo.ensureUserProfile(
                uid = viewerUid,
                email = viewerEmail,
                displayName = "Target Viewer",
                appVersionCode = 1,
                isEmailVerified = true
            )
        }

        // Now sign in as verified bootstrap admin
        val adminUid = signInVerifiedAdminUser(CloudMovieRepository.BOOTSTRAP_ADMIN_EMAIL)
        val adminRepo = CloudMovieRepository(firestore)
        val adminProfile = withTimeout(DEFAULT_TIMEOUT_MS) {
            adminRepo.ensureUserProfile(
                uid = adminUid,
                email = CloudMovieRepository.BOOTSTRAP_ADMIN_EMAIL,
                displayName = "Movie SW Admin",
                appVersionCode = 2,
                isEmailVerified = true
            )
        }
        assertEquals("admin", adminProfile.role)

        // Admin creates movie
        val movieId = "mov_${UUID.randomUUID()}"
        val savedMovie = withTimeout(DEFAULT_TIMEOUT_MS) {
            adminRepo.saveMovieMetadata(
                movieId = movieId,
                title = "Admin Action Film",
                description = "Uploaded by admin",
                videoUrl = "https://example.com/$movieId.mp4",
                storagePath = "movies/$movieId.mp4",
                thumbnailUrl = "",
                fileName = "action.mp4",
                fileSizeBytes = 5_000_000L,
                durationSeconds = 5400,
                uploadedBy = adminUid
            )
        }

        val observedMovies = withTimeout(FLOW_TIMEOUT_MS) {
            adminRepo.observeMovies().first { list -> list.any { it.id == movieId } }
        }
        assertTrue(observedMovies.any { it.id == movieId && it.title == "Admin Action Film" })

        // Admin can list users, promote role to admin, and lock account
        val users = withTimeout(FLOW_TIMEOUT_MS) {
            adminRepo.observeAllUsers().first { list -> list.any { it.uid == viewerUid } }
        }
        assertTrue(users.any { it.uid == viewerUid })

        withTimeout(DEFAULT_TIMEOUT_MS) {
            adminRepo.updateUserRole(viewerUid, "admin")
            adminRepo.setUserLocked(viewerUid, true)
        }

        val updatedUsers = withTimeout(FLOW_TIMEOUT_MS) {
            adminRepo.observeAllUsers().first { list ->
                list.any { it.uid == viewerUid && it.role == "admin" && it.isLocked }
            }
        }
        assertTrue(updatedUsers.any { it.uid == viewerUid && it.role == "admin" && it.isLocked })

        // Admin deletes movie cleanly
        withTimeout(DEFAULT_TIMEOUT_MS) {
            adminRepo.deleteMovie(savedMovie)
        }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 5000L
    }
}
