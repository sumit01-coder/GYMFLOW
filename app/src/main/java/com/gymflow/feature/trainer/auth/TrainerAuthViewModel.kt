package com.gymflow.feature.trainer.auth

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

data class TrainerLoginUiState(
    val email: String = "trainer@gymflow.com",
    val password: String = "password123",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val loggedInUser: User? = null
)

@HiltViewModel
class TrainerAuthViewModel @Inject constructor(
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainerLoginUiState())
    val uiState: StateFlow<TrainerLoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your trainer email address") }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(400)

            // Role verification check
            if (email.contains("member", ignoreCase = true)) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Access Denied: This account has MEMBER role. Trainer Studio is exclusively for Coach accounts."
                    )
                }
                return@launch
            }

            val existing = userDao.getUserByEmail(email)
            val trainerName = existing?.name ?: run {
                val prefix = email.substringBefore("@").replace(".", " ").replace("_", " ")
                val clean = prefix.split(" ").filter { it.isNotBlank() }
                    .joinToString(" ") { part -> part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
                if (clean.isBlank()) "Coach" else "Coach $clean"
            }
            val userId = existing?.id ?: "trainer_${System.currentTimeMillis()}"
            val token = existing?.token ?: "jwt-trainer-$userId"

            tokenManager.saveAuth(
                token = token,
                userId = userId,
                email = email,
                name = trainerName,
                role = Role.TRAINER
            )

            userDao.upsertUser(
                UserEntity(
                    id = userId,
                    name = trainerName,
                    email = email,
                    role = Role.TRAINER,
                    token = token,
                    lastUpdated = System.currentTimeMillis()
                )
            )

            val trainerUser = User(
                id = userId,
                name = trainerName,
                email = email,
                role = Role.TRAINER,
                token = token
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSuccess = true,
                    loggedInUser = trainerUser
                )
            }
        }
    }

    fun fillSampleTrainer() {
        _uiState.update {
            it.copy(
                email = "trainer@gymflow.com",
                password = "password123",
                errorMessage = null
            )
        }
    }
}
