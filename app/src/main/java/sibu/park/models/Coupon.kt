package sibu.park.models

import com.google.firebase.Timestamp

data class CouponPackage(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val usageCount: Int = 10, // 默认10次使用
    val validityDays: Int = 30, // 有效期（天）
    val isActive: Boolean = true
)

data class Coupon(
    val id: String = "",
    val userId: String = "",
    val packageId: String = "",
    val packageName: String = "",
    val remainingUses: Int = 10,
    val purchaseDate: Timestamp = Timestamp.now(),
    val expiryDate: Timestamp = Timestamp(Timestamp.now().seconds + 30 * 24 * 60 * 60, 0), // 30天后
    val isActive: Boolean = true
)

data class CouponUsage(
    val id: String = "",
    val couponId: String = "",
    val userId: String = "",
    val parkingArea: String = "",
    val parkingSpot: String = "",
    val licensePlate: String = "",
    val usageTime: Timestamp = Timestamp.now()
) 