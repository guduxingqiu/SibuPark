package sibu.park.data.model

import java.util.Date

enum class TransactionType {
    PARKING_FEE,      // 停车费
    COUPON_PURCHASE,  // 购买优惠券
    FINE_PAYMENT,     // 缴纳罚款
    MEMBERSHIP_FEE,   // 会员费
    REFUND,           // 退款
    OTHER             // 其他
}

enum class TransactionStatus {
    PENDING,          // 待处理
    COMPLETED,        // 已完成
    FAILED,           // 失败
    REFUNDED,         // 已退款
    CANCELLED         // 已取消
}

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val type: TransactionType = TransactionType.PARKING_FEE,
    val amount: Double = 0.0,          // 交易金额
    val status: TransactionStatus = TransactionStatus.PENDING,
    val creationTime: Long = System.currentTimeMillis(),
    val completionTime: Long? = null,  // 完成时间
    val paymentMethod: String? = null, // 支付方式，如"微信支付"，"支付宝"
    val transactionReference: String? = null, // 第三方交易参考号
    val parkingRecordId: String? = null, // 关联的停车记录
    val couponPackageId: String? = null, // 关联的优惠券套餐
    val fineId: String? = null,        // 关联的罚款
    val description: String? = null,   // 交易描述
    val receiptUrl: String? = null,    // 电子收据URL
    val notes: String? = null          // 备注
) 