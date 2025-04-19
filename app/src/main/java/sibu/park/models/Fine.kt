package sibu.park.models

import com.google.firebase.Timestamp

enum class FineStatus {
    PENDING, PAID, CANCELLED
}

data class Fine(
    val id: String = "",
    val userId: String = "",
    val licensePlate: String = "",
    val parkingArea: String = "",
    val parkingSpot: String = "",
    val amount: Double = 50.0, // 默认罚款金额
    val issuedBy: String = "", // 工作人员ID
    val issuedAt: Timestamp = Timestamp.now(),
    val status: FineStatus = FineStatus.PENDING,
    val paidAt: Timestamp? = null,
    val notes: String = ""
)

data class FinePayment(
    val id: String = "",
    val fineId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "",
    val transactionId: String = "",
    val paymentDate: Timestamp = Timestamp.now()
) 