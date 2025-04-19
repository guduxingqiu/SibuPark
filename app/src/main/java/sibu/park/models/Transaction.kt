package sibu.park.models

import com.google.firebase.Timestamp

enum class TransactionType {
    COUPON_PURCHASE, FINE_PAYMENT
}

enum class TransactionStatus {
    PENDING, COMPLETED, FAILED, REFUNDED
}

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val type: TransactionType = TransactionType.COUPON_PURCHASE,
    val amount: Double = 0.0,
    val referenceId: String = "", // 可以是优惠券ID或罚款ID
    val paymentMethod: String = "",
    val transactionId: String = "",
    val status: TransactionStatus = TransactionStatus.PENDING,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val notes: String = ""
) 