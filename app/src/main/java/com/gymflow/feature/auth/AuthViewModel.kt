package com.gymflow.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class AuthViewModel @Inject constructor() : ViewModel() {

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
            delay(600) // Simulating network auth

            val role = if (email.contains("trainer", ignoreCase = true)) Role.TRAINER else Role.MEMBER
            val user = User(
                id = "usr_${System.currentTimeMillis()}",
                name = if (role == Role.TRAINER) "Marcus Vance" else "Alex Johnson",
                email = email,
                role = role,
                token = "jwt_mock_token"
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
            delay(600)

            val user = User(
                id = "usr_code_member",
                name = "Alex Johnson",
                email = "alex.johnson@gymflow.com",
                role = Role.MEMBER,
                token = "jwt_join_token"
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
            delay(600)

            val user = User(
                id = "usr_new_${System.currentTimeMillis()}",
                name = name,
                email = email,
                role = _uiState.value.role,
                token = "jwt_mock_token"
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
