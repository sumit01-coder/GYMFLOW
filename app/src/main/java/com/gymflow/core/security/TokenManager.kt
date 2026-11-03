package com.gymflow.core.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.gymflow.domain.model.Role
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tag = "TokenManager"

    private val prefs: SharedPreferences by lazy {
        createEncryptedPrefs()
    }

    private val _tokenFlow = MutableStateFlow<String?>(null)
    private val _roleFlow = MutableStateFlow<Role?>(null)
    private val _userIdFlow = MutableStateFlow<String?>(null)
    private val _userEmailFlow = MutableStateFlow<String?>(null)
    private val _userNameFlow = MutableStateFlow<String?>(null)
    private val _emailVerifiedFlow = MutableStateFlow<Boolean>(false)

    init {
        // Initialize reactive cache from secure storage
        _tokenFlow.value = prefs.getString(KEY_AUTH_TOKEN, null)
        _userIdFlow.value = prefs.getString(KEY_USER_ID, null)
        _userEmailFlow.value = prefs.getString(KEY_USER_EMAIL, null)
        _userNameFlow.value = prefs.getString(KEY_USER_NAME, null)
        _emailVerifiedFlow.value = prefs.getBoolean(KEY_EMAIL_VERIFIED, false)
        val roleStr = prefs.getString(KEY_USER_ROLE, null)
        _roleFlow.value = roleStr?.let {
            try {
                Role.valueOf(it)
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to create EncryptedSharedPreferences, falling back to standard prefs", e)
            context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    // Token Operations
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
        _tokenFlow.value = token
    }

    fun getToken(): Flow<String?> = _tokenFlow.asStateFlow()

    fun getTokenSync(): String? = _tokenFlow.value ?: prefs.getString(KEY_AUTH_TOKEN, null)

    // Role Operations
    fun saveRole(role: Role) {
        prefs.edit().putString(KEY_USER_ROLE, role.name).apply()
        _roleFlow.value = role
    }

    fun getRole(): Flow<Role?> = _roleFlow.asStateFlow()

    fun getRoleSync(): Role? = _roleFlow.value

    // User ID Operations
    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
        _userIdFlow.value = userId
    }

    fun getUserId(): Flow<String?> = _userIdFlow.asStateFlow()

    fun getUserIdSync(): String? = _userIdFlow.value ?: prefs.getString(KEY_USER_ID, null)

    // User Email Operations
    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
        _userEmailFlow.value = email
    }

    fun getUserEmail(): Flow<String?> = _userEmailFlow.asStateFlow()

    fun getEmail(): String? = _userEmailFlow.value ?: prefs.getString(KEY_USER_EMAIL, null)

    fun getUserEmailSync(): String? = getEmail()

    // User Name Operations
    fun saveUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
        _userNameFlow.value = name
    }

    fun getUserName(): Flow<String?> = _userNameFlow.asStateFlow()

    fun getName(): String? = _userNameFlow.value ?: prefs.getString(KEY_USER_NAME, null)

    fun getUserNameSync(): String? = getName()

    // Combined Auth Persistence
    fun saveAuth(
        token: String,
        userId: String,
        email: String,
        name: String,
        role: Role,
        isEmailVerified: Boolean = false
    ) {
        prefs.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_ROLE, role.name)
            .putBoolean(KEY_EMAIL_VERIFIED, isEmailVerified)
            .apply()

        _tokenFlow.value = token
        _userIdFlow.value = userId
        _userEmailFlow.value = email
        _userNameFlow.value = name
        _roleFlow.value = role
        _emailVerifiedFlow.value = isEmailVerified
    }

    // Email Verification Operations
    fun setEmailVerified(verified: Boolean) {
        prefs.edit().putBoolean(KEY_EMAIL_VERIFIED, verified).apply()
        _emailVerifiedFlow.value = verified
    }

    fun isEmailVerified(): Flow<Boolean> = _emailVerifiedFlow.asStateFlow()

    fun isEmailVerifiedSync(): Boolean = _emailVerifiedFlow.value

    // Auth state check
    fun isAuthenticated(): Flow<Boolean> = _tokenFlow.map { !it.isNullOrBlank() }

    fun isAuthenticatedSync(): Boolean = !getTokenSync().isNullOrBlank()

    // Clear session
    fun clearAll() {
        prefs.edit().clear().apply()
        _tokenFlow.value = null
        _userIdFlow.value = null
        _userEmailFlow.value = null
        _userNameFlow.value = null
        _roleFlow.value = null
        _emailVerifiedFlow.value = false
    }

    companion object {
        private const val ENCRYPTED_PREFS_NAME = "gymflow_secure_prefs"
        private const val FALLBACK_PREFS_NAME = "gymflow_fallback_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_EMAIL_VERIFIED = "email_verified"
    }
}
