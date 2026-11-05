package com.gymflow.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.UserEntity
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val role: Role = Role.MEMBER,
    val joinCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val resetEmailSent: Boolean = false,
    val currentUser: User? = null,
    val isAuthenticated: Boolean = false,
    val isVerificationEmailSent: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val isCheckingVerification: Boolean = false,
    val verificationMessage: String? = null,
    val verificationError: String? = null,
    val isEmailVerified: Boolean = false,
    val graceDaysRemaining: Int = 7,
    val isGracePeriodExpired: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userDao: UserDao,
    private val tokenManager: TokenManager,
    private val firebaseAuth: FirebaseAuth,
    private val firestoreSyncManager: com.gymflow.core.firebase.FirestoreSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    companion object {
        const val SEVEN_DAYS_MS = 7L * 24 * 60 * 60 * 1000L
    }

    init {
        val savedEmail = tokenManager.getEmail() ?: firebaseAuth.currentUser?.email ?: ""
        val savedName = tokenManager.getName() ?: firebaseAuth.currentUser?.displayName ?: ""
        val savedRole = tokenManager.getRoleSync() ?: Role.MEMBER
        val isVerified = tokenManager.isEmailVerifiedSync() || (firebaseAuth.currentUser?.isEmailVerified == true)
        val daysRemaining = tokenManager.getGraceDaysRemaining()
        val isExpired = tokenManager.isGracePeriodExpired()

        _uiState.update {
            it.copy(
                email = savedEmail,
                name = savedName,
                role = savedRole,
                isEmailVerified = isVerified,
                graceDaysRemaining = daysRemaining,
                isGracePeriodExpired = isExpired
            )
        }
    }

    fun getTokenSync(): String? = tokenManager.getTokenSync()
    fun getRoleSync(): Role? = tokenManager.getRoleSync()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun onRoleChange(role: Role) {
        _uiState.update { it.copy(role = role) }
    }

    fun onJoinCodeChange(code: String) {
        _uiState.update { it.copy(joinCode = code.uppercase(), error = null) }
    }

    fun login(onSuccess: (Role) -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email and password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, verificationError = null) }

            try {
                // 1. Primary Authentication: Firebase Auth
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Authentication failed. Please check credentials.") }
                    return@launch
                }

                // Refresh email verification status from Firebase servers
                try { firebaseUser.reload().await() } catch (_: Exception) {}

                val firebaseUid = firebaseUser.uid
                val isVerified = firebaseUser.isEmailVerified

                // 2. Retrieve User Profile from Primary Database (Cloud Firestore)
                val firestoreUser = try {
                    firestoreSyncManager.getUserProfileFromFirestore(firebaseUid)
                } catch (_: Exception) {
                    null
                }

                val creationTime = firestoreUser?.createdAtEpochMs
                    ?: firebaseUser.metadata?.creationTimestamp
                    ?: System.currentTimeMillis()

                val now = System.currentTimeMillis()
                val elapsedMs = now - creationTime
                val isExpired = !isVerified && (elapsedMs > SEVEN_DAYS_MS)
                val daysLeft = if (!isVerified && !isExpired) {
                    (((SEVEN_DAYS_MS - elapsedMs) / (24 * 60 * 60 * 1000L)) + 1).toInt().coerceIn(1, 7)
                } else 0

                val role = firestoreUser?.role
                    ?: if (email.contains("trainer", ignoreCase = true)) Role.TRAINER else Role.MEMBER
                val userName = firestoreUser?.name
                    ?: firebaseUser.displayName
                    ?: email.substringBefore("@").replace(".", " ")
                        .split(" ").filter { it.isNotBlank() }
                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                        .ifBlank { if (role == Role.TRAINER) "Coach Marcus" else "Member" }

                val token = "firebase_jwt_${firebaseUid}"

                // 3. Enforce 7-Day Email Verification Requirement:
                if (isExpired) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Your 7-day email verification period has expired. Please verify your email ($email) to continue.",
                            verificationError = "Your 7-day email verification period has expired. Please verify your email ($email) to continue.",
                            isEmailVerified = false,
                            isGracePeriodExpired = true
                        )
                    }
                    return@launch
                }

                // 4. User is verified or within 7-day grace period -> Save Auth and Allow Login
                tokenManager.saveAuth(
                    token = token,
                    userId = firebaseUid,
                    email = email,
                    name = userName,
                    role = role,
                    isEmailVerified = isVerified,
                    createdAtEpochMs = creationTime
                )

                val userToPersist = firestoreUser?.copy(
                    isEmailVerified = isVerified,
                    lastUpdated = now
                ) ?: UserEntity(
                    id = firebaseUid,
                    name = userName,
                    email = email,
                    role = role,
                    token = token,
                    heightCm = 0f,
                    currentWeightKg = 0f,
                    fitnessGoal = "General Fitness",
                    age = 0,
                    syncStatus = "Connected",
                    isEmailVerified = isVerified,
                    createdAtEpochMs = creationTime,
                    lastUpdated = now
                )

                userDao.upsertUser(userToPersist)

                // Sync with Firestore (pull all user data down and sync pending local items)
                launch {
                    try {
                        firestoreSyncManager.saveUserProfile(userToPersist)
                        firestoreSyncManager.pullAllUserDataFromFirestore(firebaseUid)
                        firestoreSyncManager.syncAllLocalUserData(firebaseUid)
                    } catch (_: Exception) {}
                }

                val domainUser = User(
                    id = firebaseUid,
                    name = userName,
                    email = email,
                    role = role,
                    token = token,
                    isEmailVerified = isVerified
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = domainUser,
                        isAuthenticated = true,
                        isEmailVerified = isVerified,
                        graceDaysRemaining = daysLeft,
                        isGracePeriodExpired = false
                    )
                }

                onSuccess(role)

            } catch (e: Exception) {
                // Handle specific Firebase Auth exceptions
                val errorMessage = when (e) {
                    is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                        "No account found with this email. Please create an account first."
                    }
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                        "Invalid email or password. Please check your credentials or create an account."
                    }
                    is com.google.firebase.FirebaseNetworkException -> {
                        val existingUser = userDao.getUserByEmail(email)
                        if (existingUser != null) {
                            val creationTime = existingUser.createdAtEpochMs
                            val isExpired = !existingUser.isEmailVerified && ((System.currentTimeMillis() - creationTime) > SEVEN_DAYS_MS)
                            if (isExpired) {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = "Your 7-day email verification period has expired. Please connect to the internet and verify your email.",
                                        isGracePeriodExpired = true
                                    )
                                }
                                return@launch
                            }

                            tokenManager.saveAuth(
                                token = existingUser.token ?: "offline_token",
                                userId = existingUser.id,
                                email = existingUser.email,
                                name = existingUser.name,
                                role = existingUser.role,
                                isEmailVerified = existingUser.isEmailVerified,
                                createdAtEpochMs = creationTime
                            )
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    currentUser = existingUser.toDomain(),
                                    isAuthenticated = true,
                                    isEmailVerified = existingUser.isEmailVerified
                                )
                            }
                            onSuccess(existingUser.role)
                            return@launch
                        } else {
                            "Unable to connect to Firebase. Please check your internet connection."
                        }
                    }
                    else -> {
                        val msg = e.localizedMessage ?: "Login failed"
                        if (msg.contains("no user record", ignoreCase = true) || msg.contains("user-not-found", ignoreCase = true)) {
                            "No account found with this email. Please create an account first."
                        } else if (msg.contains("password", ignoreCase = true) || msg.contains("invalid-credential", ignoreCase = true)) {
                            "Invalid email or password. Please check your credentials."
                        } else {
                            msg
                        }
                    }
                }
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    fun loginWithJoinCode(onSuccess: (Role) -> Unit) {
        val code = _uiState.value.joinCode.trim()
        if (code.isBlank() || code.length < 5) {
            _uiState.update { it.copy(error = "Please enter a valid join code (e.g. GYM-82K91)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(400)

            val userId = "usr_code_${System.currentTimeMillis()}"
            val token = "jwt_join_token"
            val user = User(
                id = userId,
                name = "Member $code",
                email = "member.$code@gymflow.com",
                role = Role.MEMBER,
                token = token,
                isEmailVerified = true
            )

            tokenManager.saveAuth(
                token = token,
                userId = user.id,
                email = user.email,
                name = user.name,
                role = user.role,
                isEmailVerified = true
            )

            userDao.upsertUser(
                UserEntity(
                    id = user.id,
                    name = user.name,
                    email = user.email,
                    role = user.role,
                    token = token,
                    connectedTrainerId = "trainer_gym82k91",
                    syncStatus = "Connected",
                    isEmailVerified = true,
                    lastUpdated = System.currentTimeMillis()
                )
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentUser = user,
                    isAuthenticated = true,
                    isEmailVerified = true
                )
            }
            onSuccess(Role.MEMBER)
        }
    }

    fun register(onSuccess: (Role) -> Unit) {
        val name = _uiState.value.name.trim()
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (name.isBlank() || email.isBlank() || password.length < 6) {
            _uiState.update { it.copy(error = "Please fill in all fields (password min 6 chars)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, verificationError = null) }

            // 1. Real-time duplicate account validation against Primary Database (Firestore)
            try {
                val alreadyExists = firestoreSyncManager.isEmailRegistered(email)
                if (alreadyExists) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "An account with this email address already exists. Please log in instead."
                        )
                    }
                    return@launch
                }
            } catch (_: Exception) {}

            val role = _uiState.value.role

            try {
                // 2. Create User Account in Firebase Auth
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to create account in Firebase.") }
                    return@launch
                }

                val firebaseUid = firebaseUser.uid
                val now = System.currentTimeMillis()

                // Send email verification link immediately
                try {
                    firebaseUser.sendEmailVerification().await()
                    _uiState.update { it.copy(isVerificationEmailSent = true) }
                    startCooldownTimer(60)
                } catch (e: Exception) {
                    android.util.Log.e("AuthViewModel", "sendEmailVerification error: ${e.message}", e)
                }

                val token = "firebase_jwt_${firebaseUid}"

                // 3. Save User Profile in Cloud Firestore (Primary Database)
                val newUserEntity = UserEntity(
                    id = firebaseUid,
                    name = name,
                    email = email,
                    role = role,
                    token = token,
                    heightCm = 0f,
                    currentWeightKg = 0f,
                    fitnessGoal = "General Fitness",
                    age = 0,
                    syncStatus = "Connected",
                    isEmailVerified = false,
                    createdAtEpochMs = now,
                    lastUpdated = now
                )

                firestoreSyncManager.saveUserProfile(newUserEntity)

                // 4. Save to Room SQLite & Encrypted TokenManager
                tokenManager.saveAuth(
                    token = token,
                    userId = firebaseUid,
                    email = email,
                    name = name,
                    role = role,
                    isEmailVerified = false,
                    createdAtEpochMs = now
                )

                userDao.upsertUser(newUserEntity)

                val user = User(
                    id = firebaseUid,
                    name = name,
                    email = email,
                    role = role,
                    token = token,
                    isEmailVerified = false
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        isAuthenticated = true,
                        isEmailVerified = false,
                        graceDaysRemaining = 7,
                        isGracePeriodExpired = false
                    )
                }

                onSuccess(user.role)

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                        "An account with this email address already exists. Please log in instead."
                    }
                    is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> {
                        "Password is too weak. Please use at least 6 characters with letters and numbers."
                    }
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                        "Please enter a valid email address."
                    }
                    is com.google.firebase.FirebaseNetworkException -> {
                        "Unable to connect to Firebase. Please check your internet connection to create an account."
                    }
                    else -> {
                        val msg = e.localizedMessage ?: "Registration failed"
                        if (msg.contains("already in use", ignoreCase = true) || msg.contains("collision", ignoreCase = true)) {
                            "An account with this email address already exists. Please log in instead."
                        } else {
                            msg
                        }
                    }
                }
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    fun checkEmailVerification(onSuccess: (Role) -> Unit) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCheckingVerification = true,
                    verificationError = null,
                    verificationMessage = null
                )
            }

            try {
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null) {
                    currentUser.reload().await()
                    val isVerified = currentUser.isEmailVerified

                    if (isVerified) {
                        val userId = tokenManager.getUserIdSync() ?: currentUser.uid
                        val role = tokenManager.getRoleSync() ?: _uiState.value.role

                        userDao.updateEmailVerified(userId, true)
                        tokenManager.setEmailVerified(true)

                        val updatedUser = userDao.getUserById(userId)
                        if (updatedUser != null) {
                            try { firestoreSyncManager.saveUserProfile(updatedUser.copy(isEmailVerified = true)) } catch (_: Exception) {}
                        }

                        _uiState.update {
                            it.copy(
                                isCheckingVerification = false,
                                isEmailVerified = true,
                                isGracePeriodExpired = false,
                                verificationMessage = "Email verified successfully! Welcome to GYMFLOW."
                            )
                        }
                        delay(600)
                        onSuccess(role)
                        return@launch
                    }
                }
            } catch (_: Exception) {
            }

            val daysLeft = tokenManager.getGraceDaysRemaining()
            val isExpired = tokenManager.isGracePeriodExpired()

            _uiState.update {
                it.copy(
                    isCheckingVerification = false,
                    isGracePeriodExpired = isExpired,
                    graceDaysRemaining = daysLeft,
                    verificationError = if (isExpired) {
                        "Your 7-day email verification period has expired. Please check your inbox and click the verification link before logging in."
                    } else {
                        "Email not verified yet ($daysLeft day(s) left in grace period). Please click the link sent to your email, then tap below."
                    }
                )
            }
        }
    }

    fun resendVerificationEmail() {
        if (_uiState.value.resendCooldownSeconds > 0) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    verificationError = null,
                    verificationMessage = null
                )
            }
            try {
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null) {
                    currentUser.sendEmailVerification().await()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isVerificationEmailSent = true,
                            verificationMessage = "Verification email resent! Check your inbox."
                        )
                    }
                    startCooldownTimer(60)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            verificationError = "User session not found. Please log in first."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        verificationError = e.localizedMessage ?: "Failed to resend verification email."
                    )
                }
            }
        }
    }

    private fun startCooldownTimer(seconds: Int) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                _uiState.update { it.copy(resendCooldownSeconds = remaining) }
                delay(1000)
                remaining--
            }
            _uiState.update { it.copy(resendCooldownSeconds = 0) }
        }
    }

    fun skipVerificationForNow(onSuccess: (Role) -> Unit) {
        if (_uiState.value.isGracePeriodExpired || tokenManager.isGracePeriodExpired()) {
            _uiState.update {
                it.copy(
                    verificationError = "Your 7-day verification grace period has expired. You must verify your email to access GYMFLOW."
                )
            }
            return
        }
        val role = tokenManager.getRoleSync() ?: _uiState.value.role
        onSuccess(role)
    }

    fun sendPasswordReset() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your registered email") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                firebaseAuth.sendPasswordResetEmail(email).await()
            } catch (_: Exception) {
                // Offline fallback
            }
            delay(300)
            _uiState.update { it.copy(isLoading = false, resetEmailSent = true) }
        }
    }
}
