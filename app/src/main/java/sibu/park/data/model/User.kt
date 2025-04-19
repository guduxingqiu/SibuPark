package sibu.park.data.model

enum class UserRole {
    USER,   // 普通用户
    STAFF,  // 停车场工作人员
    ADMIN   // 系统管理员
}

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.USER,
    val licensePlates: List<String> = emptyList(),  // 用户的车牌列表
    val registrationDate: Long = System.currentTimeMillis(),
    val lastLoginDate: Long = System.currentTimeMillis(),
    val profileImageUrl: String? = null,
    val isVerified: Boolean = false,
    val preferredParkingArea: String? = null  // 用户偏好的停车区域
) 