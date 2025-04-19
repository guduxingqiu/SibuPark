package sibu.park.data.model

enum class SpotType {
    REGULAR,      // 普通车位
    DISABLED,     // 残疾人车位
    ELECTRIC,     // 电动车充电车位
    VIP,          // VIP车位
    LARGE         // 大型车辆车位
}

enum class SpotStatus {
    AVAILABLE,    // 可用
    OCCUPIED,     // 已占用
    RESERVED,     // 已预订
    MAINTENANCE   // 维护中
}

data class ParkingSpot(
    val id: String = "",
    val parkingAreaId: String = "",
    val spotNumber: String = "",   // 车位编号，如"A-123"
    val type: SpotType = SpotType.REGULAR,
    val status: SpotStatus = SpotStatus.AVAILABLE,
    val floor: Int = 0,            // 楼层，对于多层停车场
    val section: String = "",      // 区域，如"A区"
    val isChargingEnabled: Boolean = false, // 是否有充电设施
    val isReservedFor: String? = null,      // 是否为某用户预留
    val lastStatusChange: Long = System.currentTimeMillis()
) 