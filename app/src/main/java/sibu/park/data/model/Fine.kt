package sibu.park.data.model

enum class FineStatus {
    PENDING,   // 待支付
    PAID,      // 已支付
    APPEALED,  // 已申诉
    CANCELLED, // 已取消
    OVERDUE    // 逾期未支付
}

data class Fine(
    val id: String = "",
    val userId: String? = null,      // 用户ID，可能为空（未注册用户）
    val licensePlate: String = "",   // 车牌号
    val parkingAreaId: String = "",  // 停车场ID
    val spotId: String? = null,      // 停车位ID
    val issueTime: Long = System.currentTimeMillis(), // 开罚时间
    val dueTime: Long = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000, // 到期时间，默认7天后
    val amount: Double = 0.0,        // 罚款金额
    val reason: String = "",         // 违规原因
    val description: String? = null, // 详细描述
    val evidenceUrls: List<String> = emptyList(), // 证据照片URL
    val status: FineStatus = FineStatus.PENDING,
    val paymentTime: Long? = null,   // 支付时间
    val paymentMethod: String? = null, // 支付方式
    val paymentTransactionId: String? = null, // 支付交易ID
    val appealReason: String? = null, // 申诉理由
    val appealTime: Long? = null,    // 申诉时间
    val appealStatus: String? = null, // 申诉状态
    val staffId: String? = null,     // 开罚工作人员ID
    val notes: String? = null        // 备注
) 