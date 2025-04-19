package sibu.park.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sibu.park.models.Report
import sibu.park.models.ReportStatus
import sibu.park.models.ReportType
import sibu.park.repositories.ReportRepository
import sibu.park.utils.Resource

data class ReportState(
    val isLoading: Boolean = false,
    val reports: List<Report> = emptyList(),
    val currentReport: Report? = null,
    val isSubmitSuccess: Boolean = false,
    val isStatusUpdated: Boolean = false,
    val error: String? = null
)

class ReportViewModel(private val repository: ReportRepository = ReportRepository()) : ViewModel() {
    
    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()
    
    init {
        getUserReports()
    }
    
    fun getUserReports() {
        viewModelScope.launch {
            repository.getUserReports().collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                reports = result.data ?: emptyList(),
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
    
    fun getReportDetails(reportId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.getReportDetails(reportId)) {
                is Resource.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            currentReport = result.data,
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
    
    fun submitReport(
        type: ReportType,
        title: String,
        description: String,
        parkingArea: String? = null,
        parkingSpot: String? = null,
        attachmentUrls: List<String> = listOf()
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isSubmitSuccess = false) }
            
            when (val result = repository.submitReport(
                type, title, description, parkingArea, parkingSpot, attachmentUrls
            )) {
                is Resource.Success -> {
                    getUserReports()
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            currentReport = result.data,
                            isSubmitSuccess = true,
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
    
    // 工作人员使用的方法
    fun updateReportStatus(reportId: String, status: ReportStatus, staffNotes: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isStatusUpdated = false) }
            
            when (val result = repository.updateReportStatus(reportId, status, staffNotes)) {
                is Resource.Success -> {
                    getUserReports()
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            currentReport = result.data,
                            isStatusUpdated = true,
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
    
    fun clearSubmitSuccess() {
        _state.update { it.copy(isSubmitSuccess = false) }
    }
    
    fun clearStatusUpdated() {
        _state.update { it.copy(isStatusUpdated = false) }
    }
    
    fun clearCurrentReport() {
        _state.update { it.copy(currentReport = null) }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 