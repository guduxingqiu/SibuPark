package sibu.park.models

import com.google.firebase.Timestamp

enum class UserRole {
    USER, STAFF
}

data class User(
    val id: String = "", // Firebase Auth UID
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.USER,
    val savedPlates: List<String> = listOf(), // 保存的车牌号码
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) 