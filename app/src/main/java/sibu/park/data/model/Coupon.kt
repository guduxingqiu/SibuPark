package sibu.park.data.model

import java.util.Date

enum class CouponType {
    TIME_BASED,  // 基于时间的优惠券，如"免费停车2小时"
    FEE_BASED,   // 基于费用的优惠券，如"减免10元"
    PERCENTAGE   // 百分比折扣，如"8折"
}

data class Coupon(
    val id: String = "",
    val userId: String = "",         // 持有此优惠券的用户ID
    val code: String = "",           // 优惠券代码
    val type: CouponType = CouponType.TIME_BASED,
    val value: Double = 0.0,         // 优惠券值，对于TIME_BASED表示小时数，FEE_BASED表示金额，PERCENTAGE表示折扣率
    val description: String = "",    // 描述，如"工作日免费停车2小时"
    val creationTime: Long = System.currentTimeMillis(),
    val expiryTime: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000, // 默认30天后过期
    val isUsed: Boolean = false,     // 是否已使用
    val usedTime: Long? = null,      // 使用时间
    val usedForParkingId: String? = null, // 用于哪次停车
    val minPurchaseAmount: Double = 0.0,  // 最低消费额
    val maxDiscountAmount: Double? = null, // 最大优惠金额
    val isActive: Boolean = true,    // 是否有效
    val restrictions: List<String> = emptyList() // 使用限制，如"仅限工作日使用"
) 