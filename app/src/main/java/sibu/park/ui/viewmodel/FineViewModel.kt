package sibu.park.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sibu.park.data.model.Fine
import sibu.park.data.model.FineStatus
import sibu.park.data.repository.FineRepository
import java.util.*
import javax.inject.Inject

data class FineState(
    val fines: List<Fine> = emptyList(),
    val selectedFine: Fine? = null,
    val isLoading: Boolean = false,
    val isFineIssued: Boolean = false,
    val isFinePaid: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FineViewModel @Inject constructor(
    private val fineRepository: FineRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(FineState())
    val state: StateFlow<FineState> = _state
    
    // 获取用户罚款
    fun getUserFines() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val fines = fineRepository.getUserFines()
                _state.update {
                    it.copy(
                        fines = fines,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "获取罚款失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    // 获取未支付的罚款
    fun getUnpaidFines() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val unpaidFines = fineRepository.getUnpaidFines()
                _state.update {
                    it.copy(
                        fines = unpaidFines,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "获取未支付罚款失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    // 获取罚款详情
    fun getFineDetails(fineId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val fine = fineRepository.getFineById(fineId)
                _state.update {
                    it.copy(
                        selectedFine = fine,
                        isLoading = false,
                        error = if (fine == null) "找不到罚款详情" else null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "获取罚款详情失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    // 支付罚款
    fun payFine(fineId: String, transactionId: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val success = fineRepository.payFine(fineId, transactionId)
                if (success) {
                    // 罚款支付成功后更新罚款列表
                    getUserFines()
                    _state.update {
                        it.copy(
                            isFinePaid = true,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "支付罚款失败"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "支付罚款失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    // 发布罚款（工作人员使用）
    fun issueFine(
        licensePlate: String,
        parkingArea: String,
        parkingSpot: String?,
        amount: Double,
        notes: String?
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val fine = fineRepository.issueFine(
                    licensePlate,
                    parkingArea,
                    parkingSpot,
                    amount,
                    notes
                )
                
                _state.update {
                    it.copy(
                        isLoading = false,
                        isFineIssued = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "发布罚款失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    // 根据车牌查询罚款
    fun getFinesByLicensePlate(licensePlate: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val fines = fineRepository.getFinesByLicensePlate(licensePlate)
                _state.update {
                    it.copy(
                        fines = fines,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "查询罚款失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearFineIssued() {
        _state.update { it.copy(isFineIssued = false) }
    }
    
    fun clearFinePaid() {
        _state.update { it.copy(isFinePaid = false) }
    }
    
    fun clearSelectedFine() {
        _state.update { it.copy(selectedFine = null) }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 