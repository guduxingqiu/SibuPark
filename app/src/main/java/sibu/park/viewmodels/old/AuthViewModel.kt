package sibu.park.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sibu.park.models.User
import sibu.park.models.UserRole
import sibu.park.repositories.AuthRepository
import sibu.park.utils.Resource

data class AuthState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val isLoginSuccess: Boolean = false,
    val isRegisterSuccess: Boolean = false,
    val isResetPasswordEmailSent: Boolean = false
)

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        val isLoggedIn = repository.isLoggedIn()
        _state.update { it.copy(isLoggedIn = isLoggedIn) }
        
        if (isLoggedIn) {
            getUserData()
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            repository.login(email, password).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        getUserData()
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                isLoggedIn = true,
                                isLoginSuccess = true,
                                error = null
                            ) 
                        }
                    }
                    is Resource.Error -> {
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                error = result.message
                            ) 
                        }
                    }
                }
            }
        }
    }
    
    fun register(username: String, email: String, phoneNumber: String, password: String, role: UserRole = UserRole.USER) {
        viewModelScope.launch {
            repository.register(username, email, phoneNumber, password, role).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        getUserData()
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                isLoggedIn = true,
                                isRegisterSuccess = true,
                                error = null
                            ) 
                        }
                    }
                    is Resource.Error -> {
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                error = result.message
                            ) 
                        }
                    }
                }
            }
        }
    }
    
    fun resetPassword(email: String) {
        viewModelScope.launch {
            repository.resetPassword(email).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                isResetPasswordEmailSent = true,
                                error = null
                            ) 
                        }
                    }
                    is Resource.Error -> {
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                error = result.message
                            ) 
                        }
                    }
                }
            }
        }
    }
    
    fun logout() {
        repository.logout()
        _state.update { 
            AuthState()
        }
    }
    
    fun getUserData() {
        viewModelScope.launch {
            when (val result = repository.getCurrentUserData()) {
                is Resource.Success -> {
                    result.data?.let { user ->
                        _state.update { 
                            it.copy(
                                user = user,
                                isLoggedIn = true,
                                error = null
                            ) 
                        }
                    }
                }
                is Resource.Error -> {
                    _state.update { 
                        it.copy(
                            error = result.message
                        ) 
                    }
                }
                else -> {}
            }
        }
    }
    
    fun updateProfile(username: String? = null, email: String? = null, phoneNumber: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.updateUserProfile(username, email, phoneNumber)) {
                is Resource.Success -> {
                    getUserData()
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = null
                        ) 
                    }
                }
                is Resource.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        ) 
                    }
                }
                else -> {}
            }
        }
    }
    
    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.updatePassword(newPassword)) {
                is Resource.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = null
                        ) 
                    }
                }
                is Resource.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        ) 
                    }
                }
                else -> {}
            }
        }
    }
    
    fun clearLoginSuccess() {
        _state.update { it.copy(isLoginSuccess = false) }
    }
    
    fun clearRegisterSuccess() {
        _state.update { it.copy(isRegisterSuccess = false) }
    }
    
    fun clearResetPasswordState() {
        _state.update { it.copy(isResetPasswordEmailSent = false) }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 