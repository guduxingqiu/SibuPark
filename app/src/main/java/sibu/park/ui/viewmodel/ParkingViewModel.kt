package sibu.park.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ParkingSpot(
    val id: String,
    val area: String,
    val number: String,
    val isOccupied: Boolean,
    val occupiedSince: Date? = null,
    val licensePlate: String? = null
)

data class ParkingState(
    val parkingSpots: List<ParkingSpot> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ParkingViewModel @Inject constructor() : ViewModel() {
    
    private val _state = MutableStateFlow(ParkingState())
    val state: StateFlow<ParkingState> = _state
    
    // 获取停车场状态
    fun getParkingStatus() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // 这里应该是获取停车场状态的逻辑
                // 暂时使用模拟数据
                val mockParkingSpots = listOf(
                    ParkingSpot(
                        id = "spot1",
                        area = "A区",
                        number = "A-001",
                        isOccupied = true,
                        occupiedSince = Calendar.getInstance().apply { 
                            add(Calendar.HOUR, -2) 
                        }.time,
                        licensePlate = "粤B12345"
                    ),
                    ParkingSpot(
                        id = "spot2",
                        area = "A区",
                        number = "A-002",
                        isOccupied = false
                    ),
                    ParkingSpot(
                        id = "spot3",
                        area = "B区",
                        number = "B-001",
                        isOccupied = true,
                        occupiedSince = Calendar.getInstance().apply { 
                            add(Calendar.HOUR, -1) 
                        }.time,
                        licensePlate = "粤B54321"
                    )
                )
                
                _state.update {
                    it.copy(
                        parkingSpots = mockParkingSpots,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "获取停车场状态失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    // 查找特定车辆
    fun findVehicle(licensePlate: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // 这里应该是查找车辆的逻辑
                // 暂时使用模拟数据，过滤现有停车位
                val filteredSpots = _state.value.parkingSpots.filter {
                    it.licensePlate?.contains(licensePlate, ignoreCase = true) == true
                }
                
                _state.update {
                    it.copy(
                        parkingSpots = filteredSpots,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "查找车辆失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 