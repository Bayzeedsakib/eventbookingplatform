package com.example.eventbookingplatform.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventbookingplatform.data.model.UserProfile
import com.example.eventbookingplatform.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Loading : AuthUiState()
    data object Guest : AuthUiState()
    data class SignedIn(val profile: UserProfile) : AuthUiState()
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _authState = MutableLiveData<AuthUiState>(AuthUiState.Loading)
    val authState: LiveData<AuthUiState> = _authState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        repository.addAuthStateListener { user ->
            if (user == null) {
                _authState.postValue(AuthUiState.Guest)
            } else {
                refreshProfile(user.uid)
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email and password are required."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            runCatching { repository.login(email, password) }
                .onSuccess { profile -> _authState.value = AuthUiState.SignedIn(profile) }
                .onFailure { error -> _errorMessage.value = mapAuthError(error) }
            _isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String) {
        when {
            name.isBlank() -> _errorMessage.value = "Name is required."
            email.isBlank() || password.isBlank() -> _errorMessage.value = "Email and password are required."
            password.length < 6 -> _errorMessage.value = "Password must be at least 6 characters."
            else -> viewModelScope.launch {
                _isLoading.value = true
                _errorMessage.value = null
                runCatching { repository.register(name, email, password) }
                    .onSuccess { profile -> _authState.value = AuthUiState.SignedIn(profile) }
                    .onFailure { error -> _errorMessage.value = mapAuthError(error) }
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        _errorMessage.value = null
        repository.logout()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun refreshProfile(uid: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val profile = runCatching { repository.fetchUserProfile(uid) }.getOrNull()
            _authState.value = if (profile != null) {
                AuthUiState.SignedIn(profile)
            } else {
                val user = repository.currentUser
                if (user != null) {
                    val ensured = runCatching { repository.ensureUserProfile(user) }.getOrNull()
                    if (ensured != null) AuthUiState.SignedIn(ensured) else AuthUiState.Guest
                } else {
                    AuthUiState.Guest
                }
            }
        }
    }

    private fun mapAuthError(error: Throwable): String {
        return when (error) {
            is FirebaseAuthException -> when (error.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email address."
                "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Incorrect email or password."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists."
                "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters."
                "ERROR_USER_NOT_FOUND" -> "No account found for this email."
                else -> error.message ?: "Authentication failed."
            }
            else -> error.message ?: "Something went wrong. Please try again."
        }
    }
}
