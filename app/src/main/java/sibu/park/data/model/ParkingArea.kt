package sibu.park.data.model

enum class ParkingAreaType {
    INDOOR,       // 室内停车场
    OUTDOOR,      // 室外停车场
    UNDERGROUND   // 地下停车场
}

data class ParkingArea(
    val id: String = "",
    val name: String = "",
    val type: ParkingAreaType = ParkingAreaType.OUTDOOR,
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val totalSpots: Int = 0,      // 总停车位数量
    val availableSpots: Int = 0,  // 可用停车位数量
    val hourlyRate: Double = 0.0, // 每小时费率
    val imageUrl: String? = null,
    val features: List<String> = emptyList(), // 特点，如"电动车充电桩"，"摄像监控"等
    val openingHours: String = "24/7",
    val isActive: Boolean = true
) 