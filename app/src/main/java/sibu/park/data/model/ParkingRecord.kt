package sibu.park.data.model

data class ParkingRecord(
    val id: String = "",
    val userId: String? = null,     // 用户ID，可能为空（未注册用户）
    val parkingAreaId: String = "", // 停车场ID
    val spotId: String = "",        // 停车位ID
    val licensePlate: String = "",  // 车牌号
    val entryTime: Long = System.currentTimeMillis(),
    val exitTime: Long? = null,     // 出场时间，为空表示仍在停车
    val fee: Double = 0.0,          // 停车费用
    val isPaid: Boolean = false,    // 是否已支付
    val paymentMethod: String? = null, // 支付方式
    val paymentTime: Long? = null,  // 支付时间
    val couponId: String? = null,   // 使用的优惠券ID
    val billImageUrl: String? = null, // 发票图片URL
    val vehicleType: String = "普通汽车", // 车辆类型
    val notes: String? = null       // 备注
) 