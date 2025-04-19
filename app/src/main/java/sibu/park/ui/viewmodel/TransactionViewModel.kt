package sibu.park.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sibu.park.data.model.Transaction
import sibu.park.data.model.TransactionType
import java.util.*
import javax.inject.Inject

data class TransactionState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TransactionViewModel @Inject constructor() : ViewModel() {
    
    private val _state = MutableStateFlow(TransactionState())
    val state: StateFlow<TransactionState> = _state
    
    // 获取用户的交易记录
    fun getUserTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // 这里应该是获取用户交易记录的逻辑
                // 暂时使用模拟数据
                val mockTransactions = listOf(
                    Transaction(
                        id = "tx1",
                        userId = "user123",
                        amount = 50.0,
                        type = TransactionType.COUPON_PURCHASE,
                        description = "购买日停车券",
                        timestamp = Date(),
                        relatedItemId = "coupon1"
                    ),
                    Transaction(
                        id = "tx2",
                        userId = "user123",
                        amount = 100.0,
                        type = TransactionType.FINE_PAYMENT,
                        description = "支付罚款",
                        timestamp = Calendar.getInstance().apply { 
                            add(Calendar.DAY_OF_MONTH, -2) 
                        }.time,
                        relatedItemId = "fine1"
                    ),
                    Transaction(
                        id = "tx3",
                        userId = "user123",
                        amount = 200.0,
                        type = TransactionType.COUPON_PURCHASE,
                        description = "购买月停车券",
                        timestamp = Calendar.getInstance().apply { 
                            add(Calendar.DAY_OF_MONTH, -5) 
                        }.time,
                        relatedItemId = "coupon2"
                    )
                )
                
                _state.update {
                    it.copy(
                        transactions = mockTransactions,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "获取交易记录失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 