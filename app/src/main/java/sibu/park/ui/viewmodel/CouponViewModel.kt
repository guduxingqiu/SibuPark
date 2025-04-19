package sibu.park.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sibu.park.models.Coupon
import sibu.park.models.CouponPackage
import sibu.park.data.repository.CouponRepository
import java.util.*
import javax.inject.Inject

data class CouponState(
    val userCoupons: List<Coupon> = emptyList(),
    val couponPackages: List<CouponPackage> = emptyList(),
    val currentCoupon: Coupon? = null,
    val isLoading: Boolean = false,
    val isPurchaseSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CouponViewModel @Inject constructor(
    private val couponRepository: CouponRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(CouponState())
    val state: StateFlow<CouponState> = _state
    
    // 获取优惠券套餐
    fun getCouponPackages() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val packages = couponRepository.getCouponPackages()
                _state.update {
                    it.copy(
                        couponPackages = packages,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "获取优惠券套餐失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    // 购买优惠券
    fun purchaseCoupon(packageId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val purchasedCoupon = couponRepository.purchaseCoupon(packageId)
                _state.update {
                    it.copy(
                        userCoupons = it.userCoupons + purchasedCoupon,
                        isLoading = false,
                        isPurchaseSuccess = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "购买停车券失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    // 获取用户的停车券
    fun getUserCoupons() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val userCoupons = couponRepository.getUserCoupons()
                _state.update {
                    it.copy(
                        userCoupons = userCoupons,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "获取停车券失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    // 使用停车券
    fun useCoupon(couponId: String, licensePlate: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val success = couponRepository.useCoupon(couponId, licensePlate)
                if (success) {
                    // 更新成功后重新获取用户的优惠券
                    getUserCoupons()
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "使用停车券失败"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "使用停车券失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    // 根据优惠券ID检查优惠券
    fun checkCoupon(couponId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val coupon = couponRepository.getCouponById(couponId)
                _state.update {
                    it.copy(
                        currentCoupon = coupon,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "查询优惠券失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    // 根据车牌号码检查优惠券
    fun checkCouponByLicensePlate(licensePlate: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val coupon = couponRepository.getCouponByLicensePlate(licensePlate)
                _state.update {
                    it.copy(
                        currentCoupon = coupon,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "查询优惠券失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearCurrentCoupon() {
        _state.update { it.copy(currentCoupon = null) }
    }
    
    // 清除购买成功状态
    fun clearPurchaseSuccess() {
        _state.update { it.copy(isPurchaseSuccess = false) }
    }
    
    // 清除错误信息
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 