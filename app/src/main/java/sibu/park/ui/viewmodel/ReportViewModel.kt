package sibu.park.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sibu.park.data.model.Report
import sibu.park.data.model.ReportStatus
import java.util.*
import javax.inject.Inject

data class ReportState(
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor() : ViewModel() {
    
    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state
    
    // 获取所有报告
    fun getAllReports() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // 这里应该是获取所有报告的逻辑
                // 暂时使用模拟数据
                val mockReports = listOf(
                    Report(
                        id = "report1",
                        userId = "user123",
                        title = "停车位问题",
                        description = "A区停车位标线模糊",
                        parkingArea = "A区",
                        createdAt = Date(),
                        status = ReportStatus.PENDING
                    ),
                    Report(
                        id = "report2",
                        userId = "user456",
                        title = "支付问题",
                        description = "使用停车券时系统异常",
                        parkingArea = "B区",
                        createdAt = Calendar.getInstance().apply { 
                            add(Calendar.DAY_OF_MONTH, -2) 
                        }.time,
                        status = ReportStatus.IN_PROGRESS
                    ),
                    Report(
                        id = "report3",
                        userId = "user789",
                        title = "安全问题",
                        description = "C区照明不足",
                        parkingArea = "C区",
                        parkingSpot = "C-023",
                        createdAt = Calendar.getInstance().apply { 
                            add(Calendar.DAY_OF_MONTH, -5) 
                        }.time,
                        status = ReportStatus.COMPLETED
                    )
                )
                
                _state.update {
                    it.copy(
                        reports = mockReports,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "获取报告失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    // 获取用户的报告
    fun getUserReports() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // 这里应该是获取用户报告的逻辑
                // 暂时使用模拟数据
                val mockReports = listOf(
                    Report(
                        id = "report1",
                        userId = "user123",
                        title = "停车位问题",
                        description = "A区停车位标线模糊",
                        parkingArea = "A区",
                        createdAt = Date(),
                        status = ReportStatus.PENDING
                    ),
                    Report(
                        id = "report2",
                        userId = "user123",
                        title = "支付问题",
                        description = "使用停车券时系统异常",
                        parkingArea = "B区",
                        createdAt = Calendar.getInstance().apply { 
                            add(Calendar.DAY_OF_MONTH, -2) 
                        }.time,
                        status = ReportStatus.IN_PROGRESS
                    )
                )
                
                _state.update {
                    it.copy(
                        reports = mockReports,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "获取报告失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    // 提交报告
    fun submitReport(
        type: Any,
        title: String,
        description: String,
        parkingArea: String,
        parkingSpot: String?
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // 这里应该是提交报告的逻辑
                // 暂时使用模拟数据
                val newReport = Report(
                    id = UUID.randomUUID().toString(),
                    userId = "user123",
                    title = title,
                    description = description,
                    parkingArea = parkingArea,
                    parkingSpot = parkingSpot,
                    createdAt = Date(),
                    status = ReportStatus.PENDING
                )
                
                _state.update {
                    it.copy(
                        reports = it.reports + newReport,
                        isLoading = false,
                        isSubmitSuccess = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "提交报告失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    // 更新报告状态
    fun updateReportStatus(reportId: String, newStatus: ReportStatus) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // 这里应该是更新报告状态的逻辑
                // 暂时使用模拟数据
                val updatedReports = _state.value.reports.map { report ->
                    if (report.id == reportId) {
                        report.copy(status = newStatus, updatedAt = Date())
                    } else {
                        report
                    }
                }
                
                _state.update {
                    it.copy(
                        reports = updatedReports,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "更新报告状态失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearSubmitSuccess() {
        _state.update { it.copy(isSubmitSuccess = false) }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 