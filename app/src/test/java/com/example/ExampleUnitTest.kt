package com.example

import com.example.data.MovieRepository
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
        assertEquals(VipTier.VIP_PRO_MAX, adminUser?.vipTier)

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
}
