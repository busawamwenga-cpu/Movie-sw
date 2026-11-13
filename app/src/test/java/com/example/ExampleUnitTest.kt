package com.example

import com.example.data.MovieRepository
import com.example.data.local.EntityMappers.toDomain
import com.example.data.local.EntityMappers.toEntity
import com.example.model.*
import com.example.util.SecurityUtil
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPbkdf2PasswordHashing() {
        val salt = SecurityUtil.generateSalt()
        val plain = "SecretPass123"
        val hash = SecurityUtil.hashPassword(plain, salt)

        assertTrue(hash.isNotEmpty())
        assertTrue(SecurityUtil.verifyPassword(plain, salt, hash))
        assertFalse(SecurityUtil.verifyPassword("WrongPass", salt, hash))
    }

    @Test
    fun testUserRegistrationAndAuth() {
        val email = "testuser_${System.currentTimeMillis()}@moviesw.com"
        val registered = MovieRepository.registerUser(
            name = "Amani Test",
            country = "Tanzania",
            emailOrPhone = email,
            passwordPlain = "mypassword123",
            ageTier = AgeTier.AGE_18_PLUS
        )

        assertNotNull(registered)
        assertEquals(VipTier.BURE, registered.vipTier)

        val authenticated = MovieRepository.authenticateUser(email, "mypassword123")
        assertNotNull(authenticated)
        assertEquals(registered.id, authenticated?.id)

        val failedAuth = MovieRepository.authenticateUser(email, "wrongpassword")
        assertNull(failedAuth)
    }

    @Test
    fun testVipTiersAndDownloadQuotaRules() {
        // Test Bure tier download restriction
        val bureUser = MovieRepository.registerUser("Bure Guy", "Kenya", "bure@test.com", "pass12345", AgeTier.AGE_18_PLUS)
        val actionMovie = MovieItem(
            id = "test_action_01",
            title = "Test Action",
            category = MovieCategory.ACTION,
            ageRating = "18+",
            descriptionSw = "",
            descriptionFr = "",
            videoUrl = "https://drive.google.com/test",
            isDriveLink = true,
            isVipOnly = false
        )

        val canBureDownload = MovieRepository.downloadMovie(bureUser.id, actionMovie)
        assertFalse(canBureDownload)

        // Upgrade to VIP Pro
        MovieRepository.updateUserVip(bureUser.id, VipTier.VIP_PRO)
        val canProDownload = MovieRepository.downloadMovie(bureUser.id, actionMovie)
        assertTrue(canProDownload)
    }

    @Test
    fun testAdminLoginWithSpecificCredentials() {
        val adminUser = MovieRepository.authenticateUser("yesukishilo08@gmail.com", "boBO200@")
        assertNotNull(adminUser)
        assertEquals(UserRole.ADMIN, adminUser?.role)
        assertEquals(VipTier.VIP_PRO, adminUser?.vipTier)
        assertEquals("3.99 $ / mwezi", VipTier.VIP_PRO.priceSw)
        assertEquals("3.99 $ / mois", VipTier.VIP_PRO.priceFr)

        // Invalid code should fail
        val wrongAdmin = MovieRepository.authenticateUser("yesukishilo08@gmail.com", "wrongcode")
        assertNull(wrongAdmin)
    }

    @Test
    fun testPasswordResetViaGmail() {
        val email = "reset_test_${System.currentTimeMillis()}@gmail.com"
        MovieRepository.registerUser("Test Person", "DRC", email, "oldPassword123", AgeTier.AGE_18_PLUS)

        // Reset password
        val resetSuccess = MovieRepository.resetPassword(email, "newSecretPassword456")
        assertTrue(resetSuccess)

        // Old password fails
        val oldLogin = MovieRepository.authenticateUser(email, "oldPassword123")
        assertNull(oldLogin)

        // New password works
        val newLogin = MovieRepository.authenticateUser(email, "newSecretPassword456")
        assertNotNull(newLogin)
        assertEquals(email, newLogin?.emailOrPhone)
    }

    @Test
    fun testCountryDataAndDialCodes() {
        val defaultCountry = com.example.util.CountryData.defaultCountry
        assertEquals("CD", defaultCountry.code)
        assertEquals("+243", defaultCountry.dialCode)
        assertEquals("RD Congo (RDC)", defaultCountry.nameSw)

        val countries = com.example.util.CountryData.allCountries
        assertTrue(countries.size > 40)
        assertTrue(countries.any { it.code == "CD" && it.dialCode == "+243" })
        assertTrue(countries.any { it.code == "TZ" && it.dialCode == "+255" })
        assertTrue(countries.any { it.code == "KE" && it.dialCode == "+254" })
        assertTrue(countries.any { it.code == "FR" && it.dialCode == "+33" })
    }

    @Test
    fun testEmailVerificationAntiHijack() {
        val email = "antihijack_${System.currentTimeMillis()}@gmail.com"
        val unverifiedUser = MovieRepository.registerUser(
            name = "Security Test",
            country = "DRC",
            emailOrPhone = email,
            passwordPlain = "strongPass123",
            ageTier = AgeTier.AGE_18_PLUS,
            isEmailVerified = false
        )
        assertFalse(unverifiedUser.isEmailVerified)

        val verified = MovieRepository.verifyUserEmail(unverifiedUser.id)
        assertTrue(verified)

        val updatedUser = MovieRepository.users.value.find { it.id == unverifiedUser.id }
        assertNotNull(updatedUser)
        assertTrue(updatedUser!!.isEmailVerified)
    }

    @Test
    fun testDjAccountCreationAndAccessControl() {
        val email = "dj_test_${System.currentTimeMillis()}@moviesw.com"
        val createdDj = MovieRepository.createDjAccount(
            name = "Dj Test Full",
            email = email,
            passwordPlain = "djPass1234",
            djStageName = "DJ Mwanga",
            country = "RD Congo (RDC)",
            grantAccess = false
        )

        assertEquals(UserRole.DJ, createdDj.role)
        assertEquals("DJ Mwanga", createdDj.djStageName)
        assertFalse(createdDj.hasDashboardAccess)

        // Authentication succeeds but dashboard access starts false
        val authed = MovieRepository.authenticateUser(email, "djPass1234")
        assertNotNull(authed)
        assertFalse(authed!!.hasDashboardAccess)

        // Admin toggles access on
        val toggled = MovieRepository.toggleDjDashboardAccess(createdDj.id, true)
        assertTrue(toggled)
        val updated = MovieRepository.users.value.find { it.id == createdDj.id }
        assertTrue(updated!!.hasDashboardAccess)

        // Admin revokes access
        MovieRepository.toggleDjDashboardAccess(createdDj.id, false)
        val revoked = MovieRepository.users.value.find { it.id == createdDj.id }
        assertFalse(revoked!!.hasDashboardAccess)
    }

    @Test
    fun testOneMonthVipActivationAndExpiration() {
        val user = MovieRepository.registerUser(
            name = "Vip Expiry Tester",
            country = "DRC",
            emailOrPhone = "vip_exp_${System.currentTimeMillis()}@gmail.com",
            passwordPlain = "secret1234",
            ageTier = AgeTier.AGE_18_PLUS
        )
        assertFalse(user.isVipActive())
        assertEquals(0, user.daysRemaining())

        // Activate VIP Pro for 1 month (30 days)
        MovieRepository.updateUserVip(user.id, VipTier.VIP_PRO)
        val activeUser = MovieRepository.users.value.find { it.id == user.id }!!
        assertEquals(VipTier.VIP_PRO, activeUser.vipTier)
        assertTrue(activeUser.isVipActive())
        assertTrue(activeUser.daysRemaining() in 29..31)

        // Simulate expired timestamp
        val pastTime = System.currentTimeMillis() - 5_000L
        val expiredSnapshot = activeUser.copy(vipExpiresAt = pastTime)
        assertFalse(expiredSnapshot.isVipActive())
        assertEquals(0, expiredSnapshot.daysRemaining())

        // Revoking resets to Bure
        MovieRepository.updateUserVip(user.id, VipTier.BURE)
        val reverted = MovieRepository.users.value.find { it.id == user.id }!!
        assertEquals(VipTier.BURE, reverted.vipTier)
        assertFalse(reverted.isVipActive())
    }

    @Test
    fun testRoomEntityMappersRoundTrip() {
        val user = UserAccount(
            id = "u_roundtrip",
            name = "Kofi",
            country = "DRC",
            emailOrPhone = "kofi@gmail.com",
            passwordHash = "hash123",
            salt = "salt123",
            ageTier = AgeTier.AGE_18_PLUS,
            vipTier = VipTier.VIP_PRO,
            role = UserRole.DJ,
            preferredQuality = VideoQuality.P8K,
            actionDownloadsThisMonth = 4,
            seriesDownloadsThisMonth = 2,
            favoritesMovieIds = listOf("mov_01", "mov_02"),
            downloadedMovieIds = listOf("mov_01"),
            isEmailVerified = true,
            djStageName = "DJ Kofi",
            hasDashboardAccess = true,
            vipActivatedAt = 1000L,
            vipExpiresAt = 9999999999999L
        )
        val userRoundTrip = user.toEntity().toDomain()
        assertEquals(user, userRoundTrip)

        val movie = MovieItem(
            id = "m_roundtrip",
            title = "Upendo wa Kongo",
            category = MovieCategory.LOVE_STORY,
            ageRating = "10+",
            descriptionSw = "Hadithi nzuri, yenye hisia!",
            descriptionFr = "Une belle histoire | émouvante",
            posterRes = null,
            posterUrl = "https://example.com/poster.jpg",
            djName = "DJ Kofi",
            videoUrl = "https://drive.google.com/sample",
            isDriveLink = true,
            isVipOnly = true,
            releaseYear = "2025",
            durationMinutes = 120,
            rating = 4.9,
            viewsCount = 500,
            isSeries = true,
            episodes = listOf(
                Episode(1, "Kipindi 1", "Épisode 1", 45, "https://drive.google.com/ep1"),
                Episode(2, "Kipindi 2", "Épisode 2", 48, "https://drive.google.com/ep2")
            )
        )
        val movieRoundTrip = movie.toEntity(sortOrder = 42L).toDomain()
        assertEquals(movie, movieRoundTrip)

        val dl = DownloadTask(
            id = "dl_m_roundtrip",
            movieId = "m_roundtrip",
            movieTitle = "Upendo wa Kongo",
            moviePosterRes = null,
            category = MovieCategory.LOVE_STORY,
            progress = 0.75f,
            percentage = 75,
            downloadedMb = 300f,
            totalMb = 400f,
            speed = "4.2 MB/s",
            status = DownloadStatus.PAUSED
        )
        val dlRoundTrip = dl.toEntity("u_roundtrip").toDomain()
        assertEquals(dl, dlRoundTrip)
    }

    @Test
    fun testUpdateJsonParsingAndVersionComparison() {
        val validJson = """
            {
              "versionCode": 3,
              "versionName": "1.2",
              "apkUrl": "https://github.com/busawamwenga-cpu/Movie-sw/releases/download/v1.2/app-release.apk"
            }
        """.trimIndent()
        val info = com.example.util.AppUpdateManager.parseUpdateJson(validJson)
        assertEquals(3, info.versionCode)
        assertEquals("1.2", info.versionName)
        assertTrue(com.example.util.AppUpdateManager.isUpdateAvailable(info.versionCode, 1))
        assertFalse(com.example.util.AppUpdateManager.isUpdateAvailable(1, 1))
        assertFalse(com.example.util.AppUpdateManager.isUpdateAvailable(1, 2))

        assertThrows(IllegalArgumentException::class.java) {
            com.example.util.AppUpdateManager.parseUpdateJson("""{"versionCode": 2}""")
        }
    }

    @Test
    fun testAdminPublishedApkUpdateSerialization() {
        val adminUpdate = com.example.util.RemoteUpdateInfo(
            versionCode = 99,
            versionName = "9.9",
            apkUrl = "file:///data/user/0/com.example/files/apk_releases/release.apk",
            releaseNotes = "Sasisho jipya la Admin"
        )
        val encoded = com.example.util.AppUpdateManager.encodeUpdateJson(adminUpdate)
        val decoded = com.example.util.AppUpdateManager.decodeStoredUpdateJson(encoded)
        assertNotNull(decoded)
        assertEquals(99, decoded?.versionCode)
        assertEquals("9.9", decoded?.versionName)
        assertEquals(adminUpdate.apkUrl, decoded?.apkUrl)
        assertEquals("Sasisho jipya la Admin", decoded?.releaseNotes)
    }
}
