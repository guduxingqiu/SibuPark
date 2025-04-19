package sibu.park.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sibu.park.models.Transaction
import sibu.park.models.TransactionStatus
import sibu.park.models.TransactionType
import sibu.park.repositories.TransactionRepository
import sibu.park.utils.Resource

data class TransactionState(
    val isLoading: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val currentTransaction: Transaction? = null,
    val isTransactionCreated: Boolean = false,
    val isStatusUpdated: Boolean = false,
    val error: String? = null
)

class TransactionViewModel(private val repository: TransactionRepository = TransactionRepository()) : ViewModel() {
    
    private val _state = MutableStateFlow(TransactionState())
    val state: StateFlow<TransactionState> = _state.asStateFlow()
    
    init {
        getUserTransactions()
    }
    
    fun getUserTransactions() {
        viewModelScope.launch {
            repository.getUserTransactions().collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                transactions = result.data ?: emptyList(),
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
    
    fun getTransactionById(transactionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.getTransactionById(transactionId)) {
                is Resource.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            currentTransaction = result.data,
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
    
    fun createTransaction(
        type: TransactionType,
        amount: Double,
        referenceId: String,
        paymentMethod: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isTransactionCreated = false) }
            
            when (val result = repository.createTransaction(type, amount, referenceId, paymentMethod, notes)) {
                is Resource.Success -> {
                    getUserTransactions()
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            currentTransaction = result.data,
                            isTransactionCreated = true,
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
    
    fun updateTransactionStatus(
        transactionId: String,
        status: TransactionStatus,
        paymentTransactionId: String = ""
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isStatusUpdated = false) }
            
            when (val result = repository.updateTransactionStatus(transactionId, status, paymentTransactionId)) {
                is Resource.Success -> {
                    getUserTransactions()
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            currentTransaction = result.data,
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
    
    fun clearTransactionCreated() {
        _state.update { it.copy(isTransactionCreated = false) }
    }
    
    fun clearStatusUpdated() {
        _state.update { it.copy(isStatusUpdated = false) }
    }
    
    fun clearCurrentTransaction() {
        _state.update { it.copy(currentTransaction = null) }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 