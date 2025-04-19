package sibu.park.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sibu.park.data.model.User
import sibu.park.data.model.UserRole
import sibu.park.data.repository.AuthRepository
import javax.inject.Inject

data class AuthState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isProfileUpdated: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val isRegisterSuccess: Boolean = false,
    val isResetPasswordEmailSent: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state
    
    init {
        // 检查用户是否已登录
        val isLoggedIn = authRepository.isLoggedIn()
        if (isLoggedIn) {
            getCurrentUser()
        } else {
            _state.update { it.copy(isLoggedIn = false) }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val user = authRepository.login(email, password)
                _state.update {
                    it.copy(
                        user = user,
                        isLoggedIn = true,
                        isLoading = false,
                        isLoginSuccess = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "登录失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun register(username: String, email: String, phoneNumber: String, password: String, role: UserRole) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val user = authRepository.register(username, email, phoneNumber, password, role)
                _state.update {
                    it.copy(
                        user = user,
                        isLoggedIn = true,
                        isLoading = false,
                        isRegisterSuccess = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "注册失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                authRepository.resetPassword(email)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isResetPasswordEmailSent = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "发送重置密码邮件失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun getCurrentUser() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val user = authRepository.getCurrentUserData()
                _state.update {
                    it.copy(
                        user = user,
                        isLoggedIn = user != null,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "获取用户信息失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun updateProfile(username: String, email: String, phoneNumber: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val updatedUser = authRepository.updateUserProfile(username, email, phoneNumber)
                _state.update {
                    it.copy(
                        user = updatedUser,
                        isLoading = false,
                        isProfileUpdated = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "更新资料失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                authRepository.logout()
                _state.update { 
                    AuthState(isLoading = false) 
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "登出失败: ${e.message}"
                    )
                }
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
    
    fun clearProfileUpdated() {
        _state.update { it.copy(isProfileUpdated = false) }
    }
} 