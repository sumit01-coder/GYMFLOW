package com.gymflow.feature.trainer.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrainerLoginUiState(
    val email: String = "trainer@gymflow.com",
    val password: String = "password123",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val loggedInUser: User? = null
)

class TrainerAuthViewModel : ViewModel() {

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
            delay(600) // Simulated network verification

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

            // Successfully authenticated as Trainer
            val trainerUser = User(
                id = "trainer-marcus-01",
                name = "Coach Marcus Vance",
                email = email,
                role = Role.TRAINER,
                token = "jwt-trainer-auth-token-sample"
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
