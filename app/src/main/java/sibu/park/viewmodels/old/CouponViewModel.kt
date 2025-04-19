package sibu.park.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sibu.park.models.Coupon
import sibu.park.models.CouponPackage
import sibu.park.models.CouponUsage
import sibu.park.repositories.CouponRepository
import sibu.park.utils.Resource

data class CouponState(
    val isLoading: Boolean = false,
    val couponPackages: List<CouponPackage> = emptyList(),
    val userCoupons: List<Coupon> = emptyList(),
    val usageHistory: List<CouponUsage> = emptyList(),
    val savedLicensePlates: List<String> = emptyList(),
    val isPurchaseSuccess: Boolean = false,
    val isUsageSuccess: Boolean = false,
    val error: String? = null
)

class CouponViewModel(private val repository: CouponRepository = CouponRepository()) : ViewModel() {
    
    private val _state = MutableStateFlow(CouponState())
    val state: StateFlow<CouponState> = _state.asStateFlow()
    
    init {
        getCouponPackages()
        getUserCoupons()
        getUsageHistory()
        getSavedLicensePlates()
    }
    
    fun getCouponPackages() {
        viewModelScope.launch {
            repository.getCouponPackages().collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                couponPackages = result.data ?: emptyList(),
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
    
    fun getUserCoupons() {
        viewModelScope.launch {
            repository.getUserCoupons().collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                userCoupons = result.data ?: emptyList(),
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
    
    fun purchaseCoupon(packageId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isPurchaseSuccess = false) }
            
            when (val result = repository.purchaseCoupon(packageId)) {
                is Resource.Success -> {
                    getUserCoupons()
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            isPurchaseSuccess = true,
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
    
    fun useCoupon(couponId: String, parkingArea: String, parkingSpot: String, licensePlate: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isUsageSuccess = false) }
            
            when (val result = repository.useCoupon(couponId, parkingArea, parkingSpot, licensePlate)) {
                is Resource.Success -> {
                    getUserCoupons()
                    getUsageHistory()
                    saveLicensePlate(licensePlate)
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            isUsageSuccess = true,
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
    
    fun getUsageHistory() {
        viewModelScope.launch {
            repository.getCouponUsageHistory().collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                usageHistory = result.data ?: emptyList(),
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
    
    fun saveLicensePlate(licensePlate: String) {
        viewModelScope.launch {
            when (val result = repository.saveLicensePlate(licensePlate)) {
                is Resource.Success -> {
                    getSavedLicensePlates()
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
    
    fun getSavedLicensePlates() {
        viewModelScope.launch {
            when (val result = repository.getSavedLicensePlates()) {
                is Resource.Success -> {
                    _state.update { 
                        it.copy(
                            savedLicensePlates = result.data ?: emptyList(),
                            error = null
                        )
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
    
    fun checkCouponUsageByLicensePlate(licensePlate: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.checkCouponUsageByLicensePlate(licensePlate)) {
                is Resource.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    onResult(result.data ?: false)
                }
                is Resource.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    onResult(false)
                }
                else -> {
                    onResult(false)
                }
            }
        }
    }
    
    fun clearPurchaseSuccess() {
        _state.update { it.copy(isPurchaseSuccess = false) }
    }
    
    fun clearUsageSuccess() {
        _state.update { it.copy(isUsageSuccess = false) }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 