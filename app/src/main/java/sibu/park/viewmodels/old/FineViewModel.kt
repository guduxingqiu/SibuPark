package sibu.park.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sibu.park.models.Fine
import sibu.park.models.FinePayment
import sibu.park.repositories.FineRepository
import sibu.park.utils.Resource

data class FineState(
    val isLoading: Boolean = false,
    val fines: List<Fine> = emptyList(),
    val unpaidFines: List<Fine> = emptyList(),
    val currentFine: Fine? = null,
    val isPaymentSuccess: Boolean = false,
    val error: String? = null
)

class FineViewModel(private val repository: FineRepository = FineRepository()) : ViewModel() {
    
    private val _state = MutableStateFlow(FineState())
    val state: StateFlow<FineState> = _state.asStateFlow()
    
    init {
        getUserFines()
        getUnpaidFines()
    }
    
    fun getUserFines() {
        viewModelScope.launch {
            repository.getUserFines().collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                fines = result.data ?: emptyList(),
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
    
    fun getUnpaidFines() {
        viewModelScope.launch {
            repository.getUnpaidFines().collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                unpaidFines = result.data ?: emptyList(),
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
    
    fun getFineDetails(fineId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.getFineDetails(fineId)) {
                is Resource.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            currentFine = result.data,
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
    
    fun payFine(
        fineId: String,
        amount: Double,
        paymentMethod: String,
        transactionId: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isPaymentSuccess = false) }
            
            when (val result = repository.payFine(fineId, amount, paymentMethod, transactionId)) {
                is Resource.Success -> {
                    getUserFines()
                    getUnpaidFines()
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            isPaymentSuccess = true,
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
    
    fun issueFine(
        userId: String,
        licensePlate: String,
        parkingArea: String,
        parkingSpot: String,
        amount: Double,
        notes: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.issueFine(userId, licensePlate, parkingArea, parkingSpot, amount, notes)) {
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
    
    fun getFinesByLicensePlate(licensePlate: String, onResult: (List<Fine>) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.getFinesByLicensePlate(licensePlate)) {
                is Resource.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    onResult(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    onResult(emptyList())
                }
                else -> {
                    onResult(emptyList())
                }
            }
        }
    }
    
    fun clearPaymentSuccess() {
        _state.update { it.copy(isPaymentSuccess = false) }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun clearCurrentFine() {
        _state.update { it.copy(currentFine = null) }
    }
} 