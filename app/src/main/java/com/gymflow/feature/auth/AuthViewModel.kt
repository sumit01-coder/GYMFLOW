package com.gymflow.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.UserEntity
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

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
            _uiState.update { it.copy(error = "Please enter email and password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(400)

            val role = if (email.contains("trainer", ignoreCase = true)) Role.TRAINER else Role.MEMBER
            
            // Check if user already exists in local database
            val existingUser = userDao.getUserByEmail(email)
            val userId = existingUser?.id ?: "usr_${System.currentTimeMillis()}"
            val userName = existingUser?.name ?: run {
                val prefix = email.substringBefore("@").replace(".", " ").replace("_", " ")
                prefix.split(" ").filter { it.isNotBlank() }
                    .joinToString(" ") { part -> part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
                    .ifBlank { if (role == Role.TRAINER) "Coach Marcus" else "Alex Johnson" }
            }
            val token = existingUser?.token ?: "jwt_${userId}_token"

            // Persist to Room & DataStore
            tokenManager.saveAuth(
                token = token,
                userId = userId,
                email = email,
                name = userName,
                role = role
            )

            if (existingUser == null) {
                userDao.upsertUser(
                    UserEntity(
                        id = userId,
                        name = userName,
                        email = email,
                        role = role,
                        token = token,
                        heightCm = 178f,
                        currentWeightKg = 67.8f,
                        fitnessGoal = "Build Muscle",
                        age = 28,
                        syncStatus = "Connected",
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            } else {
                userDao.upsertUser(existingUser.copy(lastUpdated = System.currentTimeMillis()))
            }

            val user = User(
                id = userId,
                name = userName,
                email = email,
                role = role,
                token = token
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentUser = user,
                    isAuthenticated = true
                )
            }
            onSuccess(role)
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
                token = token
            )

            tokenManager.saveAuth(
                token = token,
                userId = user.id,
                email = user.email,
                name = user.name,
                role = user.role
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
                    lastUpdated = System.currentTimeMillis()
                )
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentUser = user,
                    isAuthenticated = true
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(400)

            val role = _uiState.value.role
            val userId = "usr_${System.currentTimeMillis()}"
            val token = "jwt_new_${userId}_token"

            val user = User(
                id = userId,
                name = name,
                email = email,
                role = role,
                token = token
            )

            // Persist the real registered credentials immediately in TokenManager & Room!
            tokenManager.saveAuth(
                token = token,
                userId = userId,
                email = email,
                name = name,
                role = role
            )

            userDao.upsertUser(
                UserEntity(
                    id = userId,
                    name = name,
                    email = email,
                    role = role,
                    token = token,
                    heightCm = 178f,
                    currentWeightKg = 67.8f,
                    fitnessGoal = "Build Muscle",
                    age = 28,
                    syncStatus = "Connected",
                    lastUpdated = System.currentTimeMillis()
                )
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentUser = user,
                    isAuthenticated = true
                )
            }
            onSuccess(user.role)
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your registered email") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(500)
            _uiState.update { it.copy(isLoading = false, resetEmailSent = true) }
        }
    }
}
